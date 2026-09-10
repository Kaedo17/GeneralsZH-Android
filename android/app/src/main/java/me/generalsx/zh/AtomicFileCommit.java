// GeneralsX @bugfix android-port 10/09/2026 Transactional file installation
// keeps interrupted SAF and network copies from replacing usable game data.
package me.generalsx.zh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Small Java-only helper so file replacement behavior can be tested off-device. */
final class AtomicFileCommit {
    interface Cancellation { boolean isCancelled(); }
    interface Promoter { boolean move(File source, File destination); }

    static long copy(InputStream in, File destination, long expectedLength,
                     Cancellation cancellation) throws IOException {
        return copy(in, destination, expectedLength, cancellation, new Promoter() {
            @Override public boolean move(File source, File target) {
                // Android's rename within one app-owned directory is atomic and
                // replaces the target without first removing it.
                return source.renameTo(target);
            }
        });
    }

    static long copy(InputStream in, File destination, long expectedLength,
                     Cancellation cancellation, Promoter promoter) throws IOException {
        File parent = destination.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs())) {
            throw new IOException("cannot create destination directory");
        }
        File temp = File.createTempFile(destination.getName() + ".part-", ".tmp", parent);
        try {
            long bytes = 0;
            try (OutputStream out = new FileOutputStream(temp)) {
                byte[] buffer = new byte[1 << 16];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    if (cancellation.isCancelled()) throw new IOException("Import cancelled.");
                    out.write(buffer, 0, read);
                    bytes += read;
                }
                if (cancellation.isCancelled()) throw new IOException("Import cancelled.");
                out.flush();
            }
            if (expectedLength >= 0 && bytes != expectedLength) {
                throw new IOException("source changed while copying " + destination.getName());
            }
            promote(temp, destination, promoter);
            return bytes;
        } finally {
            if (temp.exists()) temp.delete();
        }
    }

    static void promote(File temp, File destination) throws IOException {
        promote(temp, destination, new Promoter() {
            @Override public boolean move(File source, File target) {
                return source.renameTo(target);
            }
        });
    }

    static void promote(File temp, File destination, Promoter promoter) throws IOException {
        if (temp == null || destination == null || promoter == null || !temp.isFile()) {
            throw new IOException("invalid file promotion");
        }
        // The promoter must replace the destination in one operation. Never move
        // the live file aside: a failed promotion must leave it recoverable.
        if (!promoter.move(temp, destination)) {
            throw new IOException("cannot finalize " + destination.getName());
        }
    }

    private AtomicFileCommit() {}
}
