package me.generalsx.zh;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import org.junit.Test;

public class AtomicFileCommitTest {
    @Test public void cancellationPreservesExistingFile() throws Exception {
        File dir = Files.createTempDirectory("gx-import-").toFile();
        File dst = new File(dir, "archive.big");
        Files.write(dst.toPath(), new byte[] {1, 2, 3});
        assertThrows(IOException.class, () -> AtomicFileCommit.copy(
            new ByteArrayInputStream(new byte[] {9, 8, 7, 6}), dst, 4,
            new AtomicFileCommit.Cancellation() { @Override public boolean isCancelled() { return true; } }));
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(dst.toPath()));
        assertNoPartFiles(dir);
    }

    @Test public void shortReadIsRejectedAndPreservesExistingFile() throws Exception {
        File dir = Files.createTempDirectory("gx-import-").toFile();
        File dst = new File(dir, "archive.big");
        Files.write(dst.toPath(), new byte[] {1, 2, 3});
        assertThrows(IOException.class, () -> AtomicFileCommit.copy(
            new ByteArrayInputStream(new byte[] {9}), dst, 4,
            new AtomicFileCommit.Cancellation() { @Override public boolean isCancelled() { return false; } }));
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(dst.toPath()));
        assertNoPartFiles(dir);
    }

    @Test public void completeCopyReplacesExistingFile() throws Exception {
        File dir = Files.createTempDirectory("gx-import-").toFile();
        File dst = new File(dir, "archive.big");
        Files.write(dst.toPath(), new byte[] {1, 2, 3});
        assertEquals(4, AtomicFileCommit.copy(new ByteArrayInputStream(
            new byte[] {9, 8, 7, 6}), dst, 4,
            new AtomicFileCommit.Cancellation() { @Override public boolean isCancelled() { return false; } },
            new AtomicFileCommit.Promoter() {
                @Override public boolean move(File source, File target) {
                    try {
                        Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        return true;
                    } catch (IOException e) { return false; }
                }
            }));
        assertArrayEquals(new byte[] {9, 8, 7, 6}, Files.readAllBytes(dst.toPath()));
        assertNoPartFiles(dir);
    }

    @Test public void zeroLengthExpectationIsValidated() throws Exception {
        File dir = Files.createTempDirectory("gx-import-").toFile();
        File dst = new File(dir, "archive.big");
        Files.write(dst.toPath(), new byte[] {1});
        assertThrows(IOException.class, () -> AtomicFileCommit.copy(
            new ByteArrayInputStream(new byte[] {9}), dst, 0,
            new AtomicFileCommit.Cancellation() { @Override public boolean isCancelled() { return false; } }));
        assertArrayEquals(new byte[] {1}, Files.readAllBytes(dst.toPath()));
        assertNoPartFiles(dir);
    }

    @Test public void readFailurePreservesExistingFileAndCleansTemp() throws Exception {
        File dir = Files.createTempDirectory("gx-import-").toFile();
        File dst = new File(dir, "archive.big");
        Files.write(dst.toPath(), new byte[] {1, 2, 3});
        InputStream failing = new InputStream() {
            private boolean first = true;
            @Override public int read(byte[] b, int off, int len) throws IOException {
                if (first) { first = false; b[off] = 9; return 1; }
                throw new IOException("simulated source failure");
            }
            @Override public int read() throws IOException { throw new IOException("simulated source failure"); }
        };
        assertThrows(IOException.class, () -> AtomicFileCommit.copy(failing, dst, -1,
            () -> false));
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(dst.toPath()));
        assertNoPartFiles(dir);
    }

    @Test public void cancellationAfterBytesPreservesExistingFileAndCleansTemp() throws Exception {
        File dir = Files.createTempDirectory("gx-import-").toFile();
        File dst = new File(dir, "archive.big");
        Files.write(dst.toPath(), new byte[] {1, 2, 3});
        final boolean[] cancelled = {false};
        final InputStream bytes = new ByteArrayInputStream(new byte[] {9, 8, 7, 6});
        InputStream source = new InputStream() {
            @Override public int read(byte[] b, int off, int len) throws IOException {
                int n = bytes.read(b, off, len);
                if (n > 0) cancelled[0] = true;
                return n;
            }
            @Override public int read() throws IOException { return bytes.read(); }
        };
        assertThrows(IOException.class, () -> AtomicFileCommit.copy(source, dst, -1,
            () -> cancelled[0]));
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(dst.toPath()));
        assertNoPartFiles(dir);
    }

    @Test public void failedPromoterThroughCopyPreservesOriginalAndCleansTemp() throws Exception {
        File dir = Files.createTempDirectory("gx-import-").toFile();
        File dst = new File(dir, "archive.big");
        Files.write(dst.toPath(), new byte[] {1, 2, 3});
        assertThrows(IOException.class, () -> AtomicFileCommit.copy(
            new ByteArrayInputStream(new byte[] {9, 8, 7}), dst, 3, () -> false,
            (source, target) -> false));
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(dst.toPath()));
        assertNoPartFiles(dir);
    }

    @Test public void archiveExtensionMatchingIsLocaleIndependent() {
        Locale old = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertTrue(LauncherConfig.isArchive("DATA.BIG"));
            assertTrue(LauncherConfig.isArchive("MOD.GIB"));
        } finally {
            Locale.setDefault(old);
        }
    }

    @Test public void promotionFailureRestoresOriginalFile() throws Exception {
        File dir = Files.createTempDirectory("gx-import-").toFile();
        File dst = new File(dir, "archive.big");
        File temp = new File(dir, "complete.part");
        Files.write(dst.toPath(), new byte[] {1, 2, 3});
        Files.write(temp.toPath(), new byte[] {9, 8, 7});
        assertThrows(IOException.class, () -> AtomicFileCommit.promote(temp, dst,
            new AtomicFileCommit.Promoter() {
                @Override public boolean move(File source, File target) {
                    return !source.equals(temp);
                }
            }));
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(dst.toPath()));
    }

    private static void assertNoPartFiles(File dir) {
        File[] parts = dir.listFiles((d, name) -> name.endsWith(".part") || name.endsWith(".tmp"));
        assertFalse(parts != null && parts.length != 0);
    }
}
