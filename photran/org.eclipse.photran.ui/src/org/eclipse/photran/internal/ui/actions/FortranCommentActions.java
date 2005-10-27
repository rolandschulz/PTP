/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.photran.internal.ui.actions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.photran.internal.ui.actions.FortranCommentActions.Edit.EditFactory;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Action supporting block commenting in the Fortran editor
 * 
 * @author cheahcf
 */
//TODO-Cheah: taken from org.eclipse.cdt.internal.ui.actions
public class FortranCommentActions extends TextEditorAction {
	
	 private AbstractFortranEditor fEditor;
    /**
     * Creates a new instance.
     * @param bundle
     * @param prefix
     * @param editor
     */
    public FortranCommentActions(ResourceBundle bundle, String prefix, ITextEditor editor) {
        super(bundle, prefix, editor);
        fEditor=(AbstractFortranEditor) editor;
    }

    /**
     * An edit is a kind of <code>DocumentEvent</code>, in this case an edit instruction, that is 
     * affiliated with a <code>Position</code> on a document. The offset of the document event is 
     * not stored statically, but taken from the affiliated <code>Position</code>, which gets 
     * updated when other edits occur.
     */
    static class Edit extends DocumentEvent {
        
        /**
         * Factory for edits which manages the creation, installation and destruction of 
         * position categories, position updaters etc. on a certain document. Once a factory has
         * been obtained, <code>Edit</code> objects can be obtained from it which will be linked to
         * the document by positions of one position category.
         * <p>Clients are required to call <code>release</code> once the <code>Edit</code>s are not
         * used any more, so the positions can be discarded.</p>
         */
        public static class EditFactory {
    
            /** The position category basename for this edits. */
            private static final String CATEGORY= "__positionalEditPositionCategory"; //$NON-NLS-1$
            
            /** The count of factories. */
            private static int fgCount= 0;
        
            /** This factory's category. */
            private final String fCategory;
            private IDocument fDocument;
            private IPositionUpdater fUpdater;
            
            /**
             * Creates a new <code>EditFactory</code> with an unambiguous position category name.
             * @param document the document that is being edited.
             */
            public EditFactory(IDocument document) {
                fCategory= CATEGORY + fgCount++;
                fDocument= document;
            }
            
            /**
             * Creates a new edition on the document of this factory.
             * 
             * @param offset the offset of the edition at the point when is created.
             * @param length the length of the edition (not updated via the position update mechanism)
             * @param text the text to be replaced on the document
             * @return an <code>Edit</code> reflecting the edition on the document
             */
            public Edit createEdit(int offset, int length, String text) throws BadLocationException {;
                if (!fDocument.containsPositionCategory(fCategory)) {
                    fDocument.addPositionCategory(fCategory);
                    fUpdater= new DefaultPositionUpdater(fCategory);
                    fDocument.addPositionUpdater(fUpdater);
                }
                
                Position position= new Position(offset);
                try {
                    fDocument.addPosition(fCategory, position);
                } catch (BadPositionCategoryException e) {
                    System.err.println("BadPosition within Create edit");
                    Assert.isTrue(false);
                }
                return new Edit(fDocument, length, text, position);
            }
            
            /**
             * Releases the position category on the document and uninstalls the position updater. 
             * <code>Edit</code>s managed by this factory are not updated after this call.
             */
            public void release() {
                if (fDocument != null && fDocument.containsPositionCategory(fCategory)) {
                    fDocument.removePositionUpdater(fUpdater);
                    try {
                        fDocument.removePositionCategory(fCategory);
                    } catch (BadPositionCategoryException e) {
                        Assert.isTrue(false);
                    }
                    fDocument= null;
                    fUpdater= null;
                }
            }
        }
        
        /** The position in the document where this edit be executed. */
        private Position fPosition;
        
        /**
         * Creates a new edition on <code>document</code>, taking its offset from <code>position</code>.
         * 
         * @param document the document being edited
         * @param length the length of the edition
         * @param text the replacement text of the edition
         * @param position the position keeping the edition's offset
         */
        protected Edit(IDocument document, int length, String text, Position position) {
            super(document, 0, length, text);
            fPosition= position;
        }
        
        /*
         * @see org.eclipse.jface.text.DocumentEvent#getOffset()
         */
        public int getOffset() {
            return fPosition.getOffset();
        }
        
