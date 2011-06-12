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

import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

/**
 * Provides completion proposals during a content assist invocation.
 */
public interface IContentAssistService {
	/**
	 * Returns a list of completion <code>Proposal</code>s that start with the
	 * given prefix.  The proposals are inferred from the invocation context,
	 * which describes the current position of the caret within the editor.
	 * The completion node, which is derived from the invocation context,
	 * provides semantic information about the grammatical position of the
	 * cursor.
	 *   
	 * @param context
	 * @param completionNode
	 * @param prefix
	 * @return
	 */
	List<Proposal> computeCompletionProposals(Scope scope, ContentAssistInvocationContext context, ITranslationUnit unit);
}
