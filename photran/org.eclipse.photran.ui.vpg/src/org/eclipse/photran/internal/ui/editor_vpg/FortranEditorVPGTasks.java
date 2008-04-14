package org.eclipse.photran.internal.ui.editor_vpg;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;

public class FortranEditorVPGTasks
{
    /**
     * @return the instance of FortranEditorVPGTasks associated with the given
     * editor, creating the instance on-demand if necessary
     */
    public static FortranEditorVPGTasks instance(AbstractFortranEditor editor)
    {
        if (editor.reconcilerTasks == null)
            editor.reconcilerTasks = new FortranEditorVPGTasks(editor);
        
        return (FortranEditorVPGTasks)editor.reconcilerTasks;
    }

    private FortranEditorVPGTasks(AbstractFortranEditor editor)
    {
        editor.reconcilerTasks = this;
    }
    
    /**
     * These jobs will be run (in order) if the contents of the editor parses successfully.  The VPG will probably
     * <i>not</i> be up to date, but token positions will correspond to the contents of the editor.
     */
    final Set<IFortranEditorASTTask> astTasks = new HashSet<IFortranEditorASTTask>();
    
    /**
     * These jobs will be run (in order) when the VPG is more-or-less up-to-date and an AST is available for the
     * file in the editor.
     */
    final Set<IFortranEditorVPGTask> vpgTasks = new HashSet<IFortranEditorVPGTask>();
    
    public synchronized void addASTTask(IFortranEditorASTTask task)
    {
        astTasks.add(task);
    }
    
    public synchronized void addVPGTask(IFortranEditorVPGTask task)
    {
        vpgTasks.add(task);
    }
    
    public synchronized void removeASTTask(IFortranEditorASTTask task)
    {
        astTasks.remove(task);
    }
    
    public synchronized void removeVPGTask(IFortranEditorVPGTask task)
    {
        vpgTasks.remove(task);
    }
}
