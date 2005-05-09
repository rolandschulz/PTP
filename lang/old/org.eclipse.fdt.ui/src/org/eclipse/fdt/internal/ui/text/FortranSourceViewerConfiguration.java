package org.eclipse.fdt.internal.ui.text;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Vector;

import org.eclipse.fdt.internal.ui.editor.FortranEditor;
import org.eclipse.cdt.internal.ui.text.CDoubleClickSelector;
import org.eclipse.cdt.internal.ui.text.IColorManager;
import org.eclipse.fdt.internal.ui.text.IFortranPartitions;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverDescriptor;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverProxy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;



/**
 * Configuration for an <code>SourceViewer</code> which shows Fortran code.
 */
public class FortranSourceViewerConfiguration extends SourceViewerConfiguration {
	
	/** Key used to look up display tab width */
	public final static String PREFERENCE_TAB_WIDTH= "org.eclipse.fdt.editor.tab.width"; //$NON-NLS-1$

    private FortranTextTools fTextTools;
	private FortranEditor fEditor;
	
	/**
	 * Creates a new Fortran source viewer configuration for viewers in the given editor using
	 * the given Fortran tools collection.
	 *
	 * @param tools the Fortran text tools collection to be used
	 * @param editor the editor in which the configured viewer will reside
	 */
	public FortranSourceViewerConfiguration(FortranTextTools tools, FortranEditor editor) {
		fTextTools= tools;
		fEditor= editor;
	}

	
	/**
	 * Returns the Fortran singleline comment scanner for this configuration.
	 *
	 * @return the Fortran singleline comment scanner
	 */
	protected RuleBasedScanner getSinglelineCommentScanner() {
		return fTextTools.getSinglelineCommentScanner();
	}
	
	/**
	 * Returns the Fortran string scanner for this configuration.
	 *
	 * @return the Fortran string scanner
	 */
	protected RuleBasedScanner getStringScanner() {
		return fTextTools.getStringScanner();
	}	
	
	/**
	 * Returns the color manager for this configuration.
	 *
	 * @return the color manager
	 */
	protected IColorManager getColorManager() {
		return fTextTools.getColorManager();
	}
	
	/**
	 * Returns the editor in which the configured viewer(s) will reside.
	 *
	 * @return the enclosing editor
	 */
	protected ITextEditor getEditor() {
		return fEditor;
	}

    /**
     * Creates outline presenter. 
     * @param editor Editor.
     * @return Presenter with outline view.
     */
    public IInformationPresenter getOutlinePresenter(FortranEditor editor)
    {
		return null;
		/*
        final IInformationControlCreator outlineControlCreator = getOutlineContolCreator(editor);
        final InformationPresenter presenter = new InformationPresenter(outlineControlCreator);
        final IInformationProvider provider = new CElementContentProvider(getEditor());
        presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
        presenter.setInformationProvider(provider, ICPartitions.C_MULTILINE_COMMENT);
        presenter.setInformationProvider(provider, ICPartitions.C_SINGLE_LINE_COMMENT);
        presenter.setInformationProvider(provider, ICPartitions.C_STRING);
        presenter.setSizeConstraints(20, 20, true, false);
        presenter.setRestoreInformationControlBounds(getSettings("outline_presenter_bounds"), true, true); //$NON-NLS-1$        
        return presenter;
        */
    }

