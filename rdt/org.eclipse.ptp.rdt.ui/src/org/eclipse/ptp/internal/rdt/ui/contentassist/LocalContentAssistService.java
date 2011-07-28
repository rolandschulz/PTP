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

package org.eclipse.ptp.internal.rdt.ui.contentassist;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.ptp.internal.rdt.core.contentassist.CompletionProposalComputer;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.contentassist.RemoteContentAssistInvocationContext;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

/**
 * Provides local index-based completions for content assist.
 */
public class LocalContentAssistService implements IContentAssistService {
	private static CompletionProposalComputer fComputer = new CompletionProposalComputer();

	public List<Proposal> computeCompletionProposals(Scope scope, ContentAssistInvocationContext context, ITranslationUnit unit) {
		if (!(context instanceof CContentAssistInvocationContext)) {
			return Collections.emptyList();
		}
		
		RemoteContentAssistInvocationContext remoteContext = ContentAssistUtil.adaptContext((CContentAssistInvocationContext) context);
		IASTCompletionNode completionNode = ((CContentAssistInvocationContext) context).getCompletionNode();
		if(completionNode == null)
			return Collections.emptyList();
		String prefix = completionNode.getPrefix();
		if (prefix == null) {
			prefix = remoteContext.computeIdentifierPrefix().toString();
		}
		return fComputer.computeCompletionProposals(remoteContext, completionNode, prefix);
	}
}
