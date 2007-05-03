package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import java.util.ArrayList;

public class SgVariableDeclaration extends SgDeclarationStatement {

	// SgFileInfo, String, SgType
	
	ArrayList<SgInitializedName> p_variables;
	
	public SgVariableDeclaration() {
		p_variables = new ArrayList<SgInitializedName>();
	}
	
	public void append_variable(SgInitializedName what, SgInitializer init) {
		// TODO - do something with the init variable
		p_variables.add(what);
	}

}
