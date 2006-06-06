package org.eclipse.fdt.refactoring;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.f95refactoringparser.BuildParseTreeParserAction;
import org.eclipse.photran.internal.core.f95refactoringparser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.f95refactoringparser.ILexer;
import org.eclipse.photran.internal.core.f95refactoringparser.Lexer;
import org.eclipse.photran.internal.core.f95refactoringparser.Parser;
import org.eclipse.photran.internal.core.f95refactoringparser.PreprocessingReader;
import org.eclipse.photran.internal.core.f95refactoringparser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95refactoringparser.Terminal;
import org.eclipse.photran.internal.core.f95refactoringparser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableType;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableTypeProcessor;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.AbstractSubprogramEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.FunctionEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.SubroutineEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.VariableEntry;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.internal.ide.dialogs.CleanDialog;

public class ConstantPromotionEditorActionDelegate implements IEditorActionDelegate
{
    protected IEditorPart activeEditor = null;

    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        this.activeEditor = targetEditor;
    }

    public void run(IAction action)
    {
        Job job = new Job("Constant Replacement")
        {
            protected IStatus run(IProgressMonitor monitor)
            {
                monitor.beginTask("Running constant replacement refactoring; please wait...", IProgressMonitor.UNKNOWN);
                try
                {
                    final InputStream in = getActiveEditorInput();
                    final String filename = getActiveEditorFile().getName();
                    
                    boolean isFixedForm = false;
                    IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(filename);
                    if (contentType != null
                    		&& contentType.getId().equals(FortranCorePlugin.FIXED_FORM_CONTENT_TYPE))
                    {
                    	isFixedForm = true;
                    }
                    
                    final ILexer lexer = Lexer.createLexer(new PreprocessingReader(in, filename), filename, isFixedForm);
                	final String[] constants = TextChanges.processConstants(lexer);
                    
            		showReplaceDialog(constants);
            		   
                    return new Status(IStatus.OK, RefactoringPlugin.PLUGIN_ID, IStatus.OK, "Done", null);
                }
                catch (Exception e)
                {
                    return new Status(IStatus.ERROR, RefactoringPlugin.PLUGIN_ID, IStatus.OK, e.toString(), e);
                }
                finally
                {
                    monitor.done();
                }
            }

            private void showReplaceDialog(final String[] constants)
            {
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                    	 ReplaceDialog dialog = new ReplaceDialog(activeEditor, constants);
                		 dialog.open();
                    }
                });
            }

        };
        job.setUser(true);
        job.schedule();
    }

    protected IFile getActiveEditorFile()
    {
        if (activeEditor == null) return null;

        IEditorInput einput = activeEditor.getEditorInput();
        if (!(einput instanceof IFileEditorInput)) return null;

        IFileEditorInput input = (IFileEditorInput)einput;
        return input.getFile();
    }

    protected InputStream getActiveEditorInput()
    {
        IFile file = getActiveEditorFile();
        if (file == null) return null;

        try
        {
            return file.getContents();
        }
        catch (CoreException x)
        {
            return null;
        }
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
    }
}
