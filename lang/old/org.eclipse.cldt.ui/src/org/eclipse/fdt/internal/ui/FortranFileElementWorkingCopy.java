package org.eclipse.fdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.IBuffer;
import org.eclipse.fdt.core.model.ITranslationUnit;
import org.eclipse.fdt.internal.core.model.WorkingCopy;

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
	 * @see org.eclipse.fdt.core.model.IOpenable#getBuffer()
	 */
	public IBuffer getBuffer() throws CModelException {
		return unit.getBuffer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.model.IWorkingCopy#getOriginalElement()
	 */
	public ITranslationUnit getOriginalElement() {
		return unit;
	}

}
