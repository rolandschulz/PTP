/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.text.CReconciler;
import org.eclipse.cdt.internal.ui.text.CReconcilingStrategy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.ui.actions.FortranBlockCommentActionDelegate;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Base class for the fixed and free-form Fortran editors
 * 
 * @author Jeff Overbey
 */
public abstract class AbstractFortranEditor extends TextEditor implements ISelectionChangedListener
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected static String[] PARTITION_TYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE };
    
    protected static String FORTRAN_EDITOR_CONTEXT_ID = "org.eclipse.photran.ui.FortranEditorContext";
    
    protected static String CONTEXT_MENU_ID = "#FortranEditorContextMenu";
    
    protected static String BLOCK_COMMENT_COMMAND_ID = "org.eclipse.photran.ui.CommentCommand";
    
    protected static final RGB VERTICAL_LINE_COLOR = new RGB(176, 180, 185);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    protected IPreferenceStore fCombinedPreferenceStore;
    protected Composite fMainComposite;
    protected CContentOutlinePage fOutlinePage;
    protected FortranHorizontalRuler fHRuler;
    protected Color verticalLineColor;
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public AbstractFortranEditor()
    {
        super();
        setSourceViewerConfiguration(createSourceViewerConfiguration());
        setRangeIndicator(new DefaultRangeIndicator());
        // We must use the CUIPlugin's document provider in order for the
        // working copy manager in setOutlinePageInput (below) to function correctly.
        setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());
        
        // This has to be set to be notified of changes to preferences
        // Without this, the editor will not auto-update
        IPreferenceStore store = FortranUIPlugin.getDefault().getPreferenceStore();
        IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
        fCombinedPreferenceStore = new ChainedPreferenceStore(new IPreferenceStore[] { store, generalTextStore, getPreferenceStore()});
        setPreferenceStore(fCombinedPreferenceStore);
        // This enables any global changes to editor e.g. font type and size to take effect
        WorkbenchChainedTextFontFieldEditor.startPropagate(store, JFaceResources.TEXT_FONT);

        // JO: This gives you a "Toggle Breakpoint" action (and others)
        // when you right-click the Fortran editor's ruler
        setRulerContextMenuId("#CEditorRulerContext"); //$NON-NLS-1$
        
        setEditorContextMenuId(CONTEXT_MENU_ID);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // JFace Text Overrides
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void doSetInput(IEditorInput input) throws CoreException
    {
        super.doSetInput(input);
        IDocument document = this.getDocumentProvider().getDocument(input);
        if (document == null) return;
        
        configurePartitionScanner(document);
    }

    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);

        Composite childComp = (Composite)((Composite) parent.getChildren()[0]).getChildren()[0];
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 2;
        childComp.setLayout(layout);

        GridData data = new GridData(GridData.FILL_BOTH);
        childComp.getChildren()[0].setLayoutData(data);

        fMainComposite = childComp;

        createHorizontalRuler(fMainComposite);
        createLightGrayLines();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Ctrl+/ Block Commenting Support
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope", FORTRAN_EDITOR_CONTEXT_ID });
    }

