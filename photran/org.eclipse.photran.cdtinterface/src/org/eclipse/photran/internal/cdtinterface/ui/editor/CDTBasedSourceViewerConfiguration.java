/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.cdtinterface.ui.editor;

import org.eclipse.cdt.internal.ui.text.CCompositeReconcilingStrategy;
import org.eclipse.cdt.internal.ui.text.CReconciler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A {@link SourceViewerConfiguration} that uses the CDT's reconciler to update the editor's
 * partitioning (syntax highlighting) and the Outline view when text is edited.
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public abstract class CDTBasedSourceViewerConfiguration extends SourceViewerConfiguration
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Reconciling Support - Supports Outline View and Syntax Highlighting
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected ITextEditor editor;
    
    /**
     * @param editor may be <code>null</code> if there is no Outline view to update
     */
    public CDTBasedSourceViewerConfiguration(ITextEditor editor)
    {
        this.editor = editor;
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
        if (editor != null)
        {
            //MonoReconciler r = new CReconciler(editor, new CReconcilingStrategy(editor));
            MonoReconciler r = new CReconciler(editor, createReconcilingStrategy(sourceViewer));
            r.setIsIncrementalReconciler(false);
            r.setProgressMonitor(new NullProgressMonitor());
            r.setDelay(500);
            return r;
        }
        else return super.getReconciler(sourceViewer);
    }

    protected CCompositeReconcilingStrategy createReconcilingStrategy(ISourceViewer sourceViewer)
    {
        return new CCompositeReconcilingStrategy(sourceViewer, editor, getConfiguredDocumentPartitioning(sourceViewer));
    }
}
