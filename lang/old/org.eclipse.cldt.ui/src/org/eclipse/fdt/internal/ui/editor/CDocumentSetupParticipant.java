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

package org.eclipse.fdt.internal.ui.editor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.fdt.internal.ui.text.CTextTools;
import org.eclipse.fdt.ui.CUIPlugin;
import org.eclipse.jface.text.IDocument;

/**
 * CDocumentSetupParticipant
 */
public class CDocumentSetupParticipant implements IDocumentSetupParticipant {
	/**
	 * 
	 */
	public CDocumentSetupParticipant() {
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
	 */
	public void setup(IDocument document) {
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		tools.setupCDocument(document);
	}

}
