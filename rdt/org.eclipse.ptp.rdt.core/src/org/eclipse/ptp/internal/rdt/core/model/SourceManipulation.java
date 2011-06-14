/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceManipulation;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class SourceManipulation extends Parent implements ISourceManipulation, ISourceReference {
	private static final long serialVersionUID = 1L;

	protected SourceRange fRange;
	private boolean fIsActive= true;
	private short fIndex= 0;

	
	protected SourceManipulation(ICElement parent, int type, String name) {
		super(parent, type, name);
		fRange = new SourceRange();
	}

	protected SourceManipulation(ICElement parent, ICElement element, ISourceReference reference) throws CModelException {
		this(parent, element.getElementType(), element.getElementName());
		ISourceRange range = reference.getSourceRange();
		fRange.setIdPos(range.getIdStartPos(), range.getIdLength());
		fRange.setPos(range.getStartPos(), range.getLength());
		fRange.setLines(range.getStartLine(), range.getEndLine());
	}
	
	public void copy(ICElement container, ICElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws CModelException {
	}

	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
	}

	public void move(ICElement container, ICElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws CModelException {
	}

	public void rename(String name, boolean replace, IProgressMonitor monitor) throws CModelException {
	}

	public SourceManipulationInfo getSourceManipulationInfo() throws CModelException {
		if (fInfo == null) {
			fInfo = new SourceManipulationInfo(this);
		}
		return (SourceManipulationInfo) fInfo;
	}

	public void setIdPos(int offset, int length) {
		fRange.setIdPos(offset, length);
	}

	public int getStartPos() {
		return fRange.getStartPos();
	}

	public int getIdStartPos() {
		return fRange.getIdStartPos();
	}
	
	public int getLength() {
		return fRange.getLength();
	}
	
	public int getIdLength() {
		return fRange.getIdLength();
	}
	
	public int getStartLine() {
		return fRange.getStartLine();
	}

	public int getEndLine() {
		return fRange.getEndLine();
	}

	public void setPos(int offset, int length) {
		fRange.setPos(offset, length);
	}

	public void setLines(int startingLineNumber, int endingLineNumber) {
		fRange.setLines(startingLineNumber, endingLineNumber);
	}
	
	public String getSource() throws CModelException {
		return null;
	}
	
	public ISourceRange getSourceRange() throws CModelException {
		return fRange;
	}
	
	public ITranslationUnit getTranslationUnit() {
		return (ITranslationUnit) getAncestor(ICElement.C_UNIT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceReference#getIndex()
	 */
	public int getIndex() {
		return fIndex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceReference#isActive()
	 */
	public boolean isActive() {
		return fIsActive;
	}
	
	public void setActive(boolean active) {
		fIsActive= active;
	}

	public void setIndex(int i) {
		fIndex= (short) i;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.core.model.CElement#getElementInfo()
	 */
	@Override
	public CElementInfo getElementInfo() {
		if (fInfo == null) {
			fInfo = new SourceManipulationInfo(this);
		}
		return fInfo;
	}
}
