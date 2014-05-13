package compiler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.bcel.Constants.*;

public class Compiler {
	private static final char E = 'E';
	private static final char E_S = 'e';
	private static final char EXP = '7';
	private static final char DOT = '.';
	private static final char NUMBER = '9';
	private static final char START = 's';
	private static final char MANTISSA = '8';
	private int readPos;
	private int lookAheadPos;
	private Token currentToken = new Token();
	private String text;

	@Test
	public void testConstants() {

		testText = "";
		doTest(testText, 0.0);

		testText = "0";
		doTest(testText, 0.0);

		testText = "9";
		doTest(testText, 9.0);

		testText = "1234567890";
		doTest(testText, 1234567890.0);
	}

	@Test
	public void testPlus() {

		testText = "1.33+1.4";
		doTest(testText, 2.73);

		testText = "1+2+3+4+5+6+7+8+9";
		doTest(testText, 45.0);
	}

	@Test
	public void testMinus() {

		testText = " 2.61-1.3";
		doTest(testText, 1.31);

		testText = "8-1-1-2-3";
		doTest(testText, 1.0);
	}

	@Test
	public void testMult() {

		testText = "5.2*1.5";
		doTest(testText, 7.8);

		testText = "1*2*3*4*5";
		doTest(testText, 120.0);
	}

	@Test
	public void testDiv() {

		testText = "8.4/2.8";
		doTest(testText, 3.0);

		testText = "4.2/2.1";
		doTest(testText, 2.0);
	}

	@Test
	public void testMixed() {

		testText = "4/2-1";
		doTest(testText, 1.0);

		testText = "1+2/4";
		doTest(testText, 1.5);

		testText = "2*3+3";
		doTest(testText, 9.0);

		testText = "4+1*6";
		doTest(testText, 10.0);

		testText = "9-5-5+9-4";
		doTest(testText, 4.0);

		testText = "2*6/4";
		doTest(testText, 3.0);

		testText = "6/4*2";
		doTest(testText, 3.0);
	}

	@Test
	public void testBrackets() {

		testText = "(((4)";
		doTest(testText, 4);

		testText = "1+(((((2*3)";
		doTest(testText, 7.0);

		testText = "(1+2)*3";
		doTest(testText, 9.0);

		testText = "(1+2)/3";
		doTest(testText, 1.0);

		testText = "(3+4)*5";
		doTest(testText, 35.0);

		testText = "if (3>2) {1 + 2*(2+2} else {5}";
		doTest(testText, 9.0);
	}

	@Test
	public void testMultiDigitNumbers() {

		testText = "42534";
		doTest(testText, 42534);
	}

	@Test
	public void testAssignments() {

		testText = "i = 1 n = 2 i n 1 (1 + 3) 1 n ";
		doTest(testText, 2.0);

		testText = "x = 2 " + "y = x * x " + "y + 2 ";
		doTest(testText, 6.0);

		testText = "x = 20 " + "z = 10 " + "y = (3 * z) + x " + "y + 2 ";
		doTest(testText, 52.0);

		testText = "x = z ";
		doTest(testText, 0.0);
	}

	@Test
	public void testMultiCharVariables() {
		testText = "__var = 2 " + "num = __var * __var " + "num + 2 ";
		doTest(testText, 6.0);
	}

	@Test
	public void testDecimalNumbers() {

		testText = "26.var";
		doTest(testText, 0.0);

		testText = "26.";
		doTest(testText, 0.0);

		testText = "0.23 + 0.54";
		doTest(testText, 0.77);

		testText = "56.23567";
		doTest(testText, 56.23567);
	}

	@Test
	public void testExponentialNumbers() {

		testText = "0.23e8var";
		doTest(testText, 0.0);

		testText = "0.23e";
		doTest(testText, 0.0);

		testText = "12E-3 * 12E-3";
		doTest(testText, 0.000144);

		testText = "0.23e+3";
		doTest(testText, 230);

		testText = "12E3";
		doTest(testText, 12000);
	}

