package org.eclipse.fdt.refactoring;

import java.io.InputStream;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.photran.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.f95refactoringparser.ILexer;
import org.eclipse.photran.internal.core.f95refactoringparser.Lexer;
import org.eclipse.photran.internal.core.f95refactoringparser.PreprocessingReader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchSite;

/*
 * This code was inspired by org.eclipse.jdt.ui.actions.InferTypeArgumentsAction.java
 * and org.eclipse.cdt.debug.ui.internal.ui.action.AbstractDebugActionDelegate.java
 * 
 * Probably should copy from org.eclipse.jdt.internal.ui.actions.ExtractSuperTypeAction.java
 * instead.
 * 
 */

public class ConstantPromotionAction extends SelectionDispatchAction {
	
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
					if (((ITranslationUnit) object).getContentTypeId().equals("org.eclipse.photran.core.freeFormFortranSource")
					 || ((ITranslationUnit) object).getContentTypeId().equals("org.eclipse.photran.core.fixedFormFortranSource")) {
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
	
	/**
	 * Creates a new extract super type action. The action requires that the
	 * selection provided by the site's selection provider is of type
	 * <code>org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site
	 *            the workbench site
	 */
	public ConstantPromotionAction(final IWorkbenchSite site) {
		super(site);
		//setText(RefactoringMessages.ExtractSuperTypeAction_label);
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.EXTRACT_SUPERTYPE_ACTION);
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
                    IFile file = fFiles[i];
                    String filename = file.getName();                    
                    InputStream in = file.getContents();
                    
                    boolean isFixedForm = false;
                    IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(filename);
                    if (contentType != null
                    		&& contentType.getId().equals(FortranCorePlugin.FIXED_FORM_CONTENT_TYPE))
                    {
                    	isFixedForm = true;
                    }
                    
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
    	
		final RefactoringSaveHelper saveHelper= new RefactoringSaveHelper();
		if (!saveHelper.saveEditors(getShell())) return;
		
		IFile[] files = getSelectedFiles(selection);
    	for (int i = 0; i < files.length; i++) {
    		try {
    			//TODO - put up dialog box to save file (if dirty) or cancel

                IFile file = files[i];
                String filename = file.getName();                    
                InputStream in = file.getContents();
                
                boolean isFixedForm = false;
                IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(filename);
                if (contentType != null
                		&& contentType.getId().equals(FortranCorePlugin.FIXED_FORM_CONTENT_TYPE))
                {
                	isFixedForm = true;
                }
                
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
    		TextChanges changes = new TextChanges(monitor, file);
    		
    	   	for (int i = 0; i < changeList.length; i++) {
    	   		changes.apply(changeList[i]);
        	}
    	   	changes.commit();
    	   	
    	} catch (Exception ex) {
    		System.out.println(ex);
    	}
    	
     }

}
