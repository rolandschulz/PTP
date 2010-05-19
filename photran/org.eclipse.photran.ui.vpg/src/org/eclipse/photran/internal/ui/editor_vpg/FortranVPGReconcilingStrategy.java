/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.cdt.internal.ui.text.CCompositeReconcilingStrategy;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.photran.internal.ui.editor.FortranEditor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A replacemenet reconciling strategy for the Fortran editor which runs {@link FortranEditorTasks}.
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class FortranVPGReconcilingStrategy extends CCompositeReconcilingStrategy
{
    protected final FortranEditor editor;
    
    public FortranVPGReconcilingStrategy(ISourceViewer sourceViewer, ITextEditor editor, String documentPartitioning)
    {
        super(sourceViewer, editor, documentPartitioning);
        if (editor instanceof FortranEditor)
        {
            this.editor = (FortranEditor)editor;
        }
        else
        {
            this.editor = null;
        }
    }

    @Override public void initialReconcile()
    {
        super.initialReconcile();
        FortranEditorTasks.instance(editor).getRunner().runTasks();
    }

    @Override public void reconcile(IRegion region)
    {
        super.reconcile(region);
        FortranEditorTasks.instance(editor).getRunner().runTasks();
    }
}
