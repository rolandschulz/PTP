package org.eclipse.cldt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.IBuffer;
import org.eclipse.cldt.core.model.ITranslationUnit;
import org.eclipse.cldt.internal.core.model.WorkingCopy;
import org.eclipse.core.runtime.CoreException;

public class FortranFileElementWorkingCopy extends WorkingCopy {

	ITranslationUnit unit;


	/**
	 * Creates a working copy of this element
	 */
	public FortranFileElementWorkingCopy(ITranslationUnit unit) throws CoreException {
		super(unit.getParent(), unit.getPath(), null);
		this.unit = unit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IOpenable#getBuffer()
	 */
	public IBuffer getBuffer() throws CModelException {
		return unit.getBuffer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.IWorkingCopy#getOriginalElement()
	 */
	public ITranslationUnit getOriginalElement() {
		return unit;
	}

}
