/**********************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.IDs;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.messages.Messages;
import org.eclipse.ptp.pldt.common.util.AnalysisUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * 
 * RunAnalyseBase - run analysis to create generic artifact markers. <br>
 * The analysis is done in the doArtifactAnalysis() method
 * 
 * @author Beth Tibbitts
 * 
 * IObjectActionDelegate enables popup menu selection <br>
 * IWindowActionDelegate enables toolbar(or menu) selection
 */

public abstract class RunAnalyseHandlerBase extends RunAnalyseHandler {
	protected static /*final*/ boolean traceOn = false;

	/**
	 * indent amount for each level of nesting; useful when printing debug
	 * statements
	 */
	private static final int INDENT_INCR = 2;

	protected boolean forceEcho = false;

	protected IWorkbenchWindow window;

	protected boolean cancelledByUser = false;

	protected int cumulativeArtifacts = 0;

	/** the type of artifact e.g. "MPI" or "OpenMP" */
	protected String name;// = "MPI";

	protected ArtifactMarkingVisitor visitor;

	protected String markerID;

	private boolean err = false;

	protected Shell shell;

	/**
	 * Constructor for the "Run Analysis" action
	 * 
	 * @param name
	 *            the type of artifact e.g. "MPI" or "OpenMP"
	 * @param visitor
	 *            the visitor that will put the markers on the source files
	 * @param markerID
	 *            marker ID
	 */
	public RunAnalyseHandlerBase(String name, ArtifactMarkingVisitor visitor,
			String markerID) {
		this.name = name;
		this.visitor = visitor;
		this.markerID = markerID;
		
		traceOn=CommonPlugin.getTraceOn();
		if(traceOn)System.out.println("RunAnalyseBase.ctor: traceOn="+traceOn); //$NON-NLS-1$
		
		// get the navigator, project explorer, c/C++ projects view, etc
		//need to get selectionChanged on that, to cache the most recent selection there,
		// otherwise HanderUtil will tell us latest selection including ones we don't want
		
		
		
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (traceOn)
			System.out.println("RunAnalyseBase.setActivePart()..."); //$NON-NLS-1$
		shell = targetPart.getSite().getShell();
	}

	/**
	 * Do the "Run Analysis" on a resource (project, folder, or file).
	 * Descends to all child nodes, collecting artifacts on each.
	 * 
	 */

