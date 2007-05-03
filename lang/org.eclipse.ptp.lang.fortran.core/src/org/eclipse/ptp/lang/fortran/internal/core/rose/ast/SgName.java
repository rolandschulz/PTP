package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgName extends SgSupport {
	
	String p_char = null;
	
	public SgName(String str) {
		p_char = str;
	}

	public String getString() {
		return p_char;
	}
	
	public String toString() {
		return p_char;
	}

}
