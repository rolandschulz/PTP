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

public class SourceManipulationInfo extends CElementInfo {
	private static final long serialVersionUID = 1L;

	public SourceManipulationInfo(CElement parent) {
		super(parent);
	}

	public int getStartPos() {
		return ((SourceManipulation) fParent).getStartPos();
	}

	public int getIdStartPos() {
		return ((SourceManipulation) fParent).getIdStartPos();
	}

	public void setPos(int nodeOffset, int nodeLength) {
		((SourceManipulation) fParent).setPos(nodeOffset, nodeLength);
	}

	public void setLines(int startingLineNumber, int endingLineNumber) {
		((SourceManipulation) fParent).setLines(startingLineNumber, endingLineNumber);
	}

	public void setIdPos(int nodeOffset, int nodeLength) {
		((SourceManipulation) fParent).setIdPos(nodeOffset, nodeLength);
	}
}
