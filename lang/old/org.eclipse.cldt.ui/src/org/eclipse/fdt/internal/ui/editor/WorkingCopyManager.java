/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.internal.ui.editor;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.fdt.core.model.IWorkingCopy;
import org.eclipse.fdt.ui.IWorkingCopyManager;
import org.eclipse.fdt.ui.IWorkingCopyManagerExtension;
import org.eclipse.jface.text.Assert;
import org.eclipse.ui.IEditorInput;


/**
 * This working copy manager works together with a given compilation unit document provider and
 * additionally offers to "overwrite" the working copy provided by this document provider.
 */
public class WorkingCopyManager implements IWorkingCopyManager, IWorkingCopyManagerExtension {
	
	private CDocumentProvider fDocumentProvider;
	private Map fMap;
	private boolean fIsShuttingDown;

	/**
	 * Creates a new working copy manager that co-operates with the given
	 * compilation unit document provider.
	 * 
	 * @param provider the provider
	 */
	public WorkingCopyManager(CDocumentProvider provider) {
		Assert.isNotNull(provider);
		fDocumentProvider= provider;
	}

	/*
	 * @see org.eclipse.fdt.ui.IWorkingCopyManager#connect(org.eclipse.ui.IEditorInput)
	 */
	public void connect(IEditorInput input) throws CoreException {
		fDocumentProvider.connect(input);
	}
	
	/*
	 * @see org.eclipse.fdt.ui.IWorkingCopyManager#disconnect(org.eclipse.ui.IEditorInput)
	 */
	public void disconnect(IEditorInput input) {
		fDocumentProvider.disconnect(input);
	}
	
	/*
	 * @see org.eclipse.fdt.ui.IWorkingCopyManager#shutdown()
	 */
	public void shutdown() {
		if (!fIsShuttingDown) {
			fIsShuttingDown= true;
			try {
				if (fMap != null) {
					fMap.clear();
					fMap= null;
				}
				fDocumentProvider.shutdown();
			} finally {
				fIsShuttingDown= false;
			}
		}
	}

	/*
	 * @see org.eclipse.fdt.ui.IWorkingCopyManager#getWorkingCopy(org.eclipse.ui.IEditorInput)
	 */
	public IWorkingCopy getWorkingCopy(IEditorInput input) {
		IWorkingCopy unit= fMap == null ? null : (IWorkingCopy) fMap.get(input);
		return unit != null ? unit : fDocumentProvider.getWorkingCopy(input);
	}
	
	/*
	 * @see org.eclipse.fdt.internal.ui.editor.IWorkingCopyManagerExtension#setWorkingCopy(org.eclipse.ui.IEditorInput, org.eclipse.fdt.core.model.ITranslationUnit)
	 */
	public void setWorkingCopy(IEditorInput input, IWorkingCopy workingCopy) {
		if (fDocumentProvider.getDocument(input) != null) {
			if (fMap == null)
				fMap= new HashMap();
			fMap.put(input, workingCopy);
		}
	}
	
	/*
	 * @see org.eclipse.fdt.internal.ui.editor.IWorkingCopyManagerExtension#removeWorkingCopy(org.eclipse.ui.IEditorInput)
	 */
	public void removeWorkingCopy(IEditorInput input) {
		fMap.remove(input);
		if (fMap.isEmpty())
			fMap= null;
	}
}
