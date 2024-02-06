package the.grid.smp.curator.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.function.BiFunction;

public record TransformQuery(Class<?> clazz, String method, BiFunction<ClassReader, ClassWriter, ClassVisitor> function) { }
