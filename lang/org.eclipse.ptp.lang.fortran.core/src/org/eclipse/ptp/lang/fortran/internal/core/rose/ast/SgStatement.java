package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class SgStatement extends SgLocatedNode implements IASTStatement {
	
	private int numericLabel = -1;
	
	public SgStatement() {
		super();
	}

	public void set_numeric_label(int label) {
		numericLabel = label;
	}
}