    /**
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		PresentationReconciler reconciler= new PresentationReconciler();

		RuleBasedScanner scanner = fTextTools.getFortranCodeScanner();

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);

		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		//TextAttribute attr = new TextAttribute(manager.getColor(IFortranColorConstants.FORTRAN_DEFAULT));
		
		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());		
		reconciler.setDamager(dr, IFortranPartitions.FORTRAN_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, IFortranPartitions.FORTRAN_SINGLE_LINE_COMMENT);
		
		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, IFortranPartitions.FORTRAN_STRING);
		reconciler.setRepairer(dr, IFortranPartitions.FORTRAN_STRING);

		return reconciler;
	}


	/**
	 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		return null;
		
		/*
		if(getEditor() == null) {
			return null;
		}

		ContentAssistant assistant = new ContentAssistant();
		
		IContentAssistProcessor processor
			= getPreferenceStore().getBoolean(ContentAssistPreference.USE_DOM)
			? (IContentAssistProcessor)new CCompletionProcessor2(getEditor())
			: (IContentAssistProcessor)new CCompletionProcessor(getEditor());
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);

		//Will this work as a replacement for the configuration lines below?
		ContentAssistPreference.configure(assistant, getPreferenceStore());
		
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);		
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;*/
	}
	
	
	/**
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fEditor != null && fEditor.isEditable()) {
			Reconciler reconciler= new Reconciler() {
				protected void initialProcess() {
					// prevent case where getDocument() returns null
					// and causes exception in initialProcess()
					IDocument doc = getDocument();
					if (doc != null)
						super.initialProcess();
				}
			};
			reconciler.setDelay(1000);
			reconciler.setIsIncrementalReconciler(false);
			reconciler.setReconcilingStrategy(new FortranReconcilingStrategy(fEditor), IDocument.DEFAULT_CONTENT_TYPE);
			return reconciler;
		}
		return null;
	}


	/**
	 * @see SourceViewerConfiguration#getAutoIndentStrategy(ISourceViewer, String)
	 */
	public IAutoIndentStrategy getAutoIndentStrategy(ISourceViewer sourceViewer, String contentType) {
		return new FortranAutoIndentStrategy();
	}


	/**
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new CDoubleClickSelector();
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDefaultPrefixes(ISourceViewer, String)
	 */
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "!", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see SourceViewerConfiguration#getDefaultPrefix(ISourceViewer, String)
	 */
	public String getDefaultPrefix(ISourceViewer sourceViewer, String contentType) {
		if(IDocument.DEFAULT_CONTENT_TYPE.equals(contentType))
			return "!"; //$NON-NLS-1$
		if(IFortranPartitions.FORTRAN_SINGLE_LINE_COMMENT.equals(contentType)) {
			return "!"; //$NON-NLS-1$
		}
		return null;
	}


	/*
	 * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {

		Vector vector= new Vector();

		// prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces
		int tabWidth= getPreferenceStore().getInt(PREFERENCE_TAB_WIDTH);
		boolean useSpaces= getPreferenceStore().getBoolean(FortranEditor.SPACES_FOR_TABS); //$NON-NLS-1$

		for (int i= 0; i <= tabWidth; i++) {
		    StringBuffer prefix= new StringBuffer();

			if (useSpaces) {
			    for (int j= 0; j + i < tabWidth; j++)
			    	prefix.append(' ');
		    	
				if (i != 0)
		    		prefix.append('\t');				
			} else {    
			    for (int j= 0; j < i; j++)
			    	prefix.append(' ');
		    	
				if (i != tabWidth)
		    		prefix.append('\t');
			}
			
			vector.add(prefix.toString());
		}

		vector.add(""); //$NON-NLS-1$
		
		return (String[]) vector.toArray(new String[vector.size()]);
	}


	/**
	 * @see SourceViewerConfiguration#getTabWidth(ISourceViewer)
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		return getPreferenceStore().getInt(PREFERENCE_TAB_WIDTH);
	}


	/**
	 * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return null; //new CAnnotationHover();
	}


	
	/*
	 * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer, String)
	 * @since 2.1
	 */
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
		CEditorTextHoverDescriptor[] hoverDescs= CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
		int stateMasks[]= new int[hoverDescs.length];
		int stateMasksLength= 0;		
		for (int i= 0; i < hoverDescs.length; i++) {
			if (hoverDescs[i].isEnabled()) {
				int j= 0;
				int stateMask= hoverDescs[i].getStateMask();
				while (j < stateMasksLength) {
					if (stateMasks[j] == stateMask)
						break;
					j++;
				}
				if (j == stateMasksLength)
					stateMasks[stateMasksLength++]= stateMask;
			}
		}
		if (stateMasksLength == hoverDescs.length)
			return stateMasks;
		
		int[] shortenedStateMasks= new int[stateMasksLength];
		System.arraycopy(stateMasks, 0, shortenedStateMasks, 0, stateMasksLength);
		return shortenedStateMasks;
	}
	
	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String, int)
	 * @since 2.1
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		CEditorTextHoverDescriptor[] hoverDescs= CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
		int i= 0;
		while (i < hoverDescs.length) {
			if (hoverDescs[i].isEnabled() &&  hoverDescs[i].getStateMask() == stateMask)
				return new CEditorTextHoverProxy(hoverDescs[i], getEditor());
			i++;
		}

		return null;
	}

	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return getTextHover(sourceViewer, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
	}

	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { 	IDocument.DEFAULT_CONTENT_TYPE, 
								IFortranPartitions.FORTRAN_SINGLE_LINE_COMMENT,
								IFortranPartitions.FORTRAN_STRING };
	}
	
	/**
	 * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
	 */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		
		/*final MultiPassContentFormatter formatter = 
			new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer), 
				IDocument.DEFAULT_CONTENT_TYPE);
		
		formatter.setMasterStrategy(new CFormattingStrategy());
		return formatter;*/
		return null;
		
	}
	
	protected IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}
	
	/*
	 * @see SourceViewerConfiguration#getHoverControlCreator(ISourceViewer)
	 * @since 2.0
	 */
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return getInformationControlCreator(sourceViewer, true);
	}
	

	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer, final boolean cutDown) {
			/*return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int style = cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
				return new DefaultInformationControl(parent, style, new HTMLTextPresenter(cutDown));
				// return new HoverBrowserControl(parent);
			}
		};*/
		return null;
	}

	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		return super.getInformationPresenter(sourceViewer);
	}
    
    
    /**
     * Creates control for outline presentation in editor.
     * @param editor Editor.
     * @return Control.
     */
    private IInformationControlCreator getOutlineContolCreator(final FortranEditor editor)
    {
        //final IInformationControlCreator conrolCreator = new IInformationControlCreator()
        //{
            /**
             * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
             */
        //    public IInformationControl createInformationControl(Shell parent)
        //    {
        //        int shellStyle= SWT.RESIZE;
        //        int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
        //        return new COutlineInformationControl(editor, parent, shellStyle, treeStyle);   
        //    }
        //};
        //return conrolCreator;*/
		return null;
    }

    /**
     * Returns the settings for the given section.
     *
     * @param sectionName the section name
     * @return the settings
     */
    private IDialogSettings getSettings(String sectionName) {
        IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings().getSection(sectionName);
        if (settings == null) {
            settings= CUIPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
        }
        
        return settings;
    }
    
}
