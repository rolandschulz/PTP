package org.eclipse.fdt.internal.ui.text.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ArrayList;
import java.util.List;

import org.eclipse.fdt.core.model.ITranslationUnit;
import org.eclipse.fdt.internal.corext.template.c.TranslationUnitContext;
import org.eclipse.fdt.internal.corext.template.c.TranslationUnitContextType;
import org.eclipse.fdt.internal.ui.FortranPluginImages;
import org.eclipse.fdt.internal.ui.text.c.hover.SourceViewerInformationControl;
import org.eclipse.fdt.ui.FortranUIPlugin;
import org.eclipse.fdt.ui.text.ICCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class TemplateEngine {

	private TemplateContextType fContextType;	
	private ArrayList fProposals= new ArrayList();

	public class CTemplateProposal extends TemplateProposal implements ICCompletionProposal {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getInformationControlCreator()
		 */
		public IInformationControlCreator getInformationControlCreator() {
			return new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					int shellStyle= SWT.RESIZE;
					int style= SWT.V_SCROLL | SWT.H_SCROLL;				
					return new SourceViewerInformationControl(parent, shellStyle, style);
				}
			};
		}
		/**
		 * @param template
		 * @param context
		 * @param region
		 * @param image
		 */
		public CTemplateProposal(Template template, TemplateContext context, IRegion region, Image image) {
			super(template, context, region, image, 90);
		}

//		/* (non-Javadoc)
//		 * @see org.eclipse.fdt.ui.text.ICCompletionProposal#getRelevance()
//		 */
//		public int getRelevance() {
//			return 90;
//		}
	}
	/**
	 * Creates the template engine for a particular context type.
	 * See <code>TemplateContext</code> for supported context types.
	 */
	public TemplateEngine(TemplateContextType contextType) {
		Assert.isNotNull(contextType);
		fContextType= contextType;
	}

	/**
	 * Empties the collector.
	 * 
	 * @param viewer the text viewer  
	 * @param unit   the compilation unit (may be <code>null</code>)
	 */
	public void reset() {
		fProposals.clear();
	}

	/**
	 * Returns the array of matching templates.
	 */
	public List getResults() {
		//return (TemplateProposal[]) fProposals.toArray(new TemplateProposal[fProposals.size()]);
		return fProposals;
	}

	/**
	 * Inspects the context of the compilation unit around <code>completionPosition</code>
	 * and feeds the collector with proposals.
	 * @param viewer the text viewer
	 * @param completionPosition the context position in the document of the text viewer
	 * @param compilationUnit the compilation unit (may be <code>null</code>)
	 */
	public void complete(ITextViewer viewer, int completionPosition, ITranslationUnit translationUnit)
	{
	    IDocument document= viewer.getDocument();
	    
		if (!(fContextType instanceof TranslationUnitContextType))
			return;

		Point selection= viewer.getSelectedRange();

		// remember selected text
		String selectedText= null;
		if (selection.y != 0) {
			try {
				selectedText= document.get(selection.x, selection.y);
			} catch (BadLocationException e) {}
		}

		((TranslationUnitContextType) fContextType).setContextParameters(document.get(), completionPosition, translationUnit);		
		TranslationUnitContext context= ((TranslationUnitContextType) fContextType).createContext(document, completionPosition, selection.y, translationUnit);
		int start= context.getStart();
		int end= context.getEnd();
		IRegion region= new Region(start, end - start);

		Template[] templates= FortranUIPlugin.getDefault().getTemplateStore().getTemplates();
		for (int i= 0; i != templates.length; i++)
			if (context.canEvaluate(templates[i]))
				fProposals.add(new CTemplateProposal(templates[i], context, region, FortranPluginImages.get(FortranPluginImages.IMG_OBJS_TEMPLATE)));
	}
	
}

