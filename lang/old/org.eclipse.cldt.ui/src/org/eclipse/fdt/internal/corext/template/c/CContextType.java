/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.fdt.internal.corext.template.c;

import org.eclipse.fdt.core.model.ITranslationUnit;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;

/**
 * CContextType
 */
public class CContextType extends TranslationUnitContextType {

	public final static String CCONTEXT_TYPE = "org.eclipse.fdt.ui.text.templates.c"; //$NON-NLS-1$

	/**
	 * @param name
	 */
	public CContextType() {
		super();
		// global
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
		
		// translation unit
		addResolver(new File());
		addResolver(new ReturnType());
		addResolver(new Method());
		addResolver(new Project());
		addResolver(new Arguments());

	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.corext.template.c.TranslationUnitContextType#createContext(org.eclipse.jface.text.IDocument, int, int, org.eclipse.fdt.core.model.ITranslationUnit)
	 */
	public TranslationUnitContext createContext(IDocument document, int offset,
			int length, ITranslationUnit translationUnit) {
		return new CContext(this, document, offset, length, translationUnit);
	}

}