	public void run() {
		if (traceOn)
			System.out.println("RunAnalyseHandlerBase.run()..."); //$NON-NLS-1$

		cancelledByUser = false;
		err = false;
		cumulativeArtifacts = 0;
		readPreferences();

		final int indent = 0;
 
		if ((selection == null) || selection.isEmpty()) {
			MessageDialog
					.openWarning(null, Messages.RunAnalyseHandlerBase_no_files_selected,
							Messages.RunAnalyseHandlerBase_please_select);

			return;
		} else {
			// get preference for include paths
			final List<String> includes = getIncludePath();
			if (areIncludePathsNeeded() && includes.isEmpty()) {
				//System.out.println("RunAnalyseHandlerBase.run(), no include paths found.");
				MessageDialog.openWarning(shell, name
						+ Messages.RunAnalyseHandlerBase_include_paths_not_found,
						Messages.RunAnalyseHandlerBase_please_first_specify + name
								+ Messages.RunAnalyseHandlerBase_incl_paths_in_pref_page);

			} else {

				// batch ws modifications *and* report progress
				WorkspaceModifyOperation wmo = new WorkspaceModifyOperation() {
					@Override
					protected void execute(IProgressMonitor monitor)
							throws CoreException, InvocationTargetException,
							InterruptedException {
						err = runResources(monitor, indent, includes);
					}
				};
				ProgressMonitorDialog pmdialog = new ProgressMonitorDialog(
						shell);
				try {
					pmdialog.run(true, true, wmo); // fork=true; if false, not
													// cancelable

				} catch (InvocationTargetException e) {
					err = true;
					Throwable cause = e.getCause();
					System.out.println("Error running analysis: ITE: " //$NON-NLS-1$
							+ e.getMessage());
					System.out.println("  cause: " + cause + " - " //$NON-NLS-1$ //$NON-NLS-2$
							+ cause.getMessage());
					
					cause.printStackTrace();
				} catch (InterruptedException e) {
					cancelledByUser = true;
				}

			}// end else
		}
		if (traceOn)
			System.out.println("RunAnalyseBase: retd from run iterator, err=" //$NON-NLS-1$
					+ err);
		String artsFound = "\nNumber of " + name + " Artifacts found: " //$NON-NLS-1$ //$NON-NLS-2$
				+ cumulativeArtifacts;
		if (cancelledByUser) {
			MessageDialog.openInformation(null, Messages.RunAnalyseHandlerBase_partial_analysis_complete,
					Messages.RunAnalyseHandlerBase_15
							+ artsFound);
		} else {
			String msg = Messages.RunAnalyseHandlerBase_cancelled_by_user;
			if (!err) {
				String key = IDs.SHOW_ANALYSIS_CONFIRMATION;
				IPreferenceStore pf = CommonPlugin.getDefault()
						.getPreferenceStore();
				boolean showDialog = pf
						.getBoolean(IDs.SHOW_ANALYSIS_CONFIRMATION);
				if (showDialog) {
					String title = Messages.RunAnalyseHandlerBase_analysis_complete;
					StringBuffer sMsg = new StringBuffer(cumulativeArtifacts + " " + name //$NON-NLS-1$
							+ Messages.RunAnalyseHandlerBase_artifacts_found);
					// provide some explanation of why perhaps no artifacts were found.
					// Note: should this perhaps be in a "Details" section of the dialog?
					if(cumulativeArtifacts==0) {
						sMsg.append(Messages.RunAnalyseHandlerBase_20).append(name).append(Messages.RunAnalyseHandlerBase_21);
						sMsg.append(name).append(Messages.RunAnalyseHandlerBase_22);
						sMsg.append(Messages.RunAnalyseHandlerBase_23);
					}
					String togMsg = Messages.RunAnalyseHandlerBase_dont_show_this_again;
					MessageDialogWithToggle.openInformation(shell, title, sMsg.toString(),
							togMsg, false, pf, key);
					showStatusMessage(sMsg.toString(), "RunAnalyseBase.run()"); //$NON-NLS-1$
				}
				activateProblemsView();
				activateArtifactView();
			} else { // error occurred
				showStatusMessage(msg, "RunAnalyseBase.run() error"); //$NON-NLS-1$
				msg = Messages.RunAnalyseHandlerBase_27;
				MessageDialog.openError(null, Messages.RunAnalyseHandlerBase_28,
						msg + artsFound);
			}
		}

	}

	/**
	 * Run the analysis on the current selection (file, container, or
	 * multiple-selection)
	 * 
	 * @param monitor
	 *            progress monitor on which to report progress.
	 * @param indent
	 *            indent amount, in number of spaces, used only for debug
	 *            printing.
	 * @param includes
	 * @return true if any errors were found.
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked") // on Iterator
	protected boolean runResources(IProgressMonitor monitor, int indent,
			List<String> includes) throws InterruptedException {
		boolean foundError = false;
		// First, count files so we know how much work to do.
		// note this is number of files of any type, not necessarily number of
		// files that will be analyzed.
		int count = countFilesSelected();

		monitor.beginTask(Messages.RunAnalyseHandlerBase_29, count);
		// Get elements of a possible multiple selection
		Iterator<IStructuredSelection> iter = selection.iterator();
		while (iter.hasNext()) {
			if (monitor.isCanceled()) {
				// this is usually caught here while processing
				// multiple-selection of files
				throw new InterruptedException();
			}
			Object obj = (Object) iter.next();// piece of selection
			// It can be a Project, Folder, File, etc...
			if (obj instanceof IAdaptable) {
				// ICElement covers folders and translationunits
				// If fortran file (*.f*) and Photran not installed, ce is null and we ignore it (first) here.
				final ICElement ce = (ICElement) ((IAdaptable) obj)
						.getAdapter(ICElement.class);// cdt40
				if (ce != null) {
					// cdt40
					// IASTTranslationUnit atu = tu.getAST(); not yet
					boolean err = runResource(monitor, ce, indent, includes);
					if(traceOn)System.out.println("Error (err="+err+")running analysis on "+ce.getResource().getName()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		monitor.done();
		return foundError;
	}

	abstract protected void activateArtifactView();

	/**
	 * If the analysis has an additional view to bring up, override this
	 */
	protected void activateProblemsView(){}

