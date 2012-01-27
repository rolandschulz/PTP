/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.io.Serializable;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.model.ISourceRange;

public class DummyFileLocation implements IASTFileLocation, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final int endLine;
	private final int startLine;
	private final int length;
	private final int offset;
	private final String fileName;
	
	
	public DummyFileLocation(ISourceRange range) {
		this.length = range.getLength();
		this.offset = range.getStartPos();
		this.endLine = range.getEndLine();
		this.startLine = range.getStartLine();
		this.fileName = null;
	}
	
	public DummyFileLocation(IASTFileLocation location) {
		this.length = location.getNodeLength();
		this.offset = location.getNodeOffset();
		this.endLine = location.getEndingLineNumber();
		this.startLine = location.getStartingLineNumber();
		this.fileName = location.getFileName();
	}
	
	public DummyFileLocation(String fileName, int length, int offset, int startLine, int endLine) {
		this.length = length;
		this.offset = offset;
		this.endLine = endLine;
		this.startLine = startLine;
		this.fileName = fileName;
	}

	public int getEndingLineNumber() {
		return endLine;
	}

	public String getFileName() {
		return fileName;
	}

	public int getStartingLineNumber() {
		return startLine;
	}

	public IASTFileLocation asFileLocation() {
		return this;
	}

	public int getNodeLength() {
		return length;
	}

	public int getNodeOffset() {
		return offset;
	}

	public IASTPreprocessorIncludeStatement getContextInclusionStatement() {
		// TODO Auto-generated method stub
		return null;
	}

}
