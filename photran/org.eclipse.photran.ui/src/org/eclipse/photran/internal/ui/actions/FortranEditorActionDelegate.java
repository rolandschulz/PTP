package org.eclipse.photran.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * Action supporting block commenting in the Fortran editor
 * 
 * @author cheahcf  from org.eclipse.cdt.internal.ui.actions
 */
public abstract class FortranEditorActionDelegate
    extends Action
    implements IEditorActionDelegate, IWorkbenchWindowActionDelegate, IRunnableWithProgress
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
        if (this.window != null)
        {
            IEditorPart editor = this.window.getActivePage().getActiveEditor();
            fEditor = editor instanceof AbstractFortranEditor ? (AbstractFortranEditor)editor : null;
        }
        
        if (this.fEditor != null)
        {
            IProgressService context = PlatformUI.getWorkbench().getProgressService();
            ISchedulingRule lockEntireWorkspace = ResourcesPlugin.getWorkspace().getRoot();
            try
            {
                context.runInUI(context, this, lockEntireWorkspace);
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
                MessageDialog.openError(fEditor.getShell(), "Unhandled Exception", e.getMessage());
            }
            catch (InterruptedException e)
            {
                // Do nothing
            }
        }
    }

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
