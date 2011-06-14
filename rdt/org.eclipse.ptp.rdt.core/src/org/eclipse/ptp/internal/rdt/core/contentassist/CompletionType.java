/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.contentassist;

import java.io.Serializable;

/**
 * Provides information about the ICElement type of a completion proposal.
 * 
 * Intended for serialization.
 */
public class CompletionType implements Serializable {
	private static final long serialVersionUID = 1L;
	
	int fElementType;
	Visibility fVisibility;
	
	public CompletionType(int elementType) {
		this(elementType, Visibility.NotApplicable);
	}
	
	public CompletionType(int elementType, Visibility visibility) {
		fElementType = elementType;
		fVisibility = visibility;
	}
	
	public int getElementType() {
		return fElementType;
	}
	
	public Visibility getVisibility() {
		return fVisibility;
	}
}
