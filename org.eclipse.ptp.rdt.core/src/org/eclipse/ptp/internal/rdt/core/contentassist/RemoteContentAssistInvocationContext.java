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

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;

/**
 * The context in which content assist is invoked.
 * 
 * Intended for serialization.
 */
public class RemoteContentAssistInvocationContext implements Serializable {
	private static final long serialVersionUID = 1L;
	
	transient IASTCompletionNode fCompletionNode;
	boolean fInPreprocessorDirective;
	boolean fIsContextInformationStyle;
	int fContextInformationOffset;
	int fParseOffset;
	int fInvocationOffset;
	private boolean fInPreprocessorKeyword;
	private CharSequence fIdentifierPrefix;
	
	private boolean showCamelCaseMatches;

	public boolean isInPreprocessorDirective() {
		return fInPreprocessorDirective;
	}
	
	public void setInPreprocessorDirective(boolean inPreprocessorDirective) {
		fInPreprocessorDirective = inPreprocessorDirective;
	}

	public IASTCompletionNode getCompletionNode() {
		return fCompletionNode;
	}

	public boolean isContextInformationStyle() {
		return fIsContextInformationStyle;
	}

	public int getContextInformationOffset() {
		return fContextInformationOffset;
	}

	public int getParseOffset() {
		return fParseOffset;
	}

	public int getInvocationOffset() {
		return fInvocationOffset;
	}

	public void setCompletionNode(IASTCompletionNode completionNode) {
		fCompletionNode = completionNode;
	}

	public void setIsContextInformationStyle(boolean contextInformationStyle) {
		fIsContextInformationStyle = contextInformationStyle;
	}

	public void setContextInformationOffset(int contextInformationOffset) {
		fContextInformationOffset = contextInformationOffset;
	}

	public void setParseOffset(int parseOffset) {
		fParseOffset = parseOffset;
	}

	public void setInvocationOffset(int invocationOffset) {
		fInvocationOffset = invocationOffset;
	}

	public boolean inPreprocessorKeyword() {
		return fInPreprocessorKeyword;
	}
	
	public void setInPreprocessorKeyword(boolean inPreprocessorKeyword) {
		fInPreprocessorKeyword = inPreprocessorKeyword;
	}

	public CharSequence computeIdentifierPrefix() {
		if (fIdentifierPrefix == null) {
			return null;
		}
		return fIdentifierPrefix;
	}
	
	public void setIdentifierPrefix(CharSequence identifierPrefix) {
		fIdentifierPrefix = identifierPrefix;
	}

	public void setShowCamelCaseMatches(boolean showCamelCaseMatches) {
		this.showCamelCaseMatches = showCamelCaseMatches;
	}

	public boolean getShowCamelCaseMatches() {
		return showCamelCaseMatches;
	}
	
	
}
