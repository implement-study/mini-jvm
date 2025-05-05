package club.shengsheng.jvm;

import tech.medivh.classpy.classfile.ClassFile;
import tech.medivh.classpy.classfile.MethodInfo;
import tech.medivh.classpy.classfile.bytecode.Branch;
import tech.medivh.classpy.classfile.bytecode.GetStatic;
import tech.medivh.classpy.classfile.bytecode.Instruction;
import tech.medivh.classpy.classfile.bytecode.InvokeStatic;
import tech.medivh.classpy.classfile.bytecode.InvokeVirtual;
import tech.medivh.classpy.classfile.constant.ConstantMethodrefInfo;
import tech.medivh.classpy.classfile.constant.ConstantPool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class Thread {

    private final String threadName;

    private final JvmStack stack;

    private final PcRegister pcRegister;

    private final BootstrapClassLoader classLoader;

    public Thread(String threadName, StackFrame stackFrame, BootstrapClassLoader classLoader) {
        this.threadName = threadName;
        this.stack = new JvmStack();
        this.stack.push(stackFrame);
        this.pcRegister = new PcRegister(stack);
        this.classLoader = classLoader;
    }


    public void start() throws Exception {
        for (Instruction instruction : pcRegister) {
//            System.out.println(instruction);
            ConstantPool constantPool = stack.peek().constantPool;
            switch (instruction.getOpcode()) {
                case getstatic -> {
                    GetStatic getStatic = (GetStatic) instruction;
                    String className = getStatic.getClassName(constantPool);
                    String fieldName = getStatic.getFieldName(constantPool);
                    Object staticField;
                    if (className.contains("java")) {
                        Class<?> aClass = Class.forName(className);
                        Field declaredField = aClass.getDeclaredField(fieldName);
                        staticField = declaredField.get(null);
                        stack.peek().pushObjectToOperandStack(staticField);
                    }
                }
                case iconst_1 -> stack.peek().pushObjectToOperandStack(1);
                case iconst_2 -> stack.peek().pushObjectToOperandStack(2);
                case iconst_3 -> stack.peek().pushObjectToOperandStack(3);
                case iconst_4 -> stack.peek().pushObjectToOperandStack(4);
                case iload_0 -> stack.peek().pushObjectToOperandStack(stack.peek().localVariable[0]);
                case iload_1 -> stack.peek().pushObjectToOperandStack(stack.peek().localVariable[1]);
                case invokevirtual -> {
                    InvokeVirtual invokeVirtual = (InvokeVirtual) instruction;
                    ConstantMethodrefInfo methodInfo = invokeVirtual.getMethodInfo(constantPool);
                    String className = methodInfo.className(constantPool);
                    String methodName = methodInfo.methodName(constantPool);
                    List<String> params = methodInfo.paramClassName(constantPool);
                    if (className.contains("java")) {
                        Class<?> aClass = Class.forName(className);
                        Method declaredMethod = aClass.getDeclaredMethod(methodName,
                            params.stream().map(this::nameToClass).toArray(Class[]::new));
                        Object[] args = new Object[params.size()];
                        for (int index = args.length - 1; index >= 0; index--) {
                            args[index] = stack.peek().operandStack.pop();
                        }
                        Object result = declaredMethod.invoke(stack.peek().operandStack.pop(), args);
                        if (!methodInfo.isVoid(constantPool)) {
                            stack.peek().pushObjectToOperandStack(result);
                        }
                        break;
                    }
                    ClassFile classFile = classLoader.loadClass(className);
                    MethodInfo finalMethodInfo = classFile.getMethods(methodName).get(0);
                    Object[] args = new Object[params.size() + 1];
                    for (int index = args.length - 1; index >= 0; index--) {
                        args[index] = stack.peek().operandStack.pop();
                    }
                    StackFrame stackFrame = new StackFrame(finalMethodInfo, classFile.getConstantPool(), args);
                    stack.push(stackFrame);
                }
                case invokestatic -> {
                    InvokeStatic invokeStatic = (InvokeStatic) instruction;
                    ConstantMethodrefInfo methodInfo = invokeStatic.getMethodInfo(constantPool);
                    String className = methodInfo.className(constantPool);
                    String methodName = methodInfo.methodName(constantPool);
                    List<String> params = methodInfo.paramClassName(constantPool);
                    if (className.contains("java")) {
                        Class<?> aClass = Class.forName(className);
                        Method declaredMethod = aClass.getDeclaredMethod(methodName,
                            params.stream().map(this::nameToClass).toArray(Class[]::new));
                        Object[] args = new Object[params.size()];
                        for (int index = args.length - 1; index >= 0; index--) {
                            args[index] = stack.peek().operandStack.pop();
                        }
                        Object result = declaredMethod.invoke(null, args);
                        if (!methodInfo.isVoid(constantPool)) {
                            stack.peek().pushObjectToOperandStack(result);
                        }
                        break;
                    }
                    ClassFile classFile = classLoader.loadClass(className);
                    MethodInfo finalMethodInfo = classFile.getMethods(methodName).get(0);
                    Object[] args = new Object[params.size()];
                    for (int index = args.length - 1; index >= 0; index--) {
                        args[index] = stack.peek().operandStack.pop();
                    }
                    StackFrame stackFrame = new StackFrame(finalMethodInfo, classFile.getConstantPool(), args);
                    stack.push(stackFrame);
                }
                case _return -> {
                    this.stack.pop();
                }
                case ireturn -> {
                    int result = (int) this.stack.peek().operandStack.pop();
                    stack.pop();
                    stack.peek().pushObjectToOperandStack(result);
                }
                case if_icmple -> {
                    int value2 = (int) stack.peek().operandStack.pop();
                    int value1 = (int) stack.peek().operandStack.pop();
                    if (value1 <= value2) {
                        Branch branch = (Branch) instruction;
                        int jumpTo = branch.getJumpTo();
                        stack.peek().jumpTo(jumpTo);
                    }
                }
                default -> throw new IllegalArgumentException("这个指令还没有实现" + instruction);
            }
        }
    }

    private Class<?> nameToClass(String className) {
        if (className.equals("int")) {
            return int.class;
        }
        try {
            return Class.forName(className);
        } catch (Exception e) {
            return null;
        }

    }

}
