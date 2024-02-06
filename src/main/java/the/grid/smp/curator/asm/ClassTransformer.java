package the.grid.smp.curator.asm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Collection;

public class ClassTransformer implements ClassFileTransformer {

    private static final String ATTACH_MOD_PATH = "jmods/jdk.attach.jmod";
    private final Instrumentation instrumentation;

    private final Multimap<Class<?>, TransformQuery> queries = ArrayListMultimap.create();

    public ClassTransformer() {
        ClassLoader loader = ClassLoader.getSystemClassLoader();

        String javaHome = System.getProperty("java.home");
        File java = new File(javaHome);

        if (loader instanceof URLClassLoader urlLoader) {
            try {
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);

                File toolsJar = new File(java, "lib/tools.jar");
                if (!toolsJar.exists())
                    throw new RuntimeException("Not running with JDK!");

                method.invoke(urlLoader, toolsJar.toURI().toURL());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else {
            Path attachMod = java.toPath().resolve(ATTACH_MOD_PATH);

            if (Files.notExists(attachMod)) {
                throw new RuntimeException("Not running with JDK!");
            }
        }

        if (javaHome.contains("pebble")) {
            String[] split = javaHome.split("/");
            System.setProperty("java.bin", Path.of(javaHome, "bin", split[split.length - 1]).toString());
        }

        this.instrumentation = ByteBuddyAgent.install();
        this.instrumentation.addTransformer(this, true);
    }

    public void retransformAll(Collection<TransformQuery> queries) {
        try {
            this.queries.clear();

            for (TransformQuery query : queries) {
                this.queries.put(query.clazz(), query);
            }

            this.instrumentation.retransformClasses(this.queries.keySet().toArray(new Class[0]));
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String path, Class<?> clazz, ProtectionDomain domain, byte[] bytes) {
        Collection<TransformQuery> queries = this.queries.get(clazz);

        if (queries.isEmpty())
            return bytes;

        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

        for (TransformQuery query : queries) {
            query.function().apply(reader, writer);
        }

        return writer.toByteArray();
    }
}
