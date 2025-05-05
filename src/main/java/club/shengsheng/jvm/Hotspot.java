package club.shengsheng.jvm;

import tech.medivh.classpy.classfile.ClassFile;

import java.io.File;
import java.util.Arrays;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class Hotspot {

    private String mainClass;

    private BootstrapClassLoader classLoader;

    public Hotspot(String mainClass, String classPathString) {
        this.mainClass = mainClass;
        this.classLoader = new BootstrapClassLoader(Arrays.asList(classPathString.split(File.pathSeparator)));
    }

    public void start() throws Exception {
        ClassFile mainClassFile = classLoader.loadClass(mainClass);
        StackFrame mainFrame = new StackFrame(mainClassFile.getMainMethod(), mainClassFile.getConstantPool());
        Thread mainThread = new Thread("main", mainFrame, classLoader);
        mainThread.start();

    }


}
