package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgVariableSymbol extends SgSymbol {
	
	protected SgInitializedName p_declaration;
	
	public SgVariableSymbol(SgInitializedName declaration) {
		p_declaration = declaration;
	}

}
