/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cldt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cldt.core.dom.CDOM;
import org.eclipse.cldt.core.dom.ICodeReaderFactory;
import org.eclipse.cldt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cldt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cldt.core.dom.ast.DOMException;
import org.eclipse.cldt.core.dom.ast.IASTName;
import org.eclipse.cldt.core.dom.ast.IBinding;
import org.eclipse.cldt.core.dom.ast.ICompositeType;
import org.eclipse.cldt.core.dom.ast.IFunction;
import org.eclipse.cldt.core.dom.ast.ITypedef;
import org.eclipse.cldt.core.dom.ast.IVariable;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cldt.core.model.IWorkingCopy;
import org.eclipse.cldt.core.parser.CodeReader;
import org.eclipse.cldt.core.parser.ParserUtil;
import org.eclipse.cldt.internal.ui.viewsupport.FortranElementImageProvider;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;

/**
 * @author Doug Schaefer
 */
public class CCompletionProcessor2 implements IContentAssistProcessor {

	private IEditorPart editor;
	private String errorMessage;
	
	public CCompletionProcessor2(IEditorPart editor) {
		this.editor = editor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer,
			int offset) {
		try {
			long startTime = System.currentTimeMillis();
			IWorkingCopy workingCopy = FortranUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
			ASTCompletionNode completionNode = CDOM.getInstance().getCompletionNode(
				(IFile)workingCopy.getResource(),
				offset,
				new ICodeReaderFactory() {
					public CodeReader createCodeReaderForTranslationUnit(String path) {
						return new CodeReader(viewer.getDocument().get().toCharArray());
					}
					public CodeReader createCodeReaderForInclusion(String path) {
						return ParserUtil.createReader(path,
							Arrays.asList(FortranUIPlugin.getSharedWorkingCopies()).iterator());
					}
					public int getUniqueIdentifier() {
						return 99;
					}
				}	
			);
			long stopTime = System.currentTimeMillis();

			List proposals = null;
			
			if (completionNode != null) {
				int repLength = completionNode.getLength();
				int repOffset = offset - repLength;
				proposals = new ArrayList();
				
				IASTName[] names = completionNode.getNames();
				for (int i = 0; i < names.length; ++i) {
					IBinding [] bindings = names[i].resolvePrefix();
					if (bindings != null)
						for (int j = 0; j < bindings.length; ++j)
							proposals.add(createBindingCompletionProposal(bindings[j], repOffset, repLength));
				}
			}
			
			long propTime = System.currentTimeMillis();
			System.out.println("Completion Parse: " + (stopTime - startTime) + " + Resolve:"
					+ (propTime - stopTime));
			System.out.flush();

			if (proposals != null && !proposals.isEmpty()) {
				errorMessage = null;
				return (ICompletionProposal[])proposals.toArray(new ICompletionProposal[proposals.size()]);
			}

			// The rest are error conditions
			errorMessage = "No completions found";
		} catch (UnsupportedDialectException e) {
			errorMessage = "Unsupported Dialect Exception";
		} catch (Throwable e) {
			errorMessage = e.toString();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	private ICompletionProposal createBindingCompletionProposal(IBinding binding, int offset, int length) {
		ImageDescriptor imageDescriptor = null;
		
		try {
			if (binding instanceof ITypedef) {
				imageDescriptor = FortranElementImageProvider.getTypedefImageDescriptor();
			} else if (binding instanceof ICompositeType) {
				if (((ICompositeType)binding).getKey() == ICPPClassType.k_class)
					imageDescriptor = FortranElementImageProvider.getClassImageDescriptor();
				else if (((ICompositeType)binding).getKey() == ICompositeType.k_struct)
					imageDescriptor = FortranElementImageProvider.getStructImageDescriptor();
				else if (((ICompositeType)binding).getKey() == ICompositeType.k_union)
					imageDescriptor = FortranElementImageProvider.getUnionImageDescriptor();
			} else if (binding instanceof IFunction) {
				imageDescriptor = FortranElementImageProvider.getFunctionImageDescriptor();
			} else if (binding instanceof IVariable) {
				imageDescriptor = FortranElementImageProvider.getVariableImageDescriptor();
			}
		} catch (DOMException e) {
		}
		
		Image image = imageDescriptor != null
			? FortranUIPlugin.getImageDescriptorRegistry().get( imageDescriptor )
			: null;

		return new CCompletionProposal(binding.getName(), offset, length, image, binding.getName(), 1);
	}
}