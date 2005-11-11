package org.eclipse.photran.internal.ui.editor;


import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.photran.internal.ui.actions.FortranCommentActions;
import org.eclipse.photran.internal.ui.preferences.FortranEditorPreferencePage;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * The Fortran text editor, including an inner class containing a coloring
 * scheme.
 * 
 * @author joverbey
 * @author cheahcf
 * @author nchen
 */
public class FortranFreeFormEditor extends AbstractFortranEditor {
	public static final String EDITOR_ID = "org.eclipse.photran.internal.ui.Editor.FortranEditor";

	private TabConverter tabConverter;

	private Composite fMainComposite;

	private static final int MAX_LINES_FOR_LEXER_BASED_SCANNER = 1000;

	public FortranFreeFormEditor() {
		super();
		setSourceViewerConfiguration(new FortranSourceViewerConfiguration(this));
		setRangeIndicator(new DefaultRangeIndicator());
		// We must use the CUIPlugin's document provider in order for the
		// working copy manager in setOutlinePageInput (below) to
		// function correctly.
		// setDocumentProvider(new EbnfDocumentProvider());
		setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());
	}

	// Since we don't have our "own" document provider, we can't set up
	// partitioning there... so we do it here instead
	// TODO: dynamic change to
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		IDocument document = this.getDocumentProvider().getDocument(input);
	
		if (document != null) {
			linesOfCode = document.getNumberOfLines();
			if (document.getNumberOfLines() > MAX_LINES_FOR_LEXER_BASED_SCANNER) {
				RuleBasedPartitionScanner sScanner = new RuleBasedPartitionScanner();
				IDocumentPartitioner partitioner = new DefaultPartitioner(
						sScanner,
						new String[] { FortranPartitionScanner.F90_STRING_CONSTANTS_PARTITION });
				partitioner.connect(document);
				document.setDocumentPartitioner(partitioner);
			} else {
				IDocumentPartitioner partitioner = new FortranPartitionScanner(
						input.getName(), false);
				partitioner.connect(document);
				document.setDocumentPartitioner(partitioner);
			}
			configureTabConverter(); // prepare a new TabConverter for the
			// document
		}
	}

	// --- RESPOND TO PREFERENCE CHANGES ---//

	/**
	 * Handles preference changes. Usually it is sufficient to use the
	 * superclass implementation. This time, we need to perform conversion and
	 * also change the tab width.
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		try {
			if (getSourceViewer() == null
					|| getSourceViewer().getTextWidget() == null)
				return;
			AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();
			if (asv != null) {
				String property = event.getProperty();

				if (FortranEditorPreferencePage.SPACES_FOR_TABS_PREF.equals(property)) {
					asv.updateIndentationPrefixes();
					if (FortranEditorPreferencePage.isTabConversionEnabled())
						doTabConversion();
					else
						endTabConversion();
					return;
				}

				if (FortranEditorPreferencePage.TAB_WIDTH_PREF.equals(property)) {
					asv.updateIndentationPrefixes();
					if (tabConverter != null)
						tabConverter
								.setNumberOfSpacesPerTab(FortranEditorPreferencePage
										.getTabSize());
					Object value = event.getNewValue();
					if (value instanceof Integer) {
						asv.getTextWidget().setTabs(
								((Integer) value).intValue());
					} else if (value instanceof String) {
						asv.getTextWidget().setTabs(
								Integer.parseInt((String) value));
					}
					return;
				}
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	/**
	 * Creates a new tabCoverter for this editor that is currently opened in the
	 * workspace
	 * 
	 */
	private void configureTabConverter() {
		if (tabConverter != null) {
			tabConverter.setLineTracker(new DefaultLineTracker());
		}
	}

	/**
	 * Converts any new tabs that are entered into spaces
	 * 
	 */
	private void doTabConversion() {
		if (tabConverter == null) {
			tabConverter = new TabConverter();
			configureTabConverter();
			tabConverter.setNumberOfSpacesPerTab(FortranEditorPreferencePage
					.getTabSize());
			AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();
			asv.addTextConverter(tabConverter);
			asv.updateIndentationPrefixes();
		}
	}

	/**
	 * Stops converting tabs that the user enters. Any previous tabs that were
	 * converted to spaces are left as they are.
	 */
	private void endTabConversion() {
		if (tabConverter != null) {
			AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();
			asv.removeTextConverter(tabConverter);
			asv.updateIndentationPrefixes();
			tabConverter = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		if (FortranEditorPreferencePage.isTabConversionEnabled())
			doTabConversion();

		Composite childComp = (Composite) ((Composite) parent.getChildren()[0])
				.getChildren()[0];
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 2;
		childComp.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		childComp.getChildren()[0].setLayoutData(data);

		fMainComposite = childComp;

		createHorizontalRuler(fMainComposite);
	}

	// --- EVERYTHING BELOW IS FOR OUTLINING ---//

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

	protected CContentOutlinePage fOutlinePage;

	private AbstractHorizontalRuler fHRuler;

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

	// --- TOGGLE COMMENTING SUPPORT
	// ---------------------------------------------- //
	/**
	 * cheahcf Required to intialize key bindings specified in plugin.xml
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "org.eclipse.photran.ui.FEditorScope" }); //$NON-NLS-1$
	}

	/**
	 * Create actions that will be registered with the editor.
	 */
	protected void createActions() {
		super.createActions();
		Action action = new FortranCommentActions(CEditorMessages
				.getResourceBundle(), "AddBlockComment.", this); //$NON-NLS-1$
		action
				.setActionDefinitionId(ICEditorActionDefinitionIds.ADD_BLOCK_COMMENT);
		setAction("AddBlockComment", action); //$NON-NLS-1$
		markAsStateDependentAction("AddBlockComment", true); //$NON-NLS-1$
		markAsSelectionDependentAction("AddBlockComment", true); //$NON-NLS-1$      
	}

	// --FOLDING
	// SUPPORT----------------------------------------------------------------------------

	// protected ProjectionSupport projectionSupport;
	//
	// protected ProjectionAnnotationModel annotationModel;
	//
	// public ProjectionAnnotationModel getAnnotationModel() {
	// return annotationModel;
	// }
	//
	// public void createPartControl(Composite parent) {
	// super.createPartControl(parent);
	//
	// ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
	//
	// projectionSupport = new ProjectionSupport(viewer,
	// getAnnotationAccess(), getSharedColors());
	// projectionSupport.install();
	//
	// // turn projection mode on
	// viewer.doOperation(ProjectionViewer.TOGGLE);
	//
	// annotationModel = viewer.getProjectionAnnotationModel();
	// }
	//
	// protected ISourceViewer createSourceViewer(Composite parent,
	// IVerticalRuler ruler, int styles) {
	// ISourceViewer viewer = new ProjectionViewer(parent, ruler,
	// getOverviewRuler(), isOverviewRulerVisible(), styles);
	//
	// // ensure decoration support has been created and configured.
	// getSourceViewerDecorationSupport(viewer);
	//
	// return viewer;
	// }

	// -- ISelectionChangedListener implementation -----------------------------

	/**
	 * @param mainComposite
	 * This creates the horizontal ruler and adds it to the top of the editor
	 */
	private void createHorizontalRuler(Composite mainComposite) {

		GC gc = new GC(getSourceViewer().getTextWidget());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = gc.getFontMetrics().getHeight();
		gc.dispose();

		fHRuler = new FortranFreeFormHorizontalRuler(getVerticalRuler(), mainComposite);
		fHRuler.setFont(getSourceViewer().getTextWidget().getFont());
		fHRuler.setSourceViewer(getSourceViewer());
		fHRuler.setLayoutData(data);
		fHRuler.moveAbove(null);

	}
}
