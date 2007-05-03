package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgFunctionTypeSymbol extends SgSymbol {

	SgName p_name;
	SgType p_type;
	
	public SgFunctionTypeSymbol(SgName name, SgType type) {
		p_name = name;
		p_type = type;
	}
	
}
