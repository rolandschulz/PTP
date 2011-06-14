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
 * Provides context information to the user during a content assist
 * invocation.
 * 
 * Intended for serialization.
 */
public class RemoteProposalContextInformation implements Serializable {
	private static final long serialVersionUID = 1L;
	
	int fOffset;
	private CompletionType fType;
	private String fDisplayText;
	private String fDisplayArguments;

	public RemoteProposalContextInformation(CompletionType type, String displayText, String displayArguments) {
		fType = type;
		fDisplayText = displayText;
		fDisplayArguments = displayArguments;
	}

	public void setContextInformationPosition(int offset) {
		fOffset = offset;
	}

	public String getDisplayArguments() {
		return fDisplayArguments;
	}
	
	public String getDisplayText() {
		return fDisplayText;
	}
	
	public int getContextInformationPosition() {
		return fOffset;
	}
	
	public CompletionType getType() {
		return fType;
	}
}
