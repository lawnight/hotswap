package fix.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * <pre>
 *  重转换可能会更改方法体、常量池和属性。重转换不得添加、移除、重命名字段或方法（包括父类有的方法，重新实现也不行）；
 *  不得更改方法签名、继承关系。在以后的版本中，可能会取消这些限制。
 *  在应用转换之前，类文件字节不会被检查、验证和安装。如果结果字节错误，此方法将抛出异常。
 * </pre>
 */
public final class GameAgentMain {

    /**
     *
     */
    private static Map<String, JarClassInfo> bugFiles = new HashMap<>();

    /**
     *
     */
    private GameAgentMain() {

    }

    /**
     * @param args
     * @param inst
     */
    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("===agentmain begin===");
        try {
            parseFiles(args);

            inst.addTransformer(new Transformer(), true);
            List<Class<?>> retransformClasses = new ArrayList<>();

            retransformClasses.add(GameAgentMain.class);

            for (JarClassInfo jarClassInfo : GameAgentMain.bugFiles.values()) {
                try {
                    Class<?> clazz = Class.forName(jarClassInfo.getClassName());
                    retransformClasses.add(clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Class<?>[] classes = new Class[retransformClasses.size()];
            if (classes.length > 0) {
                retransformClasses.toArray(classes);
                for (Class<?> class1 : classes) {
                    System.out.println("the class ready to be instrumented:" + class1.getName());
                }
                inst.retransformClasses(classes);

            } else {
                System.err.println("not class be instrumented!");
            }

            //TODO : 及时执行的代码

            //StorageManager
//            RankExService activityFubenTemplate = (RankExService) ApplicationContextUtil.getContext().getBean(RankExService.class);
//            System.out.println("加载配置表开始------------");
//            long rank = activityFubenTemplate.getRankIndex("RANK_ID_CHARM", "aLE7neGtMULZtH2JUvP");
//            System.out.println("rank:::::::" + rank);
//            ArrayList<SIRankInfo> info = activityFubenTemplate.rangePlayerInfo("RANK_ID_CHARM", 0, 8);
//            for (SIRankInfo siRankInfo : info) {
//                System.out.println(siRankInfo.getPlayerId() + "----------" + siRankInfo.getRankIndex());
//            }

//            Class cls = ActivityFubenTemplate.class;
//            Method method = cls.getDeclaredMethod("init", null);
//            method.invoke(activityFubenTemplate, null);
//            method.setAccessible(true);
            System.out.println("加载配置表完成");

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("===agentmain finish===");
    }


    private static class Transformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                byte[] classfileBuffer) throws IllegalClassFormatException {

            if (GameAgentMain.bugFiles.containsKey(className)) {
                return bugFiles.get(className).getBytecode();
            }
            return null;
        }
    }

    private static void parseFiles(String name) throws Exception {
        bugFiles.clear();
        // URL url =
        // GameAgentMain.class.getProtectionDomain().getCodeSource().getLocation();
        JarFile agentJar = new JarFile(name);
        Manifest manifest = agentJar.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        String fileParams = attributes.getValue("File-Params");
        for (String jarEntryName : fileParams.split(",")) {
            JarEntry jarEntry = agentJar.getJarEntry(jarEntryName);
            if (jarEntry != null) {
                InputStream is = null;
                is = agentJar.getInputStream(jarEntry);
                byte[] bytes = new byte[1024 * 1024];
                int len = -1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((len = is.read(bytes)) != -1) {
                    baos.write(bytes, 0, len);
                }
                bytes = baos.toByteArray();
                baos.close();
                String className = jarEntryName.replaceAll("\\.class", "");
                GameAgentMain.bugFiles.put(className, new JarClassInfo(className.replaceAll("/", "."), bytes));
            }
        }
        agentJar.close();
    }

}
