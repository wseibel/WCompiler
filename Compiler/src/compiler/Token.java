package compiler;

public class Token {

	public char kind;
	
	public StringBuilder text = new StringBuilder();
	
	public double value;
	
	public String toString(){
		return "text: " + text.toString() + " kind: " + kind + " value: " + value;
	}
}
