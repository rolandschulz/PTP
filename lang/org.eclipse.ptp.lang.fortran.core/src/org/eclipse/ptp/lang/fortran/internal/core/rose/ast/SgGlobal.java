package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import java.util.ArrayList;

public class SgGlobal extends SgScopeStatement {

    // This is an STL list of SgDeclarationStatement
	protected ArrayList<SgDeclarationStatement> p_declarations;
	
	public SgGlobal(Sg_File_Info startOfConstruct) {
		p_declarations = new ArrayList<SgDeclarationStatement>();
	}
	
	public SgGlobal() {
		this(null);
	}

	public void append_declaration(SgDeclarationStatement element) {
		p_declarations.add(element);
	}

}
