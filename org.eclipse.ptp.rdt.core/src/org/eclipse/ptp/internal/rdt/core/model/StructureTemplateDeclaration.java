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
import org.eclipse.cdt.core.model.IStructureTemplateDeclaration;

public class StructureTemplateDeclaration extends StructureDeclaration implements IStructureTemplateDeclaration {
	private static final long serialVersionUID = 1L;

	private Template fTemplate;

	public StructureTemplateDeclaration(Parent parent, int kind, String className) {
		super(parent, className, kind);
		fTemplate = new Template();
	}

	public StructureTemplateDeclaration(Parent parent, IStructureTemplateDeclaration element) throws CModelException {
		super(parent, element);
		fTemplate = new Template();
		fTemplate.setTemplateParameterTypes(element.getTemplateParameterTypes());
	}

	public String[] getTemplateArguments() {
		return  fTemplate.getTemplateArguments();
	}
	
	public int getNumberOfTemplateParameters() {
		return fTemplate.getNumberOfTemplateParameters();
	}

	public String[] getTemplateParameterTypes() {
		return fTemplate.getTemplateParameterTypes();
	}

	public String getTemplateSignature() throws CModelException {
		return fTemplate.getTemplateSignature();
	}

	public void setTemplateParameterTypes(String[] parameterTypes) {
		fTemplate.setTemplateParameterTypes(parameterTypes);
	}
}
