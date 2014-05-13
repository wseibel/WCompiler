package compiler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.bcel.Constants.*;

public class Interpreter {
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
	
	private String testString;

	@Test
	public void testConstants() {

		String text = " ";
		doTest(text, 0.0);

		text = "";
		doTest(text, 0.0);

		text = "0";
		doTest(text, 0.0);

		text = "9";
		doTest(text, 9.0);
	}

	@Test
	public void testPlus() {

		testString = "1++2";
		doTest(testString, 0.0);

		testString = "1+";
		doTest(testString, 0.0);

		testString = "1+2+3+4+5+6+7+8+9";
		doTest(testString, 45.0);
	}

	@Test
	public void testMinus() {

		testString = "2--1";
		doTest(testString, 0.0);

		testString = "1-";
		doTest(testString, 0.0);

		testString = "1-4";
		doTest(testString, -3.0);

		testString = "8-1-1-2-3";
		doTest(testString, 1.0);
	}

	@Test
	public void testMult() {

		testString = "2**3";
		doTest(testString, 0.0);

		testString = "2*";
		doTest(testString, 0.0);

		testString = "1*2*3*4*5";
		doTest(testString, 120.0);
	}

	@Test
	public void testDiv() {

		testString = "6//2";
		doTest(testString, 0.0);

		testString = "2/";
		doTest(testString, 0.0);

		testString = "2/4";
		doTest(testString, 0.5);

		testString = "8/4";
		doTest(testString, 2.0);
	}

	@Test
	public void testMixed() {

		testString = "4/2-1";
		doTest(testString, 1.0);

		testString = "1+2/4";
		doTest(testString, 1.5);

		testString = "2*3+3";
		doTest(testString, 9.0);

		testString = "4+1*6";
		doTest(testString, 10.0);

		testString = "9-5-5+9-4";
		doTest(testString, 4.0);

		testString = "2*6/4";
		doTest(testString, 3.0);

		testString = "6/4*2";
		doTest(testString, 3.0);
	}

	@Test
	public void testBrackets() {

		testString = "(((4)";
		doTest(testString, 4);

		testString = "1+(((((2*3)";
		doTest(testString, 7.0);

		testString = "(1+2)*3";
		doTest(testString, 9.0);

		testString = "(1+2)/3";
		doTest(testString, 1.0);
	}

	@Test
	public void testMultiDigitNumbers() {

		testString = "42534";
		doTest(testString, 42534);
	}

	@Test
	public void testAssignments() {

		testString = "i = 1 n = 2 i n 1 (1 + 3) 1 n ";
		doTest(testString, 2.0);

		testString = "x = 2 " + "y = x * x " + "y + 2 ";
		doTest(testString, 6.0);

		testString = "x = 20 " + "z = 10 " + "y = (3 * z) + x " + "y + 2 ";
		doTest(testString, 52.0);

		testString = "x = z ";
		doTest(testString, 0.0);
	}

	@Test
	public void testMultiCharVariables() {
		testString = "__var = 2 " + "num = __var * __var " + "num + 2 ";
		doTest(testString, 6.0);
	}

	@Test
	public void testDecimalNumbers() {

		testString = "26.var";
		doTest(testString, 0.0);

		testString = "26.";
		doTest(testString, 0.0);

		testString = "0.23 + 0.54";
		doTest(testString, 0.77);

		testString = "56.23567";
		doTest(testString, 56.23567);
	}

	@Test
	public void testExponentialNumbers() {

		testString = "0.23e8var";
		doTest(testString, 0.0);

		testString = "0.23e";
		doTest(testString, 0.0);

		testString = "12E-3 * 12E-3";
		doTest(testString, 0.000144);

		testString = "0.23e+3";
		doTest(testString, 230);

		testString = "12E3";
		doTest(testString, 12000);
	}

	@Test
	public void testIf() {
		testString = "value = 42 " + "if value > 0 " + "{ x = 42 }" + "else "
				+ "{ x = 23 }" + "x";

		doTest(testString, 42);
	}

	@Test
	public void testWhile() {
		testString = "i = 1 " + "sum = 0 " + "while i < 11 " + "{ "
				+ "	sum = sum + i " + "	i = i + 1 " + "} " + "sum";

		doTest(testString, 55);
	}

	@Test
	public void testFunction() {

		testString = "a = 1 "
				+ "function fact (x) { if x = 1 { x } else { x*fact ( x-1 ) } } "
				+ "fact(6)" + "";
		doTest(testString, 720);

		testString = "a = 1 " + "function sum (a b) { 2 * a + b } " + "sum (a 23) "
				+ "";
		doTest(testString, 25);

		testString = "function pi () { 3.1415927 } " + "r = 1 " + "l = 2 * pi() * r "
				+ "l";
		doTest(testString, 2 * 3.1415927);
		
		doTest("function mult (a b){ a * b } mult( 2.1 5.3 ) ", 11.13);
	}
	
	private void doTest(String string, double expected) {
		double result = 0.0;
		
		result = parse(string);

		Assert.assertEquals("Wrong result: ", expected, result, 0.0000001);
	}

