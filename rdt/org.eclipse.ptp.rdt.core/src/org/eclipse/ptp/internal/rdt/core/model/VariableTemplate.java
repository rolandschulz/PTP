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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.IVariable;

public class VariableTemplate extends Variable implements ITemplate {
	private static final long serialVersionUID = 1L;

	private Template fTemplate;

	public VariableTemplate(Parent parent, String variableName) {
		super(parent, ICElement.C_TEMPLATE_VARIABLE, variableName);
		fTemplate = new Template();
	}

	public VariableTemplate(Parent parent, IVariable element) throws CModelException {
		super(parent, element);
		fTemplate = new Template();
		fTemplate.setTemplateParameterTypes(((ITemplate) element).getTemplateParameterTypes()); 
	}

	public int getNumberOfTemplateParameters() {
		return fTemplate.getNumberOfTemplateParameters();
	}

	public String[] getTemplateParameterTypes() {
		return fTemplate.getTemplateParameterTypes();
	}
	
	public String[] getTemplateArguments() {
		return new String[0];
	}

	public String getTemplateSignature() throws CModelException {
		return fTemplate.getTemplateSignature();
	}

	public void setTemplateParameterTypes(String[] parameterTypes) {
		fTemplate.setTemplateParameterTypes(parameterTypes);
	}
}
