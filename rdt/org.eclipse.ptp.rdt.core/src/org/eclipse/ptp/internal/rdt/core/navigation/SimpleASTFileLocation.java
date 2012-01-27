/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.navigation;

import java.io.Serializable;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;

/**
 * @author vkong
 * @since 2.0
 *
 */
public class SimpleASTFileLocation implements IASTFileLocation, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String fileName;
	private int offset;
	private int length;
	private int startLineNumber;
	private int endLineNumber;
	
	public SimpleASTFileLocation(IASTFileLocation location) {
		this(location.getFileName(), location.getNodeOffset(), location.getNodeLength(),
				location.getStartingLineNumber(), location.getEndingLineNumber());
	}

	public SimpleASTFileLocation(String fileName, int nodeOffset,
			int nodeLength, int start, int end) {
		this.fileName = fileName;
		offset = nodeOffset;
		length = nodeLength;
		startLineNumber = start;
		endLineNumber = end;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#asFileLocation()
	 */
	public IASTFileLocation asFileLocation() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#getFileName()
	 */
	public String getFileName() {
		return fileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#getNodeOffset()
	 */
	public int getNodeOffset() {
		return offset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#getNodeLength()
	 */
	public int getNodeLength() {
		return length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#getStartingLineNumber()
	 */
	public int getStartingLineNumber() {
		return startLineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#getEndingLineNumber()
	 */
	public int getEndingLineNumber() {
		return endLineNumber;
	}

	@Override
	public IASTPreprocessorIncludeStatement getContextInclusionStatement() {
		// TODO Auto-generated method stub
		return null;
	}

}
