package org.eclipse.photran.internal.ui.editor;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CTextEditorActionConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.ui.actions.FortranBlockCommentActionDelegate;
import org.eclipse.photran.internal.ui.actions.FortranOpenDeclarationActionDelegate;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public abstract class AbstractFortranEditor extends TextEditor implements ISelectionChangedListener
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    private static String[] PARTITION_TYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE };
    
    private static String FORTRAN_EDITOR_CONTEXT_ID = "org.eclipse.photran.ui.FortranEditorContext";
    
    private static String CONTEXT_MENU_ID = "#FortranEditorContextMenu";
    
    private static String BLOCK_COMMENT_COMMAND_ID = "org.eclipse.photran.ui.CommentCommand";
    private static String OPEN_DECLARATION_COMMAND_ID = "org.eclipse.photran.ui.OpenDeclarationCommand";
    
    private static final RGB VERTICAL_LINE_COLOR = new RGB(176, 180, 185);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    private IPreferenceStore fCombinedPreferenceStore;
    private Composite fMainComposite;
    private CContentOutlinePage fOutlinePage;
    private FortranHorizontalRuler fHRuler;
    private Color verticalLineColor;
    
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

    /**
     * Create actions that will be registered with the editor.
     */
    protected void createActions()
    {
        super.createActions();
        createAction(new FortranBlockCommentActionDelegate(this), BLOCK_COMMENT_COMMAND_ID);
        createAction(new FortranOpenDeclarationActionDelegate(this), OPEN_DECLARATION_COMMAND_ID);
    }

    private void createAction(IAction action, String id)
    {
        action.setActionDefinitionId(id);
        setAction(id, action);
        markAsStateDependentAction(id, true);
        markAsSelectionDependentAction(id, true);      
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Syntax Highlighting
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void configurePartitionScanner(IDocument document)
    {
        IDocumentPartitioner partitioner = new FastPartitioner(new RuleBasedPartitionScanner(),
                                                               PARTITION_TYPES);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
    }
    
    private SourceViewerConfiguration createSourceViewerConfiguration()
    {
        return new SourceViewerConfiguration()
        {
            private PresentationReconciler reconciler = null;
            
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
        };
    }

    protected abstract ITokenScanner getTokenScanner();

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Custom Ruler
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param mainComposite
     * This creates the horizontal ruler and adds it to the top of the editor
     */
    private void createHorizontalRuler(Composite mainComposite) {

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
    private void createLightGrayLines()
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
    // Outline Support (mostly copied from CDT)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public Object getAdapter(Class required) {
        if (IContentOutlinePage.class.equals(required)) {
            return getOutlinePage();
        }
        if (required == IShowInTargetList.class) {
            return new IShowInTargetList() {
                public String[] getShowInTargetIds() {
                    return new String[] { CUIPlugin.CVIEW_ID,
                            IPageLayout.ID_OUTLINE, IPageLayout.ID_RES_NAV };
                }

            };
        }
        return super.getAdapter(required);
    }

    /**
     * Gets the outline page of the c-editor.
     * 
     * @return Outline page.
     */
    public CContentOutlinePage getOutlinePage() {
        if (fOutlinePage == null) {
            // CContentOutlinePage currently does nothing with its editor
            // parameter,
            // so we can pass in null rather than trying to convince it to use
            // our
            // editor (e.g., by subclassing CEditor).
            fOutlinePage = new CContentOutlinePage(null);
            fOutlinePage.addSelectionChangedListener(this);
        }
        setOutlinePageInput(fOutlinePage, getEditorInput());
        return fOutlinePage;
    }

    /**
     * Sets an input for the outline page.
     * 
     * @param page
     *            Page to set the input.
     * @param input
     *            Input to set.
     */
    public static void setOutlinePageInput(CContentOutlinePage page,
            IEditorInput input) {
        if (page != null) {
            IWorkingCopyManager manager = CUIPlugin.getDefault()
                    .getWorkingCopyManager();
            page.setInput(manager.getWorkingCopy(input));
        }
    }
    
    // ISelectionChangedListener Implementation ///////////////////////////////////////////////////
    // (for updating editor when Outline clicked)
    
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
     * Checks is the editor active part.
     * 
     * @return <code>true</code> if editor is the active part of the
     *         workbench.
     */
    private boolean isActivePart() {
        IWorkbenchWindow window = getSite().getWorkbenchWindow();
        IPartService service = window.getPartService();
        return (this == service.getActivePart());
    }

    /**
     * Sets the current editor selection to the source range. Optionally sets
     * the current editor position.
     * 
     * @param element
     *            the source range to be shown in the editor, can be null.
     * @param moveCursor
     *            if true the editor is scrolled to show the range.
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
                // Binary elements return the first executable statement so we
                // have to substract -1
                start = getDocumentProvider().getDocument(getEditorInput())
                        .getLineOffset(element.getStartLine() - 1);
                if (element.getEndLine() > 0) {
                    length = getDocumentProvider()
                            .getDocument(getEditorInput()).getLineOffset(
                                    element.getEndLine())
                            - start;
                } else {
                    length = start;
                }
                // create an alternate region for the keyword highlight.
                alternateRegion = getDocumentProvider().getDocument(
                        getEditorInput()).getLineInformation(
                        element.getStartLine() - 1);
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
                updateStatusField(CTextEditorActionConstants.STATUS_CURSOR_POS);
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public abstract boolean isFixedForm();
}
