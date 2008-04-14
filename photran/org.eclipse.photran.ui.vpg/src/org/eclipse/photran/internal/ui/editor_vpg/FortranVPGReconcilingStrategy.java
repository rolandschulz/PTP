package org.eclipse.photran.internal.ui.editor_vpg;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.cdt.internal.ui.text.CReconcilingStrategy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;
import org.eclipse.photran.core.FortranAST;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.ui.texteditor.ITextEditor;

import bz.over.vpg.VPGDependency;
import bz.over.vpg.eclipse.VPGJob;

@SuppressWarnings("restriction")
public class FortranVPGReconcilingStrategy extends CReconcilingStrategy
{
    protected final AbstractFortranEditor editor;
    protected PhotranVPG vpg = PhotranVPG.getInstance();
    protected VPGJob<IFortranAST, Token> dispatchASTTasksJob = null;
    protected VPGJob<IFortranAST, Token> updateVPGJob = null, dispatchVPGTasksJob = null;
    protected IFortranAST vpgAST = null;
    
    private Parser parser = new Parser();
    
    public FortranVPGReconcilingStrategy(ITextEditor editor)
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
        }
        else
        {
            this.editor = null;
        }
    }

    @Override public void initialReconcile()
    {
        super.initialReconcile();
        runTasks();
    }

    @Override public void reconcile(IRegion region)
    {
        super.reconcile(region);
        runTasks();
    }

    private void runTasks()
    {
        if (editor == null) return;
        
        if (SearchPathProperties.getProperty(editor.getIFile().getProject(),
                                             SearchPathProperties.ENABLE_VPG_PROPERTY_NAME).equals("true"))
        {
            runASTTasks();
            runVPGTasks();
        }
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
                                                                        SourceForm.preprocessedFreeForm(new IncludeLoaderCallback(editor.getIFile().getProject())));
                    astRootNode = parser.parse(lexer);
                    if (astRootNode == null) return Status.OK_STATUS;
                    
                    synchronized (FortranEditorVPGTasks.instance(editor))
                    {
                        for (IFortranEditorASTTask task : FortranEditorVPGTasks.instance(editor).astTasks)
                            task.handle(new FortranAST(editor.getIFile(),
                                                       astRootNode,
                                                       lexer.getTokenList()));
                    }
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
                    synchronized (FortranEditorVPGTasks.instance(editor))
                    {
                        for (IFortranEditorVPGTask task : FortranEditorVPGTasks.instance(editor).vpgTasks)
                            task.handle(editor.getIFile(), vpgAST);
                    }
                }
                dispatchVPGTasksJob = null;
                return Status.OK_STATUS;
            }
        };
        dispatchVPGTasksJob.schedule();
    }
}
