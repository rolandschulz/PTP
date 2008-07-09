/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

/**
 * @author crecoskie
 *
 */
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.model.ISourceRange;

public class DummyFileLocation implements IASTFileLocation {

	ISourceRange fRange;
	
	public DummyFileLocation(ISourceRange range) {
		fRange = range;
	}
	
	public int getEndingLineNumber() {
		return fRange.getEndLine();
	}

	public String getFileName() {
		return null;
	}

	public int getStartingLineNumber() {
		return fRange.getStartLine();
	}

	public IASTFileLocation asFileLocation() {
		return this;
	}

	public int getNodeLength() {
		return fRange.getLength();
	}

	public int getNodeOffset() {
		return fRange.getStartPos();
	}

}
