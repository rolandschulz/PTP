/*******************************************************************************
 * Copyright (c) 2009, 2011 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.gem.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.gem.GemPlugin;
import org.eclipse.ptp.gem.messages.Messages;
import org.eclipse.ptp.gem.preferences.PreferenceConstants;
import org.eclipse.ptp.gem.views.GemAnalyzer;
import org.eclipse.ptp.gem.views.GemBrowser;
import org.eclipse.ptp.gem.views.GemConsole;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class GemUtilities {

	public static enum TaskStatus {
		IDLE,
		ACTIVE,
		ABORTED;
	}

	private static Process process;
	private static GemAnalyzer analyzer;
	private static GemBrowser browser;
	private static GemConsole console;
	protected static TaskStatus taskStatus;
	private static boolean doCompile;
	private static boolean doVerify;
	private static String consoleStdOutMessage;
	private static String consoleStdErrMessage;
	private static String outputSameMessage;
	private static String outputSameDetails;
	private static IFile gemInputFile;
	private static IFile gemLogfile;

	// This thread exists to update SWT components belonging to the UI thread.
	private final static Thread updateGemConsoleThread = new Thread() {
		@Override
		public void run() {
			final IWorkbench wb = PlatformUI.getWorkbench();
			final IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			final IWorkbenchPage page = window.getActivePage();
			GemConsole gemConsole = null;
			IViewPart gemViewPart = null;

			try {
				page.showView(GemConsole.ID);
				gemViewPart = page.findView(GemConsole.ID);
				gemConsole = (GemConsole) gemViewPart;
				gemConsole.writeStdOut(consoleStdOutMessage);
				gemConsole.writeStdErr(consoleStdErrMessage);
			} catch (final Exception e) {
				logExceptionDetail(e);
			}
		}
	};

	/*
	 * Used to cancel the current GEM analysis without killing any processes.
	 */
	private static void cancelAnalysis() {
		analyzer.clear();
		browser.clear();
		console.cancel();
		taskStatus = TaskStatus.IDLE;
	}

	private static void compareOutput(String consoleStdIn) {

		// Create an array that holds the output of each interleaving
		final ArrayList<String> outputs = new ArrayList<String>();
		final Scanner scanner = new Scanner(consoleStdIn);
		int interIndex = -1;

		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			if (line.contains("INTERLEAVING :")) {//$NON-NLS-1$
				outputs.add("");//$NON-NLS-1$
				interIndex++;
				continue;
			}
			outputs.set(interIndex, outputs.get(interIndex).concat("\n" + line));//$NON-NLS-1$
		}

		// Compare each output against first interleaving, report discrepancy
		for (interIndex = 1; interIndex < outputs.size(); interIndex++) {
			if (!outputs.get(0).equals(outputs.get(interIndex))) {
				final StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(Messages.GemUtilities_19);
				stringBuffer.append(outputs.get(0));
				stringBuffer.append("\n"); //$NON-NLS-1$
				stringBuffer.append("\n"); //$NON-NLS-1$
				stringBuffer.append(Messages.GemUtilities_22);
				stringBuffer.append(outputs.get(interIndex));
				outputSameDetails = stringBuffer.toString();

				stringBuffer.setLength(0);
				stringBuffer.append(Messages.GemUtilities_23);
				stringBuffer.append(" "); //$NON-NLS-1$
				stringBuffer.append((interIndex - 1));
				stringBuffer.append(" "); //$NON-NLS-1$
				stringBuffer.append(Messages.GemUtilities_26);
				outputSameMessage = stringBuffer.toString();

				return;
			}
		}

		outputSameMessage = Messages.GemUtilities_27;
		return;
	}

	/**
	 * Runs the Happens Before Viewer on the log file associated with the
	 * specified input source file.
	 * 
	 * @param inputFile
	 *            The resource handle representing the input file.
	 * @return void
	 */
	public static void doHbv(IFile inputFile) {

		// Create our GEM directory to hold the generated log and executable
		final String projectName = inputFile.getProject().getName();
		final IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		// Build up command line String
		final String relativeLogFilePath = getLogfilePath(inputFile);
		final IFile logFile = currentProject.getFile(new Path(relativeLogFilePath));
		refreshProject(currentProject);

		if (!logFile.exists()) {
			showInformationDialog(Messages.GemUtilities_2);
			if (doIspcc(inputFile) != -1) {
				doIsp(inputFile);
			}
		}

		final IFolder gemFolder = currentProject.getFolder(new Path("gem")); //$NON-NLS-1$
		IPath fullLogFilePath = new Path(gemFolder.getLocationURI().getPath());
		fullLogFilePath = fullLogFilePath.append(currentProject.getName()).removeFileExtension().addFileExtension("gem.log"); //$NON-NLS-1$

		// Get the ispui.jar path from the preference store
		final String hbvPath = GemPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.GEM_PREF_HBV_PATH);
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(hbvPath);
		stringBuffer.append(hbvPath == "" ? "" : "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuffer.append("ispUI "); //$NON-NLS-1$
		stringBuffer.append(fullLogFilePath);
		final String hbvCommand = stringBuffer.toString();
		runCommandAsThread(hbvCommand);
	}

	/**
	 * Runs ISP on the specified, profiled executable.
	 * 
	 * @param inputFile
	 *            The resource handle representing the profiled executable.
	 * @return void
	 */
	public static void doIsp(IFile inputFile) {

		// Get all the current preferences
		final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();

		// Create GEM directory to hold the generated log file and executable
		final IProject currentProject = inputFile.getProject();
		gemLogfile = currentProject.getFile(getLogfilePath(inputFile));
		final IFolder gemFolder = currentProject.getFolder(new Path("gem")); //$NON-NLS-1$

		if (!gemFolder.exists()) {
			try {
				gemFolder.create(true, true, null);
				refreshProject(currentProject);
			} catch (final CoreException e) {
				logExceptionDetail(e);
			}
		}

		// Build up the command line String
		IPath executablePath = null;
		if (inputFile.getFileExtension().equals("gem")) { //$NON-NLS-1$
			executablePath = new Path(inputFile.getLocationURI().getPath());
		} else {
			executablePath = new Path(gemFolder.getLocationURI().getPath());
			executablePath = executablePath.append(currentProject.getName()).removeFileExtension().addFileExtension("gem"); //$NON-NLS-1$
		}

		// generate the path for the log file to be created by ISP
		IPath logfilePath = new Path(gemFolder.getLocationURI().getPath());
		logfilePath = logfilePath.append(currentProject.getName()).removeFileExtension().addFileExtension("gem.log"); //$NON-NLS-1$

		final int numprocs = pstore.getInt(PreferenceConstants.GEM_PREF_NUMPROCS);
		int portnum = pstore.getInt(PreferenceConstants.GEM_PREF_PORTNUM);
		final int reportnum = pstore.getInt(PreferenceConstants.GEM_PREF_REPORTNUM);
		final boolean fibPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_FIB);
		final boolean mpiCallsPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_MPICALLS);
		final boolean openmpPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_OPENMP);
		final boolean blockingSendsPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_BLOCK);
		final boolean compareOutputPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_COMPARE_OUTPUT);
		final boolean reportPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_REPORT);
		final boolean unixSocketsPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_UNIXSOCKETS);
		final boolean verbosePreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_VERBOSE);
		final String hostName = pstore.getString(PreferenceConstants.GEM_PREF_HOSTNAME);
		String ispExePath = pstore.getString(PreferenceConstants.GEM_PREF_ISPEXE_PATH);
		ispExePath += (ispExePath == "") ? "" : "/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Find an available port to use
		boolean available = isPortAvailable(portnum);
		while (!available) {

			// keep ports <= 1024 available to the system
			if (portnum < 1025) {
				portnum = 9999;
			}
			portnum--;
			available = isPortAvailable(portnum);
		}

		// Reset the default port on local machine
		pstore.setValue(PreferenceConstants.GEM_PREF_PORTNUM, portnum);

		// Build up command line String
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(ispExePath);
		stringBuffer.append("isp -n "); //$NON-NLS-1$
		stringBuffer.append(numprocs);
		stringBuffer.append(" -p "); //$NON-NLS-1$
		stringBuffer.append(portnum);
		stringBuffer.append(" "); //$NON-NLS-1$
		stringBuffer.append((blockingSendsPreference) ? "-b " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuffer.append((compareOutputPreference) ? "-P " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuffer.append((mpiCallsPreference) ? "-m " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuffer.append((unixSocketsPreference) ? "-x " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuffer.append((!hostName.trim().equals("")) ? "-h " : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuffer.append((!hostName.trim().equals("")) ? hostName + " " : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stringBuffer.append((verbosePreference) ? "-O " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuffer.append((openmpPreference) ? "-s " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuffer.append((reportPreference) ? "-r " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuffer.append((reportPreference) ? reportnum + " " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuffer.append((fibPreference) ? "-f " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		stringBuffer.append("-l "); //$NON-NLS-1$
		stringBuffer.append(logfilePath);
		stringBuffer.append(" "); //$NON-NLS-1$
		stringBuffer.append(executablePath);
		// Now add command line options
		final String ispCmd = stringBuffer.toString();

		// Store the name of the child processes ISP will create
		final String processName = executablePath.lastSegment();
		pstore.setValue(PreferenceConstants.GEM_PREF_PROCESS_NAME, processName);

		// Now run ISP
		runCommand(ispCmd, true);

		// Sync the project with the underlying file system
		refreshProject(currentProject);
	}

	/**
	 * Compiles the specified file with ispcc, which links against the profiler
	 * (interposition layer) library.
	 * 
	 * @param inputFile
	 *            The resource handle representing the source code file to
	 *            compile.
	 * @return int Returns 1 if everything went smoothly, -1 otherwise.
	 */
	public static int doIspcc(IFile inputFile) {

		// Create GEM folder to hold the generated log file and executable
		final IPath gemFolderPath = new Path("gem"); //$NON-NLS-1$
		final String projectName = inputFile.getProject().getName();
		final IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		final IFolder gemFolder = currentProject.getFolder(gemFolderPath);

		if (!gemFolder.exists()) {
			try {
				gemFolder.create(IResource.FORCE, true, null);
				refreshProject(currentProject);
			} catch (final CoreException e) {
				logExceptionDetail(e);
			}
		}

		// Build up command line String
		IPath executablePath = new Path(gemFolder.getLocationURI().getPath());
		executablePath = executablePath.append(currentProject.getName()).addFileExtension("gem"); //$NON-NLS-1$

		// Determine if we need C or C++ ISP compile
		final String fileExtension = inputFile.getFileExtension();
		int exitValue = 0;
		final StringBuffer stringBuffer = new StringBuffer();

		// Deal with C compile
		if (fileExtension.equals("c")) { //$NON-NLS-1$
			final String ispccPath = GemPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.GEM_PREF_ISPCC_PATH);
			stringBuffer.append(ispccPath);
			stringBuffer.append(ispccPath == "" ? "" : "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			stringBuffer.append("ispcc -o "); //$NON-NLS-1$
			stringBuffer.append(executablePath);
			stringBuffer.append(" "); //$NON-NLS-1$
			stringBuffer.append(inputFile.getLocationURI().getPath());
			final String ispccStr = stringBuffer.toString();
			exitValue = runCommand(ispccStr, true);
		} else { // Deal with C++ compile
			final String ispCppPath = GemPlugin.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.GEM_PREF_ISPCPP_PATH);
			stringBuffer.append(ispCppPath);
			stringBuffer.append((ispCppPath == "") ? "" : "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			stringBuffer.append("ispCC -o "); //$NON-NLS-1$
			stringBuffer.append(executablePath);
			stringBuffer.append(" "); //$NON-NLS-1$
			stringBuffer.append(inputFile.getLocationURI().getPath());
			final String ispCppStr = stringBuffer.toString();
			exitValue = runCommand(ispCppStr, true);
		}

		return exitValue;
	}

	/**
	 * Creates a log file from the specified source file by running ISP.
	 * 
	 * @param inputFile
	 *            The resource handle representing the file for GEM to process.
	 * @param compile
	 *            Whether or not to run ispcc.
	 * @param verify
	 *            Whether or not to run ISP.
	 * @return void
	 */
	public static int generateLogFile(IFile inputFile, boolean compile, boolean verify) {

		int exitStatus = 0;
		if (compile && taskStatus != TaskStatus.ABORTED) {
			exitStatus = doIspcc(inputFile);
			if (exitStatus != -1 && taskStatus != TaskStatus.ABORTED) {
				doIsp(inputFile);
			}
		} else if (verify && taskStatus != TaskStatus.ABORTED) {
			doIsp(inputFile);
		}

		return exitStatus;
	}

	/**
	 * Returns a string representing the version of ISP being used.
	 * 
	 * @param none
	 * @return String The version of ISP installed on the target machine.
	 */
	public static String getIspVersion() {
		// Get the location of ISP
		String exePath = GemPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.GEM_PREF_ISPEXE_PATH);

		// Run isp -v to get version number
		exePath += (exePath == "") ? "" : "/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		exePath += "isp -v"; //$NON-NLS-1$

		// Abort if ISP is not installed
		if (runCommand(exePath, false) == -1) {
			return null;
		}

		// Parse first line of STDOUT from GEM Console
		final Scanner scanner = new Scanner(consoleStdOutMessage);
		final String version = scanner.nextLine();
		scanner.close();
		final Pattern intraCbRegex = Pattern.compile("([0-9]+.[0-9]+.[0-9]+)$"); //$NON-NLS-1$
		final Matcher versionMatcher = intraCbRegex.matcher(version);
		if (versionMatcher.find()) {
			return versionMatcher.group(1);
		}

		return null;
	}

	/**
	 * Returns a String representation of the path to the log file that will be
	 * generated. This path will be relative to the gem directory (as the base).
	 * 
	 * e.g. gem/project-name.gem.log
	 * 
	 * @param inputFile
	 *            The resource handle to get the project location from.
	 * @return String The relative path of the log file to be generated.
	 */
	public static String getLogfilePath(IFile inputFile) {
		// Create GEM folder to hold the generated log file and executable
		final String projectName = inputFile.getProject().getName();
		final IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		final IPath gemFolderPath = new Path("gem"); //$NON-NLS-1$
		final IFolder gemFolder = currentProject.getFolder(gemFolderPath);

		if (!gemFolder.exists()) {
			try {
				gemFolder.create(IResource.FORCE, true, null);
			} catch (final CoreException e) {
				logExceptionDetail(e);
			}
		}

		// Build up the correct project relative path for the log file
		IPath logFilePath = gemFolderPath.append(currentProject.getName()).removeFileExtension();
		logFilePath = logFilePath.addFileExtension("gem.log"); //$NON-NLS-1$

		return logFilePath.toString();
	}

	/**
	 * Returns the path of the last used file relative to the workspace from the
	 * PreferenceStore of this plug-in. This will be a String representation of
	 * the URI for the most recent file accessed by this plug-in.
	 * 
	 * @param none
	 * @return String A String representation of the URI for the most recently
	 *         accessed file.
	 */
	public static URI getMostRecentURI() {
		final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		final String mostRecentURIStr = pstore.getString(PreferenceConstants.GEM_PREF_MOST_RECENT_FILE);
		URI mostRecentURI = null;
		try {
			mostRecentURI = new URI(mostRecentURIStr);
		} catch (final URISyntaxException e) {
			logExceptionDetail(e);
		}
		return mostRecentURI;
	}

	/**
	 * Returns the value of 'outputSameDetails'
	 * 
	 * @param none
	 * @return String The details of the output comparison.
	 */
	public static String getOutputSameDetails() {
		return outputSameDetails;
	}

	/**
	 * Returns the value of 'outputSameMessage'
	 * 
	 * @param none
	 * @return String The output to compare against.
	 */
	public static String getOutputSameMessage() {
		return outputSameMessage;
	}

	/**
	 * Returns the path of the source files form the specified logFile path.
	 * This method will be used when GEM is run on an existing executable and
	 * the source file necessary to populate the Analyzer code windows is
	 * needed.
	 * 
	 * @param logfile
	 *            The resource handle representing the log file.
	 * @return String The first source file name found in the log file. This
	 *         will include its full path on the local file system.
	 */
	public static IFile getSourceFilePath(IFile logfile) {

		Scanner scanner = null;
		InputStream logFileStream = null;
		String sourcefileInfo = ""; //$NON-NLS-1$

		if (!logfile.exists()) {
			refreshProject(logfile.getProject());
		}

		// Scan the second line of the log file for first source file name
		try {
			logFileStream = logfile.getContents(true);
		} catch (final CoreException e) {
			logExceptionDetail(e);
		}
		scanner = new Scanner(logFileStream);
		// If the log file is empty
		if (!scanner.hasNextLine()) {
			if (taskStatus != TaskStatus.ABORTED) {
				GemUtilities.showErrorDialog(Messages.GemUtilities_4);
			}
			return null;
		}

		// Skip the line holding the number of processes
		scanner.nextLine();

		// If ISP exited out without running any MPI Calls
		if (!scanner.hasNextLine()) {
			GemUtilities.showErrorDialog(Messages.GemUtilities_5);
			return null;
		}

		sourcefileInfo = scanner.nextLine();
		sourcefileInfo = sourcefileInfo.substring(sourcefileInfo.indexOf("/"), sourcefileInfo.lastIndexOf(" ")); //$NON-NLS-1$ //$NON-NLS-2$
		final IPath sourcefilePath = new Path(sourcefileInfo);
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		final IFile sourceFile = workspaceRoot.getFileForLocation(sourcefilePath);

		// Close the open InputStream
		try {
			if (logFileStream != null) {
				logFileStream.close();
			}
		} catch (final IOException e) {
			logExceptionDetail(e);
		} finally {
			scanner.close();
		}

		return sourceFile;
	}

	/*
	 * Returns whether or not the correct version of ISP is installed on the
	 * host machine (to support GEM operations). When version changes, be sure
	 * to update the corresponding entry in messages package!
	 */
	private static boolean hasCorrectIspVersion() {
		final String ispVersion = getIspVersion();
		if (ispVersion == null) {
			return false;
		}
		final StringTokenizer st = new StringTokenizer(ispVersion, ".", false); //$NON-NLS-1$
		st.nextToken();
		final int majorVersionNum = Integer.parseInt(st.nextToken());
		final int minorVersionNum = Integer.parseInt(st.nextToken());

		// if we have version 2, we need at least 2.5
		if (majorVersionNum == 2) {
			return minorVersionNum >= 5;
		}

		// otherwise we have a compatible version
		return true;
	}

	/**
	 * Activates appropriate GEM views and launches necessary operations.
	 * 
	 * @param inputFile
	 *            The resource obtained from the project to be processed.
	 * @param compile
	 *            Performs necessary compilation if true. If false, the compile
	 *            step is skipped.
	 * @param verify
	 *            Performs necessary verification if true. If false, the
	 *            verification step is skipped.
	 * @return void
	 */
	public static void initGemViews(IFile inputFile, boolean compile, boolean verify) {

		gemInputFile = inputFile;
		doCompile = compile;
		doVerify = verify;

		if (taskStatus == TaskStatus.ACTIVE) {
			showInformationDialog(Messages.GemUtilities_6);
			return;
		}

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		// Tell the Analyzer where to find the source and log files
		final IViewPart gemViewPart = window.getActivePage().findView(GemAnalyzer.ID);
		analyzer = (GemAnalyzer) gemViewPart;
		// Clear the Analyzer view in case we're doing a repeated run
		analyzer.clear();
		analyzer.init(inputFile);

		// Tell the Browser where to find the source and log files
		final IViewPart browserPart = window.getActivePage().findView(GemBrowser.ID);
		browser = (GemBrowser) browserPart;
		// Clear the Browser view in case we're doing a repeated run
		browser.clear();
		browser.init();

		// Initialize the console
		final IViewPart consolePart = window.getActivePage().findView(GemConsole.ID);
		console = (GemConsole) consolePart;
		console.init();

		// Create and parse log file, pass transitions to Browser & Analyzer
		final Thread initGemViewsThread = new Thread() {
			@Override
			public void run() {
				taskStatus = TaskStatus.ACTIVE;

				// Check that ISP itself is installed on the target machine
				if (getIspVersion() == null) {
					cancelAnalysis();
					return;
				}

				// Check for correct version of ISP on the target machine
				if (!hasCorrectIspVersion() && taskStatus == TaskStatus.ACTIVE) {
					cancelAnalysis();
					showErrorDialog(Messages.GemUtilities_7);
					return;
				}

				// Run ispcc and/or isp and check for creation of the log file
				if (generateLogFile(gemInputFile, doCompile, doVerify) == -1) {
					cancelAnalysis();
					showErrorDialog(Messages.GemUtilities_3);
					return;
				}

				if (taskStatus == TaskStatus.ACTIVE) {
					IFile logFile = null;
					IFile sourceFile = null;

					// If it's a log file, that's all we're interested in
					if (gemInputFile.getFileExtension().equals("log")) { //$NON-NLS-1$
						logFile = gemInputFile;
					} else {
						logFile = gemLogfile;
					}

					if (logFile == null) {
						cancelAnalysis();
						showErrorDialog(Messages.GemUtilities_14);
						return;
					}

					Transitions transitions = null;
					transitions = initTransitions(logFile);
					sourceFile = getSourceFilePath(logFile);

					if (transitions == null || sourceFile == null) {
						// Error message issued in getSourceFilePath()
						cancelAnalysis();
						return;
					}
					if (analyzer != null) {
						analyzer.update(sourceFile, transitions);
					}
					if (browser != null) {
						browser.update(transitions);
					}
					if (console != null) {
						console.cancel();
					}
					taskStatus = TaskStatus.IDLE;

					// Place the focus on the correct view
					final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
					final String activeView = pstore.getString(PreferenceConstants.GEM_ACTIVE_VIEW);
					if (activeView.equals(PreferenceConstants.GEM_ANALYZER)) {
						analyzer.setFocus();
					} else if (activeView.equals(PreferenceConstants.GEM_BROWSER)) {
						browser.setFocus();
					}
					else {
						console.setFocus();
					}
				}
			}
		};

		initGemViewsThread.start();
	}

	/*
	 * Just a wrapper method that returns a newly created Transitions object.
	 */
	private static Transitions initTransitions(IFile logFile) {
		try {
			return new Transitions(logFile);
		} catch (final ParseException e) {
			logExceptionDetail(e);
		}
		return null;
	}

	/**
	 * Checks if the current task has been aborted.
	 * 
	 * @param none
	 * @return boolean True if task was aborted, false otherwise.
	 */
	public static boolean isAborted() {
		return taskStatus == TaskStatus.ABORTED;
	}

	/*
	 * Helper method Returns true if the specified port number is available,
	 * false otherwise.
	 */
	private static boolean isPortAvailable(int portNum) {
		try {
			ServerSocket srv = new ServerSocket(portNum);
			srv.close();
			srv = null;
			return true;
		} catch (final IOException e) {
			return false;
		}
	}

	/*
	 * Kills the child processes spawned from the global process object and then
	 * forcibly closes the global process object. This would be, for example
	 * multiple processes created from running ISP via the runtime instance.
	 */
	private static void killProcesses() {
		final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		final String processName = pstore.getString(PreferenceConstants.GEM_PREF_PROCESS_NAME);
		final String command = "pkill " + processName; //$NON-NLS-1$
		Process killProc = null;
		try {
			killProc = Runtime.getRuntime().exec(command);
		} catch (final IOException e) {
			logExceptionDetail(e);
		} finally {
			if (killProc != null) {
				try {
					killProc.waitFor();
				} catch (final InterruptedException e) {
					logExceptionDetail(e);
				}
				killProc.destroy();
			}
			if (process != null) {
				process.destroy();
			}
		}
	}

	/**
	 * Logs the specified error using the Eclipse error logging mechanism. Log
	 * entries are placed in the org.eclipse.ptp.gem log file and will show up
	 * in the Eclipse Error Log View.
	 * 
	 * @param message
	 *            The error message to log.
	 * @param exception
	 *            The Exception that occurred.
	 * @return void
	 */
	public static void logError(String message, Throwable exception) {
		final IStatus status = new Status(IStatus.ERROR, GemPlugin.PLUGIN_ID, IStatus.ERROR, message, exception);
		GemPlugin.getDefault().getLog().log(status);
	}

	/**
	 * Bundles the specified Exception with a standard message and passes this
	 * information to the Eclipse logging mechanism via this classes logError
	 * method.
	 * 
	 * @param e
	 *            The exception to log error information for.
	 * @return void
	 */
	public static void logExceptionDetail(Exception e) {
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Messages.GemUtilities_1);
		stringBuffer.append("\""); //$NON-NLS-1$
		stringBuffer.append(GemPlugin.PLUGIN_ID);
		stringBuffer.append("\"."); //$NON-NLS-1$
		GemUtilities.logError(stringBuffer.toString(), e);
	}

	/**
	 * Refreshes the specified project. This helps in synchronizing the project
	 * with the local file system after new resource creation.
	 * 
	 * @param project
	 *            This resource, and all its children will be refreshed.
	 * @return void
	 */
	public static void refreshProject(final IProject project) {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (final CoreException e) {
			logExceptionDetail(e);
		}
	}

	/**
	 * Executes the specified command via a native runtime process instance.
	 * 
	 * @param command
	 *            The command to send to the Runtime instance.
	 * @param verbose
	 *            Writes output to the Gem Console if true, silent otherwise.
	 * @return int Returns 1 if everything went smoothly, -1 otherwise.
	 */
	public static int runCommand(String command, boolean verbose) {
		try {
			final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
			process = Runtime.getRuntime().exec(command);
			final StringBuffer stringBuffer = new StringBuffer();
			String stdOut = ""; //$NON-NLS-1$
			String stdOutResult = ""; //$NON-NLS-1$
			String stdErr = ""; //$NON-NLS-1$
			String stdErrResult = ""; //$NON-NLS-1$
			final BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			final BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			consoleStdOutMessage = "";//$NON-NLS-1$
			consoleStdErrMessage = "";//$NON-NLS-1$

			// clear the console if the preference is set
			final boolean clearConsole = pstore.getBoolean(PreferenceConstants.GEM_PREF_CLRCON);
			if (clearConsole) {
				console.clear();
			}

			// Try breaks when the process is terminated prematurely
			try {
				// read the process input stream
				while (process != null && (stdOut = stdOutReader.readLine()) != null && taskStatus == TaskStatus.ACTIVE) {
					stringBuffer.append(stdOut);
					stringBuffer.append("\n"); //$NON-NLS-1$
					consoleStdOutMessage += stdOut + "\n";//$NON-NLS-1$
					if (!stdOutReader.ready()) {
						updateConsole(verbose, true);
						consoleStdOutMessage = "";//$NON-NLS-1$
					}
				}
				stdOutResult = stringBuffer.toString();

				// cleanup, send last few lines
				updateConsole(verbose, true);

				// read the process error stream
				stringBuffer.setLength(0);
				while (process != null && (stdErr = stdErrReader.readLine()) != null && taskStatus == TaskStatus.ACTIVE) {
					stringBuffer.append(stdErr);
					stringBuffer.append("\n"); //$NON-NLS-1$
					consoleStdErrMessage += stdErr + "\n";//$NON-NLS-1$

					if (!stdErrReader.ready()) {
						updateConsole(verbose, true);
						consoleStdErrMessage = "";//$NON-NLS-1$
					}
				}
				stdErrResult = stringBuffer.toString();

				// cleanup, send last few lines
				updateConsole(verbose, true);
			} catch (final Exception e) {
				logExceptionDetail(e);
				return -1;
			}

			consoleStdOutMessage = stdOutResult;
			consoleStdErrMessage = stdErrResult;

			outputSameMessage = ""; // means that the output was not compared //$NON-NLS-1$
			outputSameDetails = ""; //$NON-NLS-1$
			if (verbose && pstore.getBoolean(PreferenceConstants.GEM_PREF_COMPARE_OUTPUT)) {
				compareOutput(consoleStdOutMessage);
			}

			if (process == null) {
				return -1;
			}

			return consoleStdErrMessage.contains("ld returned 1 exit status") ? -1 : 1; //$NON-NLS-1$

		} catch (final IOException e) { // thrown when ISP is not installed
			final StringTokenizer st = new StringTokenizer(command);
			String commandName = st.nextToken();
			commandName = new Path(commandName).lastSegment().toString();
			showErrorDialog(Messages.GemUtilities_8 + "\"" + commandName + "\"" + "\n" + Messages.GemUtilities_13); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (final Exception e) {
			logExceptionDetail(e);
		}
		return -1;
	}

	/**
	 * Runs the specified command as a thread via the Runtime instance.
	 * 
	 * @param command
	 *            The command to run as a thread via the Runtime instance.
	 * @return void
	 */
	public static void runCommandAsThread(String command) {
		final CommandThread thread = new CommandThread(command);
		thread.run();
	}

	/**
	 * Saves the URI of the last used file to the preference store. This URI
	 * will be represented as a String in the preference store.
	 * 
	 * @param relativePath
	 *            The String representation of the URI for the last used file in
	 *            the workspace.
	 * @return void
	 */
	public static void saveMostRecentURI(URI mostRecentURI) {
		final String mostRecentURIStr = mostRecentURI.toString();
		final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		pstore.setValue(PreferenceConstants.GEM_PREF_MOST_RECENT_FILE, mostRecentURIStr);
	}

	/**
	 * Writes the contents of the GEM console to the indicated local file.
	 * 
	 * @param file
	 *            The local file to write the specified content to.
	 * @return void
	 */
	public static void saveToLocalFile(File file, String content) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file.getCanonicalPath()));
			content.replaceAll("\n", System.getProperty("line.separator")); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write(content);
		} catch (final IOException e) {
			logExceptionDetail(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (final IOException e) {
					logExceptionDetail(e);
				}
			}
		}
	}

	/**
	 * Sets the number of processes for the next run of GEM. Updates the drop
	 * down list boxes in the GEM views if they are open.
	 * 
	 * @param none
	 * @return void
	 */
	public static void setNumProcesses() {
		final Shell shell = Display.getCurrent().getActiveShell();
		final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		Integer numProcs = pstore.getInt(PreferenceConstants.GEM_PREF_NUMPROCS);
		final String title = Messages.GemUtilities_9;
		final String message = Messages.GemUtilities_10;
		final InputDialog dlg = new InputDialog(shell, title, message, numProcs.toString(), new NumProcsValidator());
		Window.setDefaultImage(GemPlugin.getImageDescriptor("icons/processes.gif").createImage()); //$NON-NLS-1$
		dlg.open();

		// This avoids and ui event loop exception
		if (dlg.getReturnCode() == Window.CANCEL) {
			return;
		}

		numProcs = Integer.parseInt(dlg.getValue());
		pstore.setValue(PreferenceConstants.GEM_PREF_NUMPROCS, dlg.getValue());

		// Update the drop down in the Analyzer
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IViewPart gemViewPart = window.getActivePage().findView(GemAnalyzer.ID);
		if (gemViewPart != null) {
			final GemAnalyzer analyzer = (GemAnalyzer) gemViewPart;
			analyzer.updateDropDown();
		}

		// Update the drop down in the Issue Browser
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IViewPart browserViewPart = window.getActivePage().findView(GemBrowser.ID);
		if (browserViewPart != null) {
			final GemBrowser browser = (GemBrowser) browserViewPart;
			browser.updateDropDown();
		}
	}

	/**
	 * Sets the current task status to the specified state.
	 * 
	 * @param state
	 *            The state to assign the current task status.
	 * @return void
	 */
	public static void setTaskStatus(TaskStatus state) {
		taskStatus = state;
	}

	/**
	 * Simply opens an error message dialog with the specified String.
	 * 
	 * @param message
	 *            The message body.
	 * @return void
	 */
	public static void showErrorDialog(String message) {
		final String title = Messages.GemUtilities_11;
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Simply open an ExceptionDetailsDialog for the specified exception.
	 * 
	 * @param message
	 *            The message body.
	 * @param e
	 *            The Exception title.
	 * @return void
	 */
	public static void showExceptionDialog(String message, Exception e) {
		showErrorDialog(message);
		e.printStackTrace();
	}

	/**
	 * Simply opens a dialog with the specified String.
	 * 
	 * @param message
	 *            The message body.
	 * @return void
	 */
	public static void showInformationDialog(String message) {
		final String title = Messages.GemUtilities_12;
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Clears and shuts down all GEM views then forcibly terminates the shared
	 * Process object associated with this class and all its children.
	 * 
	 * @param none
	 * @return void
	 */
	public static void terminateOperation() {
		// Prevents destroying a process after it has finished (but before the
		// termination buttons have been deactivated)
		if (taskStatus != TaskStatus.IDLE) {
			taskStatus = TaskStatus.ABORTED;
			analyzer.clear();
			browser.clear();
			console.cancel();
			console.writeStdErr(Messages.GemConsole_11 + "\n"); //$NON-NLS-1$
			killProcesses();
		}
	}

	/*
	 * Starts the updateGemConsoleThread which writes STDOUT and STDERR to the
	 * GEM Console.
	 */
	private static void updateConsole(boolean verbose, boolean showConsole) {
		if (verbose && showConsole) {
			try {
				Display.getDefault().syncExec(updateGemConsoleThread);
			} catch (final Exception e) {
				logExceptionDetail(e);
			}
		}
	}

}
