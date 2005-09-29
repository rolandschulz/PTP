package org.eclipse.photran.internal.ui.editor;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.editor.CTextEditorActionConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.photran.internal.ui.preferences.ColorPreferencePage;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;

public abstract class AbstractFortranEditor extends TextEditor implements
ISelectionChangedListener {

	protected int linesOfCode = 0;
	
	private IPreferenceStore fCombinedPreferenceStore;

	private AdaptedSourceViewer viewer;
	
	public AbstractFortranEditor() {
		// This has to be set to be notified of changes to preferences
		// Without this, the editor will not auto-update
		IPreferenceStore store = FortranUIPlugin.getDefault()
				.getPreferenceStore();
		IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
		fCombinedPreferenceStore = new ChainedPreferenceStore(
				new IPreferenceStore[] { store, generalTextStore, getPreferenceStore()});
		setPreferenceStore(fCombinedPreferenceStore);
		// This enables any global changes to editor e.g. font type and size
		// to take effect
		WorkbenchChainedTextFontFieldEditor.startPropagate(store,
				JFaceResources.TEXT_FONT);

		// JO: This will put the Refactor menu in the editor's
		// context menu, among other things
		setEditorContextMenuId("#CEditorContext"); //$NON-NLS-1$

		// JO: This gives you a "Toggle Breakpoint" action (and others)
		// when you right-click the Fortran editor's ruler
		setRulerContextMenuId("#CEditorRulerContext"); //$NON-NLS-1$
	}

	/**
	 * Returns true if the event will require us to perform a damage and repair
	 * e.g. a color preference change
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return (ColorPreferencePage.respondToPreferenceChange(event));
	}

	/**
	 * @return the number of lines for the currently opened file
	 * Used to determine whether lexer-based syntax coloring is enabled
	 */
	public int getLinesOfCode() {
		return linesOfCode;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		ISelection sel = event.getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			Object obj = selection.getFirstElement();
			if (obj instanceof ISourceReference) {
				try {
					ISourceRange range = ((ISourceReference) obj)
							.getSourceRange();
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

	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());
		
		viewer = new AdaptedSourceViewer(getSourceViewerConfiguration(), parent, ruler, getOverviewRuler(), isOverviewRulerVisible(),styles);
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

}
