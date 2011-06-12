/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodTemplateDeclaration;

public class MethodTemplateDeclaration extends MethodDeclaration implements IMethodTemplateDeclaration {
	private static final long serialVersionUID = 1L;

	protected Template fTemplate;

	public MethodTemplateDeclaration(Parent parent, String functionName) {
		super(parent, ICElement.C_TEMPLATE_METHOD_DECLARATION, functionName);
		fTemplate = new Template();
	}

	public MethodTemplateDeclaration(Parent parent, IMethodTemplateDeclaration element) throws CModelException {
		super(parent, element);
		fTemplate = new Template();
		fTemplate.setTemplateParameterTypes(element.getTemplateParameterTypes());
	}

	public MethodTemplateDeclaration(Parent parent, ICPPMethod binding, ICPPTemplateDefinition template) throws DOMException {
		super(parent, binding);
		fTemplate = new Template();
		fTemplate.setTemplateParameterTypes(extractTemplateParameterTypes(template));
	}

	public int getNumberOfTemplateParameters() {
		return fTemplate.getNumberOfTemplateParameters();
	}

	public String[] getTemplateParameterTypes() {
		return fTemplate.getTemplateParameterTypes();
	}
	
	public String[] getTemplateArguments() {
		return  fTemplate.getTemplateArguments();
	}

	public String getTemplateSignature() throws CModelException {
		return fTemplate.getTemplateSignature();
	}

	public void setTemplateParameterTypes(String[] parameterTypes) {
		fTemplate.setTemplateParameterTypes(parameterTypes);
	}
}
