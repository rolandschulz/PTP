/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.internal.core.model;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.IProblemRequestor;
import org.eclipse.fdt.core.model.ITranslationUnit;

/**
 * CreateWorkingCopyOperation
 */
public class CreateWorkingCopyOperation extends CModelOperation {
	
	Map perFactoryWorkingCopies;
	IBufferFactory factory;
	IProblemRequestor problemRequestor;
	
	/*
	 * Creates a working copy from the given original tu and the given buffer factory.
	 * perFactoryWorkingCopies map is not null if the working copy is a shared working copy.
	 */
	public CreateWorkingCopyOperation(ITranslationUnit originalElement, Map perFactoryWorkingCopies, IBufferFactory factory, IProblemRequestor problemRequestor) {
		super(new ICElement[] {originalElement});
		this.perFactoryWorkingCopies = perFactoryWorkingCopies;
		this.factory = factory;
		this.problemRequestor = problemRequestor;
	}
	protected void executeOperation() throws CModelException {
		ITranslationUnit tu = getTranslationUnit();

		WorkingCopy workingCopy = new WorkingCopy(tu.getParent(), (IFile)tu.getResource(), this.factory, this.problemRequestor);
		// open the working copy now to ensure contents are that of the current state of this element
		workingCopy.open(this.fMonitor);
		
		if (this.perFactoryWorkingCopies != null) {
			this.perFactoryWorkingCopies.put(tu, workingCopy);
			//if (TranslationUnit.SHARED_WC_VERBOSE) {
			//	System.out.println("Creating shared working copy " + workingCopy.toStringWithAncestors()); //$NON-NLS-1$
			//}
		}

		// report added java delta
		CElementDelta delta = new CElementDelta(this.getCModel());
		delta.added(workingCopy);
		addDelta(delta);

		fResultElements = new ICElement[] {workingCopy};
	}

	/**
	 * Returns the translation unit this operation is working on.
	 */
	protected ITranslationUnit getTranslationUnit() {
		return (ITranslationUnit)getElementToProcess();
	}

	/**
	 * @see JavaModelOperation#isReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}

}
