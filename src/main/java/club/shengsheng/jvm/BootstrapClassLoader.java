package club.shengsheng.jvm;

import tech.medivh.classpy.classfile.ClassFile;
import tech.medivh.classpy.classfile.ClassFileParser;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class BootstrapClassLoader {

    private final List<String> classPath;

    public BootstrapClassLoader(List<String> classPath) {
        this.classPath = classPath;
    }


    public ClassFile loadClass(String fqcn) throws ClassNotFoundException {
        return classPath.stream()
            .map(path -> tryLoad(path, fqcn))
            .filter(Objects::nonNull)
            .findAny()
            .orElseThrow(() -> new ClassNotFoundException(fqcn + "找不到"));
    }

    private ClassFile tryLoad(String path, String mainClass) {
        File classFilePath = new File(path, mainClass.replace(".", File.separator) + ".class");
        if (!classFilePath.exists()) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(classFilePath.toPath());
            return new ClassFileParser().parse(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
