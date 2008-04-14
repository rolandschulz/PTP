package org.eclipse.photran.internal.ui.editor_vpg;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;

import bz.over.vpg.eclipse.VPGJob;

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
        this.editor = editor;
        editor.reconcilerTasks = this;
        
        this.runner = new Runner();
    }
    
    private AbstractFortranEditor editor;
    
    private Runner runner;
    
    /**
     * These jobs will be run if the contents of the editor parses successfully.  The VPG will probably
     * <i>not</i> be up to date, but token positions will correspond to the contents of the editor.
     */
    private final Set<IFortranEditorASTTask> astTasks = new HashSet<IFortranEditorASTTask>();
    
    /**
     * These jobs will be run when the VPG is more-or-less up-to-date and an AST is available for the
     * file in the editor.
     */
    private final Set<IFortranEditorVPGTask> vpgTasks = new HashSet<IFortranEditorVPGTask>();

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
    
    public Runner getRunner()
    {
        return runner;
    }
    
    public class Runner
    {
        protected DefinitionMap<Definition> defMap = null;
        
        protected PhotranVPG vpg = PhotranVPG.getInstance();
        protected Job dispatchASTTasksJob = null;
        protected VPGJob<IFortranAST, Token> updateVPGJob = null;
        protected Job dispatchVPGTasksJob = null;
        protected IFortranAST vpgAST = null;
        
        private Parser parser = new Parser();
        
        public void runTasks()
        {
            runTasks(true);
        }
        
        public void runTasks(boolean runVPGTasks)
        {
            if (editor == null) return;
            
            if (SearchPathProperties.getProperty(editor.getIFile().getProject(),
                                                 SearchPathProperties.ENABLE_VPG_PROPERTY_NAME).equals("true"))
            {
                runASTTasks();
                if (runVPGTasks) runVPGTasks();
            }
        }
        
        private void runASTTasks()
        {
            if (dispatchASTTasksJob != null) return; // Already running an update

            dispatchASTTasksJob = new Job("Updating editor")
            {
                @Override
                protected IStatus run(IProgressMonitor monitor)
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
                        
                        HashSet<IFortranEditorASTTask> tasksToRemove = new HashSet<IFortranEditorASTTask>();
                        synchronized (FortranEditorVPGTasks.instance(editor))
                        {
                            for (IFortranEditorASTTask task : FortranEditorVPGTasks.instance(editor).astTasks)
                                if (!task.handle(astRootNode, lexer.getTokenList(), defMap))
                                    tasksToRemove.add(task);
                        }
                        FortranEditorVPGTasks.instance(editor).astTasks.removeAll(tasksToRemove);
                    }
                    catch (Exception e)
                    {
                        ;
                    }
                    
                    dispatchASTTasksJob = null;
                    return Status.OK_STATUS;
                }
            };
            dispatchASTTasksJob.setPriority(Job.DECORATE);
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
                        scheduleVPGTaskDispatchJob();
                        return Status.OK_STATUS;
                    }
                };
                updateVPGJob.setPriority(Job.DECORATE);
                updateVPGJob.schedule();
            }
        }

        private void scheduleVPGTaskDispatchJob()
        {
            dispatchVPGTasksJob = new Job("Updating editor")
            {
                @Override
                protected IStatus run(IProgressMonitor monitor)
                {
                    if (vpgAST != null) // Parse might have failed
                    {
                        synchronized (FortranEditorVPGTasks.instance(editor))
                        {
                            defMap = createDefMap();
                            for (IFortranEditorVPGTask task : FortranEditorVPGTasks.instance(editor).vpgTasks)
                                task.handle(editor.getIFile(), vpgAST, defMap);
                        }
                    }
                    dispatchVPGTasksJob = null;
                    return Status.OK_STATUS;
                }

                private DefinitionMap<Definition> createDefMap()
                {
                    return new DefinitionMap<Definition>(vpgAST)
                    {
                        @Override protected Definition map(String qualifiedName, Definition def)
                        {
                            return def;
                        }
                    };
                }
            };
            dispatchVPGTasksJob.setPriority(Job.DECORATE);
            dispatchVPGTasksJob.schedule();
        }
    }
}
