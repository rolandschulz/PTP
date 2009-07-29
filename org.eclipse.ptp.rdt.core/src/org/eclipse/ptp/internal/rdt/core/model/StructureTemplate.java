/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructureTemplate;

public class StructureTemplate extends Structure implements IStructureTemplate {
	private static final long serialVersionUID = 1L;

	private Template fTemplate;

	public StructureTemplate(Parent parent, int kind, String className) {
		super(parent, kind, className);
		fTemplate = new Template();
	}

	public StructureTemplate(Parent parent, IStructureTemplate element) throws CModelException {
		super(parent, element);
		fTemplate = new Template();
		fTemplate.setTemplateParameterTypes(element.getTemplateParameterTypes());
	}

	public StructureTemplate(Parent parent, ICompositeType binding, ICPPTemplateDefinition template) throws DOMException {
		super(parent, adaptASTType(binding), binding);
		fTemplate = new Template();
		ICPPTemplateParameter[] params = template.getTemplateParameters();
		String[] parameterTypes = new String[params.length];
		for(int i = 0; i < params.length; i++) {
			parameterTypes[i] = params[i].getName();
		}
		fTemplate.setTemplateParameterTypes(parameterTypes);
	}

	static int adaptASTType(ICompositeType type) throws DOMException {
		switch (type.getKey()) {
		case ICompositeType.k_struct:
			return ICElement.C_TEMPLATE_STRUCT;
		case ICompositeType.k_union:
			return ICElement.C_TEMPLATE_UNION;
		default:
			return ICElement.C_TEMPLATE_CLASS;
		}
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