	/**
	 * Get the include path. Subclass should override this method.
	 * 
	 * @return
	 */
	abstract protected List<String> getIncludePath();

	/**
	 * Show something in the status line; this is used when we don't have easy
	 * access to the view for getting the StatusLineManager.
	 * 
	 * @param message
	 * @param debugMessage
	 */
	private void showStatusMessage(String message, String debugMessage) {
		if (false) {
			message += " - "; //$NON-NLS-1$
			message += debugMessage;
		}
		IWorkbenchWindow ww = CommonPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = ww.getActivePage();
		IViewReference[] viewRefs = page.getViewReferences();
		for (int j = 0; j < viewRefs.length; j++) {
			IViewReference reference = viewRefs[j];
			IViewPart vp = reference.getView(false);
			if (vp != null)
				vp.getViewSite().getActionBars().getStatusLineManager()
						.setMessage(message);
		}

	}

	/**
	 * Read preferences
	 * 
	 */
	protected void readPreferences() {
		Preferences pref = CommonPlugin.getDefault().getPluginPreferences();
		forceEcho = pref.getBoolean(IDs.P_ECHO_FORCE);

	}

	/**
	 * Run analysis on a resource (e.g. File or Folder) Will descend to members
	 * of folder
	 * 
	 * @param atu
	 *            the resource
	 * @param indent
	 *            number of levels of nesting/recursion for prettyprinting
	 * @param includes
	 *            contains header files include paths from the Preference page
	 * @return true if an error was encountered
	 * @throws InterruptedException
	 */
	public boolean runResource(IProgressMonitor monitor, ICElement ce,
			int indent, List<String> includes) throws InterruptedException {
		indent += INDENT_INCR;
		ScanReturn results;
		boolean foundError = false;

		if (!monitor.isCanceled()) {
			if (ce instanceof ITranslationUnit) {
				IResource res = ce.getResource(); // null if not part of C
													// project in ws
				// cdt40: eventually shd be able to deal with just tu;
				// tu.getResource() can always work later...
				if (res instanceof IFile) {//shd always be true (but might be null)
					IFile file = (IFile) res;
					String filename = file.getName();
					//String fn2 = ce.getElementName();// shd be filename too
														// cdt40
				    boolean cpp = isCPPproject(ce);
				    //if (AnalysisUtil.validForAnalysis(filename,cpp)) {
				    
					if (validForAnalysis(filename,cpp)) {
						if (traceOn)
							println(getSpaces(indent) + "file: " + filename); //$NON-NLS-1$
						results = analyse(monitor, (ITranslationUnit) ce,
								includes);

						foundError = foundError || results == null
								|| results.wasError();
						if (foundError) {
							int stopHere = 0;
							System.out.println("found error on " //$NON-NLS-1$
									+ file.getName() + " " + stopHere); //$NON-NLS-1$
						}
						if (traceOn)
							println("******** RunAnalyseBase, analysis complete; ScanReturn=" //$NON-NLS-1$
									+ results);
						if (results != null) {
							// apply markers to the file
							processResults(results, file);
						}

					} else {
						if (traceOn)
							println(getSpaces(indent)
									+ "---omit: not valid file: " + filename); //$NON-NLS-1$
					}
					return foundError;
				}
			}

			else if (ce instanceof ICContainer) {
				ICContainer container = (ICContainer) ce;
				try {
					ICElement[] mems = container.getChildren();
					for (int i = 0; i < mems.length; i++) {
						if (monitor.isCanceled()) {
							// this is usually hit while processing normal
							// analysis of e.g. container
							throw new InterruptedException();
						}
						boolean err = runResource(monitor, mems[i], indent,
								includes);
						foundError = foundError || err;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
			}
			else if (ce instanceof ICProject) {
				ICProject proj = (ICProject) ce;
				try {
					ICElement[] mems = proj.getChildren();
					for (int i = 0; i < mems.length; i++) {
						if (monitor.isCanceled()) {
							// this is usually hit while processing normal
							// analysis of e.g. container
							throw new InterruptedException();
						}
						boolean err = runResource(monitor, mems[i], indent,
								includes);
						foundError = foundError || err;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}			
			}
			// container could be project or folder		
		} // end if !monitor.isCanceled()
		else {
			String name = ""; //$NON-NLS-1$
			//cdt40
				name = ce.getElementName();
				//String p=ce.getPath().toString();
			 
			System.out.println("Cancelled by User, aborting analysis on subsequent files... " //$NON-NLS-1$
							+ name);
			throw new InterruptedException();
		}

		return foundError;
	}
/**
 * Determine if the project is a C++ project
 * @param ce the ICElement representing a file 
 * @return
 */
  protected boolean isCPPproject(ICElement ce) {
    IProject p = ce.getCProject().getProject();
    try {
      IProjectNature nature = p.getNature("org.eclipse.cdt.core.ccnature"); //$NON-NLS-1$
      if(nature!=null) {
        return true;
      }
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      //e.printStackTrace();
    }
    return false;
  }

	protected void processResults(ScanReturn results, IResource resource) {
		List<Artifact> artifacts = results.getArtifactList();
		visitor.visitFile(resource, artifacts);
	}



	public ScanReturn analyse(IProgressMonitor monitor, ITranslationUnit tu,
			List<String> includes) {
		if (traceOn)
			println("RunAnalyseBase.analyse()..."); //$NON-NLS-1$
		//ScanReturn nr = null;
		String errMsg = null;

		monitor.subTask(Messages.RunAnalyseHandlerBase_42);

		// fully qualified file location
		// String rawPath = tu.getRawLocation().toString();
		String rawPath = tu.getLocation().toString();// cdt40
		if (traceOn)
			println("RunAnalyseBase:              file = " + tu.getLocation()); //$NON-NLS-1$

		monitor.subTask(Messages.RunAnalyseHandlerBase_44 + rawPath);
		// did tu parse w/o errors?  If we can determine that, we can
		// warn user; otherwise OpenMP analysis will, for example, "finish with errors"
		// e.g. if header file can't be found.
		/*
		try {
      IASTProblem[] problems  = CPPVisitor.getProblems(tu.getAST());
      IASTProblem[] problems2 = CVisitor.getProblems(tu.getAST());
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
		*/
		ScanReturn scanReturn = doArtifactAnalysis(tu, includes);
		monitor.worked(1);
		if (traceOn)
			println("Artifact analysis complete..."); //$NON-NLS-1$
		int numArtifacts = scanReturn.getArtifactList().size();
		cumulativeArtifacts = cumulativeArtifacts + numArtifacts;

		if (traceOn)
			System.out.println("Artifacts found for " //$NON-NLS-1$
					+ tu.getResource().getProjectRelativePath() + ": " + numArtifacts); //$NON-NLS-1$
		if (traceOn)
			System.out.println("   Total # found: " + cumulativeArtifacts); //$NON-NLS-1$

		if (scanReturn == null) {
			System.out.println("ScanReturn result is NULL.  No results for " //$NON-NLS-1$
					+ tu.getResource().getProjectRelativePath());
			errMsg = "Error: No results were returned from analysis of " //$NON-NLS-1$
					+ tu.getResource().getProjectRelativePath();
			MessageDialog.openError(shell, "Error in Analysis", errMsg); //$NON-NLS-1$
		} else {
			if (traceOn)
				System.out.println("RunAnalyzeBase: ScanReturn received for " //$NON-NLS-1$
						+ tu.getElementName());
		}

		if (scanReturn != null) {
			boolean wasError = scanReturn.wasError();
			if (traceOn)
				System.out.println("error occurred =" + wasError); //$NON-NLS-1$
			if (wasError) {
				System.out.println("RunAnalyseBase.analyse...Error..."); //$NON-NLS-1$
			}
		}
		return scanReturn;
	}

	/**
	 * return a string of spaces of a certain length
	 * 
	 * @param indent
	 *            the number of spaces to return (used for successively
	 *            indenting debug statements based on depth of nesting)
	 */
	private static final String SPACES = "                                                                                            "; //$NON-NLS-1$

	private String getSpaces(int indent) {
		String indentSpace = ""; //$NON-NLS-1$
		try {
			indentSpace = SPACES.substring(0, indent);
		} catch (StringIndexOutOfBoundsException e) {
			println("RunAnalyseBase: Nesting level " + indent //$NON-NLS-1$
					+ " exceeds print indent; INCR at each level is " //$NON-NLS-1$
					+ INDENT_INCR);
			// e.printStackTrace();
		}
		return indentSpace;
	}

	/**
	 * print to log
	 * 
	 * @param str
	 */
	void println(String str) {
		System.out.println(str);
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose implemented for toolbar
	 *      enablement of this action
	 */
	public void dispose() {
	}

	/**
	 * Cache the window object to be able to provide parent shell for the
	 * message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init implemented for toolbar
	 *      enablement of this action NOTE: window object will thus be null for
	 *      context menu use!! so...we are not using this, using
	 *      Display.getCurrent() and Display.getCurrent().getActiveShell();
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Provide a human-readable version of what will be analyzed.
	 * 
	 * @param obj
	 *            the file, folder, or project
	 * @return a string indicating what it is
	 */
	public String getPrefacedName(Object obj) {
		String preface = ""; //$NON-NLS-1$
		if (obj instanceof IFolder)
			preface = Messages.RunAnalyseHandlerBase_60;
		else if (obj instanceof IProject)
			preface = Messages.RunAnalyseHandlerBase_61;
		else if (obj instanceof IFile)
			preface = Messages.RunAnalyseHandlerBase_62;
		String res = preface + ((IResource) obj).getName();
		return res;
	}

	/**
	 * Returns artifact analysis for file. <br>
	 * Derived class should override this method.
	 * 
	 * @param tu
	 * @param includes
	 *            header files include paths
	 * @return
	 */
	public abstract ScanReturn doArtifactAnalysis(final ITranslationUnit tu,
			final List<String> includes);

	/**
	 * returns true if include paths must be set for this implementation. For
	 * example, C needs include paths, but Fortran doesn't.
	 */
	public boolean areIncludePathsNeeded() {
		return true;
	}
	
	/**
	 * Implemented for Handler; this replaces run() which is for actions.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(traceOn)System.out.println("RunAnalyseHandlerBase.execute()..."); //$NON-NLS-1$
		getSelection(event);
		if(traceOn)System.out.println("selection: "+selection); //$NON-NLS-1$
		run();
		AnalysisDropdownHandler.setLastHandledAnalysis(this, selection);
	    return null;
	}
	
	/**
	 * Default determination of if a given filename is valid for our artifact analysis
	 * @param filename
	 * @param isCPP  is the project a C++ project or not
	 * @return
	 */
	protected boolean validForAnalysis(String filename, boolean isCPP) {
		return AnalysisUtil.validForAnalysis(filename,isCPP);
	}

  /**
   * Get AST from index, not full tu
   * @param tu translation unit from which to get the AST
   * @return
   */
  protected IASTTranslationUnit getAST(ITranslationUnit tu) {
    IIndex index;
    try {
      index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
      IASTTranslationUnit ast = tu.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
      //IASTTranslationUnit ast = tu.getAST(index, 0);
      if(traceOn)System.out.println("    getAST(index,AST_SKIP_ALL_HEADERS)"); //$NON-NLS-1$
      
      return ast;
    } catch (CoreException e) {
      CommonPlugin.log(IStatus.ERROR,"RunAnalyseMPICommandHandler.getAST():Error getting AST (from index) for project "+tu.getCProject()); //$NON-NLS-1$
      return null;
    }
    
  }


}