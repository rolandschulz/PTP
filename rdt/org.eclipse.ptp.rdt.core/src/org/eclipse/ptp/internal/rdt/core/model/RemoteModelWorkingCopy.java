/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CElementInfo;
import org.eclipse.cdt.internal.core.model.OpenableInfo;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.WorkingCopy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * A working copy translation unit.  Its structure is built from a remote model.
 * @author vkong
 *
 */
public class RemoteModelWorkingCopy extends WorkingCopy {

	WorkingCopy fOriginal;
	

	public RemoteModelWorkingCopy (WorkingCopy original, boolean hasFile) {	
		super(original.getParent(), original.getLocationURI(), original.getContentTypeId(), original.getBufferFactory());
		this.fOriginal = original;
	}
	
	public RemoteModelWorkingCopy (WorkingCopy original) {	
		super(original.getParent(), original.getFile(), original.getContentTypeId(), original.getBufferFactory());
		this.fOriginal = original;
	}
	
	/**
	 * @return the original working copy instance
	 */
	public WorkingCopy getOriginalWorkingCopy() {
		return fOriginal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.TranslationUnit#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm,
			Map<ICElement, CElementInfo> newElements,
			IResource underlyingResource) throws CModelException {
		
		//remove previous info before we start creating new ones
		removeChildrenInfo(fOriginal);
		
		// generate structure - using remote model	
		IModelBuilderService service = new RemoteModelBuilderServiceFactory().getModelBuilderService(getCProject().getProject());
		ITranslationUnit tu = null;
		try {
			tu = service.getModel(fOriginal, new NullProgressMonitor());
			ModelBuilder builder = new ModelBuilder(fOriginal, new NullProgressMonitor());
			builder.buildLocalModel((TranslationUnit)tu);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	//remove any cached info from itself and its children
	private void removeChildrenInfo(Parent parent) throws CModelException {
		ICElement[] children = parent.getChildren();
		for (ICElement child : children) {
			if (child instanceof Parent) {
				removeChildrenInfo((Parent)child);
			}
		}
		parent.removeChildren();
	}
}
