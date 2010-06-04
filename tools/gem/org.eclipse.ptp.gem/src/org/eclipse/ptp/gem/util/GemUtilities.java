/*******************************************************************************
 * Copyright (c) 2009, 2010 University of Utah School of Computing
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.gem.GemPlugin;
import org.eclipse.ptp.gem.messages.Messages;
import org.eclipse.ptp.gem.preferences.PreferenceConstants;
import org.eclipse.ptp.gem.views.GemAnalyzer;
import org.eclipse.ptp.gem.views.GemConsole;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class GemUtilities {

	private static Process process;
	private static String gemConsoleMessage = ""; //$NON-NLS-1$

	// This thread exists to update SWT components belonging to the UI thread.
	private final static Thread updateGemConsoleThread = new Thread() {
		public void run() {
			// Get a handle on the GEM Console
			IViewPart ISPViewPart = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().findView(
							GemConsole.ID);
			GemConsole ispCon = (GemConsole) ISPViewPart;
			ispCon.write(gemConsoleMessage);
			gemConsoleMessage = ""; //$NON-NLS-1$
		}
	};

	/**
	 * Executes the specified command process via the Runtime instance.
	 * 
	 * @param command The command to send to the Runtime instance.
	 * @param verbose Outputs to GemConsole if true, silent otherwise.
	 * @return int 1 if everything went smoothly, -1 otherwise.
	 */
	public static int runCommand(String command) {
		try {
			process = Runtime.getRuntime().exec(command);
			String stdIn = ""; //$NON-NLS-1$
			String stdInResult = ""; //$NON-NLS-1$
			BufferedReader inReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			// Try breaks when the process is terminated prematurely
			try {
				// read the process input stream
				while (process != null && (stdIn = inReader.readLine()) != null) {
					stdInResult += stdIn + "\n"; //$NON-NLS-1$
				}
			} catch (Exception e) {
				return -1;
			}
			gemConsoleMessage = stdInResult;

			if (process == null) {
				return -1;
			} else {
				Display.getDefault().syncExec(updateGemConsoleThread);
				return 1;
			}
		} catch (Exception e) {
			String message = Messages.GemUtilities_0;
			showExceptionDialog(message, e);
			logError(Messages.GemUtilities_1, e);
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Kills the static Process object associated with this class.
	 * 
	 * @param none
	 * @return void
	 */
	public static void killProcess() {
		process.destroy();
	}

	/**
	 * Compiles the specified file with ispcc.
	 * 
	 * @param sourceFilePath The source file with absolute path.
	 * @return int 1 if everything went smoothly, -1 otherwise.
	 */
	public static int doIspcc(String sourceFilePath) {
		// Create our isp directory to hold the generated executable
		File gemDirectory = new File(getFilePath(sourceFilePath) + "/gem"); //$NON-NLS-1$
		gemDirectory.mkdir();

		// Build up command line String
		String exePath = gemDirectory.getAbsolutePath() + "/" //$NON-NLS-1$
				+ getFileName(sourceFilePath);

		// Determine if we need C or C++ ISP compile
		int dotIndex = sourceFilePath.lastIndexOf("."); //$NON-NLS-1$
		String extension = sourceFilePath.substring(dotIndex);
		int error = 0;

		// Deal with C compile
		if (extension.equals(".c")) { //$NON-NLS-1$
			String ispccStr = GemPlugin.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.GEM_PREF_ISPCC_PATH);
			if (ispccStr != "") { //$NON-NLS-1$
				ispccStr += "/"; //$NON-NLS-1$
			}
			ispccStr += "ispcc -o " + exePath + ".gem " + sourceFilePath; //$NON-NLS-1$ //$NON-NLS-2$
			error = runCommand(ispccStr);
		} else { // Deal with C++ compile
			String ispCppStr = GemPlugin.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.GEM_PREF_ISPCPP_PATH);
			if (ispCppStr != "") { //$NON-NLS-1$
				ispCppStr += "/"; //$NON-NLS-1$
			}
			ispCppStr += "ispCC -o " + exePath + ".gem " + sourceFilePath; //$NON-NLS-1$ //$NON-NLS-2$
			error = runCommand(ispCppStr);
		}

		// if ispcc ran smoothly
		if (error == 1) {
			File executable = new File(exePath + ".gem"); //$NON-NLS-1$
			if (!executable.canRead()) {
				showErrorDialog(Messages.GemUtilities_2,
						Messages.GemUtilities_3);
				return -1;
			}
		}
		return error;
	}

	/**
	 * Runs ISP on the specified file.
	 * 
	 * @param sourceFilePath The source file with absolute path.
	 * @return void
	 */
	public static void doIsp(String sourceFilePath) {

		// Get all the current preferences
		IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();

		// Create our GEM directory to hold the generated log file
		File gemDirectory = new File(getFilePath(sourceFilePath) + "/gem"); //$NON-NLS-1$
		gemDirectory.mkdir();

		File executable = null;
		String filename = ""; //$NON-NLS-1$

		if (!sourceFilePath.endsWith(".gem")) { //$NON-NLS-1$
			filename = getFileName(sourceFilePath);
			executable = new File(gemDirectory.getAbsolutePath()
					+ "/" + filename //$NON-NLS-1$
					+ ".gem"); //$NON-NLS-1$
			filename += ".gem"; //$NON-NLS-1$
		} else {
			filename = getFullFileName(sourceFilePath);
			executable = new File(sourceFilePath);
		}

		// Start building our string to pass to teh command line
		String exePath = executable.toString();

		int numprocs = pstore.getInt(PreferenceConstants.GEM_PREF_NUMPROCS);
		int portnum = pstore.getInt(PreferenceConstants.GEM_PREF_PORTNUM);
		int reportnum = pstore.getInt(PreferenceConstants.GEM_PREF_REPORTNUM);
		boolean fibPreference = pstore
				.getBoolean(PreferenceConstants.GEM_PREF_FIB_OPTION);
		boolean mpicallsPreference = pstore
				.getBoolean(PreferenceConstants.GEM_PREF_MPICALLS_OPTION);
		boolean openmpPreference = pstore
				.getBoolean(PreferenceConstants.GEM_PREF_OPENMP_OPTION);
		boolean blockingsendsPreference = pstore
				.getBoolean(PreferenceConstants.GEM_PREF_BLOCK_OPTION);
		boolean reportPreference = pstore
				.getBoolean(PreferenceConstants.GEM_PREF_REPORT_OPTION);
		boolean verbosePreference = pstore
				.getBoolean(PreferenceConstants.GEM_PREF_VERBOSE);
		String path = pstore
				.getString(PreferenceConstants.GEM_PREF_ISPEXE_PATH);
		if (path != "") { //$NON-NLS-1$
			path += "/"; //$NON-NLS-1$
		}

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
		String ispCmd = path + "isp -n " + numprocs + " -p " + portnum; //$NON-NLS-1$ //$NON-NLS-2$

		// Now add command line options
		ispCmd = ispCmd
				+ " " + ((blockingsendsPreference) ? "-b " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ ((mpicallsPreference) ? "-m " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((verbosePreference) ? "-O " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((openmpPreference) ? "-s " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((reportPreference) ? "-r " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((reportPreference) ? reportnum + " " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((fibPreference) ? "-f " : "") + "-l " + gemDirectory.getAbsolutePath() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "/" + filename + ".log " + exePath; //$NON-NLS-1$ //$NON-NLS-2$

		runCommand(ispCmd);
	}

	/**
	 * Runs the Happens Before Viewer on the log file associated with the
	 * specified source file.
	 * 
	 * @param sourceFilePath The source file with absolute path.
	 * @return void
	 */
	public static void doHpv(String sourceFilePath) {

		// Check if the directory exists
		File gemDirectory = new File(getFilePath(sourceFilePath) + "/gem"); //$NON-NLS-1$
		if (!gemDirectory.exists()) {
			showInformationDialog(Messages.GemUtilities_4,
					Messages.GemUtilities_5);
			gemDirectory.mkdir();
			if (doIspcc(sourceFilePath) != -1) {
				doIsp(sourceFilePath);
			}
		}

		// Check if the file exists
		String filename = getFileName(sourceFilePath);
		File logfile = new File(gemDirectory.getAbsolutePath() + "/" + filename //$NON-NLS-1$
				+ ".gem.log"); //$NON-NLS-1$
		String logpath = logfile.getAbsolutePath();
		if (!logfile.exists()) {
			showInformationDialog(Messages.GemUtilities_4,
					Messages.GemUtilities_5);
			if (doIspcc(sourceFilePath) == -1) {
				return;
			}
			doIsp(sourceFilePath);
		}

		// Get the ispui.jar path from the preference store
		String hbvStr = GemPlugin.getDefault().getPreferenceStore().getString(
				PreferenceConstants.GEM_PREF_HBV_PATH);
		if (hbvStr != "") { //$NON-NLS-1$
			hbvStr += "/"; //$NON-NLS-1$
		}

		// Now build up command line String and run as thread
		hbvStr += "ispUI " + logpath; //$NON-NLS-1$
		runCommandAsThread(hbvStr);
	}

	/**
	 * Returns the absolute path to the specified file.
	 * 
	 * @param sourceFileWithPath The source file with absolute path.
	 * @return String The absolute path of the source code file.
	 */
	public static String getFilePath(String fileWithPath) {
		int index = fileWithPath.lastIndexOf('/');
		return fileWithPath.substring(0, index);
	}

	/**
	 * Returns only the base name of the file (without extension).
	 * 
	 * @param sourceFilePath The source file with absolute path.
	 * @return String The name of the source code file.
	 */
	public static String getFileName(String sourceFilePath) {
		int lastSlash = sourceFilePath.lastIndexOf('/');
		int lastDot = sourceFilePath.lastIndexOf('.');

		// If there is no dot
		if (lastDot == -1) {
			return sourceFilePath.substring(lastSlash + 1);
		}
		return sourceFilePath.substring(lastSlash + 1, lastDot);
	}

	/**
	 * Returns only the full name of the file.
	 * 
	 * @param sourceFilePath The source file with absolute path.
	 * @return String The name of the source code file.
	 */
	public static String getFullFileName(String sourceFilePath) {
		int lastSlash = sourceFilePath.lastIndexOf('/');
		int lastDot = sourceFilePath.lastIndexOf('.');

		// If there is no dot
		if (lastDot == -1) {
			return sourceFilePath.substring(lastSlash + 1);
		}
		return sourceFilePath.substring(lastSlash + 1);
	}

	/**
	 * Returns the path of the source files form the specified logFilePath
	 * 
	 * @param logFilePath The path of the input log file.
	 * @return String The source file path.
	 */
	public static String getSourcePathFromLog(String logFilePath) {
		// Check if the log file is where we expect it to be
		Scanner scanner = null;
		String sourceFilePath = ""; //$NON-NLS-1$

		try {
			scanner = new Scanner(new File(logFilePath));
		} catch (FileNotFoundException pie) {
			GemUtilities.showExceptionDialog(
					Messages.AnalyzerLogFilePopUpAction_0, pie);
			GemUtilities.logError(Messages.AnalyzerLogFilePopUpAction_0, pie);
		}

		// If the log file is empty
		if (!scanner.hasNextLine()) {
			GemUtilities.showErrorDialog(Messages.GemUtilities_17,
					Messages.GemUtilities_16);
			return ""; //$NON-NLS-1$
		}

		// Skip the line holding the number of processes
		scanner.nextLine();

		// If ISP exited out without running any MPI Calls (ie two deadlocks w/
		// <2 procs)
		if (!scanner.hasNextLine()) {
			GemUtilities.showErrorDialog(Messages.GemUtilities_15,
					Messages.GemUtilities_16);
			return ""; //$NON-NLS-1$
		}

		sourceFilePath = scanner.nextLine();
		sourceFilePath = sourceFilePath.substring(sourceFilePath.indexOf("/"), //$NON-NLS-1$
				sourceFilePath.lastIndexOf(" ")); //$NON-NLS-1$
		File file = new File(sourceFilePath);
		if (!file.exists()) {
			GemUtilities.showExceptionDialog(Messages.GemUtilities_14, null);
		}
		return sourceFilePath;
	}

	/**
	 * Runs the specified command as a thread.
	 * 
	 * @param command The command to run as a thread.
	 * @return void
	 */
	public static void runCommandAsThread(String command) {
		CommandThread thread = new CommandThread(command);
		thread.run();
	}

	/**
	 * Sets the number of processes for the next run of ISP.
	 * 
	 * @param none
	 * @return void
	 */
	public static void setNumProcs() {
		Shell shell = Display.getCurrent().getActiveShell();
		IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		Integer numProcs = pstore.getInt(PreferenceConstants.GEM_PREF_NUMPROCS);
		String title = Messages.GemUtilities_6;
		String message = Messages.GemUtilities_7;
		InputDialog dlg = new InputDialog(shell, title, message, numProcs
				.toString(), new NumProcsValidator());
		Window.setDefaultImage(GemPlugin.getImageDescriptor(
				"icons/processes.gif").createImage()); //$NON-NLS-1$
		dlg.open();
		numProcs = Integer.parseInt(dlg.getValue());
		pstore.setValue(PreferenceConstants.GEM_PREF_NUMPROCS, dlg.getValue());

		// Update the drop down in the Analyzer
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IViewPart gemViewPart = window.getActivePage().findView(GemAnalyzer.ID);
		GemAnalyzer analyzer = (GemAnalyzer) gemViewPart;
		analyzer.updateDropDown();
	}

	/**
	 * Activates the GEM Analyzer by sending it the source and log files
	 * 
	 * @param src The source file.
	 * @param log The log file.
	 * @return void
	 */
	public static void activateAnalyzer(String sourceFilePath,
			String logFilePath, boolean compile, boolean runIsp) {

		// Tell the Analyzer where to find the source and log files
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IViewPart gemViewPart = window.getActivePage().findView(GemAnalyzer.ID);
		GemAnalyzer analyzer = (GemAnalyzer) gemViewPart;
		analyzer.init(sourceFilePath, logFilePath, compile, runIsp);
	}

	/**
	 * Given the absolute path of the source file, this returns the name of the
	 * log file and its fully qualified path.
	 * 
	 * @param location The location of the log file.
	 * @return String The log file with absolute path.
	 */
	public static String getLogFilePathAndName(String sourceFilePath) {

		// get the name of the file
		String name = getFileName(sourceFilePath);
		String path = getFilePath(sourceFilePath);
		return path + "/gem/" + name + ".gem.log"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Logs the specified error.
	 * 
	 * @param message The error message to log.
	 * @param exception The Exception that occurred.
	 * @return void
	 */
	public static void logError(String message, Throwable exception) {
		IStatus status = new Status(IStatus.ERROR, GemPlugin.PLUGIN_ID,
				IStatus.OK, message, exception);
		GemPlugin.getDefault().getLog().log(status);
	}

	/**
	 * Simply open an ExceptionDetailsDialog for the specified exception.
	 * 
	 * @param message The message body.
	 * @param e The title Exception.
	 * @return void
	 */
	public static void showExceptionDialog(String message, Exception e) {
		String title = Messages.GemUtilities_8;
		showErrorDialog(message, title);
		e.printStackTrace();
	}

	/**
	 * Simply opens an error message dialog with the specified strings.
	 * 
	 * @param message The message body.
	 * @param title The title string.
	 * @return void
	 */
	public static void showErrorDialog(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Simply opens a dialog with the specified strings.
	 * 
	 * @param message The message body.
	 * @param title The title string.
	 * @return void
	 */
	public static void showInformationDialog(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Returns the path of the last used file relative to the workspace.
	 * 
	 * @param none
	 * @return String The path of the last used file relative to the workspace.
	 */
	public static String getLastFile() {
		IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		return pstore.getString(PreferenceConstants.GEM_PREF_LAST_FILE);
	}

	/**
	 * Saves the full path of the last used file to the preference store.
	 * 
	 * @param The path of the last used file in the workspace.
	 * @return void
	 */
	public static void saveLastFile(String relativePath) {
		IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();
		pstore.setValue(PreferenceConstants.GEM_PREF_LAST_FILE, relativePath);
	}

	/*
	 * Returns true if the specified port number is available, false otherwise.
	 */
	private static boolean isPortAvailable(int portNum) {
		try {
			ServerSocket srv = new ServerSocket(portNum);
			srv.close();
			srv = null;
			return true;
		} catch (IOException e) {
			String msg = Messages.GemUtilities_9;
			logError(msg, e);
			return false;
		}
	}

}