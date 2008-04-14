package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.cdt.internal.ui.text.CReconcilingStrategy;
import org.eclipse.jface.text.IRegion;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
public class FortranVPGReconcilingStrategy extends CReconcilingStrategy
{
    protected final AbstractFortranEditor editor;
    
    public FortranVPGReconcilingStrategy(ITextEditor editor)
    {
        super(editor);
        if (editor instanceof AbstractFortranEditor)
        {
            this.editor = (AbstractFortranEditor)editor;
        }
        else
        {
            this.editor = null;
        }
    }

    @Override public void initialReconcile()
    {
        super.initialReconcile();
        FortranEditorVPGTasks.instance(editor).getRunner().runTasks();
    }

    @Override public void reconcile(IRegion region)
    {
        super.reconcile(region);
        FortranEditorVPGTasks.instance(editor).getRunner().runTasks();
    }
}