        /**
         * Executes the edition on document. The offset is taken from the position.
         * 
         * @throws BadLocationException if the execution of the document fails.
         */
        public void perform() throws BadLocationException {
            getDocument().replace(getOffset(), getLength(), getText());
        }
        
    }

    public void run() {
        if (!isEnabled())
            return;
            
        ITextEditor editor= getTextEditor();
      
        ITextSelection selection= getCurrentSelection();
        
        if (!validateEditorInputState())
            return;
        
        IDocumentProvider docProvider= editor.getDocumentProvider();
        IEditorInput input= editor.getEditorInput();
        if (docProvider == null || input == null)
            return;
            
        IDocument document= docProvider.getDocument(input);
        if (document == null)
            return;
       
        
        IRewriteTarget target= (IRewriteTarget)editor.getAdapter(IRewriteTarget.class);
        if (target != null) {
            target.beginCompoundChange();
        }
        
        Edit.EditFactory factory= new Edit.EditFactory(document);
        
        try {
            runInternal(selection,factory);
    
        } catch (BadLocationException e) {
            // can happen on concurrent modification, deletion etc. of the document 
            // -> don't complain, just bail out
        } finally {
            factory.release();
            
            if (target != null) {
                target.endCompoundChange();
            }
        }
    }

    /**
     * Calls <code>perform</code> on all <code>Edit</code>s in <code>edits</code>.
     * 
     * @param edits a list of <code>Edit</code>s
     * @throws BadLocationException if an <code>Edit</code> threw such an exception.
     */
    protected void executeEdits(List edits) throws BadLocationException {

        for (Iterator it= edits.iterator(); it.hasNext();) {
            Edit edit= (Edit) it.next();
            edit.perform();
        }
    }

    /*
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     */
    public void update() {
        super.update();
        
        if (isEnabled()) {
            if (!canModifyEditor())
                setEnabled(false);
        }
    }

    /**
     * Returns the editor's selection, or <code>null</code> if no selection can be obtained or the 
     * editor is <code>null</code>.
     * 
     * @return the selection of the action's editor, or <code>null</code>
     */
    protected ITextSelection getCurrentSelection() {
        ITextEditor editor= getTextEditor();
        if (editor != null) {
            ISelectionProvider provider= editor.getSelectionProvider();
            if (provider != null) {
                ISelection selection= provider.getSelection();
                if (selection instanceof ITextSelection) 
                    return (ITextSelection) selection;
            }
        }
        return null;
    }

    /**
     * Runs the real command once all the editor, document, and selection checks have succeeded.
     * 
     * @param selection the current selection we are being called for
     * @param factory the edit factory we can use to create <code>Edit</code>s 
     * @throws BadLocationException if an edition fails
     * @throws BadPartitioningException if a partitioning call fails
     */

    protected void runInternal(ITextSelection selection,
            EditFactory factory)
            throws BadLocationException {
        // ITextSelection ts = selection;
        int selectionOffset = selection.getStartLine();
        int selectionEndOffset = selection.getEndLine();
        List edits = new LinkedList();
        IDocumentProvider dp = fEditor.getDocumentProvider();
        IDocument doc = dp.getDocument(fEditor.getEditorInput());

        for (int i = selectionOffset; i <= selectionEndOffset; i++) {
       
            int eff;
            eff = doc.getLineOffset(i);
            if (doc.getChar(eff) == '!') {
                edits.add(factory.createEdit(eff, 1, ""));
            } else {
                edits.add(factory.createEdit(eff, 0, "!"));
            }

        }
        executeEdits(edits);
        
        if (selectionEndOffset == doc.getNumberOfLines() - 1) {
            // case when lines get to the end
        	fEditor.selectAndReveal(selection.getOffset(),selection.getLength());
            fEditor.selectAndReveal(doc.getLineOffset(selectionOffset), doc
                    .getLineOffset(selectionEndOffset)
                    - doc.getLineOffset(selectionOffset)+doc.getLineLength(doc.getLineLength(doc.getNumberOfLines())));
        } else {
            // normal case
            fEditor.selectAndReveal(doc.getLineOffset(selectionOffset), doc
                    .getLineOffset(selectionEndOffset + 1)
                    - doc.getLineOffset(selectionOffset));
        }
    }
}
