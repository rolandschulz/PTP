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
import java.util.List;

public class CElementInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected CElement fParent;
	
	public CElementInfo(CElement parent) {
		fParent = parent;
	}
	
	public void setIsStructureKnown(boolean b) {
	}

	public List<SourceManipulation> internalGetChildren() {
		return fParent.internalGetChildren();
	}
}
