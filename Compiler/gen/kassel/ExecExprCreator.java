package kassel;

import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

public class ExecExprCreator implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;

  public ExecExprCreator() {
    _cg = new ClassGen("kassel.ExecExpr", "java.lang.Object", "ExecExpr.java", ACC_PUBLIC | ACC_SUPER, new String[] {  });

    _cp = _cg.getConstantPool();
    _factory = new InstructionFactory(_cg, _cp);
  }

  public void create(OutputStream out) throws IOException {
    createMethod_0();
    createMethod_1();
    createMethod_2();
    _cg.getJavaClass().dump(out);
  }

  private void createMethod_0() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", "kassel.ExecExpr", il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_1() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, Type.VOID, new Type[] { new ArrayType(Type.STRING, 1) }, new String[] { "arg0" }, "main", "kassel.ExecExpr", il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createInvoke("kassel.ExecExpr", "test", Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKESTATIC));
    il.append(InstructionConstants.POP);
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_2() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, Type.BOOLEAN, Type.NO_ARGS, new String[] {  }, "test", "kassel.ExecExpr", il, _cp);

    InstructionHandle ih_0 = il.append(new PUSH(_cp, 2.0));
    il.append(_factory.createStore(Type.DOUBLE, 0));
    InstructionHandle ih_4 = il.append(new PUSH(_cp, 3.0));
    il.append(_factory.createStore(Type.DOUBLE, 2));
    InstructionHandle ih_8 = il.append(_factory.createLoad(Type.DOUBLE, 0));
    il.append(_factory.createLoad(Type.DOUBLE, 2));
    il.append(InstructionConstants.DCMPL);
        BranchInstruction ifne_11 = _factory.createBranchInstruction(Constants.IFNE, null);
    il.append(ifne_11);
    il.append(new PUSH(_cp, 1));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_16 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_17 = il.append(_factory.createReturn(Type.INT));
    ifne_11.setTarget(ih_16);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  public static void main(String[] args) throws Exception {
    kassel.ExecExprCreator creator = new kassel.ExecExprCreator();
    creator.create(new FileOutputStream("kassel.ExecExpr.class"));
  }
}
