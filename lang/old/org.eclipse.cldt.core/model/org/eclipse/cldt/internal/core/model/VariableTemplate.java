package org.eclipse.cldt.internal.core.model;
/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.core.model.ITemplate;

public class VariableTemplate extends Variable implements ITemplate {

	protected static final String[] fgEmptyList= new String[] {};
	protected String[] templateParameterTypes;
		
	public VariableTemplate(ICElement parent, String name) {
		super(parent, name, ICElement.C_TEMPLATE_VARIABLE);
		templateParameterTypes= fgEmptyList;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITemplate#getNumberOfTemplateParameters()
	 */
	public int getNumberOfTemplateParameters() {
		return templateParameterTypes == null ? 0 : templateParameterTypes.length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITemplate#getTemplateParameterTypes()
	 */
	public String[] getTemplateParameterTypes() {
		return templateParameterTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITemplate#setTemplateParameterTypes(java.lang.String[])
	 */
	public void setTemplateParameterTypes(String[] templateParameterTypes) {
		this.templateParameterTypes = templateParameterTypes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITemplate#getTemplateSignature()
	 */
	public String getTemplateSignature() throws CModelException {
		StringBuffer sig = new StringBuffer(getElementName());
		if(getNumberOfTemplateParameters() > 0){
			sig.append("<"); //$NON-NLS-1$
			String[] paramTypes = getTemplateParameterTypes();
			int i = 0;
			sig.append(paramTypes[i++]);
			while (i < paramTypes.length){
				sig.append(", "); //$NON-NLS-1$
				sig.append(paramTypes[i++]);
			}
			sig.append(">"); //$NON-NLS-1$
		}
		else{
			sig.append("<>"); //$NON-NLS-1$
		}
		
		sig.append(" : "); //$NON-NLS-1$
		sig.append(this.getTypeName());
		
		return sig.toString();
	}
}
