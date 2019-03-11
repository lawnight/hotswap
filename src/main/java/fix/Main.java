package fix;

import com.digisky.female.princess.server.game.module.condition.BaseCondition;
import fix.core.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * 生成jar包
 */
public class Main {

    private static final Set<Class<?>> INCLUDE_FILES = new HashSet<>();

    //需要更新，并且打包的类
    static {
        INCLUDE_FILES.add(BaseCondition.class);
    }

    private static Main instance = new Main();

    private Main() {
    }

    public static Main get() {
        return instance;
    }

    public List<BugClass> getBugClass(Class<?> claz) {
        // File file = new File(claz.getResource("/").getPath() + "/" +
        List<BugClass> clazzs = new ArrayList<>();
        // this is eclipse compile
        String path =
            claz.getProtectionDomain().getCodeSource().getLocation().getPath() + "/" + claz.getPackage().getName()
                .replaceAll("\\.", "/");
        String name = claz.getSimpleName();

        List<File> files = findFile(name, new File(path));
        List<File> packClassFiles = new ArrayList<>();
        for (File file : files) {
            if (file.exists()) {
                packClassFiles.add(file);

                BugClass bugClass = new BugClass();
                bugClass.setFile(file);
                bugClass.setPackageName(claz.getPackage().getName());
                bugClass.setClazz(claz);
                if (file.getName().contains("$")) {
                    bugClass.setInternal(true);
                }
                clazzs.add(bugClass);

            } else {
                System.err.println("file not exists:" + file.getAbsolutePath());
            }
        }
        return clazzs;
    }

    public List<BugClass> getBugClass(String fileName) {
        // File file = new File(claz.getResource("/").getPath() + "/" +
        List<BugClass> clazzs = new ArrayList<>();

        File theFile = new File((fileName));
        List<File> files = findFile(theFile.getName(), new File(theFile.getPath()));
        List<File> packClassFiles = new ArrayList<>();
        for (File file : files) {
            if (file.exists()) {
                packClassFiles.add(file);

                BugClass bugClass = new BugClass();
                bugClass.setFile(file);
                //                bugClass.setPackageName(claz.getPackage().getName());
                //                bugClass.setClazz(claz);
                if (file.getName().contains("$")) {
                    bugClass.setInternal(true);
                }
                clazzs.add(bugClass);

            } else {
                System.err.println("file not exists:" + file.getAbsolutePath());
            }
        }
        return clazzs;
    }

    public List<BugClass> getBugClassFile() {
        List<BugClass> clazzs = new ArrayList<>();
        for (Class<?> claz : INCLUDE_FILES) {
            clazzs.addAll(getBugClass(claz));
        }
        return clazzs;
    }

    public boolean agentJarGenerate() {
        try {
            List<BugClass> packClassFiles = getBugClassFile();
            List<BugClass> coreClassFiles = getCoreClasses();
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
            manifest.getMainAttributes().putValue("Can-Retransform-Classes", "true");
            manifest.getMainAttributes().putValue("Agent-Class", GameAgentMain.class.getName());
            manifest.getMainAttributes().putValue("File-Params", getFileParam(packClassFiles));
            manifest.getMainAttributes().putValue("Main-Class", Boot.class.getName());
            // support
            File file = new File(Constant.agentFile);
            JarOutputStream target = new JarOutputStream(new FileOutputStream(file), manifest);
            //bug fix class
            for (BugClass bugClass : packClassFiles) {
                JarUtils.get().add(bugClass.getFile(), bugClass.getJarName(), target);
            }
            //core class
            for (BugClass bugClass : coreClassFiles) {
                JarUtils.get().add(bugClass.getFile(), bugClass.getJarName(), target);
            }
            // 加入库
            //JarUtils.get().extract("E:/workspace/palace/test/lib/tools.jar", target);
            target.close();
            System.err.println(file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<BugClass> getCoreClasses() {
        List<BugClass> classes = new ArrayList<>();
        try {
            for (Class<?> class1 : getClasses(BugClass.class.getPackage().getName())) {
                if (!class1.getName().contains("$"))
                    classes.addAll(getBugClass(class1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

    private Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    private List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes
                    .add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    private String getFileParam(List<BugClass> packClassFiles) {
        StringBuilder fileParam = new StringBuilder();
        for (BugClass bugClass : packClassFiles) {
            fileParam.append(bugClass.getJarName());
            fileParam.append(",");
        }
        // remove last ','
        if (fileParam.length() > 0) {
            fileParam.deleteCharAt(fileParam.length() - 1);
        }
        return fileParam.toString();
    }

    // 找到class类以及其内部类 对应的文件
    public List<File> findFile(String name, File file) {
        List<File> result = new ArrayList<>();
        File[] list = file.listFiles();
        //
        String pattern = String.format(name + "\\$.*\\.class");
        if (list != null) {
            for (File fil : list) {
                if (fil.isDirectory()) {
                    findFile(name, fil);
                } else if (fil.getName().equalsIgnoreCase(name + ".class")) {
                    result.add(fil);
                } else if (Pattern.matches(pattern, fil.getName())) {
                    result.add(fil);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) cl).getURLs();
        for (URL url : urls) {
            System.out.println(url.getFile());
        }
        //生成jar包
        Main.get().agentJarGenerate();
    }
}
