/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
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
 * Version: 1.27
 */

package org.eclipse.ptp.internal.rdt.ui.contentassist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistInvocationContext;
import org.eclipse.cdt.internal.ui.text.contentassist.CProposalContextInformation;
import org.eclipse.cdt.internal.ui.text.contentassist.ParsingBasedProposalComputer;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ptp.internal.rdt.core.contentassist.CompletionType;
import org.eclipse.ptp.internal.rdt.core.contentassist.Proposal;
import org.eclipse.ptp.internal.rdt.core.contentassist.RemoteProposalContextInformation;
import org.eclipse.ptp.internal.rdt.core.contentassist.Visibility;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.swt.graphics.Image;

/**
 * Base class for contributing completion proposals to CDT's content assist
 * framework.
 */
public abstract class AbstractCompletionProposalAdapter extends ParsingBasedProposalComputer {

	protected abstract IContentAssistService getService(IProject project);
	
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		try {
			if (context instanceof CContentAssistInvocationContext) {
				CContentAssistInvocationContext cContext = (CContentAssistInvocationContext) context;
				return computeCompletionProposals(cContext, null, null);
			}
		} catch (Exception e) {
			UIPlugin.log(e);
		}

		return Collections.emptyList();
	}
	
	@Override
	protected List<ICompletionProposal> computeCompletionProposals(CContentAssistInvocationContext context, IASTCompletionNode node, String prefix) throws CoreException {
		IProject project = ((CContentAssistInvocationContext) context).getProject().getProject();
		IContentAssistService service = getService(project);
		if (service == null) {
			return Collections.emptyList();
		}
		ITranslationUnit unit = context.getTranslationUnit();
		//Scope scope = Scope.WORKSPACE_ROOT_SCOPE; // TODO: Use local scope
		Scope scope = new Scope(project);
		List<Proposal> rawProposals = service.computeCompletionProposals(scope, context, unit);
		List<ICompletionProposal> proposals = adaptProposals(context, rawProposals); // TODO: Provide IScope
		return proposals;
	}

	private List<ICompletionProposal> adaptProposals(CContentAssistInvocationContext context, List<Proposal> proposals) {
		List<ICompletionProposal> result = new LinkedList<ICompletionProposal>();
		
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
			completion.setCursorPosition(proposal.getCursorPosition());
			
			RemoteProposalContextInformation remoteContextInfo = proposal.getContextInformation();
			if (remoteContextInfo != null) {
				CProposalContextInformation contextInfo = new CProposalContextInformation(image, remoteContextInfo.getDisplayText(), remoteContextInfo.getDisplayArguments());
				contextInfo.setContextInformationPosition(remoteContextInfo.getContextInformationPosition());
				completion.setContextInformation(contextInfo);
			}
			
			result.add(completion);
		}
		return result;
	}
}
