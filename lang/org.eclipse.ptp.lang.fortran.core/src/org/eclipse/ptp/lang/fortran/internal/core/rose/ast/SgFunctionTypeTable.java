package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgFunctionTypeTable extends SgStatement {
	
 	// This pointer points to SgSymbolTable used to store function type symbols only. 
	SgSymbolTable p_function_type_table;
	
	public SgFunctionTypeTable() {
		p_function_type_table = new SgSymbolTable();
	}
	
 	/**
 	 * Returns pointer to SgSymbolTable used for function type symbols only.
 	 */
	SgSymbolTable get_function_type_table() {
		return p_function_type_table;
	}

}
