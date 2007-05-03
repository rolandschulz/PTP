package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgVarRefExp extends SgExpression {

	protected SgVariableSymbol p_symbol;

	public SgVarRefExp(SgVariableSymbol symbol) {
		p_symbol = symbol;
	}

}
