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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.fdt.core.model.IBuffer;
import org.eclipse.fdt.core.model.IOpenable;
import org.eclipse.fdt.core.model.ITranslationUnit;
import org.eclipse.fdt.core.model.IWorkingCopy;
import org.eclipse.fdt.internal.core.model.IBufferFactory;

/**
 * CustomBufferFactory
 */
public class CustomBufferFactory implements IBufferFactory {
	/**
	 * 
	 */
	public CustomBufferFactory() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.model.IBufferFactory#createBuffer(org.eclipse.fdt.core.model.IOpenable)
	 */
	public IBuffer createBuffer(IOpenable owner) {
		if (owner instanceof IWorkingCopy) {

			IWorkingCopy unit= (IWorkingCopy) owner;
			ITranslationUnit original= unit.getOriginalElement();
			IResource resource= original.getResource();
			if (resource instanceof IFile) {
				IFile fFile = (IFile)resource;
				DocumentAdapter adapter= new DocumentAdapter(owner, fFile);
				return adapter;
			}
						
		}
		return DocumentAdapter.NULL;
	}
}
