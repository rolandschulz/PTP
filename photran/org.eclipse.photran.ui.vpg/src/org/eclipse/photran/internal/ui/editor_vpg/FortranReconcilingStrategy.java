package org.eclipse.photran.internal.ui.editor_vpg;

import java.io.ByteArrayInputStream;

import org.eclipse.cdt.internal.ui.text.CReconcilingStrategy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.ui.texteditor.ITextEditor;

import bz.over.vpg.eclipse.VPGJob;

@SuppressWarnings("restriction")
public class FortranReconcilingStrategy extends CReconcilingStrategy
{
    protected ExperimentalFreeFormFortranEditor editor = null;
    protected PhotranVPG vpg = PhotranVPG.getInstance();
    protected VPGJob<IFortranAST, Token> dispatchASTTasksJob = null;
    protected VPGJob<IFortranAST, Token> updateVPGJob = null, dispatchVPGTasksJob = null;
    protected IFortranAST vpgAST = null;
    
    public FortranReconcilingStrategy(ITextEditor editor)
    {
        super(editor);
        if (editor instanceof ExperimentalFreeFormFortranEditor) this.editor = (ExperimentalFreeFormFortranEditor)editor;
    }

    @Override public void initialReconcile()
    {
        super.initialReconcile();
        this.reconcile(null);
    }

    @Override public void reconcile(IRegion region)
    {
        super.reconcile(region);
        if (editor == null) return;
        
        runASTTasks();
        runVPGTasks();
    }

    private void runASTTasks()
    {
        if (dispatchASTTasksJob != null) return; // Already running an update

        dispatchASTTasksJob = new VPGJob<IFortranAST, Token>("Updating editor")
        {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
            {
                ASTExecutableProgramNode astRootNode = null;
                try
                {
                    String editorContents = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
                    IAccumulatingLexer lexer = LexerFactory.createLexer(new ByteArrayInputStream(editorContents.getBytes()),
                                                                        null,
                                                                        SourceForm.UNPREPROCESSED_FREE_FORM);
                    astRootNode = new Parser().parse(lexer);
                    if (astRootNode == null) return Status.OK_STATUS;
                    
                    for (IEditorASTTask task : editor.astTasks)
                        task.handle(astRootNode);
                }
                catch (Exception e)
                {
                    ;
                }
                
                dispatchASTTasksJob = null;
                return Status.OK_STATUS;
            }
        };
        dispatchASTTasksJob.schedule();
    }

    private void runVPGTasks()
    {
        if (updateVPGJob != null || dispatchVPGTasksJob != null) return; // Already running an update

        if (vpgAST == null || vpg.db.isOutOfDate(PhotranVPG.getFilenameForIFile(editor.getIFile())))
        {
            vpgAST = null;
            updateVPGJob = new VPGJob<IFortranAST, Token>("Updating editor model")
            {
                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
                {
                    vpgAST = vpg.acquireTransientAST(PhotranVPG.getFilenameForIFile(editor.getIFile()));
                    updateVPGJob = null;
                    return Status.OK_STATUS;
                }
            };
            updateVPGJob.schedule();
        }
        
        dispatchVPGTasksJob = new VPGJob<IFortranAST, Token>("Updating editor")
        {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
            {
                if (vpgAST != null) // Parse might have failed
                {
                    for (IEditorVPGTask task : editor.vpgTasks)
                        task.handle(editor.getIFile(), vpgAST);
                }
                dispatchVPGTasksJob = null;
                return Status.OK_STATUS;
            }
        };
        dispatchVPGTasksJob.schedule();
    }
}
