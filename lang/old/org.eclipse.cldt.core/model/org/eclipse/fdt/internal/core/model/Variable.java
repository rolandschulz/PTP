package org.eclipse.fdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.IVariable;

public class Variable extends VariableDeclaration implements IVariable {
	
	public Variable(ICElement parent, String name) {
		super(parent, name, ICElement.C_VARIABLE);
	}

	public Variable(ICElement parent, String name, int kind) {
		super(parent, name, kind);
	}

	public String getInitializer() {
		return ""; //$NON-NLS-1$
	}

}
