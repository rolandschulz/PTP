/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.text.contentassist.DOMCompletionProposalComputer
 * Version: 1.31
 */

package org.eclipse.ptp.internal.rdt.ui.contentassist;

import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ptp.internal.rdt.core.contentassist.RemoteContentAssistInvocationContext;

/**
 * Base class for all IContentAssistService implementations.
 */
public abstract class AbstractContentAssistService implements IContentAssistService {

	protected RemoteContentAssistInvocationContext adaptContext(CContentAssistInvocationContext context) {
		RemoteContentAssistInvocationContext result = new RemoteContentAssistInvocationContext();		
		result.setCompletionNode(context.getCompletionNode());
		result.setInPreprocessorDirective(inPreprocessorDirective(context));
		result.setInPreprocessorKeyword(inPreprocessorKeyword(context));
		result.setIsContextInformationStyle(context.isContextInformationStyle());
		result.setContextInformationOffset(context.getContextInformationOffset());
		result.setParseOffset(context.getParseOffset());
		result.setInvocationOffset(context.getInvocationOffset());
		try {
			result.setIdentifierPrefix(context.computeIdentifierPrefix());
		} catch (BadLocationException e) {
			// If we get here, leave the identifier prefix as null.
			// Let RemoteContentAssistInvocationContext deal with it.
		}
		return result;
	}
	
	/**
	 * Check if given offset is inside a preprocessor directive.
	 * 
	 * @param doc  the document
	 * @param offset  the offset to check
	 * @return <code>true</code> if offset is inside a preprocessor directive
	 */
	protected boolean inPreprocessorDirective(CContentAssistInvocationContext context) {
		IDocument doc = context.getViewer().getDocument();
		int offset = context.getParseOffset();
		
		if (offset > 0 && offset == doc.getLength()) {
		--offset;
		}
		try {
			return ICPartitions.C_PREPROCESSOR
					.equals(TextUtilities.getContentType(doc, ICPartitions.C_PARTITIONING, offset, false));
		} catch (BadLocationException exc) {
		}
		return false;
	}
	
	/**
	 * Test whether the invocation offset is inside or before the preprocessor directive keyword.
	 * 
	 * @param context  the invocation context
	 * @return <code>true</code> if the invocation offset is inside or before the directive keyword 
	 */
	private boolean inPreprocessorKeyword(CContentAssistInvocationContext context) {
		IDocument doc = context.getDocument();
		int offset = context.getInvocationOffset();
		
		try {
			final ITypedRegion partition= TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, offset, true);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				String ppPrefix= doc.get(partition.getOffset(), offset - partition.getOffset());
				if (ppPrefix.matches("\\s*#\\s*\\w*")) { //$NON-NLS-1$
					// we are inside the directive keyword
					return true;
				}
			}
			
		} catch (BadLocationException exc) {
		}
		return false;
	}
}
