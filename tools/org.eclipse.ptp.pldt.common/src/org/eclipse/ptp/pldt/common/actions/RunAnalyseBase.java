/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation.
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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.common.ArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.IDs;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.AnalysisUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
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
public abstract class RunAnalyseBase implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	private static final boolean traceOn = false;

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
	public RunAnalyseBase(String name, ArtifactMarkingVisitor visitor, String markerID) {
		this.name = name;
		this.visitor = visitor;
		this.markerID = markerID;
	}

	/**
	 * the current selection is cached here
	 */
	private IStructuredSelection selection;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (traceOn)
			System.out.println("RunAnalyseBase.setActivePart()...");
		shell = targetPart.getSite().getShell();
	}

	/**
	 * Handle the action "Run Analysis" on a node (project, folder, or file).
	 * Descends to all child notes, collecting artifacts on each.
	 * 
	 */

	public void run(IAction action) {
		if (traceOn)
			System.out.println("RunAnalyseBase.run()...");

		cancelledByUser = false;
		err = false;
		cumulativeArtifacts = 0;
		readPreferences();
		if (traceOn)
			System.out.println("RunAnalyseBase.run() action id=" + action.getId());

		final int indent = 0;
		if ((selection == null) || selection.isEmpty()) {
			MessageDialog.openWarning(null, "No files selected for analysis.",
					"Please select a source file or container (folder or project) to analyze.");

			return;
		} else {
			// get preference for include paths
			final List includes = getIncludePath();
			if (areIncludePathsNeeded() && includes.isEmpty()) {
				System.out.println("RunAnalyseBase.run(), no include paths found.");
				MessageDialog.openWarning(shell, name + " Include Paths Not Found", "Please first specify the " + name
						+ " include paths in the Preferences page.");

			} else {

				// batch ws modifications *and* report progress
				WorkspaceModifyOperation wmo = new WorkspaceModifyOperation() {
					@Override
					protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
							InterruptedException {
						err = runResources(monitor, indent, includes);
					}
				};
				ProgressMonitorDialog pmdialog = new ProgressMonitorDialog(shell);
				try {
					pmdialog.run(true, true, wmo);  // fork=true; if false, not cancelable
					
				} catch (InvocationTargetException e) {
					err = true;
					System.out.println("Error running analysis: ITE " + e.getMessage());
					System.out.println("  cause: " + e.getCause()+" - "+e.getCause().getMessage());
					Throwable th=e.getCause();
					th.printStackTrace();
					} 
				catch (InterruptedException e) {
					cancelledByUser = true;
				}

			}// end else
		}
		if (traceOn)
			System.out.println("RunAnalyseBase: retd from run iterator, err=" + err);
		String artsFound = "\nNumber of " + name + " Artifacts found: " + cumulativeArtifacts;
		if (cancelledByUser) {
			MessageDialog.openInformation(null, "Partial Analysis Complete.",
					"Partial Analysis complete.  Cancelled by User." + artsFound);
		} else {
			String msg = "***Analysis is complete.";
			if (!err) {
				String key = IDs.SHOW_ANALYSIS_CONFIRMATION;
				IPreferenceStore pf = CommonPlugin.getDefault().getPreferenceStore();
				boolean showDialog = pf.getBoolean(IDs.SHOW_ANALYSIS_CONFIRMATION);
				if (showDialog) {
					String title = "Analysis complete.";
					String sMsg = cumulativeArtifacts + " " + name + " Artifacts found";
					String togMsg = "Don't show me this again";
					MessageDialogWithToggle.openInformation(shell, title, sMsg, togMsg, false, pf, key);
					showStatusMessage(sMsg, "RunAnalyseBase.run()");
				}
				activateProblemsView();
				activateArtifactView();
			} else { // error occurred
				showStatusMessage(msg, "RunAnalyseBase.run() error");
				msg = "Analysis completed with errors";
				MessageDialog.openError(null, "Analysis completed with errors", msg + artsFound);
			}
		}

	}

	/**
	 * Run the analysis on the current selection (file, container, or multiple-selection)
	 * @param monitor
	 *            progress monitor on which to report progress.
	 * @param indent
	 *            indent amount, in number of spaces, used only for debug
	 *            printing.
	 * @param includes
	 * @return true if any errors were found.
	 * @throws InterruptedException
	 */
	protected boolean runResources(IProgressMonitor monitor, int indent, List includes) throws InterruptedException {
		boolean foundError = false;
		// First, count files so we know how much work to do.
		// note this is number of files of any type, not necessarily number of
		// files that will be anlayzed.
		int count = countFilesSelected();

		monitor.beginTask("Analysis", count);
		// Get elements of a possible multiple selection
		Iterator iter = this.selection.iterator();
		while (iter.hasNext()) {
			if(monitor.isCanceled()) {
				// this is usually caught here while processing multiple-selection of files
				throw new InterruptedException();
			}
			Object obj = (Object) iter.next();
			// It can be a Project, Folder, File, etc...
			if (obj instanceof IAdaptable) {
				final IResource res = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
				if (res != null) {
					boolean err = runResource(monitor, res, indent, includes);
					foundError = foundError | err;
				}
			}
		}
		monitor.done();
		return foundError;
	}

	/**
	 * Counts the number of files in the selection (leaf nodes only - Files -
	 * not the directories/containers) <br>
	 * Note that this makes no distinction about what type of files.
	 * 
	 * @return number of files
	 */
	protected int countFilesSelected() {
		int count = 0;
		// Get elements of a possible multiple selection
		Iterator iter = this.selection.iterator();
		while (iter.hasNext()) {
			Object obj = (Object) iter.next();
			// It can be a Project, Folder, File, etc...
			if (obj instanceof IAdaptable) {
				final IResource res = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
				count = count + countFiles(res);
			}
		}
		//System.out.println("number of files: " + count);
		return count;
	}

	/**
	 * Count the number of files in this resource (file or container).
	 * 
	 * @param res
	 * @return
	 */
	protected int countFiles(IResource res) {
		if (res instanceof IFile) {
			return 1;
		} else if (res instanceof IContainer) {
			int count = 0;

			try {
				IResource[] kids = ((IContainer) res).members();
				for (int i = 0; i < kids.length; i++) {
					IResource child = kids[i];
					count = count + countFiles(child);
				}
				return count;
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	abstract protected void activateArtifactView();

	abstract protected void activateProblemsView();

	/**
	 * Get the include path. Subclass should override this method.
	 * 
	 * @return
	 */
	abstract protected List getIncludePath();

	/**
	 * Show something in the status line; this is used when we don't have easy
	 * access to the view for getting the StatusLineManager.
	 * 
	 * @param message
	 * @param debugMessage
	 */
	private void showStatusMessage(String message, String debugMessage) {
		if (false) {
			message += " - ";
			message += debugMessage;
		}
		IWorkbenchWindow ww = CommonPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = ww.getActivePage();
		IViewReference[] viewRefs = page.getViewReferences();
		for (int j = 0; j < viewRefs.length; j++) {
			IViewReference reference = viewRefs[j];
			IViewPart vp = reference.getView(false);
			if (vp != null)
				vp.getViewSite().getActionBars().getStatusLineManager().setMessage(message);
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
	 * @param resource
	 *            the resource
	 * @param indent
	 *            number of levels of nesting/recursion for prettyprinting
	 * @param includes
	 *            contains header files include paths from the Preference page
	 * @return true if an error was encountered
	 * @throws InterruptedException 
	 */
	protected boolean runResource(IProgressMonitor monitor, IResource resource, int indent, List includes) throws InterruptedException {
		indent += INDENT_INCR;
		ScanReturn results;
		boolean foundError = false;

		if (!monitor.isCanceled()) {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				String filename = file.getName();
				if (AnalysisUtil.validForAnalysis(filename)) {
					if (traceOn)
						println(getSpaces(indent) + "file: " + filename);
					results = analyse(monitor, file, includes);

					foundError = foundError || results == null || results.wasError();
					if (foundError) {
						int stopHere = 0;
						System.out.println("found error on " + file.getName() + " " + stopHere);
					}
					if (traceOn)
						println("******** RunAnalyseBase, analysis complete; ScanReturn=" + results);
					if (results != null) {
						// apply markers to the file
						processResults(results, resource);
					}

				} else {
					if (traceOn)
						println(getSpaces(indent) + "---omit: not valid file: " + filename);
				}
				return foundError;
			}

			// container could be project or folder
			else if (resource instanceof IContainer) {
				IContainer container = (IContainer) resource;
				try {
					IResource[] mems = container.members();
					for (int i = 0; i < mems.length; i++) {
						if(monitor.isCanceled()) {
							//this is usually hit while processing normal analysis of e.g. container
							throw new InterruptedException();
						}
						boolean err = runResource(monitor, mems[i], indent, includes);
						foundError = foundError || err;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}

			}
		} // end if !monitor.isCanceled()
		else {
			String name = "";
			if (resource instanceof IResource) {
				IResource res = (IResource) resource;
				// name=res.getName(); // simple filename only, no path info
				IPath path = res.getProjectRelativePath();
				name = path.toString();
			}
			System.out.println("Cancelled by User, aborting analysis on subsequent files... " + name);
			throw new InterruptedException();
		}

		return foundError;
	}

	protected void processResults(ScanReturn results, IResource resource) {
		List artifacts = results.getArtifactList();
		visitor.visitFile(resource, artifacts);
	}

	/**
	 * Remember what the selected object was. <br>
	 * If selection is empty, it's probably from another view, so don't change
	 * what we consider the current selection from this view.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (!selection.isEmpty()) {
				this.selection = (IStructuredSelection) selection;
			}
		}
	}

	public ScanReturn analyse(IProgressMonitor monitor, IFile file, List /*
																			 * of
																			 * String
																			 */includes) {
		if (traceOn)
			println("RunAnalyseBase.analyse()...");
		ScanReturn nr = null;
		String errMsg = null;

		monitor.subTask("Starting Analysis...");

		// fully qualified file location
		String rawPath = file.getRawLocation().toString();
		if (traceOn)
			println("RunAnalyseBase:              file = " + file.getLocation());

		monitor.subTask(" on " + rawPath);
		ScanReturn scanReturn = doArtifactAnalysis(file, includes);
		monitor.worked(1);
		if (traceOn)
			println("Artifact analysis complete...");
		int numArtifacts = scanReturn.getArtifactList().size();
		cumulativeArtifacts = cumulativeArtifacts + numArtifacts;

		if (traceOn)
			System.out.println("Artifacts found for " + file.getProjectRelativePath() + ": " + numArtifacts);
		if (traceOn)
			System.out.println("   Total # found: " + cumulativeArtifacts);

		if (scanReturn == null) {
			System.out.println("ScanReturn result is NULL.  No results for " + file.getProjectRelativePath());
			errMsg = "Error: No results were returned from analysis of " + file.getProjectRelativePath();
			MessageDialog.openError(shell, "Error in Analysis", errMsg);
		} else {
			if (traceOn)
				System.out.println("RunAnalyzeBase: ScanReturn received for " + file.getName());
			if (traceOn)
				System.out.println("   Analysis err? = " + nr.wasError());
		}

		if (scanReturn != null) {
			boolean wasError = scanReturn.wasError();
			if (traceOn)
				System.out.println("error occurred =" + wasError);
			if (wasError) {
				System.out.println("RunAnalyseBase.analyse...Error...");
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
	private static final String SPACES = "                                                                                            ";

	private String getSpaces(int indent) {
		String indentSpace = "";
		try {
			indentSpace = SPACES.substring(0, indent);
		} catch (StringIndexOutOfBoundsException e) {
			println("RunAnalyseBase: Nesting level " + indent + " exceeds print indent; INCR at each level is "
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
		String preface = "";
		if (obj instanceof IFolder)
			preface = "Contents of Folder: ";
		else if (obj instanceof IProject)
			preface = "Contents of Project: ";
		else if (obj instanceof IFile)
			preface = "Source file: ";
		String res = preface + ((IResource) obj).getName();
		return res;
	}

	/**
	 * Returns artifact analysis for file. <br>
	 * Derived class should override this method.
	 * 
	 * @param file
	 * @param includes
	 *            header files include paths
	 * @return
	 */
	public abstract ScanReturn doArtifactAnalysis(final IFile file, final List includes);

	/**
	 * returns true if include paths must be set for this implementation. For
	 * example, C needs include paths, but Fortran doesn't.
	 */
	public boolean areIncludePathsNeeded() {
		return true;
	}

}