//    /**
//     * Create actions that will be registered with the editor.
//     */
//    protected void createActions()
//    {
//        super.createActions();
//        createAction(new FortranBlockCommentActionDelegate(this), BLOCK_COMMENT_COMMAND_ID);
//        //createAction(new FortranOpenDeclarationActionDelegate(this), OPEN_DECLARATION_COMMAND_ID);
//    }

    protected void createAction(IAction action, String id)
    {
        action.setActionDefinitionId(id);
        setAction(id, action);
        markAsStateDependentAction(id, true);
        markAsSelectionDependentAction(id, true);      
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Custom Ruler
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param mainComposite
     * This creates the horizontal ruler and adds it to the top of the editor
     */
    protected void createHorizontalRuler(Composite mainComposite)
    {
        GC gc = new GC(getSourceViewer().getTextWidget());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = gc.getFontMetrics().getHeight();
        gc.dispose();

        fHRuler = getFortranHorizontalRuler(mainComposite);
        fHRuler.setFont(getSourceViewer().getTextWidget().getFont());
        fHRuler.setSourceViewer(getSourceViewer());
        fHRuler.setLayoutData(data);
        fHRuler.moveAbove(null);
    }

    protected abstract FortranHorizontalRuler getFortranHorizontalRuler(Composite mainComposite);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Gray Vertical Lines
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Display a light gray line between columns 6/7 and 72/73
     */
    protected void createLightGrayLines()
    {
        verticalLineColor = new Color(null, VERTICAL_LINE_COLOR);
        
        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer instanceof ITextViewerExtension2)
        {
            ITextViewerExtension2 painter = (ITextViewerExtension2)sourceViewer;
            
            int[] columns = getColumnsToDrawVerticalLinesOn();
            for (int i = 0; i < columns.length; i++)
            {
                MarginPainter p = new MarginPainter(getSourceViewer());
                p.setMarginRulerColumn(columns[i]);
                p.setMarginRulerColor(verticalLineColor);
                painter.addPainter(p);
            }
        }
    }

    protected abstract int[] getColumnsToDrawVerticalLinesOn();

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Preference Page Support
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns true if the event will require us to perform a damage and repair
     * e.g. a color preference change
     */
    protected boolean affectsTextPresentation(PropertyChangeEvent event)
    {
        return FortranPreferences.respondToPreferenceChange(event.getProperty())
               || super.affectsTextPresentation(event);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Syntax Highlighting
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void configurePartitionScanner(IDocument document)
    {
        IDocumentPartitioner partitioner = new FastPartitioner(new RuleBasedPartitionScanner(),
                                                               PARTITION_TYPES);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
    }

    protected abstract ITokenScanner getTokenScanner();
    
    protected SourceViewerConfiguration createSourceViewerConfiguration()
    {
        return new FortranSourceViewerConfiguration();
    }
    
    protected class FortranSourceViewerConfiguration extends SourceViewerConfiguration
    {
        protected PresentationReconciler reconciler = null;

        /**
         * Returns a list of the possible partitions' content types.
         * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
         */
        public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
        {
            return PARTITION_TYPES;
        }

        /**
         * Sets up rules for syntax highlighting.
         * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
         */
        public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
        {
            if (reconciler == null)
            {
                reconciler = new PresentationReconciler();
        
                // Set up a damager-repairer for each content type
                
                DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getTokenScanner());
                reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
                reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
            }
   
            return reconciler;
        }

        /*
         * The CReconciler is used to ensure that an ElementChangedEvent is fired.
         * Without this, the Outline view says "Pending..." but never populates.
         * 
         * From Anton Leherbaurer (cdt-dev, 8/16/07):
         *     The outline view waits for the initial reconciler to run and it requires
         *     an ElementChangedEvent when it is done to populate the view.
         *     See CContentOutlinerProvider$ElementChangedListener#elementChanged().
         *     The event should usually be issued from the
         *     ReconcileWorkingCopyOperation.
         */
        public IReconciler getReconciler(ISourceViewer sourceViewer)
        {
            MonoReconciler reconciler = new CReconciler(AbstractFortranEditor.this, new CReconcilingStrategy(AbstractFortranEditor.this));
            reconciler.setIsIncrementalReconciler(false);
            reconciler.setProgressMonitor(new NullProgressMonitor());
            reconciler.setDelay(500);
            return reconciler;
        }
    }

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return getOutlinePage();
		}
		if (required == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { CUIPlugin.CVIEW_ID, IPageLayout.ID_OUTLINE, IPageLayout.ID_RES_NAV };
				}

			};
		}
		if (required == IShowInSource.class) {
			ICElement ce= null;
			try {
				ce= SelectionConverter.getElementAtOffset(this);
			} catch (CModelException ex) {
				ce= null;
			}
			if (ce != null) { 
				final ISelection selection= new StructuredSelection(ce);
				return new IShowInSource() {
					public ShowInContext getShowInContext() {
						return new ShowInContext(getEditorInput(), selection);
					}
				};
			}
		}
		return super.getAdapter(required);
	}

	/**
	 * Gets the outline page of the c-editor.
     * @return Outline page.
	 */
	public CContentOutlinePage getOutlinePage() {
		if (fOutlinePage == null) {
			fOutlinePage = new CContentOutlinePage(null);
			fOutlinePage.addSelectionChangedListener(this);
		}
		setOutlinePageInput(fOutlinePage, getEditorInput());
		return fOutlinePage;
	}

	/**
     * Sets an input for the outline page.
	 * @param page Page to set the input.
	 * @param input Input to set.
	 */
	public static void setOutlinePageInput(CContentOutlinePage page, IEditorInput input) {
		if (page != null) {
			IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
			IWorkingCopy workingCopy = manager.getWorkingCopy(input);
			page.setInput(workingCopy);
		}
	}

