package org.eclipse.cldt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.core.model.IMethod;

public class Method extends MethodDeclaration implements IMethod{
	
	public Method(ICElement parent, String name){
		super(parent, name, ICElement.C_METHOD);
	}
}
