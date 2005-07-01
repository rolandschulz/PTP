package org.eclipse.sexpr;

public class SExprAtom extends SExprElement {
	public static final int SEXPR_ATOM_STRING = 1;
	public static final int SEXPR_ATOM_NUMBER = 2;
	public static final int SEXPR_ATOM_SYMBOL = 3;
	public static final int SEXPR_ATOM_QUOTED = 4;
	
	private String atomString;
	private Double atomNumber;
	private int atomType;
	
	public SExprAtom(int type, String s) {
		atomString = s;
		atomType = type;
	}
	
	public SExprAtom(Double d) {
		atomNumber = d;
		atomType = SEXPR_ATOM_NUMBER;
	}
	
	public String getString() {
		return atomString;
	}
	
	public Double getNumber() {
		return atomNumber;
	}
	
	public String toString() {
		switch (atomType) {
		case SEXPR_ATOM_STRING:
			return "\"" + atomString + "\"";
			
		case SEXPR_ATOM_SYMBOL:
			return atomString;
			
		case SEXPR_ATOM_QUOTED:
			return "'" + atomString;
			
		case SEXPR_ATOM_NUMBER:
			return atomNumber.toString();
		}
		
		return "";
	}
}
