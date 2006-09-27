package org.eclipse.photran.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Action supporting block commenting in the Fortran editor
 * 
 * @author cheahcf  from org.eclipse.cdt.internal.ui.actions
 */
public abstract class FortranEditorActionDelegate
    extends Action
    implements IEditorActionDelegate, IWorkbenchWindowActionDelegate
{

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    
    private IWorkbenchWindow window = null;
    private AbstractFortranEditor fEditor = null;
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public FortranEditorActionDelegate() {}
    
    public FortranEditorActionDelegate(AbstractFortranEditor ed) { fEditor = ed; }
    
    ///////////////////////////////////////////////////////////////////////////
    // IActionDelegate Implementation
    ///////////////////////////////////////////////////////////////////////////

    public final void run(IAction action)
    {
        if (this.fEditor == null && this.window != null)
        {
            IEditorPart editor = this.window.getActivePage().getActiveEditor();
            fEditor = editor instanceof AbstractFortranEditor ? (AbstractFortranEditor)editor : null;
        }
        
        if (this.fEditor != null) run();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // IActionDelegate Implementation
    ///////////////////////////////////////////////////////////////////////////
    
    public abstract void run();

    public void selectionChanged(IAction action, ISelection selection) {;}
    
    ///////////////////////////////////////////////////////////////////////////
    // IWorkbenchWindowActionDelegate Implementation
    ///////////////////////////////////////////////////////////////////////////

    public void init(IWorkbenchWindow window)
    {
        this.window = window;
    }
    
    public void dispose() {;}

    ///////////////////////////////////////////////////////////////////////////
    // IEditorActionDelegate Implementation
    ///////////////////////////////////////////////////////////////////////////

    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        fEditor = targetEditor instanceof AbstractFortranEditor ? (AbstractFortranEditor)targetEditor : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility Methods for Subclasses
    ///////////////////////////////////////////////////////////////////////////

    protected AbstractFortranEditor getFortranEditor()
    {
        return fEditor;
    }
}
