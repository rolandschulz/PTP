package org.eclipse.fdt.refactoring;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.photran.internal.core.f95refactoringparser.ILexer;
import org.eclipse.photran.internal.core.f95refactoringparser.Terminal;
import org.eclipse.photran.internal.core.f95refactoringparser.Token;
import org.eclipse.photran.internal.core.f95refactoringparser.Lexer;
import org.eclipse.photran.internal.core.f95refactoringparser.PreprocessingReader;
import org.eclipse.swt.widgets.Display;

/*
 * This code was inspired by org.eclipse.jdt.ui.actions.InferTypeArgumentsAction.java
 * and org.eclipse.cdt.debug.ui.internal.ui.action.AbstractDebugActionDelegate.java
 */

public class ConstantPromotionAction {
	
	/**
	 * Background job for this action, or <code>null</code> if none.
	 */
	private PromoteConstantsJob fBackgroundJob = null;
	private boolean fIsBackground = false;	// WARNING: running in background causes threading error in IDocument.replace()

	/*
	 * Returns the list of Fortran files that have been selected
	 */
	public static IFile[] getSelectedFiles(IStructuredSelection selection) {
		List list= selection.toList();
		IFile[] elements= new IFile[list.size()];
		for (int i= 0; i < list.size(); i++) {
			Object object= list.get(i);
			if (object instanceof ICElement)
				if (object instanceof ITranslationUnit) {
					if (((ITranslationUnit) object).getContentTypeId().equals("org.eclipse.photran.core.freeFormFortranSource")) {
						elements[i] = (IFile) ((ICElement)object).getResource();
					}
				} else {
					return new IFile[0];
				}
			else
				return new IFile[0];
		}
		return elements;
	}
	
	
	class PromoteConstantsJob extends Job {
		private static final String PROMOTE_CONSTANTS = "Promote Constants";
	    private IFile[] fFiles = null;
		
		public PromoteConstantsJob(IStructuredSelection selection) {
			super(PROMOTE_CONSTANTS);
			//this.fFiles = getSelectedFiles(selection);
		}
		
		protected IStatus run(IProgressMonitor monitor) {
        	monitor.beginTask("Running constant promotion refactoring; please wait...", IProgressMonitor.UNKNOWN);
        	
        	for (int i = 0; i < fFiles.length; i++) {
        		try {
                    boolean isFixedForm = false;

                    IFile file = fFiles[i];
                    
                    String filename = file.getName();                    
                    InputStream in = file.getContents();
                    ILexer scanner = Lexer.createLexer(new PreprocessingReader(in, filename), filename, isFixedForm);
        			final String[] constants = TextChanges.processConstants(scanner);
        			//showReplaceDialog(constants);
        			applyChanges(monitor, file, constants);
        			
        			return new Status(IStatus.OK, RefactoringPlugin.PLUGIN_ID, IStatus.OK, "Done", null);
        		} catch (Exception e) {
        			return new Status(IStatus.ERROR, RefactoringPlugin.PLUGIN_ID, IStatus.OK, e.toString(), e);
        		} finally {
        			monitor.done();
        		}
        	}
        	return Status.OK_STATUS;
		}
		
        private void showReplaceDialog(final String[] constants)
        {
            Display.getDefault().syncExec(new Runnable()
            {
                public void run()
                {
                	/********
                	 ReplaceDialog dialog = new ReplaceDialog(activeEditor, constants);
                    //ReplaceDialog dialog = new ReplaceDialog(activeEditor.getSite().getShell());
            		 dialog.open();
            		 ******/
                }
            });
        }

        /**
         * Sets the selection to operate on.
         * 
         * @param elements
         */
        public void setTargets(IFile[] files) {
             fFiles = files;
        }

	}

	public void run(IStructuredSelection selection) {
		if (isRunInBackground()) {
			runInBackground(selection);
		} else {
			runInForeground(selection);
		}
	}
	
	/**
	 * Runs this action in a background job.
	 */
	private void runInBackground(/*IAction action,*/ IStructuredSelection selection) {
	    if (fBackgroundJob == null) {
			fBackgroundJob = new PromoteConstantsJob(selection);
	    }
	    fBackgroundJob.setTargets(getSelectedFiles(selection));
		fBackgroundJob.schedule();
	}
	
	/**
	 * Runs this action in the UI thread.
	 */
	private void runInForeground(final IStructuredSelection selection) {
    	// monitor.beginTask("Running constant promotion refactoring; please wait...", IProgressMonitor.UNKNOWN);
    	
		IFile[] files = getSelectedFiles(selection);
    	for (int i = 0; i < files.length; i++) {
    		try {
                boolean isFixedForm = false;

                IFile file = files[i];
                
                String filename = file.getName();                    
                InputStream in = file.getContents();
                ILexer scanner = Lexer.createLexer(new PreprocessingReader(in, filename), filename, isFixedForm);
    			final String[] constants = TextChanges.processConstants(scanner);
    			//showReplaceDialog(constants);
    			applyChanges(null, file, constants);
    		} catch (Exception e) {
    			System.out.println(e);
    		}
    	}
	}

	private void reportErrors(final MultiStatus ms) {
		/******
		if (!ms.isOK()) {
			IWorkbenchWindow window= CDebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				ErrorDialog.openError(window.getShell(), getErrorDialogTitle(), getErrorDialogMessage(), ms);
			} else {
				CDebugUIPlugin.log(ms);
			}
		}
		*****/
	}

	/**
	 * Returns whether or not this action should be run in the background.
	 * @return whether or not this action should be run in the background
	 */
	protected boolean isRunInBackground() {
		return fIsBackground;
	}

    private void applyChanges(IProgressMonitor monitor, IFile file, final String[] changeList) {
    	try {
    		int prevLine = -1;
    		int extraColumns = 0;
    		
    		ITextFileBuffer textBuffer = TextChanges.getTextBuffer(file);
    		IDocument doc = textBuffer.getDocument();
    	   	for (int i = 0; i < changeList.length; i++) {
        		TextChanges ch = new TextChanges(changeList[i]);
        		String replacement = TextChanges.replacement(ch.text());
        		int line = ch.line();
        		if (prevLine < line) {
        			prevLine = line;
        			extraColumns = 0;
        		}
        		int column = ch.column() + extraColumns;
    			int offset = column + doc.getLineOffset(line);
    			int length = ch.length();
        		doc.replace(offset, length, replacement);
        		extraColumns += replacement.length() - length;
        	}
    	   	textBuffer.commit(monitor, true);
    	} catch (Exception ex) {
    		System.out.println(ex);
    	}
    	
     }

}
