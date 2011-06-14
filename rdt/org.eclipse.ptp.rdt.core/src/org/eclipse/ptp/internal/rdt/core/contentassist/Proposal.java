/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 * A completion proposal that may be used during a content assist invocation.
 * 
 * Intended for serialization.
 */
public class Proposal implements Serializable {
	private static final long serialVersionUID = 1L;
	
	String fReplacementText;
	int fReplacementOffset;
	int fReplacementLength;
	CompletionType fType;
	String fDisplayText;
	String fIdentifier;
	int fCursorPosition;
	RemoteProposalContextInformation fContextInformation;
	int fRelevance;

	public Proposal(String replacementText, int replacementOffset, int replacementLength, CompletionType type, String displayText, String identifier, int relevance) {
		fReplacementText = replacementText;
		fReplacementOffset = replacementOffset;
		fReplacementLength = replacementLength;
		fType = type;
		fDisplayText = displayText;
		fIdentifier = identifier;
		fCursorPosition = replacementText.length();
		fRelevance = relevance;
	}
	
	public String getDisplayText() {
		return fDisplayText;
	}
	
	public String getIdentifier() {
		return fIdentifier;
	}
	
	public int getReplacementLength() {
		return fReplacementLength;
	}
	
	public int getReplacementOffset() {
		return fReplacementOffset;
	}
	
	public String getReplacementText() {
		return fReplacementText;
	}
	
	public CompletionType getType() {
		return fType;
	}

	public void setCursorPosition(int position) {
		fCursorPosition = position;
	}

	public void setContextInformation(RemoteProposalContextInformation info) {
		fContextInformation = info;
	}

	public RemoteProposalContextInformation getContextInformation() {
		return fContextInformation;
	}
	
	public int getRelevance() {
		return fRelevance;
	}
	
	@Override
	public String toString() {
		return fDisplayText;
	}

	public int getCursorPosition() {
		return fCursorPosition;
	}
}