//    /**
//     * Gets the outline page of the c-editor.
//     * 
//     * @return Outline page.
//     */
//    public CContentOutlinePage getOutlinePage() {
//        if (fOutlinePage == null) {
//            // CContentOutlinePage currently does nothing with its editor
//            // parameter,
//            // so we can pass in null rather than trying to convince it to use
//            // our
//            // editor (e.g., by subclassing CEditor).
//            fOutlinePage = new CContentOutlinePage(null);
//            fOutlinePage.addSelectionChangedListener(this);
//        }
//        setOutlinePageInput(fOutlinePage, getEditorInput());
//        return fOutlinePage;
//    }
//
//    /**
//     * Sets an input for the outline page.
//     * 
//     * @param page
//     *            Page to set the input.
//     * @param input
//     *            Input to set.
//     */
//    public static void setOutlinePageInput(CContentOutlinePage page,
//            IEditorInput input) {
//        if (page != null) {
//            IWorkingCopyManager manager = CUIPlugin.getDefault()
//                    .getWorkingCopyManager();
//            page.setInput(manager.getWorkingCopy(input));
//        }
//    }
    
    // ISelectionChangedListener Implementation ///////////////////////////////////////////////////
    // (for updating editor when Outline clicked)
    
    /**
     * React to changed selection in the outline view.
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        ISelection sel = event.getSelection();
        if (sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;
            Object obj = selection.getFirstElement();
            if (obj instanceof ISourceReference) {
                try {
                    ISourceRange range = ((ISourceReference) obj).getSourceRange();
                    if (range != null) {
                        setSelection(range, !isActivePart());
                    }
                } catch (CModelException e) {
                    // Selection change not applied.
                }
            }
        }
    }

    /**
     * Sets the current editor selection to the source range. Optionally
     * sets the current editor position.
     *
     * @param element the source range to be shown in the editor, can be null.
     * @param moveCursor if true the editor is scrolled to show the range.
     */
    public void setSelection(ISourceRange element, boolean moveCursor) {

        if (element == null) {
            return;
        }

        try {
            IRegion alternateRegion = null;
            int start = element.getStartPos();
            int length = element.getLength();

            // Sanity check sometimes the parser may throw wrong numbers.
            if (start < 0 || length < 0) {
                start = 0;
                length = 0;
            }

            // 0 length and start and non-zero start line says we know
            // the line for some reason, but not the offset.
            if (length == 0 && start == 0 && element.getStartLine() > 0) {
                // We have the information in term of lines, we can work it out.
                // Binary elements return the first executable statement so we have to substract -1
                start = getDocumentProvider().getDocument(getEditorInput()).getLineOffset(element.getStartLine() - 1);
                if (element.getEndLine() > 0) {
                    length = getDocumentProvider().getDocument(getEditorInput()).getLineOffset(element.getEndLine()) - start;
                } else {
                    length = start;
                }
                // create an alternate region for the keyword highlight.
                alternateRegion = getDocumentProvider().getDocument(getEditorInput()).getLineInformation(element.getStartLine() - 1);
                if (start == length || length < 0) {
                    if (alternateRegion != null) {
                        start = alternateRegion.getOffset();
                        length = alternateRegion.getLength();
                    }
                }
            }
            setHighlightRange(start, length, moveCursor);

            if (moveCursor) {
                start = element.getIdStartPos();
                length = element.getIdLength();
                if (start == 0 && length == 0 && alternateRegion != null) {
                    start = alternateRegion.getOffset();
                    length = alternateRegion.getLength();
                }
                if (start > -1 && getSourceViewer() != null) {
                    getSourceViewer().revealRange(start, length);
                    getSourceViewer().setSelectedRange(start, length);
                }
                updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
            }
            return;
        } catch (IllegalArgumentException x) {
            // No information to the user
        } catch (BadLocationException e) {
            // No information to the user
        }

        if (moveCursor)
            resetHighlightRange();
    }

    /**
     * Checks is the editor active part. 
     * @return <code>true</code> if editor is the active part of the workbench.
     */
    private boolean isActivePart() {
        IWorkbenchWindow window = getSite().getWorkbenchWindow();
        IPartService service = window.getPartService();
        return (this == service.getActivePart());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public abstract boolean isFixedForm();
    
    public IFile getIFile()
    {
        IEditorInput input = getEditorInput();
        return (input != null && input instanceof IFileEditorInput ? ((IFileEditorInput)input).getFile() : null);
    }

    public IDocument getIDocument()
    {
        IEditorInput input = getEditorInput();
        if (input == null) return null;
        
        IDocumentProvider dp = getDocumentProvider();
        if (dp == null) return null;
        
        return dp.getDocument(input);
    }
    
    public ITextSelection getSelection()
    {
        ISelectionProvider provider = getSelectionProvider();
        if (provider == null) return null;
        
        ISelection sel = provider.getSelection();
        if (!(sel instanceof ITextSelection)) return null;
        
        return (ITextSelection)sel;
    }
    
    public Shell getShell()
    {
        return getSite().getShell();
    }
    
    public void forceOutlineViewUpdate()
    {
        //  //     ///     ////   //  //    ///
        //  //   //  //   //      // //    /////
        //////   //////   //      ////      ///
        //  //   //  //   //      // //
        //  //   //  //    ////   //  //    //
      
        IDocument doc = getIDocument();
        if (doc == null) return;
        doc.set(" " + doc.get());
        doSave(null);
        doc.set(doc.get().substring(1));
        doSave(null);
    }
}