	@Test
	public void testIf() {
		doTest("value = 13 " + "if value = 42 " + "{ x = 42 } " + "else "
				+ "{ x = 23 }" + " x", 23.0);

		testText = "value = 42 " + "if value < 0 "
				+ "{ if 3 > 2 { x = 3 } else { x = 2 } }" + "else "
				+ "{ x = 23 }" + "x";

		doTest(testText, 23);

		testText = "if 3.18E+3 > 0 " + "{ x = 3 }" + "else " + "{ x = 23 }"
				+ "x";

		doTest(testText, 3);

		testText = "value = 42 " + "if value < 0 " + "{ x = 42 }" + "else "
				+ "{ x = 23 }" + "x";

		doTest(testText, 23);

		testText = "value = 10 " + "if value = 10 " + "{ x = 42 }" + "else "
				+ "{ x = 23 }" + "x";

		doTest(testText, 42);

		testText = "value = 30 " + "if value = 10 " + "{ x = 42 }" + "else "
				+ "{ x = 23 }" + "x";

		doTest(testText, 23);
	}

	@Test
	public void testAndOr() {

		doTest("function fi ( number )"
				+ "{ if number < 2 { number } else { fi ( number - 1 ) + fi ( number - 2 ) } }"
				+ " fi(7) " + "", 13);

		testText = " x = 1 y = 2 z = 3 if (x < y " + " & z < 4) | 3.81E-6 = y"
				+ " { x = 3 } else { x = 20 } x";
		doTest(testText, 3);

		testText = " x = 1 y = 2 z = 3 if x < y " + " | z > 4 "
				+ " { x = 3 } else { x = 20 } x";
		doTest(testText, 3);
	}

	@Test
	public void testWhile() {

		testText = "i = 11e+2 " + "sum = 0 " + "while i = 11E+2 " + "{ "
				+ "	sum = sum + i " + "	i = i + 1 " + "} " + "sum";

		doTest(testText, 11e2);

		testText = "i = 5 " + "sum = 0 " + "while i > 0 " + "{ "
				+ "	sum = sum + i " + "	i = i - 1 " + "} " + "sum";

		doTest(testText, 15);

		testText = "i = 1 " + "sum = 0 " + "while i < 10.33 " + "{ "
				+ "	sum = sum + i " + "	i = i + 1 " + "} " + "sum";

		doTest(testText, 55);
	}

	@Test
	public void testFunction() {

		testText = "a = 1 "
				+ "function min (a b) { if a < b { a } else { b } } "
				+ "min (a 23) " + "";
		doTest(testText, 1);

		testText = "a = 1 " + "function sum (a b) { 2 * a + b } "
				+ "sum (a 23) " + "";
		doTest(testText, 25);

		testText = "function pi () { 3.1415927 } " + "r = 1 "
				+ "l = 2 * pi() * r " + "l";
		doTest(testText, 2 * 3.1415927);

		doTest("function mult (a b){ a * b } mult( 2.1 5.3 ) ", 11.13);
	}

	@Test
	public void testRecursiveFunction() {

		testText = "function g ( num ) { if num < 2 { num } else { g ( num -1 ) + g ( num -2 ) } }"
				+ " g(7) " + "";
		doTest(testText, 13);

		testText = "a = 1 "
				+ "function fact (x) { if x = 1 { x } else { x*fact ( x-1 ) } } "
				+ "fact(6)" + "";
		doTest(testText, 720);

		testText = "a = 6 "
				+ "function fact (x) { if x = 1 { x } else { x*fact ( x-1 ) } } "
				+ "fact(a)" + "";
		doTest(testText, 720);
	}

	public static int runningNumber = 0;

	private void doTest(String parseText, double expected) {
		double result = 0.0;
		// generate instruction list

		runningNumber++;

		class_name = "ExecExpr" + runningNumber;

		try {
			parse(parseText);
		} catch (Exception e) {
			System.err.println("Unable to parse Expression");
		}

		// load class
		ClassLoader classLoader = this.getClass().getClassLoader();

		Class<?> clazz;
		try {
			clazz = classLoader.loadClass(class_name);

			// invoke exec method
			Method method;
			method = clazz.getDeclaredMethod("exec");
			result = (Double) method.invoke(null);
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load class: " + class_name);
		} catch (NoSuchMethodException e) {
			System.err.println("Could not find method (exec) in class: "
					+ class_name);
		} catch (SecurityException e) {
			System.err.println("Could not get method (exec) in class: "
					+ class_name + " for security reasons");
		} catch (IllegalAccessException e) {
			System.err.println("Could not access method (exec) in class: "
					+ class_name);
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid arguments for method (exec) in class: "
					+ class_name);
		} catch (InvocationTargetException e) {
			System.err.println("Could not invoke method (exec) in class: "
					+ class_name);
		}

