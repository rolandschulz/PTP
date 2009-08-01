/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui.editor;

import java.util.ResourceBundle;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ConstructedCEditorMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * A {@link TextEditor} that reuses the Outline view from CDT, as well as its document provider
 * and possibly its ruler context menu.  Also uses CDT's resource bundle to support content
 * assist actions.
 * 
 * @author Jeff Overbey, mostly copied from {@link CEditor}
 */
public abstract class CDTBasedTextEditor extends TextEditor implements ISelectionChangedListener
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods to Be Called in Subclass Constructors
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void useCDTDocumentProvider()
    {
        // We must use the CUIPlugin's document provider in order for the
        // working copy manager in setOutlinePageInput (below) to function correctly.
        setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());
    }

    protected void useCDTRulerContextMenuID()
    {
        // JO: This gives you a "Toggle Breakpoint" action (and others)
        // when you right-click the Fortran editor's ruler
        setRulerContextMenuId("#CEditorRulerContext"); //$NON-NLS-1$
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Actions (use CDT resource bundle)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void createActions()
    {
        super.createActions();

        // See CEditor#createActions
        
        final ResourceBundle bundle = ConstructedCEditorMessages.getResourceBundle();

        IAction action = new ContentAssistAction(bundle, "ContentAssistProposal.", this); //$NON-NLS-1$
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action); //$NON-NLS-1$
        markAsStateDependentAction("ContentAssistProposal", true); //$NON-NLS-1$

        action= new TextOperationAction(bundle, "ContentAssistContextInformation.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION); //$NON-NLS-1$
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
        setAction("ContentAssistContextInformation", action); //$NON-NLS-1$
        markAsStateDependentAction("ContentAssistContextInformation", true); //$NON-NLS-1$
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Outline View Support
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private CContentOutlinePage fOutlinePage;

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
            fOutlinePage = new CContentOutlinePage(new CEditor());
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
            if (workingCopy != null)
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
    // Utility Methods for Subclasses
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void forceOutlineViewUpdate()
    {
        try
        {
            CoreModel.getDefault().getCModel().makeConsistent(new NullProgressMonitor());
        }
        catch (CModelException e)
        {
            // Ignore
        }
    }
}
