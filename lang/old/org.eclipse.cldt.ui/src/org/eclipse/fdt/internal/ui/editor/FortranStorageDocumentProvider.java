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

import org.eclipse.fdt.internal.ui.text.FortranTextTools;
import org.eclipse.fdt.ui.FortranUIPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * FortranStorageDocumentProvider
 */
public class FortranStorageDocumentProvider extends StorageDocumentProvider {
	
	/**
	 * 
	 */
	public FortranStorageDocumentProvider() {
		super();
	}

	/*
	 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#setupDocument(java.lang.Object, org.eclipse.jface.text.IDocument)
	 */
	protected void setupDocument(Object element, IDocument document) {
		if (document != null) {
			FortranTextTools tools= FortranUIPlugin.getDefault().getTextTools();
			tools.setupFortranDocument(document);
		}
	}

}