	public double parse(String newText) {

		text = newText;
		// parse the text

		double result = 0;
		readPos = 0;
		lookAheadPos = 0;

		symbolTable = new LinkedHashMap<String, Double>();
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

		return result;
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
		double result = 0;

		// functionDecl ::= function name (formParam*) {statList}
		skip("function");

		String funcName = currentWord();

		nextToken();

		FuncEntry funcEntry = new FuncEntry();

		skip('(');

		while (currentToken.kind == 'v') {
			funcEntry.formParams.add(currentWord());
			nextToken();
		}

		skip(')');

		funcEntry.funcStartPos = readPos - 1;

		skipBlock();

		functionTable.put(funcName, funcEntry);

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

		while (parseExpression() != 0) {
			skip('{');
			result = parseStatList();
			skip('}');

			// jump back to boolean condition
			jumpTo(conditionPos);
		}

		skipBlock();

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

		if (boolResult != 0) {
			skip('{');
			result = parseStatList();
			skip('}');
		} else {
			skipBlock();
		}

		if (currentWord().equals("else")) {
			skip("else");
			if (boolResult == 0) {
				skip('{');
				result = parseStatList();
				skip('}');
			} else {
				skipBlock();
			}
		}
		return result;
	}

	private void skipBlock() {
		int depth = 0;
		skip('{');

		while ((depth > 0 || currentToken.kind != '}')
				&& currentToken.kind != Character.MIN_VALUE) {
			if (currentToken.kind == '{') {
				depth++;
			}
			if (currentToken.kind == '}') {
				depth--;
			}
			nextToken();
		}
		if (currentToken.kind == Character.MIN_VALUE) {
			parserError("missing }", readPos - 1);
		}
		skip('}');
	}

	private void skip(String string) {
		if (currentWord().equals(string)) {
			nextToken();
		} else {
			parserError(string + " is missing", readPos - 1);
		}
	}

	private LinkedHashMap<String, Double> symbolTable;
	private LinkedHashMap<String, FuncEntry> functionTable;

	private double parseAssignment() {
		double result = 0;

		// assignment = varName = expression
		String varName = currentWord();

		nextToken();
		skip('=');

		result = parseExpression();

		symbolTable.put(varName, result);

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
			} else if (op == '-') {
				result -= second;
			} else if (op == '&') {
				if (result != 0 && second != 0) {
					result = 1;
				} else {
					result = 0;
				}
			} else if (op == '|') {
				if (result != 0 || second != 0) {
					result = 1;
				} else {
					result = 0;
				}
			}
		}
		return result;
	}

	private double parseTerm() {
		// term ::= factor [*|/ factor]*
		double result = 0;

		result = parseFactor();
		while ("*/<>=".indexOf(currentToken.kind) >= 0) {

			// skip *|/
			char op = currentToken.kind;
			nextToken();

			if (op == '*') {
				result *= parseFactor();
			} else if (op == '/') {
				result /= parseFactor();
			} else if (op == '<') {
				if (result < parseFactor()) {
					result = 1;
				} else {
					result = 0;
				}
			} else if (op == '>') {
				if (result > parseFactor()) {
					result = 1;
				} else {
					result = 0;
				}
			} else if (op == '=') {
				if (result == parseFactor()) {
					result = 1;
				} else {
					result = 0;
				}
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
			Double tableValue = symbolTable.get(currentWord());

			if (funcEntry != null) {
				result = parseFunctionCall(funcEntry);
			} else if (tableValue == null) {
				parserError("variable " + currentToken.text + " has no value",
						text.indexOf(currentWord()));
			} else {
				result = tableValue;
				nextToken();
			}
		} else {
			result = parseNumber();
		}

		return result;
	}

	private double parseFunctionCall(FuncEntry funcEntry) {
		double result = 0;

		// funcCall ::= name ( expression* )
		nextToken();
		skip('(');

		LinkedHashMap<String, Double> funcSymbolTable = new LinkedHashMap<String, Double>();
//		funcSymbolTable.putAll(symbolTable);

		int i = 0;
		while (currentToken.kind != ')'
				&& currentToken.kind != Character.MIN_VALUE) {
			double actParamValue = parseExpression();

			String formParamName = funcEntry.formParams.get(i);
			i++;

			funcSymbolTable.put(formParamName, actParamValue);
		}

		skip(')');

		// call function
		int returnPos = readPos - 1;

		jumpTo(funcEntry.funcStartPos);

		skip('{');
		LinkedHashMap<String, Double> oldSymbolTable = symbolTable;
		symbolTable = funcSymbolTable;
		result = parseStatList();
		symbolTable = oldSymbolTable;
		skip('}');

		jumpTo(returnPos);
		return result;
	}

	private double parseNumber() {
		double result = 0;

		if (Character.isDigit(currentToken.kind)) {

			result = currentToken.value;

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
			text = text + c;
			System.out.println(text + "\nMissing char " + c + " appended");
			nextToken();
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
	private String class_name;

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
					return;
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
					if (currentChar != Character.MIN_VALUE
							&& currentChar != ' ') {
						lookAheadPos--;
						// digit expected
						parserError("Digit expected", lookAheadPos - 1);
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
					if (currentChar != Character.MIN_VALUE
							&& currentChar != ' ') {
						lookAheadPos--;
						// digit expected
						parserError("Digit expected", lookAheadPos);
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
}
