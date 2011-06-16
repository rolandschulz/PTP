/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.io.Serializable;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ISourceRange;

public class SourceRange implements ISourceRange, Serializable {
	private static final long serialVersionUID = 1L;
	
	private int fEndLine;
	private int fIdLength;
	private int fIdStartPos;
	private int fLength;
	private int fStartLine;
	private int fStartPos;

	public SourceRange() {
	}
	
	public SourceRange(ISourceRange range) {
		fEndLine = range.getEndLine();
		fIdLength = range.getIdLength();
		fIdStartPos = range.getIdStartPos();
		fLength = range.getLength();
		fStartLine = range.getStartLine();
		fStartPos = range.getStartPos();
	}

	public SourceRange(IASTName name, IASTFileLocation location) {
		IASTFileLocation image = name.getImageLocation();
		fIdLength = image.getNodeOffset();
		fIdStartPos = image.getNodeLength();
		
		fEndLine = location.getEndingLineNumber();
		fLength = location.getNodeLength();
		fStartLine = location.getStartingLineNumber();
		fStartPos = location.getNodeOffset();
	}
	
	public SourceRange(IIndexName name, IASTFileLocation location) {
		fEndLine = location.getEndingLineNumber();
		fIdLength = name.getNodeLength();
		fIdStartPos = name.getNodeOffset();
		fLength = location.getNodeLength();
		fStartLine = location.getStartingLineNumber();
		fStartPos = location.getNodeOffset();
	}

	public int getEndLine() {
		return fEndLine;
	}

	public int getIdLength() {
		return fIdLength;
	}

	public int getIdStartPos() {
		return fIdStartPos;
	}

	public int getLength() {
		return fLength;
	}

	public int getStartLine() {
		return fStartLine;
	}

	public int getStartPos() {
		return fStartPos;
	}

	public void setIdPos(int offset, int length) {
		fIdStartPos = offset;
		fIdLength = length;
	}

	public void setPos(int offset, int length) {
		fStartPos = offset;
		fLength = length;
	}

	public void setLines(int startingLineNumber, int endingLineNumber) {
		fStartLine = startingLineNumber;
		fEndLine = endingLineNumber;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ISourceRange)) {
			return false;
		}
		ISourceRange range = (ISourceRange) o;
		return range.getIdStartPos() == fIdStartPos
			&& range.getIdLength() == fIdLength
			&& range.getStartPos() == fStartPos
			&& range.getLength() == fLength
			&& range.getStartLine() == fStartLine
			&& range.getEndLine() == fEndLine;
	}
	
	@Override
	public int hashCode() {
		return fEndLine + fIdLength + fIdStartPos + fLength + fStartLine + fStartPos;
	}
}
