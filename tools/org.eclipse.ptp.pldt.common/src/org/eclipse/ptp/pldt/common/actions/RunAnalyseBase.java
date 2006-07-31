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
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.common.ArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.IDs;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * 
 * RunAnalyseBase - run analysis to create generic artifact markers. Can be
 * extended to run other framework analysis, such as LAPI.<br>
 * The analysis is done in the doArtifactAnalysis() method
 * 
 * @author Beth Tibbitts
 * 
 * IObjectActionDelegate enables popup menu selection <br>
 * IWindowActionDelegate enables toolbar(or menu) selection
 */
public abstract class RunAnalyseBase implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	private static final boolean traceOn = false;

	// indent amount for each level of nesting
	private static final int INDENT_INCR = 2;

	/**
	 * run analysis? set false for debugging UI only (value will be found from
	 * preferences dialog)
	 */
	private boolean runAnalysis = true;

	protected boolean forceEcho = false;
	protected IWorkbenchWindow window;
	protected boolean cancelledByUser = false;
	protected int cumulativeArtifacts = 0;
	protected String name;// = "MPI";
	protected ArtifactMarkingVisitor visitor;
	protected String markerID;
	private boolean keepErr = false;

	/**
	 * Constructor for the "Run Analysis" action
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
	}

	/**
	 * Handle the action "Run Analysis" on a node (project, folder, or file).
	 * Descends to all child notes, collecting artifacts on each.
	 * 
	 */

	public void run(IAction action) {
		if (traceOn)
			System.out.println("RunAnalyseBase.run()...");

		cancelledByUser = false; // do we need to reset this? or is each
		// invokation a new obj?
		cumulativeArtifacts = 0;
		// Determine preference for running analysis (could be false for UI-only
		// testing)
		readPreferences();
		if (traceOn)
			System.out.println("RunAnalyseBase.run() action id=" + action.getId());

		final int indent = 0;
		String whatToRun = "";
		boolean ranIt = false;
		boolean haveConfirmedWithUser = false;
		if ((selection == null) || selection.isEmpty()) {
			MessageDialog.openWarning(null, "No files selected for analysis.",
					"Please select a source file or container (folder or project) to analyze.");
			return;
		} else {
			// get preference for include paths
			final List includes = getIncludePath();
			if (areIncludePathsNeeded() && includes.isEmpty()) {
				System.out.println("RunAnalyseBase.run(), no include paths found.");
				Shell shell = this.window.getShell();
				MessageDialog.openWarning(shell, name + " Include Paths Not Found", "Please first specify the " + name
						+ " include paths in the Preferences page.");

			} else {
				Iterator iter = this.selection.iterator();
				while (iter.hasNext()) {
					Object obj = (Object) iter.next();
					// It can be a Project, Folder, File, etc...
					if (obj instanceof IAdaptable) {
						final IResource res = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
						if (res != null) {
							whatToRun = getPrefacedName(res);
							String multipleMsg = (this.selection.size() > 1) ? "\n(Plus other files selected)" : "";
							String msg = "Run Artifact Analysis on " + whatToRun + "?" + multipleMsg;
							// Confirm with user only prior to first run.
							boolean runIt = haveConfirmedWithUser;

							runIt = true; // skip dialog, it's annoying.
							// Put in Preferences?
							if (!runIt) {
								runIt = MessageDialog.openQuestion(null, "Run Analysis", msg);
								if (!runIt)
									return; // don't run
							}
							haveConfirmedWithUser = true;
							ranIt = true;
							try {
								// put this in a Runnable to reduce Resource
								// Change events
								ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
									public void run(IProgressMonitor monitor) throws CoreException {
										boolean err = runResource(res, indent, includes);
										keepErr = err;
									}
								}, null); // null monitor
							} catch (CoreException e1) {
								e1.printStackTrace();
							}
							// refresh parent so that any resource changes will
							// show
							// (e.g. if the analysis changed any files. For
							// future use.)
							IContainer parent = res.getParent();
							try {
								parent.refreshLocal(IResource.DEPTH_INFINITE, null);
							} catch (CoreException e) {
								System.out.println("Exception refreshing after analysis");
								e.printStackTrace();
							}
						}
					}
				} // end while
			}
		}
		if (traceOn)
			System.out.println("RunAnalyseBase: retd from run iterator for " + whatToRun + ", ranIt=" + ranIt);
		if (ranIt) {
			String pisFound = "\nNumber of " + name + " Artifacts found: " + cumulativeArtifacts;
			if (cancelledByUser) {
				MessageDialog.openInformation(null, "Partial Analysis Complete.",
						"Partial Analysis complete.  Cancelled by User." + pisFound);
			} else {
				String msg = "***Analysis is complete for:  " + whatToRun;

				if (!keepErr) {
					String sMsg = cumulativeArtifacts + " " + name + " Artifacts found in " + whatToRun;
					showStatusMessage(sMsg, "RunAnalyseBase.run()");
					MessageDialog.openInformation(null, "Analysis complete.", msg + pisFound);
					showStatusMessage(sMsg, "RunAnalyseBase.run()"); // repeat;
					activateProblemsView();
					activateArtifactView();
				} else {
					showStatusMessage(msg, "RunAnalyseBase.run() error");
					msg = "Analysis for: " + whatToRun + " completed with errors. See Tasks View.";
					MessageDialog.openError(null, "Analysis completed with errors", msg + pisFound);
				}
			}
		}
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
	 * Including: set run analysis preference could be set to 'false' for
	 * UI-only testing that doesn't call analysis at each leaf node (source
	 * file)
	 * 
	 */
	protected void readPreferences() {
		Preferences pref = CommonPlugin.getDefault().getPluginPreferences();
		runAnalysis = pref.getBoolean(IDs.P_RUN_ANALYSIS);
		if (traceOn)
			println("RunAnalyseBase()... runAnalysis=" + runAnalysis);

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
	 * @return
	 */
	protected boolean runResource(IResource resource, int indent, List /*
																		 * of
																		 * String
																		 */includes) {
		// boolean ok = true;
		indent += INDENT_INCR;
		ScanReturn results;
		boolean foundError = false;
		if (!cancelledByUser) {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				String filename = file.getName();
				// if (AnalysisUtil.validForAnalysis(filename)) {
				if (true) {
					if (traceOn)
						println(getSpaces(indent) + "file: " + filename);
					runAnalysis = true;
					if (runAnalysis) {
						results = analyse(file, includes); // jx: changed to
						// file, instead of
						// resource
						foundError = foundError || results == null || results.wasError();
						if (traceOn)
							println("******** RunAnalyseBase, analysis complete; ScanReturn=" + results);
						if (results != null) {
							// apply markers
							// Use artifact objects directly from
							// ScanReturn
							// obj
							processResults(results, resource);
							// List artifacts = results.getArtifactList();
							// visitor.visitFile(resource, artifacts);
							// set the metrics in the metrics view
							// setMetrics(file, results);
						}
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
						boolean err = runResource(mems[i], indent, includes);
						foundError = foundError || err;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}

			}
		} // end if !cancelledByUser
		else {
			String name = "";
			if (resource instanceof IResource) {
				IResource res = (IResource) resource;
				// name=res.getName(); // simple filename only, no path info
				IPath path = res.getProjectRelativePath();
				name = path.toString();
			}
			System.out.println("Cancelled by User, aborting analysis on subsequent files... " + name);
		}

		return foundError;
	}

	protected void processResults(ScanReturn results, IResource resource) {
		List artifacts = results.getArtifactList();
		visitor.visitFile(resource, artifacts);
	}

	/**
	 * Remember what the selected object was
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		}
	}

	public ScanReturn analyse(IFile file, List /* of String */includes) {
		if (traceOn)
			println("RunAnalyseBase.analyse()...");
		ScanReturn nr = null;
		String errMsg = null;
		// ============= Run with Progress Monitor
		AnalyseWithProgress op = null;
		try {
			op = new AnalyseWithProgress(file, includes);
			Shell activeShell = this.window.getShell();
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(activeShell);
			pmd.run(true, true, op);
			nr = op.getResults();
		} catch (InvocationTargetException e) {
			errMsg = "RunAnalyseBase: InvocationTargetException in analysis for " + file.getLocation();
			System.out.println(errMsg);
			e.printStackTrace();
			Throwable origExcp = e.getTargetException();
			System.out.println("Original target exception is: " + origExcp);
			System.out.println("   stack trace printed to stdout.");
			origExcp.printStackTrace();
			Throwable te = origExcp;
			// System.err.println("---Original exception was: ");
			// te.printStackTrace();
			// Try to print some info to the Log file to pinpoint the error.
			StackTraceElement[] ste = te.getStackTrace();
			System.out.println("   Stack trace:");
			for (int j = 0; j < ste.length; j++) {
				StackTraceElement el = ste[j];
				System.out.println("     " + (j + 1) + " " + el.toString());
			}
		}

		catch (InterruptedException e) {
			System.out.println("RunAnalyseBase: Interrupted Analysis for " + file.getProjectRelativePath());
			System.out.println("   reason: " + e.getMessage());
			// note: user hitting 'cancel' on progress monitor will land here.
			// need graceful exit. Get results from previous run, which probably
			// finished.
			if (op != null) {
				nr = op.getResults();
			}
			cancelledByUser = true;
			// e.printStackTrace();
		}
		// =====================
		if (nr == null) {
			System.out.println("ScanReturn result is NULL.  No results for " + file.getProjectRelativePath());
			Shell shell = Display.getCurrent().getActiveShell();
			errMsg = "Error: No results were returned from analysis of " + file.getProjectRelativePath();
			MessageDialog.openError(shell, "Error in Analysis", errMsg);
		} else {
			if (traceOn)
				System.out.println("RunAnalyzeBase: ScanReturn received for " + file.getName());
			if (traceOn)
				System.out.println("   Analysis err? = " + nr.wasError());
		}

		if (nr != null) {
			boolean wasError = nr.wasError();
			if (traceOn)
				System.out.println("error occurred =" + wasError);
			if (wasError) {
				System.out.println("RunAnalyseBase.analyse...Error...");
			}
		}
		return nr;
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
	 * Class wrapper for running the analysis, so we can put up a progress
	 * monitor while it's running <br>
	 * Note: the analysis runs in the non-UI thread, so callbacks must be forced
	 * back on the UI thread
	 * 
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 * @author Beth Tibbitts
	 * 
	 * 
	 * 
	 */
	public class AnalyseWithProgress implements IRunnableWithProgress {
		private IFile file_;

		private ScanReturn scanReturn_;

		List /* of String */includes_;

		/**
		 * Constructor
		 * 
		 * @param file
		 *            the File to be analyzed
		 * @param includes
		 *            include paths from the Preference page.
		 */
		public AnalyseWithProgress(IFile file, List /* of String */includes) {
			file_ = file;
			includes_ = includes;
			// for analysis
		}

		/**
		 * run the analysis inside a progress monitor.
		 */
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			// subtasks probably happen too fast to see, but update the monitor
			// anyway...
			monitor.subTask("Starting Analysis...");

			// fully qualified file location
			String rawPath = file_.getRawLocation().toString();
			if (traceOn)
				println("RunAnalyseBase:              file = " + file_.getLocation());

			monitor.subTask("Running analysis on " + rawPath);
			// CancelChecker checker = new CancelChecker(monitor);
			if (monitor.isCanceled()) {
				// checker.setCancelRequested(true);
				System.out.println("RunAnalyseBase, progressMonitor canceled...before");
				throw new InterruptedException("Analysis canceled by user");
			}

			scanReturn_ = doArtifactAnalysis(file_, includes_);

			if (monitor.isCanceled()) {
				// checker.setCancelRequested(true);
				System.out.println("RunAnalyseBase, progressMonitor canceled...after");
				throw new InterruptedException("Analysis canceled by user");
			}

			monitor.subTask("Analysis done.");
			// if (traceOn)
			// println("ScanReturn: errorcode=" + scanReturn_.errorCode_);
			if (traceOn)
				println("Artifact analysis complete...");
			int numArtifacts = scanReturn_.getArtifactList().size();
			cumulativeArtifacts = cumulativeArtifacts + numArtifacts;

			if (traceOn)
				System.out.println("Artifacts found for " + file_.getProjectRelativePath() + ": " + numArtifacts);
			if (traceOn)
				System.out.println("   Total # found: " + cumulativeArtifacts);

			monitor.subTask("All done.");
			monitor.done();
		}

		public ScanReturn getResults() {
			return scanReturn_;
		}

	}// end AnalyseWithProgress

	/**
	 * Returns artifact analysis for file. <br>
	 * Derived class should override this method.
	 * 
	 * @param file
	 * @param includes header files include paths
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