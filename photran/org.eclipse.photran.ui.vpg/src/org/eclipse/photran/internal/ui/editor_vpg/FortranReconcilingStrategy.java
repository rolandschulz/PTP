package org.eclipse.photran.internal.ui.editor_vpg;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;

import org.eclipse.cdt.internal.ui.text.CReconcilingStrategy;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;
import org.eclipse.photran.core.FortranAST;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.views.DeclarationView;
import org.eclipse.ui.texteditor.ITextEditor;

import bz.over.vpg.eclipse.VPGJob;

@SuppressWarnings("restriction")
public class FortranReconcilingStrategy extends CReconcilingStrategy
{
    /**
     * These jobs will be run (in order) if the contents of the editor parses successfully.  The VPG will probably
     * <i>not</i> be up to date, but token positions will correspond to the contents of the editor.
     */
    protected LinkedList<IEditorASTTask> astTasks = new LinkedList<IEditorASTTask>();
    
    /**
     * These jobs will be run (in order) when the VPG is more-or-less up-to-date and an AST is available for the
     * file in the editor.
     */
    protected LinkedList<IEditorVPGTask> vpgTasks = new LinkedList<IEditorVPGTask>();
    
    protected final AbstractFortranEditor editor;
    protected PhotranVPG vpg = PhotranVPG.getInstance();
    protected VPGJob<IFortranAST, Token> dispatchASTTasksJob = null;
    protected VPGJob<IFortranAST, Token> updateVPGJob = null, dispatchVPGTasksJob = null;
    protected IFortranAST vpgAST = null;
    
    private Parser parser = new Parser();
    
    public FortranReconcilingStrategy(ITextEditor editor)
    {
        super(editor);
        if (editor instanceof AbstractFortranEditor)
        {
            this.editor = (AbstractFortranEditor)editor;
            
//            astTasks.add(new SampleEditorASTTask(this.editor));
//            vpgTasks.add(new SampleEditorVPGTask(this.editor));
            
//            SampleEditorMappingTask t = new SampleEditorMappingTask(this.editor);
//            astTasks.add(t.astTask);
//            vpgTasks.add(t.vpgTask);
            
            this.vpgTasks.add(new IEditorVPGTask()
            {
                public void handle(IFile file, IFortranAST ast)
                {
                    //declView.update("Offset " + FortranReconcilingStrategy.this.editor.getHighlightRange().getOffset());
                }
            });
        }
        else
        {
            this.editor = null;
        }
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
                    astRootNode = parser.parse(lexer);
                    if (astRootNode == null) return Status.OK_STATUS;
                    
                    for (IEditorASTTask task : astTasks)
                        task.handle(new FortranAST(editor.getIFile(),
                                                   astRootNode,
                                                   lexer.getTokenList()));
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
                    for (IEditorVPGTask task : vpgTasks)
                        task.handle(editor.getIFile(), vpgAST);
                }
                dispatchVPGTasksJob = null;
                return Status.OK_STATUS;
            }
        };
        dispatchVPGTasksJob.schedule();
    }
}
