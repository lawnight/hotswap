package fix.core;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarUtils {

    static String fixPackageName = "com";

    private static JarUtils instance = new JarUtils();

    private JarUtils() {
    }

    public static JarUtils get() {
        return instance;
    }

    public void add(File source, String jarEntryName, JarOutputStream target) throws IOException {

        System.err.println("add file:" + source.getName());
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = jarEntryName;
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";
                    rawAdd(target, name, null, source.lastModified());
                }
                for (File nestedFile : source.listFiles())
                    add(nestedFile, jarEntryName, target);
                return;
            } else {
                in = new BufferedInputStream(new FileInputStream(source));
                rawAdd(target, jarEntryName, Files.readAllBytes(source.toPath()), source.lastModified());
            }
        } finally {
            if (in != null)
                in.close();
        }
    }

    public void rawAdd(JarOutputStream target, String name, byte[] context, long time) {
        try {
            JarEntry entry = new JarEntry(name);
            if (name.contains("META-INF")) {
                return;
            }
            target.putNextEntry(entry);
            if (context != null) {
                target.write(context, 0, context.length);
            }
            entry.setTime(time);
            target.closeEntry();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void extract(String jarFile, JarOutputStream target) {
        try {

            JarFile jar = new JarFile(jarFile);
            Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();

                java.io.InputStream is = jar.getInputStream(file); // get the
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                buffer.toByteArray();

                rawAdd(target, file.getName(), buffer.toByteArray(), file.getTime());

            }
            jar.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
