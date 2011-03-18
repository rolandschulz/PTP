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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.internal.ui.text.TabsToSpacesConverter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension7;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.photran.internal.cdtinterface.ui.editor.CDTBasedSourceViewerConfiguration;
import org.eclipse.photran.internal.cdtinterface.ui.editor.CDTBasedTextEditor;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.sourceform.SourceForm;
import org.eclipse.photran.internal.ui.FortranUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;

/**
 * Fortran editor
 *
 * @author Jeff Overbey
 * @author Kurt Hendle - folding support
 */
@SuppressWarnings({ "deprecation", "restriction" })
public class FortranEditor extends CDTBasedTextEditor implements ISelectionChangedListener
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final String EDITOR_ID = "org.eclipse.photran.ui.FortranEditor"; //$NON-NLS-1$

    protected static String CONTEXT_MENU_ID = "#FortranEditorContextMenu"; //$NON-NLS-1$

    public static final String SOURCE_VIEWER_CONFIG_EXTENSION_POINT_ID =
        "org.eclipse.photran.ui.sourceViewerConfig"; //$NON-NLS-1$

    public static String[] PARTITION_TYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE };

    protected static String FORTRAN_EDITOR_CONTEXT_ID = "org.eclipse.photran.ui.FortranEditorContext"; //$NON-NLS-1$

    protected static String BLOCK_COMMENT_COMMAND_ID = "org.eclipse.photran.ui.CommentCommand"; //$NON-NLS-1$

    protected static final RGB VERTICAL_LINE_COLOR = new RGB(176, 180, 185);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Public Fields - Custom Reconciler Task Support
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /** @see org.eclipse.photran.internal.ui.editor_vpg.FortranVPGReconcilingStrategy */
    public Object reconcilerTasks = null;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected IPreferenceStore fCombinedPreferenceStore;
    protected Composite fMainComposite;
    protected Color verticalLineColor;
    protected boolean contentTypeMismatch;

    // More fields in Folding, below

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public FortranEditor()
    {
        super();
        setSourceViewerConfiguration(createSourceViewerConfiguration());
        setRangeIndicator(new DefaultRangeIndicator());
        useCDTDocumentProvider();

        // This has to be set to be notified of changes to preferences
        // Without this, the editor will not auto-update
        IPreferenceStore store = FortranUIPlugin.getDefault().getPreferenceStore();
        IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
        fCombinedPreferenceStore = new ChainedPreferenceStore(new IPreferenceStore[] { store, generalTextStore, getPreferenceStore()});
        setPreferenceStore(fCombinedPreferenceStore);
        // This enables any global changes to editor e.g. font type and size to take effect
        WorkbenchChainedTextFontFieldEditor.startPropagate(store, JFaceResources.TEXT_FONT);

        useCDTRulerContextMenuID();
        setEditorContextMenuId(CONTEXT_MENU_ID);

        contentTypeMismatch = false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // JFace Text Overrides
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override protected void doSetInput(IEditorInput input) throws CoreException
    {
        super.doSetInput(input);

        IDocument document = this.getDocumentProvider().getDocument(input);
        if (document == null) return;

        configurePartitionScanner(document);

        if (input instanceof FileEditorInput)
            checkForContentTypeMismatch((FileEditorInput)input);
    }

    @Override public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);

        if (FortranPreferences.ENABLE_FOLDING.getValue())
            installProjectionSupport();

        createLightGrayLines();

        addWatermark(parent);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Tabs to Spaces Conversion
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // See also FortranSourceViewer
    
    @Override
    protected boolean isTabsToSpacesConversionEnabled() {
        return FortranPreferences.CONVERT_TABS_TO_SPACES.getValue();
    }

    @Override
    protected void installTabsToSpacesConverter() {
        ISourceViewer sourceViewer= getSourceViewer();
        SourceViewerConfiguration config= getSourceViewerConfiguration();
        if (config != null && sourceViewer instanceof ITextViewerExtension7) {
            int tabWidth= config.getTabWidth(sourceViewer);
            TabsToSpacesConverter tabToSpacesConverter= new TabsToSpacesConverter();
            tabToSpacesConverter.setNumberOfSpacesPerTab(tabWidth);
            IDocumentProvider provider= getDocumentProvider();
            if (provider instanceof CDocumentProvider) {
                CDocumentProvider cProvider= (CDocumentProvider) provider;
                tabToSpacesConverter.setLineTracker(cProvider.createLineTracker(getEditorInput()));
            } else {
                tabToSpacesConverter.setLineTracker(new DefaultLineTracker());
            }
            ((ITextViewerExtension7) sourceViewer).setTabsToSpacesConverter(tabToSpacesConverter);
            //updateIndentationMode();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Context Menu Contribution
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
    @Override public void editorContextMenuAboutToShow(IMenuManager menu)
    {
        super.editorContextMenuAboutToShow(menu);

        try
        {
            // Instantiate RefactorMenu using reflection since it's in an optional dependency
            IContributionItem refactorMenu = (IContributionItem)Class.forName("org.eclipse.rephraserengine.ui.menus.RefactorMenu").newInstance(); //$NON-NLS-1$
            
            MenuManager refactorSubmenu = new MenuManager("Refactor"); //$NON-NLS-1$
            refactorSubmenu.add(refactorMenu);
            menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, refactorSubmenu); // cf. CEditor#createActions()
            //menu.add(refactorSubmenu);
        }
        catch (Throwable x)
        {
            // The RefactorMenu class is contributed through an optional dependency;
            // if it's not present, it's not a problem
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Editor Folding
    ///////////////////////////////////////////////////////////////////////////////////////////////

    //protected ProjectionSupport projectionSupport;
    //protected Annotation[] oldAnnotations;
    protected ProjectionAnnotationModel annotationModel;

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles)
    {
        //fAnnotationAccess = createAnnotationAccess();
        //fOverviewRuler = createOverviewRuler(getSharedColors());

        ISourceViewer sourceViewer = new FortranSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

        getSourceViewerDecorationSupport(sourceViewer); // Ensure decoration support has been created and configured

        return sourceViewer;
    }

    private void installProjectionSupport()
    {
        ProjectionViewer viewer =(ProjectionViewer)getSourceViewer();

        ProjectionSupport projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
        projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
        projectionSupport.install();

        viewer.doOperation(ProjectionViewer.TOGGLE); // Turn projection mode on

        annotationModel = viewer.getProjectionAnnotationModel();
    }

    public void updateFoldingStructure(ArrayList<Position> positions)
    {
        try
        {
            if (annotationModel != null)
                annotationModel.modifyAnnotations(null, mapAnnotationsToPositions(positions), null);
        }
        catch (Throwable t)
        {
            // Ignore
        }
    }

    private HashMap<ProjectionAnnotation, Position> mapAnnotationsToPositions(ArrayList<Position> positions)
    {
        HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();
        for (int i = 0; i < positions.size(); i++)
        {
            ProjectionAnnotation annotation = new ProjectionAnnotation();
            newAnnotations.put(annotation, positions.get(i));
            annotation.setRangeIndication(true);
        }
        return newAnnotations;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Watermark Indicating Source Form Mismatch
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void checkForContentTypeMismatch(FileEditorInput input)
    {
        contentTypeMismatch = false;

        IFile file = input.getFile();
        if (file == null || file.getProject() == null || file.getName() == null) return;

        String contentType = CoreModel.getRegistedContentTypeId(file.getProject(), file.getName());
        if (contentType == null) return;

        boolean expectedSourceForm = this.isFixedForm();
        boolean actualSourceForm = SourceForm.isFixedForm(file);
        if (actualSourceForm != expectedSourceForm)
            contentTypeMismatch = true;
    }

    private void addWatermark(Composite parent)
    {
        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer instanceof ITextViewerExtension2)
        {
            ITextViewerExtension2 painter = (ITextViewerExtension2)sourceViewer;
            painter.addPainter(new WatermarkPainter());
        }
    }

    public final class WatermarkPainter implements IPainter
    {
        private boolean active = false;
        private StyledText widget = null;
        private PaintListener listener = null;

        public void paint(int reason)
        {
            if (!active)
            {
                active = true;
                widget = FortranEditor.this.getSourceViewer().getTextWidget();
                final Font font = new Font(null, new FontData("Arial", 14, SWT.NORMAL)); //$NON-NLS-1$
                final Color lightGray = new Color(null, new RGB(192, 192, 192));
                listener = new PaintListener()
                {
                    public void paintControl(PaintEvent e)
                    {
                        if (widget == null || contentTypeMismatch == false) return;

//                        String msg = "WARNING: This file is open in a "
//                                   + (isFixedForm() ? "fixed-form" : "free-form")
//                                   + " editor,\nbut the platform content type "
//                                   + "indicates that it is a "
//                                   + (isFixedForm() ? "free-form" : "fixed-form")
//                                   + " file.";
                        String msg = "WARNING: Content type mismatch     "; //$NON-NLS-1$
                        Rectangle area = widget.getClientArea();
                        e.gc.setFont(font);
                        e.gc.setForeground(lightGray);
                        int x = Math.max(0, area.x + area.width - e.gc.textExtent(msg).x); //area.x + area.width/2;
                        int y = area.y;
                        e.gc.drawString(msg, x, y, true);
                    }
                };
                widget.addPaintListener(listener);
            }
        }

        public void dispose()
        {
            if (listener != null)
            {
                widget.removePaintListener(listener);
                listener = null;
            }

            widget = null;
        }

        public void deactivate(boolean redraw) {}
        public void setPositionManager(IPaintPositionManager manager) {}
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Ctrl+/ Block Commenting Support
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope", FORTRAN_EDITOR_CONTEXT_ID }); //$NON-NLS-1$
    }

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

    protected int[] getColumnsToDrawVerticalLinesOn()
    {
        if (isFixedForm())
        {
            int endColumnWidth = FortranPreferences.FIXED_FORM_COMMENT_COLUMN.getValue();
            return new int[] { 5, 6, endColumnWidth };
        }
        else
        {
            return new int[0];
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Preference Page Support
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns true if the event will require us to perform a damage and repair
     * e.g. a color preference change
     */
    @Override protected boolean affectsTextPresentation(PropertyChangeEvent event)
    {
        return FortranPreferences.respondToPreferenceChange(event.getProperty())
               || super.affectsTextPresentation(event);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Extensible SourceViewerConfiguration (permits Declaration View, content assist, etc.)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected SourceViewerConfiguration createSourceViewerConfiguration()
    {
        // If org.eclipse.photran.vpg.ui is contributing a SourceViewerConfiguration through the
        // extension point, load it
        IConfigurationElement[] configs = Platform.getExtensionRegistry().
            getConfigurationElementsFor(SOURCE_VIEWER_CONFIG_EXTENSION_POINT_ID);
        if (configs.length > 0)
        {
            try
            {
                IFortranSourceViewerConfigurationFactory factory =
                    (IFortranSourceViewerConfigurationFactory)
                    configs[configs.length-1].createExecutableExtension("factory"); //$NON-NLS-1$
                return factory.create(FortranEditor.this);
            }
            catch (CoreException e)
            {
                // Fall through
            }
        }

        // Otherwise, default to CDT's reconciler
        return new FortranSourceViewerConfiguration(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Syntax Highlighting and Outline View Support (Partitioning and Reconciling)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void configurePartitionScanner(IDocument document)
    {
        IDocumentPartitioner partitioner = new FastPartitioner(new RuleBasedPartitionScanner(),
                                                               PARTITION_TYPES);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
    }

    protected ITokenScanner getTokenScanner()
    {
        return new FortranKeywordRuleBasedScanner(isFixedForm(), getSourceViewer());
    }

    public static class FortranSourceViewerConfiguration extends CDTBasedSourceViewerConfiguration
    {
        protected static final Color WHITE = new Color(null, new RGB(255, 255, 255));
        //protected static final Color LIGHT_YELLOW = new Color(null, new RGB(255, 255, 191));

        protected PresentationReconciler reconciler;

        public FortranSourceViewerConfiguration(FortranEditor editor)
        {
            super(editor);
        }

        /**
         * Determines the tab width to use in the Fortran editor.
         * <ol>
         * <li> First, it looks at the custom Fortran editor preference.  If it is
         *      non-zero, this width is used.
         * <li> If the custom preference is not set, the workspace-wide text editor
         *      preference is used instead.
         * </ol>
         */
        @Override public int getTabWidth(ISourceViewer sourceViewer)
        {
            return FortranPreferences.TAB_WIDTH.getValue();
        }
        
        /**
         * Returns a list of the possible partitions' content types.
         * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
         */
        @Override public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
        {
            return PARTITION_TYPES;
        }

        /**
         * Sets up rules for syntax highlighting.
         * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
         */
        @Override public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
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

        protected ITokenScanner getTokenScanner()
        {
            return ((FortranEditor)editor).getTokenScanner();
        }

        /**
         * Default to a content assistant which shows Fortran code templates.
         * <p>
         * Subclasses may override this to provide a content assistant with more functionality.
         */
        @Override public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
        {
            ContentAssistant assistant = new ContentAssistant();
            FortranTemplateCompletionProcessor templateProcessor = new FortranTemplateCompletionProcessor();
            for (String partitionType : FortranEditor.PARTITION_TYPES)
                assistant.setContentAssistProcessor(templateProcessor, partitionType);
            assistant.enableAutoActivation(false); //assistant.setAutoActivationDelay(500);
            assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
            assistant.setContextInformationPopupBackground(WHITE);
            assistant.setProposalSelectorBackground(WHITE);
            return assistant;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isFixedForm()
    {
        IFile file = getIFile();
        if (file != null) return SourceForm.isFixedForm(getIFile());
        
        IEditorInput input = getEditorInput();
        if (input != null) return SourceForm.isFixedForm(input.getName());
        
        return false;
    }

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

    public ISourceViewer getSourceViewerx() // Annoyingly, the superclass method is declared final
    {
        return super.getSourceViewer();
    }

    public IReconciler getReconciler()
    {
        return getSourceViewerConfiguration().getReconciler(getSourceViewer());
    }
}
