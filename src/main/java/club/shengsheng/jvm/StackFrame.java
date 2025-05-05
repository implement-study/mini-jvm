package club.shengsheng.jvm;

import tech.medivh.classpy.classfile.MethodInfo;
import tech.medivh.classpy.classfile.bytecode.Instruction;
import tech.medivh.classpy.classfile.constant.ConstantPool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class StackFrame {

    final MethodInfo methodInfo;
    final Object[] localVariable;
    final Deque<Object> operandStack;
    final List<Instruction> codes;
    final ConstantPool constantPool;
    int currentIndex;

    public StackFrame(MethodInfo methodInfo, ConstantPool constantPool, Object... args) {
        this.methodInfo = methodInfo;
        this.localVariable = new Object[methodInfo.getMaxLocals()];
        this.operandStack = new ArrayDeque<>();
        this.codes = methodInfo.getCodes();
        this.constantPool = constantPool;
        System.arraycopy(args, 0, localVariable, 0, args.length);
    }

    public Instruction getNextInstruction() {
        return codes.get(currentIndex++);
    }

    public void pushObjectToOperandStack(Object object) {
        this.operandStack.push(object);
    }

    public void jumpTo(int index) {
        for (int i = 0; i < codes.size(); i++) {
            Instruction instruction = codes.get(i);
            if (instruction.getPc() == index) {
                this.currentIndex = i;
                return;
            }
        }
    }
}
