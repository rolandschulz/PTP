package org.eclipse.photran.internal.ui.editor_vpg;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.rephraserengine.core.vpg.eclipse.VPGJob;

public class FortranEditorTasks
{
    /**
     * @return the instance of FortranEditorTasks associated with the given
     * editor, creating the instance on-demand if necessary
     */
    public static FortranEditorTasks instance(AbstractFortranEditor editor)
    {
        if (editor.reconcilerTasks == null)
            editor.reconcilerTasks = new FortranEditorTasks(editor);

        return (FortranEditorTasks)editor.reconcilerTasks;
    }

    private FortranEditorTasks(AbstractFortranEditor editor)
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

            String vpgEnabledProperty = SearchPathProperties.getProperty(editor.getIFile(),
                                                 SearchPathProperties.ENABLE_VPG_PROPERTY_NAME);
            if (vpgEnabledProperty != null && vpgEnabledProperty.equals("true"))
            {
                runASTTasks();
                if (runVPGTasks) runVPGTasks();
            }
        }

        private void runASTTasks()
        {
            if (dispatchASTTasksJob != null) return; // Already running an update

            dispatchASTTasksJob = new Job("Updating Fortran editor with new parse information")
            {
                @Override
                protected IStatus run(IProgressMonitor monitor)
                {
                    ASTExecutableProgramNode astRootNode = null;
                    try
                    {
                        if (editor.getDocumentProvider() != null)
                        {
                            String editorContents = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();

                            SourceForm sourceForm =
                                editor.getIFile() == null || editor.getIFile().getProject() == null
                                ? SourceForm.UNPREPROCESSED_FREE_FORM
                                : SourceForm.preprocessedFreeForm(new IncludeLoaderCallback(editor.getIFile().getProject()));
                            IAccumulatingLexer lexer = LexerFactory.createLexer(new ByteArrayInputStream(editorContents.getBytes()),
                                                                                null,
                                                                                editor.getIFile().getName(),
                                                                                sourceForm,
                                                                                true /*false*/);
                            astRootNode = parser.parse(lexer);
                            if (astRootNode == null) return Status.OK_STATUS;

                            HashSet<IFortranEditorASTTask> tasksToRemove = new HashSet<IFortranEditorASTTask>();
                            synchronized (FortranEditorTasks.instance(editor))
                            {
                                for (IFortranEditorASTTask task : FortranEditorTasks.instance(editor).astTasks)
                                    if (!task.handle(astRootNode, lexer.getTokenList(), defMap))
                                        tasksToRemove.add(task);
                            }
                            FortranEditorTasks.instance(editor).astTasks.removeAll(tasksToRemove);
                        }
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();
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

            //if (vpgAST == null || vpg.db.isOutOfDate(PhotranVPG.getFilenameForIFile(editor.getIFile())))
            {
                vpg.queueJobToEnsureVPGIsUpToDate();

                vpgAST = null;
                updateVPGJob = new VPGJob<IFortranAST, Token>("Updating Fortran editor with new analysis information")
                {
                    @Override
                    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
                    {
                        vpgAST = vpg.acquireTransientAST(PhotranVPG.getFilenameForIFile(editor.getIFile()));
                        updateVPGJob = null;
                        //scheduleVPGTaskDispatchJob();

                        if (vpgAST != null)
                        {
                            defMap = createDefMap();
                            for (IFortranEditorVPGTask task : FortranEditorTasks.instance(editor).vpgTasks)
                                task.handle(editor.getIFile(), vpgAST, defMap);
                        }

                        return Status.OK_STATUS;
                    }
                };
                updateVPGJob.setPriority(Job.DECORATE);
                updateVPGJob.schedule();
            }
        }

//        private void scheduleVPGTaskDispatchJob()
//        {
//            dispatchVPGTasksJob = new Job("Updating Fortran editor (3/3)")
//            {
//                @Override
//                protected IStatus run(IProgressMonitor monitor)
//                {
//                    if (vpgAST != null) // Parse might have failed
//                    {
//                        synchronized (FortranEditorTasks.instance(editor))
//                        {
//                            defMap = createDefMap();
//                            for (IFortranEditorVPGTask task : FortranEditorTasks.instance(editor).vpgTasks)
//                                task.handle(editor.getIFile(), vpgAST, defMap);
//                        }
//                    }
//                    dispatchVPGTasksJob = null;
//                    return Status.OK_STATUS;
//                }

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
//            };
//            dispatchVPGTasksJob.setPriority(Job.DECORATE);
//            dispatchVPGTasksJob.schedule();
//        }
    }
}
