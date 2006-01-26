package org.eclipse.fdt.refactoring;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.photran.internal.core.f95parser.FortranProcessor;
import org.eclipse.photran.internal.core.f95parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Terminal;
import org.eclipse.photran.internal.core.f95parser.Token;
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

public class ConstantReplacementEditorActionDelegate implements IEditorActionDelegate
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
                	   FortranProcessor processor = new FortranProcessor();
                	   ParseTreeNode parseTree = processor.parse(
                	       getActiveEditorInput(),
                	       getActiveEditorFile().getName());
                    // SymbolTable symTbl = processor.parseAndCreateSymbolTableFor(
                    //SymbolTable symTbl = processor.createSymbolTableFromParseTree(ptRoot);
                    
                    final String[] constants = processConstants(parseTree);
                    
            		   showReplaceDialog(constants);
            		   
 //           		   MyDialog dialog = new MyDialog(activeEditor.getSite().getShell());
                    //ReplaceDialog dialog = new ReplaceDialog(activeEditor.getSite().getShell());
            		   //dialog.open();

                    /*
                     * You will probably want to go deeper in the symbol table, i.e., only process
                     * subprograms inside a given module (not every subprogram declaration in the
                     * entire file). In that case, call:
                     * outputDeclarationsForSubprogramsIn(symTbl.getEntryInHierarchyFor("module or program name goes here").getChildTable());
                     */
                    //String cinterface = getDeclarationsForSubprogramsIn(symTbl);
                    //showMessageDialog("C Interop Interface", constants);
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
                        //ReplaceDialog dialog = new ReplaceDialog(activeEditor.getSite().getShell());
                		 dialog.open();

                    }
                });
            }

            private void showMessageDialog(final String title, final String msg)
            {
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        MessageDialog.openInformation(activeEditor.getSite().getShell(), title, msg);
                    }
                });
            }

        };
        job.setUser(true);
        job.schedule();
    }

    private String[] processConstants(ParseTreeNode parseTree)
    {
    	    final ArrayList /*<String>*/ sa = new ArrayList();
    	    final StringBuffer sb = new StringBuffer();
    	    
        parseTree.visitUsing(new GenericParseTreeVisitor()
                {
                    public void visitToken(Token thisToken)
                    {
                		 if (thisToken.getTerminal() == Terminal.T_DCON) {
                 		sb.replace(0, sb.length(), thisToken.getStartLine() + ":" + thisToken.getStartCol() + ":" + thisToken.getEndCol());
                			sb.append(": text = " + thisToken.getText() + "\n");
                			sa.add(sb.toString());
                		 }
                		 if (thisToken.getTerminal() == Terminal.T_RCON) {
                      	sb.replace(0, sb.length(), thisToken.getStartLine() + ":" + thisToken.getStartCol() + ":" + thisToken.getEndCol());
                			sb.append(": text = " + thisToken.getText() + "\n");
                			sa.add(sb.toString());
                		 }
                		 if (thisToken.getTerminal() == Terminal.T_ICON) {
                      	sb.replace(0, sb.length(), thisToken.getStartLine() + ":" + thisToken.getStartCol() + ":" + thisToken.getEndCol());
                			sb.append(": text = " + thisToken.getText() + "\n");
                			sa.add(sb.toString());
                		 }
                    }
                });
        
        String[] s = new String[sa.size()];
        for (int i = 0; i < sa.size(); i++) {
        	  s[i] = (String) sa.get(i);
        }
        return s;
    }

    private String getDeclarationsForSubprogramsIn(SymbolTable symTbl)
    {
        final StringBuffer sb = new StringBuffer();
        
        symTbl.visitUsing(new SymbolTableVisitor()
        {
            public void visit(FunctionEntry symTblEntry)
            {
                appendDeclarationFor(symTblEntry, sb);
            }

            public void visit(SubroutineEntry symTblEntry)
            {
                appendDeclarationFor(symTblEntry, sb);
            }
            
            public void visit(VariableEntry symTblEntry)
            {
                sb.append(symTblEntry.getTypeDescription());
                sb.append(" " + symTblEntry.toString());
         	   sb.append("\n");
            }

        });

        return sb.toString();
    }

    protected void appendDeclarationFor(AbstractSubprogramEntry symTblEntry, StringBuffer sb)
    {
        SymbolTableType returnType = symTblEntry.getReturnType();
        String subprogramName = symTblEntry.getIdentifier().getText();
        ArrayList /* <VariableEntry> */params = symTblEntry.getParameters();

        sb.append(determineCEquivalentOfType(returnType));
        sb.append(" ");
        sb.append(subprogramName);
        sb.append(" (");
        for (int i = 0, size = params.size(); i < size; i++)
        {
            VariableEntry thisParam = (VariableEntry)params.get(i);

            if (i != 0) sb.append(", ");
            sb.append(determineCEquivalentOfType(thisParam.getType()));
            sb.append(" ");
            sb.append(thisParam.getIdentifier().getText());
        }
        sb.append(")\n");
    }

    protected String determineCEquivalentOfType(SymbolTableType type)
    {
        // If you don't need this, i.e., if you just
        // want to output the Fortran name of the type
        // instead (INTEGER, REAL, etc.), just call
        // type.getDescription()
        // instead of using a SymbolTableTypeProcessor

        if (type == null)
            return "void";
        else
            return (String)type.processUsing(new SymbolTableTypeProcessor()
            {
                public Object ifInteger(SymbolTableType type)
                {
                    return "int";
                }

                public Object ifReal(SymbolTableType type)
                {
                    return "float";
                }

                public Object ifDoublePrecision(SymbolTableType type)
                {
                    return "double";
                }

                public Object ifComplex(SymbolTableType type)
                {
                    return "Complex";
                }

                public Object ifLogical(SymbolTableType type)
                {
                    return "boolean";
                }

                public Object ifCharacter(SymbolTableType type)
                {
                    return "char";
                }

                public Object ifDerivedType(String derivedTypeName, SymbolTableType type)
                {
                    return derivedTypeName;
                }
            });
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
