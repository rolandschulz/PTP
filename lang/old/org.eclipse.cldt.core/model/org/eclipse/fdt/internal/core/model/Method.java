package org.eclipse.fdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.IMethod;

public class Method extends MethodDeclaration implements IMethod{
	
	public Method(ICElement parent, String name){
		super(parent, name, ICElement.C_METHOD);
	}
}