		// check result
		Assert.assertEquals("Wrong result: ", expected, result,
				expected * 0.0000001);
	}

	public double parse(String newText) {

		prepareClassFile();

		text = newText;
		// parse the text

		double result = 0;
		readPos = 0;
		lookAheadPos = 0;

		symbolTable = new LinkedHashMap<String, Double>();
		localVarAdressTable = new LinkedHashMap<String, Integer>();
		functionTable = new LinkedHashMap<String, FuncEntry>();

		try {
			nextToken();
			nextToken();

			result = parseStatList();

			if (currentToken.kind != Character.MIN_VALUE) {
				parserError("end of text", text.length());
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		closeClassFile();
		return result;
	}

	private InstructionFactory _factory;
	private ConstantPoolGen _cp;
	private ClassGen _cg;

	private void prepareClassFile() {
		_cg = new ClassGen(class_name, "java.lang.Object", "ExecExpr.java",
				ACC_PUBLIC | ACC_SUPER, new String[] {});

		_cp = _cg.getConstantPool();
		_factory = new InstructionFactory(_cg, _cp);

		il = new InstructionList();
		MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS,
				new String[] {}, "<init>", class_name, il, _cp);

		InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
		il.append(_factory.createInvoke("java.lang.Object", "<init>",
				Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
		InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
		method.setMaxStack();
		method.setMaxLocals();
		_cg.addMethod(method.getMethod());
		il.dispose();

		il = new InstructionList();

		il = new InstructionList();
		method = new MethodGen(ACC_PUBLIC | ACC_STATIC, Type.VOID,
				new Type[] { new ArrayType(Type.STRING, 1) },
				new String[] { "arg0" }, "main", "kassel.ExecExpr", il, _cp);

		ih_0 = il.append(_factory.createInvoke(class_name, "exec", Type.DOUBLE,
				Type.NO_ARGS, Constants.INVOKESTATIC));
		il.append(_factory.createStore(Type.DOUBLE, 1));
		ih_4 = il.append(_factory.createFieldAccess("java.lang.System", "out",
				new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
		il.append(_factory.createLoad(Type.DOUBLE, 1));
		il.append(_factory.createInvoke("java.io.PrintStream", "println",
				Type.VOID, new Type[] { Type.DOUBLE }, Constants.INVOKEVIRTUAL));
		InstructionHandle ih_11 = il.append(_factory.createReturn(Type.VOID));
		method.setMaxStack();
		method.setMaxLocals();
		_cg.addMethod(method.getMethod());
		il.dispose();

		il = new InstructionList();
		il.append(new PUSH(_cp, 0.0));
	}

	private void closeClassFile() {
		// try {
		MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, Type.DOUBLE,
				Type.NO_ARGS, new String[] {}, "exec", class_name, il, _cp);

		InstructionHandle ih_3 = il.append(_factory.createReturn(Type.DOUBLE));
		method.setMaxStack();
		method.setMaxLocals();
		_cg.addMethod(method.getMethod());
		il.dispose();
		// } catch (Exception e1) {
		// System.err.println("Could not generate method: " + "bin/"
		// + class_name + ".class");
		// }

		try {
			_cg.getJavaClass().dump(
					new FileOutputStream("bin/" + class_name + ".class"));
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
		} catch (IOException e) {
			System.err.println("Could not write file: " + "bin/" + class_name
					+ ".class");
		}
	}

	private double parseStatList() {
		double result = 0;

		// statList ::= (assignment | expression | ifStat | whileStat |
		// funtionDecl)*

		while (currentToken.kind != Character.MIN_VALUE
				&& currentToken.kind != '}') {
			if (lookAheadToken.kind == '=') {
				result = parseAssignment();
			} else if (currentWord().equals("if")) {
				result = parseIfStat();
			} else if (currentWord().equals("while")) {
				result = parseWhileStat();
			} else if (currentWord().equals("function")) {
				result = parseFunctionDecl();
			} else {
				result = parseExpression();
			}
		}

		return result;
	}

	private double parseFunctionDecl() {
		InstructionList parentIl = il;
		il = new InstructionList();

		double result = 0;

		// functionDecl ::= function name (formParam*) {statList}
		skip("function");

		String funcName = currentWord();

		nextToken();

		FuncEntry funcEntry = new FuncEntry();

		skip('(');

		// replace symboltable
		LinkedHashMap<String, Double> origSymbolTable = symbolTable;
		symbolTable = new LinkedHashMap<String, Double>();
		LinkedHashMap<String, Integer> origLocalVarAddressTable = localVarAdressTable;
		localVarAdressTable = new LinkedHashMap<String, Integer>();
		ArrayList<Type> formParamTypes = new ArrayList<Type>();

		while (currentToken.kind == 'v') {
			String paramName = currentWord();
			funcEntry.formParams.add(paramName);

			formParamTypes.add(Type.DOUBLE);

			localVarAdressTable.put(paramName, localVarAdressTable.size() * 2);

			nextToken();
		}

		skip(')');

		MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, Type.DOUBLE,
				formParamTypes.toArray(Type.NO_ARGS),
				funcEntry.formParams.toArray(new String[] {}), funcName,
				class_name, il, _cp);
		functionTable.put(funcName, funcEntry);

		skip('{');
		parseStatList();
		skip('}');

		functionTable.put(funcName, funcEntry);

		il.append(_factory.createReturn(Type.DOUBLE));

		method.setMaxStack();
		method.setMaxLocals();
		_cg.addMethod(method.getMethod());
		il.dispose();

		il = parentIl;
		symbolTable = origSymbolTable;
		localVarAdressTable = origLocalVarAddressTable;
		return result;
	}

	public String currentWord() {
		return currentToken.text.toString();
	}

	private double parseWhileStat() {
		double result = 0;

		// whileStat ::= while boolExpr {statlist}
		skip("while");

		int conditionPos = readPos - 1;

		InstructionHandle startNop = il.append(InstructionFactory.NOP);

		parseExpression();
		BranchInstruction condJump = _factory.createBranchInstruction(
				Constants.IFLE, null);
		il.append(condJump);

		skip('{');
		result = parseStatList();
		skip('}');

		// jump back to boolean condition
		BranchInstruction gotoNop = _factory.createBranchInstruction(
				Constants.GOTO, null);
		il.append(gotoNop);

		gotoNop.setTarget(startNop);

		InstructionHandle endNop = il.append(InstructionFactory.NOP);
		condJump.setTarget(endNop);

		return result;
	}

	public void jumpTo(int conditionPos) {
		lookAheadPos = conditionPos;
		nextToken();
		nextToken();
	}

	private double parseIfStat() {
		double result = 0;

		// ifStat ::= if boolExpr {statlist} (else {statlist})?
		skip("if");

		double boolResult = parseExpression();

		// jump on result
		BranchInstruction condJump = _factory.createBranchInstruction(
				Constants.IFLE, null);
		il.append(condJump);

		skip('{');
		result = parseStatList();
		skip('}');

		// create goto command
		BranchInstruction gotoNop = _factory.createBranchInstruction(
				Constants.GOTO, null);
		il.append(gotoNop);

		InstructionHandle startOfElse = il.append(InstructionConstants.NOP);
		condJump.setTarget(startOfElse);

		if (currentWord().equals("else")) {
			skip("else");
			skip('{');

			result = parseStatList();

			skip('}');
		} else {
			// handle case without else
		}

		// add nop command
		InstructionHandle nopCmd = il.append(InstructionConstants.NOP);

		// adjust jump adresses
		gotoNop.setTarget(nopCmd);

		return result;
	}

	private void skip(String string) {
		if (currentWord().equals(string)) {
			nextToken();
		} else {
			parserError(string + " is missing", readPos - 1);
		}
	}

	private LinkedHashMap<String, Double> symbolTable;
	private LinkedHashMap<String, Integer> localVarAdressTable;
	private LinkedHashMap<String, FuncEntry> functionTable;

	private double parseAssignment() {
		double result = 0;

		// assignment = varName = expression
		String varName = currentWord();

		nextToken();
		skip('=');

		result = parseExpression();

		symbolTable.put(varName, result);
		Integer address = localVarAdressTable.get(varName);
		if (address == null) {
			address = localVarAdressTable.size() * 2;
			localVarAdressTable.put(varName, address);
		}
		il.append(_factory.createStore(Type.DOUBLE, address));

		return result;
	}

	private double parseExpression() {
		// expression ::= term [+|-|&|| term]*
		double result = 0;
		result = parseTerm();

		while ("+-&|".indexOf(currentToken.kind) >= 0) {

			// skip +|-
			char op = currentToken.kind;
			nextToken();

			double second = parseTerm();

			if (op == '+') {
				result += second;
				il.append(InstructionConstants.DADD);
			} else if (op == '-') {
				result -= second;
				il.append(InstructionConstants.DSUB);
			} else if (op == '&') {
				il.append(InstructionConstants.IAND);
			} else if (op == '|') {
				il.append(InstructionConstants.IOR);
			}
		}
		return result;
	}

	private double parseTerm() {
		// term ::= factor [ * | / | < | > | = factor]*
		double result = 0;

		result = parseFactor();
		while ("*/<>=".indexOf(currentToken.kind) >= 0) {

			// skip *|/
			char op = currentToken.kind;
			nextToken();

			if (op == '*') {
				result *= parseFactor();
				il.append(InstructionConstants.DMUL);
			} else if (op == '/') {
				result /= parseFactor();
				il.append(InstructionConstants.DDIV);
			} else if (op == '<') {
				parseFactor();

				// add compare command
				il.append(InstructionConstants.DCMPG);
				BranchInstruction ifge_9 = _factory.createBranchInstruction(
						Constants.IFGE, null);
				il.append(ifge_9);
				il.append(new PUSH(_cp, 1));
				BranchInstruction goto_13 = _factory.createBranchInstruction(
						Constants.GOTO, null);
				il.append(goto_13);
				InstructionHandle ih_16 = il.append(new PUSH(_cp, 0));
				InstructionHandle nop = il.append(InstructionFactory.NOP);

				ifge_9.setTarget(ih_16);
				goto_13.setTarget(nop);
			} else if (op == '>') {
				parseFactor();

				// add compare command
				il.append(InstructionConstants.DCMPG);
				BranchInstruction ifge_23 = _factory.createBranchInstruction(
						Constants.IFLE, null);
				il.append(ifge_23);
				il.append(new PUSH(_cp, 1));
				BranchInstruction goto_27 = _factory.createBranchInstruction(
						Constants.GOTO, null);
				il.append(goto_27);
				InstructionHandle ih_30 = il.append(new PUSH(_cp, 0));
				InstructionHandle nop = il.append(InstructionFactory.NOP);

				ifge_23.setTarget(ih_30);
				goto_27.setTarget(nop);

			} else if (op == '=') {
				if (result == parseFactor()) {
					result = 1;
				} else {
					result = 0;
				}

				// add compare command
				il.append(InstructionConstants.DCMPG);
				BranchInstruction ifge_9 = _factory.createBranchInstruction(
						Constants.IFNE, null);
				il.append(ifge_9);
				il.append(new PUSH(_cp, 1));
				BranchInstruction goto_13 = _factory.createBranchInstruction(
						Constants.GOTO, null);
				il.append(goto_13);
				InstructionHandle ih_16 = il.append(new PUSH(_cp, 0));
				InstructionHandle nop = il.append(InstructionFactory.NOP);

				ifge_9.setTarget(ih_16);
				goto_13.setTarget(nop);

			}
		}
		return result;
	}

	private double parseFactor() {
		// factor ::= number | (expression) | varName | true | false |
		// functionCall
		double result = 0;

		if (currentToken.kind == '(') {
			// parse expression
			// skip (
			nextToken();

			result = parseExpression();

			// should be a )
			repair(')');
		} else if (currentWord().equals("true")) {
			nextToken();
			result = 1;
		} else if (currentWord().equals("false")) {
			nextToken();
			result = 0;
		} else if (Character.isLetter(currentToken.kind)) {
			// function
			FuncEntry funcEntry = functionTable.get(currentWord());

			// variable
			Integer tableValue = localVarAdressTable.get(currentWord());

			if (funcEntry != null) {
				result = parseFunctionCall(funcEntry);
			} else if (tableValue == null) {
				parserError("variable " + currentToken.text + " has no value",
						text.indexOf(currentWord()));
				nextToken();
			} else {
				result = tableValue;

				// create load command
				Integer address = localVarAdressTable.get(currentWord());

				il.append(_factory.createLoad(Type.DOUBLE, address));
				nextToken();
			}
		} else if (currentToken.kind == NUMBER || currentToken.kind == MANTISSA
				|| currentToken.kind == EXP) {
			result = parseNumber();
		} else {
			// funcName
			FuncEntry funcEntry = functionTable.get(currentWord());

			// var ::= (_|l)(l|_|d)* _ = underscore; l = letter; d = digit
			Integer tableValue = localVarAdressTable.get(currentWord());

			if (funcEntry != null) {
				result = parseFunctionCall(funcEntry);
			} else if (tableValue == null) {
				parserError("Assignment Error: Variable " + currentToken.text
						+ " has no value", text.indexOf(currentWord()));
				nextToken();
			} else {
				result = tableValue;

				// create a load command
				Integer address = localVarAdressTable.get(currentWord());

				il.append(_factory.createLoad(Type.DOUBLE, address));
				nextToken();
			}

		}

		return result;
	}

	private double parseFunctionCall(FuncEntry funcEntry) {
		double result = 0;

		// funcCall ::= name ( expression* )

		String funcName = currentWord();

		nextToken();
		skip('(');

		ArrayList<Type> formParamTypeList = new ArrayList<Type>();
		LinkedHashMap<String, Double> funcSymbolTable = new LinkedHashMap<String, Double>();

		int i = 0;
		while (currentToken.kind != ')'
				&& currentToken.kind != Character.MIN_VALUE) {
			double actParamValue = parseExpression();

			formParamTypeList.add(Type.DOUBLE);

			String formParamName = funcEntry.formParams.get(i);
			i++;

			funcSymbolTable.put(formParamName, actParamValue);
		}

		skip(')');

		// call function
		InstructionHandle ih_0 = il.append(_factory.createInvoke(class_name,
				funcName, Type.DOUBLE, formParamTypeList.toArray(Type.NO_ARGS),
				Constants.INVOKESTATIC));

		return result;
	}

	private double parseNumber() {
		double result = 0;

		if (Character.isDigit(currentToken.kind)) {

			result = currentToken.value;

			il.append(new PUSH(_cp, currentToken.value));

			nextToken();
		} else {
			parserError("Number expected", readPos - 1);
		}
		return result;
	}

	private void repair(char c) {
		if (currentToken.kind == c) {
			nextToken();
		} else {
			StringBuilder sb = new StringBuilder(text);
			if (readPos >= text.length()) {
				text = sb.append(c).toString();
				nextToken();
			} else {
				text = sb.insert(--readPos, " " + c + " ").toString();
				lookAheadPos += 3;
			}
			System.out.println(text + "\nMissing char " + c + " appended");
		}
	}

	private void skip(char c) {
		if (currentToken.kind == c) {
			nextToken();
		} else {
			parserError(c + " is missing", readPos - 1);
		}
	}

	private void parserError(String Error, int errorPos) {
		StringBuffer blanks = new StringBuffer();
		for (int i = 0; i < errorPos && i < text.length(); i++) {
			blanks.append(' ');
		}
		throw new RuntimeException(Error + ": \n" + text + "\n"
				+ blanks.toString() + "^");
	}

	private Token lookAheadToken = new Token();
	private InstructionList il;
	private String class_name = "ExecExpr" + runningNumber;

	private void nextToken() {
		Token tmp = currentToken;
		currentToken = lookAheadToken;
		lookAheadToken = tmp;
		lookAheadToken.text.setLength(0);

		readPos = lookAheadPos;

		int mantissaPos = 0;
		double exponent = 0;
		boolean expSign = true; // true means +, false -
		char currentChar = nextChar();
		lookAheadToken.kind = START;
		while (true) {
			switch (lookAheadToken.kind) {
			case START:
				if (Character.isWhitespace(currentChar)) {
					// skip
				} else if (Character.isDigit(currentChar)) {
					lookAheadToken.kind = NUMBER;
					lookAheadToken.value = currentChar - '0';
				} else if ("()*/+-=&|><{}".indexOf(currentChar) >= 0) {
					// Operator detected
					lookAheadToken.kind = currentChar;
					return; // <======================= RETURN
				} else if (Character.isLetter(currentChar)) {
					lookAheadToken.kind = 'v';
				}
				break;

			case 'v':
				if (Character.isLetter(currentChar)
						|| Character.isDigit(currentChar)) {
					// go on
				} else {
					// varName has ended
					if (currentChar != Character.MIN_VALUE) {
						lookAheadPos--;
					}
					return; // <======================= RETURN
				}
				break;
			case NUMBER:
				if (Character.isDigit(currentChar)) {
					lookAheadToken.value = lookAheadToken.value * 10
							+ currentChar - '0';
				} else if (currentChar == DOT) {
					lookAheadToken.kind = DOT;
				} else if (currentChar == E || currentChar == E_S) {
					lookAheadToken.kind = E;
				} else {
					// number has ended
					if (currentChar != Character.MIN_VALUE) {
						lookAheadPos--;
					}
					return; // <======================= RETURN
				}
				break;
			case DOT:
				if (Character.isDigit(currentChar)) {
					lookAheadToken.kind = MANTISSA;
					mantissaPos++;
					lookAheadToken.value = lookAheadToken.value
							+ ((currentChar - '0') / Math.pow(10, mantissaPos));
				} else {
					// number expected
					parserError("Digit expected", lookAheadPos - 1);
					return; // <======================= RETURN
				}
				break;
			case MANTISSA:
				if (Character.isDigit(currentChar)) {
					mantissaPos++;
					lookAheadToken.value = lookAheadToken.value
							+ ((currentChar - '0') / Math.pow(10, mantissaPos));
				} else if (currentChar == E || currentChar == E_S) {
					lookAheadToken.kind = E;
				} else {
					// number has ended
					if (currentChar == ' ') {
						lookAheadPos--;
					} else if (Character.isLetter(currentChar)) {
						lookAheadPos--;
						// digit expected
						parserError("Digit expected", lookAheadPos - 1);
					} else if (currentChar != Character.MIN_VALUE) {
						lookAheadPos--;
					}
					return; // <======================= RETURN
				}
				break;
			case E:
				if (Character.isDigit(currentChar)) {
					exponent = exponent * 10 + currentChar - '0';
					lookAheadToken.kind = EXP;
				} else if ("+-".indexOf(currentChar) >= 0) {
					if (currentChar == '-') {
						// change exponents sign
						expSign = false;
					}
					lookAheadToken.kind = EXP;
				} else {
					lookAheadPos--;
					// digit expected
					parserError("Digit expected", lookAheadPos);
					return; // <======================= RETURN
				}
				break;
			case EXP:
				if (Character.isDigit(currentChar)) {
					exponent = exponent * 10 + currentChar - '0';
				} else {
					// exponent has ended
					// number has ended
					if (currentChar == ' ') {
						lookAheadPos--;
					} else if (Character.isLetter(currentChar)) {
						lookAheadPos--;
						// digit expected
						parserError("Digit expected", lookAheadPos - 1);
					} else if (currentChar != Character.MIN_VALUE) {
						lookAheadPos--;
					}
					if (expSign) {
						lookAheadToken.value = lookAheadToken.value
								* Math.pow(10, exponent);
					} else {
						lookAheadToken.value = lookAheadToken.value
								* Math.pow(10, -exponent);
					}
					return; // <======================= RETURN
				}
				break;
			default:
				break;
			}
			if (currentChar == Character.MIN_VALUE) {
				lookAheadToken.kind = currentChar;
				return; // <======================= RETURN
			}
			if (!Character.isWhitespace(currentChar)) {
				lookAheadToken.text.append(currentChar);
			}
			currentChar = nextChar();
		}
	}

	public char nextChar() {
		char currentChar;
		if (lookAheadPos >= text.length()) {
			currentChar = Character.MIN_VALUE;
			lookAheadPos++;
		} else {
			currentChar = text.charAt(lookAheadPos);
			lookAheadPos++;
		}
		return currentChar;
	}

	private String testText;

}
