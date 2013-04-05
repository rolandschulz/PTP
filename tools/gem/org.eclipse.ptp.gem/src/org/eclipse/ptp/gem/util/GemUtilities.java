/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
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
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.ptp.rdt.core.resources.RemoteMakeNature;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class GemUtilities {

	public static enum TaskStatus {
		IDLE, ACTIVE, ABORTED;
	}

	private static Process process;
	private static IRemoteProcess remoteProcess;
	private static GemAnalyzer analyzer;
	private static GemBrowser browser;
	private static GemConsole console;
	protected static TaskStatus taskStatus;
	private static boolean doCompile;
	private static boolean doVerify;
	private static String consoleStdOutMessage;
	private static String consoleStdErrMessage;
	private static IFile gemActiveResource;
	private static IFile gemLogFile;

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

	/**
	 * Casts or adapts the specified IResource to IFile.
	 * 
	 * @param resource
	 *            The resource to cast or adapt to IFile
	 * @return IFile The cast or adapted IResource
	 */
	public static IFile adaptResource(IResource resource) {
		if (resource instanceof IFile) {
			return (IFile) resource;
		}
		return (IFile) resource.getAdapter(IFile.class);
	}

	/*
	 * Used to cancel the current GEM analysis without killing any processes.
	 */
	private static void cancelAnalysis() {
		analyzer.clear();
		browser.clear();
		console.cancel();
		taskStatus = TaskStatus.IDLE;
	}

	// Creates the GEM folder for the log file
	private static void createGemFolder(final boolean isRemote, final IFolder gemFolder) {
		try {
			gemFolder.create(IResource.FORCE, true, null);
		} catch (final CoreException e) {
			logExceptionDetail(e);
		}
		if (isRemote) {
			sync();
		}
	}

	/**
	 * Runs the Happens Before Viewer on the log file associated with the
	 * specified input source file.
	 * 
	 * @param inputFile
	 *            The resource handle representing the GEM input file.
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
	 * @param resource
	 *            The resource representing the profiled executable.
	 * @return void
	 */
	public static void doIsp(IResource resource) {

		// Get all the current preferences
		final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		final boolean isRemote = (isRemoteProject(resource) || (isSynchronizedProject(resource) && isRemoteBuildConfiguration()));

		// Create GEM directory to hold the generated log file and executable
		final IProject currentProject = resource.getProject();
		gemLogFile = currentProject.getFile(getLogfilePath(resource));
		final IFolder gemFolder = currentProject.getFolder(new Path("gem")); //$NON-NLS-1$

		// Build up the command line String
		// Also generate the path for the log file to be created by ISP
		IPath executablePath = null;
		String resourceLocation = null;
		String logFileLocation = null;
		if (resource.getFileExtension().equals("gem")) { //$NON-NLS-1$
			if (isSynchronizedProject(resource)) {
				try {
					resourceLocation = BuildConfigurationManager.getInstance().getActiveSyncLocationURI(resource).getPath();
					logFileLocation = BuildConfigurationManager.getInstance().getActiveSyncLocationURI(gemFolder).getPath();
				} catch (final CoreException e) {
					logExceptionDetail(e);
				}
				executablePath = new Path(resourceLocation);
			} else {
				resourceLocation = resource.getLocationURI().getPath();
				executablePath = new Path(resourceLocation);
				logFileLocation = gemFolder.getLocationURI().getPath();
			}
		} else {
			logFileLocation = gemFolder.getLocationURI().getPath();
			executablePath = new Path(gemFolder.getLocationURI().getPath());
			executablePath = executablePath.append(currentProject.getName()).removeFileExtension().addFileExtension("gem"); //$NON-NLS-1$
		}

		IPath logFilePath = new Path(logFileLocation);
		logFilePath = logFilePath.append(currentProject.getName()).removeFileExtension().addFileExtension("gem.log"); //$NON-NLS-1$

		final int numprocs = pstore.getInt(PreferenceConstants.GEM_PREF_NUMPROCS);
		int portnum = pstore.getInt(PreferenceConstants.GEM_PREF_PORTNUM);
		final int reportnum = pstore.getInt(PreferenceConstants.GEM_PREF_REPORTNUM);
		final boolean fibPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_FIB);
		final boolean mpiCallsPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_MPICALLS);
		final boolean openmpPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_OPENMP);
		final boolean blockingSendsPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_BLOCK);
		final boolean reportPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_REPORT);
		final boolean unixSocketsPreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_UNIXSOCKETS);
		final boolean verbosePreference = pstore.getBoolean(PreferenceConstants.GEM_PREF_VERBOSE);
		final String hostName = pstore.getString(PreferenceConstants.GEM_PREF_HOSTNAME);

		// Check for sync project
		String ispExePath = pstore.getString(isRemote ? PreferenceConstants.GEM_PREF_REMOTE_ISPEXE_PATH
				: PreferenceConstants.GEM_PREF_ISPEXE_PATH);
		ispExePath += (ispExePath == "") ? "" : "/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		final String cmdArgs = pstore.getString(PreferenceConstants.GEM_PREF_ARGS);

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
		stringBuffer.append(logFilePath);
		stringBuffer.append(" "); //$NON-NLS-1$
		stringBuffer.append(executablePath);
		stringBuffer.append(" "); //$NON-NLS-1$
		stringBuffer.append(cmdArgs);
		// Now add command line options
		final String ispCmd = stringBuffer.toString();

		// Store the name of the child processes ISP will create
		final String processName = executablePath.lastSegment();
		pstore.setValue(PreferenceConstants.GEM_PREF_PROCESS_NAME, processName);

		// Now run ISP
		runCommand(ispCmd, true);

		// Trigger a sync if the project is synchronized with remote build configuration
		if (isRemote) {
			sync();
		}

		// Sync the project with the underlying file system
		refreshProject(currentProject);
	}

	/**
	 * Compiles the specified file with ispcc (mpicc wrapper), which links
	 * against the profiler (interposition layer) library.
	 * 
	 * @param resource
	 *            The resource to compile.
	 * @return int Returns 1 if everything went smoothly, -1 otherwise.
	 */
	public static int doIspcc(IResource resource) {
		// Create GEM folder to hold the generated log file and executable
		final IProject currentProject = resource.getProject();
		final IFolder gemFolder = currentProject.getFolder(new Path("gem")); //$NON-NLS-1$
		final boolean isRemote = (isRemoteProject(resource) || (isSynchronizedProject(resource) && isRemoteBuildConfiguration()));
		if (!gemFolder.exists()) {
			createGemFolder(isRemote, gemFolder);
			refreshProject(currentProject);
		}

		// Build up command line String
		IPath executablePath = new Path(gemFolder.getLocationURI().getPath());
		executablePath = executablePath.append(currentProject.getName()).addFileExtension("gem"); //$NON-NLS-1$

		// Determine if we need C or C++ ISP compile
		final String fileExtension = resource.getFileExtension();
		int exitValue = 0;
		final StringBuffer stringBuffer = new StringBuffer();

		// Deal with C compile
		if (fileExtension.equals("c")) { //$NON-NLS-1$
			final String ispccPath = GemPlugin
					.getDefault()
					.getPreferenceStore()
					.getString(
							(isRemoteProject(resource) ? PreferenceConstants.GEM_PREF_REMOTE_ISPCC_PATH
									: PreferenceConstants.GEM_PREF_ISPCC_PATH));
			stringBuffer.append(ispccPath);
			stringBuffer.append(ispccPath == "" ? "" : "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			stringBuffer.append("ispcc -o "); //$NON-NLS-1$
			stringBuffer.append(executablePath);
			stringBuffer.append(" "); //$NON-NLS-1$
			stringBuffer.append(resource.getLocationURI().getPath());
			final String ispccStr = stringBuffer.toString();
			exitValue = runCommand(ispccStr, true);
		} else { // Deal with C++ compile
			final String ispCppPath = GemPlugin
					.getDefault()
					.getPreferenceStore()
					.getString(
							(isRemoteProject(resource) ? PreferenceConstants.GEM_PREF_REMOTE_ISPCPP_PATH
									: PreferenceConstants.GEM_PREF_ISPCPP_PATH));
			stringBuffer.append(ispCppPath);
			stringBuffer.append((ispCppPath == "") ? "" : "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			stringBuffer.append("ispCC -o "); //$NON-NLS-1$
			stringBuffer.append(executablePath);
			stringBuffer.append(" "); //$NON-NLS-1$
			stringBuffer.append(resource.getLocationURI().getPath());
			final String ispCppStr = stringBuffer.toString();
			exitValue = runCommand(ispCppStr, true);
		}

		return exitValue;
	}

	/**
	 * Creates a log file from the specified resource.
	 * 
	 * @param resource
	 *            The resource to process.
	 * @param compile
	 *            Whether or not to run ispcc script.
	 * @param verify
	 *            Whether or not to run ISP on the profiled executable.
	 * @return void
	 */
	public static int generateLogFile(IResource resource, boolean compile, boolean verify) {

		int exitStatus = 0;
		if (compile && taskStatus != TaskStatus.ABORTED) {
			exitStatus = doIspcc(resource);
			if (exitStatus != -1 && taskStatus != TaskStatus.ABORTED) {
				doIsp(resource);
			}
		} else if (verify && taskStatus != TaskStatus.ABORTED) {
			doIsp(resource);
		}

		return exitStatus;
	}

	/**
	 * Returns the current project being verified.
	 * 
	 * @param none
	 * @return The current project being verified.
	 */
	public static IProject getCurrentProject() {
		return gemActiveResource.getProject();
	}

	/**
	 * Returns the current project being verified.
	 * 
	 * @param resource
	 *            The active resource from the current project.
	 * @return The current project being verified.
	 */
	public static IProject getCurrentProject(IResource resource) {
		return resource.getProject();
	}

	/**
	 * Returns a string representing the version of ISP being used.
	 * 
	 * @param none
	 * @return String The captured String from stdout representing the version of ISP
	 *         installed on the target machine.
	 */
	public static String getIspVersion() {
		// Get the location of ISP
		final boolean useRemoteISP = (isRemoteProject(gemActiveResource) || (isSynchronizedProject(gemActiveResource) && isRemoteBuildConfiguration()));
		String ispExePath = GemPlugin
				.getDefault()
				.getPreferenceStore()
				.getString(
						useRemoteISP ? PreferenceConstants.GEM_PREF_REMOTE_ISPEXE_PATH : PreferenceConstants.GEM_PREF_ISPEXE_PATH);

		// Run isp -v to get version number
		ispExePath += (ispExePath == "") ? "" : "/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		final String ispExeCommand = ispExePath + "isp -v"; //$NON-NLS-1$

		// Abort if ISP is not installed
		if (runCommand(ispExeCommand, false) == -1) {
			return null;
		}

		// Parse first line of STDOUT from GEM Console if no errors, e.g. bad path
		if (consoleStdOutMessage.contains("In-Situ Partial Order")) { //$NON-NLS-1$
			final Scanner scanner = new Scanner(consoleStdOutMessage);
			final String version = scanner.nextLine();
			scanner.close();
			final Pattern intraCbRegex = Pattern.compile("([0-9]+.[0-9]+.[0-9]+)$"); //$NON-NLS-1$
			final Matcher versionMatcher = intraCbRegex.matcher(version);
			if (versionMatcher.find()) {
				return versionMatcher.group(1);
			}
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
	public static String getLogfilePath(IResource resource) {
		// Create GEM folder to hold the generated log file and executable
		final String projectName = resource.getProject().getName();
		final IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		final IPath gemFolderPath = new Path("gem"); //$NON-NLS-1$
		final IFolder gemFolder = currentProject.getFolder(gemFolderPath);

		final boolean isRemote = (isRemoteProject(resource) || (isSynchronizedProject(resource) && isRemoteBuildConfiguration()));
		if (!gemFolder.exists()) {
			createGemFolder(isRemote, gemFolder);
			refreshProject(currentProject);
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
		URI location = null;
		try {
			location = new URI(pstore.getString(PreferenceConstants.GEM_PREF_MOST_RECENT_FILE));
		} catch (final URISyntaxException e) {
			logExceptionDetail(e);
		}
		return location;
	}

	/**
	 * Returns the generated log file for the current project being verified.
	 * 
	 * @param none
	 * @return IFile The log file for the current project being verified.
	 */
	public static IFile getProjectLogFile() {
		return gemLogFile;
	}

	/**
	 * Returns the IRemoteConnection associated with the specified
	 * IRemoteServices.
	 * 
	 * @param services
	 *            The IRemotesServices object for which the IRemoteCOnnection
	 *            object belongs.
	 * @return IRemoteConnection The IRemoteConnection associated with the
	 *         specified IRemoteServices.
	 */
	public static IRemoteConnection getRemoteConnection(IRemoteServices services, URI projectURI) {
		final IRemoteConnection connection = services.getConnectionManager().getConnection(projectURI);

		// Open the connection if it's closed
		if (connection != null && !connection.isOpen()) {
			try {
				connection.open(null);
			} catch (final RemoteConnectionException e) {
				logExceptionDetail(e);
			}
		}

		return connection;
	}

	/**
	 * Returns the IRemoteFileManager for the specified project resource.
	 * 
	 * @param projectResource
	 *            The IResource object to get the IRemoteFileManager for.
	 * @return IRemoteFileManager The IRemoteFileManager for the specified
	 *         project resource.
	 */
	public static IRemoteFileManager getRemoteFileManager(IFile projectResource) {
		final URI projectURI = projectResource.getProject().getLocationURI();
		final IRemoteServices services = RemoteServices.getRemoteServices(projectURI);
		if (services != null) {
			final IRemoteConnection connection = services.getConnectionManager().getConnection(projectURI);
			return services.getFileManager(connection);
		}
		return null;
	}

	/**
	 * Returns the RemoteProcessBuilder associated with the connection
	 * used by the specified remote project.
	 * 
	 * @param currentProject
	 *            The project for which we need the connection.
	 * @param args
	 *            The command line arguments for the RemoteProcessBuilder to be
	 *            used.
	 * @return IRemoteProcessBuilder The RemoteProcessBuilder object associated
	 *         with the connection used by the specified remote project.
	 */
	public static IRemoteProcessBuilder getRemoteProcessBuilder(IProject currentProject, String[] args) {
		URI projectURI = null;
		if (isSynchronizedProject(gemActiveResource)) {
			try {
				projectURI = BuildConfigurationManager.getInstance().getActiveSyncLocationURI(currentProject);
			} catch (final CoreException e) {
				logExceptionDetail(e);
			}
		} else {
			projectURI = currentProject.getLocationURI();
		}

		final IRemoteServices services = RemoteServices.getRemoteServices(projectURI); // FIXME: This can return null!
		final IRemoteConnection connection = getRemoteConnection(services, projectURI);
		final IRemoteProcessBuilder rpb = services.getProcessBuilder(connection, args);

		return rpb;
	}

	/**
	 * Returns a handle to the file with the specified name in the current project.
	 * 
	 * @param fullPath
	 *            The string representation of the full path to a file contained
	 *            within the current project.
	 * @param resource
	 *            A resource within the current project (can be the current
	 *            project itself).
	 * @return IFile The handle to the member file in the current project.
	 */
	public static IFile getSourceFile(String fullPath, IResource resource) {

		final IProject currentProject = resource.getProject();

		// if (true) {
		if (!isSynchronizedProject(currentProject) && !isRemoteProject(resource)) {
			final String currentProjectPath = currentProject.getLocationURI().getPath();
			IPath sourceFilePath = new Path(fullPath);
			sourceFilePath = sourceFilePath.makeRelativeTo(new Path(currentProjectPath));
			final IFile sourceFile = currentProject.getFile(sourceFilePath);
			return sourceFile;
		}

		final String[] args = fullPath.split("/", -1); //$NON-NLS-1$
		final String name = args[args.length - 1];
		return currentProject.getFile(name);
	}

	/**
	 * Returns the handle to the source files form the specified logFile path.
	 * This method will be used when GEM is run on an existing executable and
	 * the source file necessary to populate the Analyzer code windows is
	 * needed.
	 * 
	 * @param logFile
	 *            The resource handle representing the log file.
	 * @return String The handle to the first source file found in the log file.
	 */
	public static IFile getSourceFilePathFromLog(IFile logFile) {

		Scanner scanner = null;
		InputStream logFileStream = null;
		String sourceFilePathInfo = ""; //$NON-NLS-1$

		if (!logFile.exists()) {
			refreshProject(logFile.getProject());
		}

		// Scan the second line of the log file for first source file name
		try {
			logFileStream = logFile.getContents(true);
		} catch (final CoreException e) {
			logExceptionDetail(e);
		}

		// If the log file is empty
		scanner = new Scanner(logFileStream);
		if (!scanner.hasNextLine()) {
			if (taskStatus != TaskStatus.ABORTED) {
				GemUtilities.showErrorDialog(Messages.GemUtilities_4);
			}
			return null;
		}

		// Skip the line holding the number of processes
		scanner.nextLine();

		// If ISP exited out without processing any MPI Calls
		if (!scanner.hasNextLine()) {
			GemUtilities.showErrorDialog(Messages.GemUtilities_5);
			return null;
		}

		// Grab the path string for the source file form the log file line
		sourceFilePathInfo = scanner.nextLine();
		final String[] elements = sourceFilePathInfo.split(" ", -1); //$NON-NLS-1$
		sourceFilePathInfo = elements[elements.length - 2];
		final IFile sourceFile = getSourceFile(sourceFilePathInfo, logFile);

		// Close the InputStream
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
		// final int minorVersionNum = Integer.parseInt(st.nextToken());

		// we need a compatible version of ISP, e.g. v0.3.0+
		if (majorVersionNum == 3) {
			return true;
		}

		showErrorDialog(Messages.GemUtilities_7);
		return false;
	}

	/**
	 * Activates appropriate GEM views and launches necessary operations.
	 * 
	 * @param resource
	 *            The resource obtained from the project to be processed.
	 * @param compile
	 *            Performs necessary compilation if true. If false, the compile
	 *            step is skipped.
	 * @param verify
	 *            Performs necessary verification if true. If false, the
	 *            verification step is skipped.
	 * @return void
	 */
	public static void initGemViews(IResource resource, boolean compile, boolean verify) {

		gemActiveResource = adaptResource(resource);
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
		analyzer.init(gemActiveResource);

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

				final boolean isLogFile = gemActiveResource.getFileExtension().equals("log"); //$NON-NLS-1$
				// If not a log file, check for ISP installation and version
				if (!isLogFile) {

					// Check for correct version of ISP on the target machine
					if (!hasCorrectIspVersion()) {
						cancelAnalysis();
						return;
					}

					// Run ispcc and/or isp and check for creation of the log file
					if (generateLogFile(gemActiveResource, doCompile, doVerify) == -1) {
						cancelAnalysis();
						showErrorDialog(Messages.GemUtilities_3);
						return;
					}
				}

				if (taskStatus == TaskStatus.ACTIVE) {
					IFile logFile = null;
					IFile sourceFile = null;

					// If it's a log file, that's all we're interested in
					if (isLogFile) {
						gemLogFile = gemActiveResource;
						logFile = gemLogFile;
					} else {
						logFile = gemLogFile;
					}
					if (logFile == null) {
						cancelAnalysis();
						showErrorDialog(Messages.GemUtilities_14);
						return;
					}

					Transitions transitions = null;
					transitions = initTransitions(logFile);
					sourceFile = getSourceFilePathFromLog(logFile);

					if (transitions == null || sourceFile == null) {
						// Note: Error message issued in getSourceFilePathFromLog()
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

					// Activate the correct view based on preference settings
					final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
					final String activeView = pstore.getString(PreferenceConstants.GEM_ACTIVE_VIEW);
					if (activeView.equals(PreferenceConstants.GEM_ANALYZER)) {
						analyzer.activate();
					} else if (activeView.equals(PreferenceConstants.GEM_BROWSER)) {
						browser.activate();
					} else {
						console.activate();
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

	/**
	 * Returns whether or not the current project has active resources yet.
	 * 
	 * @param none
	 * @return boolean Ture if the current project has active resource, false otherwise.
	 */
	public static boolean isProjectActive() {
		return gemActiveResource != null;
	}

	//
	private static boolean isRemoteBuildConfiguration() {

		final IProject project = getCurrentProject(gemActiveResource);
		final IConfiguration configuration = ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration();
		String buildLocation = null;
		try {
			buildLocation = BuildConfigurationManager.getInstance().getActiveSyncLocationURI(project).getPath();
		} catch (final CoreException e) {
			GemUtilities.logExceptionDetail(e);
		}
		final String projectLocation = project.getLocationURI().getPath();

		return !buildLocation.equals(projectLocation);
	}

	/**
	 * Returns whether or not the current project being verified by GEM is
	 * remote. Uses the specified resource.
	 * 
	 * @param resource
	 *            The IResource object for which to check the NATURE_ID.
	 * @return boolean True if the current project being verified by GEM is
	 *         remote, false otherwise.
	 */
	public static boolean isRemoteProject(IResource resource) {
		boolean isRemote = false;
		try {
			isRemote = getCurrentProject().hasNature(RemoteMakeNature.NATURE_ID);
		} catch (final CoreException e) {
			GemUtilities.logExceptionDetail(e);
		}
		return isRemote;
	}

	/**
	 * Returns whether or not the current project being verified by GEM is
	 * synchronized. Uses the specified resource.
	 * 
	 * @param resource
	 *            The current project member resource for which to get the enclosing project to check the NATURE_ID.
	 * @return boolean True if the current project being verified by GEM is
	 *         synchronized, false otherwise.
	 */
	public static boolean isSynchronizedProject(IResource resource) {
		boolean isSync = false;
		try {
			isSync = getCurrentProject(resource).hasNature(RemoteSyncNature.NATURE_ID);
		} catch (final CoreException e) {
			GemUtilities.logExceptionDetail(e);
		}
		return isSync;
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
		final IProject currentProject = getCurrentProject(gemActiveResource);
		final boolean isRemote = isRemoteProject(gemActiveResource)
				|| (isSynchronizedProject(gemActiveResource) && isRemoteBuildConfiguration());

		if (isRemote) {
			final String[] args = command.split(" ", -1); //$NON-NLS-1$
			final IRemoteProcessBuilder rpb = getRemoteProcessBuilder(currentProject, args);
			try {
				remoteProcess = rpb.start();
			} catch (final IOException e) {
				logExceptionDetail(e);
			}
			try {
				remoteProcess = rpb.start();
			} catch (final IOException e) {
				logExceptionDetail(e);
			} finally {
				if (remoteProcess != null) {
					try {
						remoteProcess.waitFor();
					} catch (final InterruptedException e) {
						logExceptionDetail(e);
					}
					remoteProcess.destroy();
				}
				if (remoteProcess != null) {
					remoteProcess.destroy();
				}
			}
		} else {
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

	/*
	 * Prompts an input dialog asking the user what command line arguments they
	 * want used and stores the result. The default value shown is the last used
	 * value.
	 */
	private static void requestCommandLineArgs() {
		final String message = Messages.GemUtilities_28;
		final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		final String prevArgs = pstore.getString(PreferenceConstants.GEM_PREF_ARGS);
		final String newArgs = JOptionPane.showInputDialog(null, message, prevArgs);
		if (newArgs != null) {
			pstore.setValue(PreferenceConstants.GEM_PREF_ARGS, newArgs);
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

		// Find out if the current project is local or remote
		final IProject currentProject = getCurrentProject(gemActiveResource);
		final boolean isRemote = isRemoteProject(gemActiveResource)
				|| (isSynchronizedProject(gemActiveResource) && isRemoteBuildConfiguration());

		try {
			final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
			final StringBuffer stringBuffer = new StringBuffer();
			String stdOut = ""; //$NON-NLS-1$
			String stdOutResult = ""; //$NON-NLS-1$
			String stdErr = ""; //$NON-NLS-1$
			String stdErrResult = ""; //$NON-NLS-1$

			// Check if the current project is remote
			if (isRemote) {
				final String[] args = command.split(" ", -1); //$NON-NLS-1$
				final IRemoteProcessBuilder rpb = getRemoteProcessBuilder(currentProject, args);
				remoteProcess = rpb.start();
			} else {
				process = Runtime.getRuntime().exec(command);
			}

			final BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(isRemote ? remoteProcess.getInputStream()
					: process.getInputStream()));
			final BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(isRemote ? remoteProcess.getErrorStream()
					: process.getErrorStream()));
			consoleStdOutMessage = ""; //$NON-NLS-1$
			consoleStdErrMessage = ""; //$NON-NLS-1$

			// Clear the console if the preference is set
			final boolean clearConsole = pstore.getBoolean(PreferenceConstants.GEM_PREF_CLRCON);
			if (clearConsole) {
				console.clear();
			}

			// Try breaks when the process is terminated prematurely
			try {
				// read the process input stream
				while ((isRemote ? remoteProcess != null : process != null) && (stdOut = stdOutReader.readLine()) != null
						&& taskStatus == TaskStatus.ACTIVE) {
					stringBuffer.append(stdOut);
					stringBuffer.append("\n"); //$NON-NLS-1$
					consoleStdOutMessage += stdOut + "\n"; //$NON-NLS-1$
					if (!stdOutReader.ready()) {
						updateConsole(verbose, true);
						consoleStdOutMessage = ""; //$NON-NLS-1$
					}
				}

				stdOutResult = stringBuffer.toString();

				// cleanup, send last few lines
				updateConsole(verbose, true);

				// read the process error stream
				stringBuffer.setLength(0);
				while ((isRemote ? remoteProcess != null : process != null) && (stdErr = stdErrReader.readLine()) != null
						&& taskStatus == TaskStatus.ACTIVE) {
					stringBuffer.append(stdErr);
					stringBuffer.append("\n"); //$NON-NLS-1$
					consoleStdErrMessage += ""; //stdErr + "\n"; //$NON-NLS-1$

					if (!stdErrReader.ready()) {
						updateConsole(verbose, true);
						consoleStdErrMessage = ""; //$NON-NLS-1$
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

			if (isRemote ? remoteProcess == null : process == null) {
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
	 * @param mostRecentURI
	 *            The URI of the last used file in the currently used workspace.
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
	 * @param content
	 *            The content to write to the specified local file.
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
	 * Used to pass command line arguments to the profiled executable during a
	 * verification run. If the GEM_PREF_REQUEST_ARGS preference is set,
	 * GEM_PREF_REQUEST_ARGS requestCommandLineArgs is called to get the command
	 * line arguments. Otherwise the command line arguments are simply set to
	 * "".
	 * 
	 * @param none
	 * @return void
	 */
	public static void setCommandLineArgs() {
		final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		if (pstore.getBoolean(PreferenceConstants.GEM_PREF_REQUEST_ARGS)) {
			requestCommandLineArgs();
		} else {
			pstore.setValue(PreferenceConstants.GEM_PREF_ARGS, ""); //$NON-NLS-1$
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

		// This avoids a UI event loop exception
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
	 *            The message body for the exception dialog.
	 * @param e
	 *            The exception title.
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
	 *            The message body for the information dialog.
	 * @return void
	 */
	public static void showInformationDialog(String message) {
		final String title = Messages.GemUtilities_12;
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	// Triggers a blocking remote-2-local sync.
	private static void sync() {
		final IProgressMonitor monitor = new NullProgressMonitor();
		final IProject project = getCurrentProject(gemActiveResource);
		try {
			SyncManager.syncBlocking(null, project, SyncFlag.FORCE, monitor);
		} catch (final CoreException e) {
			logExceptionDetail(e);
		}
	}

	/**
	 * Clears and shuts down all GEM views then forcibly terminates the shared
	 * Process object associated with this class and all its children. This
	 * prevents destroying a process after it has finished, but before the
	 * termination buttons have been deactivated.
	 * 
	 * @param none
	 * @return void
	 */
	public static void terminateOperation() {
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
