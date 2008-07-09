/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Anton Leherbauer (Wind River Systems)
 * IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.text.contentassist.DOMCompletionProposalComputer
 * Version: 1.18
 */

package org.eclipse.ptp.internal.rdt.ui.contentassist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistInvocationContext;
import org.eclipse.cdt.internal.ui.text.contentassist.ParsingBasedProposalComputer;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ptp.internal.rdt.core.contentassist.CompletionType;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.contentassist.Visibility;
import org.eclipse.swt.graphics.Image;

/**
 * Base class for contributing completion proposals to CDT's content assist
 * framework.
 */
public abstract class AbstractCompletionProposalAdapter extends ParsingBasedProposalComputer {

	protected abstract IContentAssistService getService();
	
	@Override
	public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		try {
			if (context instanceof CContentAssistInvocationContext) {
				CContentAssistInvocationContext cContext = (CContentAssistInvocationContext) context;
				
				IContentAssistService service = getService();
				IASTCompletionNode completionNode = service.getCompletionNode(null, context); // TODO: Provide IScope
				if (completionNode == null) return Collections.EMPTY_LIST;
				String prefix = completionNode.getPrefix();
				if (prefix == null) {
					prefix = cContext.computeIdentifierPrefix().toString();
				}

				return computeCompletionProposals(cContext, completionNode, prefix);
			}
		} catch (Exception e) {
			CUIPlugin.log(e);
		}

		return Collections.EMPTY_LIST;
	}
	
	@Override
	protected List computeCompletionProposals(CContentAssistInvocationContext context, IASTCompletionNode completionNode, String prefix) throws CoreException {
		IContentAssistService service = getService();
		List<CCompletionProposal> proposals = adaptProposals(context, service.computeCompletionProposals(null, context, completionNode, prefix)); // TODO: Provide IScope
		return proposals;
	}

	private List<CCompletionProposal> adaptProposals(CContentAssistInvocationContext context, List<Proposal> proposals) {
		List<CCompletionProposal> result = new LinkedList<CCompletionProposal>();
		
		for (Proposal proposal : proposals) {
			String replacement = proposal.getReplacementText();
			int offset = proposal.getReplacementOffset();
			int length = proposal.getReplacementLength();
			String display = proposal.getDisplayText();
			String id = proposal.getIdentifier();
			int relevance = proposal.getRelevance();
			ITextViewer viewer = context.getViewer();
			
			Image image;
			CompletionType type = proposal.getType();
			ImageDescriptor descriptor;
			if (type.getVisibility() != Visibility.NotApplicable) {
				ASTAccessVisibility visibility;
				switch (type.getVisibility()) {
				case Private:
					visibility = ASTAccessVisibility.PRIVATE;
					break;
				case Protected:
					visibility = ASTAccessVisibility.PROTECTED;
					break;
				default:
					visibility = ASTAccessVisibility.PUBLIC;
				}
				
				switch (type.getElementType()) {
				case ICElement.C_FIELD:
					descriptor = CElementImageProvider.getFieldImageDescriptor(visibility);
					break;
				case ICElement.C_METHOD:
					descriptor = CElementImageProvider.getMethodImageDescriptor(visibility);
					break;
				default:
					descriptor = CElementImageProvider.getImageDescriptor(type.getElementType());
				}
			} else {
				descriptor = CElementImageProvider.getImageDescriptor(type.getElementType());
			}
			image = CUIPlugin.getImageDescriptorRegistry().get(descriptor);
			CCompletionProposal completion = new CCompletionProposal(replacement, offset, length, image, display, id, relevance, viewer);
			result.add(completion);
		}
		return result;
	}
}
