package org.eclipse.photran.internal.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.util.OffsetLength;
import org.eclipse.photran.internal.core.lexer.LexerOptions;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
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
    
    protected ITextSelection getEditorSelection()
    {
        ISelectionProvider provider = getFortranEditor().getSelectionProvider();
        if (provider == null) return null;
        
        ISelection sel = provider.getSelection();
        if (!(sel instanceof ITextSelection)) return null;
        
        return (ITextSelection)sel;
    }
    
    protected OffsetLength getEditorSelectionOffsetLength()
    {
        ITextSelection sel = getEditorSelection();
        return sel == null ? null : new OffsetLength(sel.getOffset(), sel.getLength());
    }
    
    protected IEditorInput getEditorInput()
    {
        return (fEditor == null ? null : fEditor.getEditorInput());
    }
    
    protected IFile getEditorIFile()
    {
        IEditorInput input = getEditorInput();
        return (input != null && input instanceof IFileEditorInput ? ((IFileEditorInput)input).getFile() : null);
    }

    protected IDocument getEditorIDocument()
    {
        IEditorInput input = getEditorInput();
        return input == null ? null : fEditor.getDocumentProvider().getDocument(input);
    }
    
    protected Shell getEditorShell()
    {
        return fEditor.getSite().getShell();
    }

    protected IFortranAST parseCurrentDocument()
    {
        try
        {
            int formatOption = getFortranEditor().isFixedForm() ? LexerOptions.FIXED_FORM : LexerOptions.FREE_FORM;
            //return ASTFactory.buildAST(getEditorIFile(), formatOption | LexerOptions.ASSOCIATE_OFFSET_LENGTH);
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    protected void forceOutlineViewUpdate()
    {
        //  //     ///     ////   //  //    ///
        //  //   //  //   //      // //    /////
        //////   //////   //      ////      ///
        //  //   //  //   //      // //
        //  //   //  //    ////   //  //    //
        
        IDocument doc = getEditorIDocument();
        doc.set(" " + doc.get());
        fEditor.doSave(null);
        doc.set(doc.get().substring(1));
        fEditor.doSave(null);
    }
}
