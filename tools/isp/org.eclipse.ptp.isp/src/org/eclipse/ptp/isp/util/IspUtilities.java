/*******************************************************************************
 * Copyright (c) 2009 University of Utah School of Computing
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

package org.eclipse.ptp.isp.util;

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
import org.eclipse.ptp.isp.ISPPlugin;
import org.eclipse.ptp.isp.messages.Messages;
import org.eclipse.ptp.isp.preferences.PreferenceConstants;
import org.eclipse.ptp.isp.views.ISPAnalyze;
import org.eclipse.ptp.isp.views.ISPConsole;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class IspUtilities {

	/**
	 * Executes the specified command process via the Runtime instance.
	 * 
	 * @param command
	 *            The command to send to the Runtime instance.
	 * @param verbose
	 *            Outputs to ISPConsole if true, silent otherwise.
	 * @return int - 1 if everything went smoothly, -1 otherwise.
	 */
	public static int runCommand(String command) {

		// Get a handle on the ISP Console
		IViewPart ISPViewPart = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().findView(
						ISPConsole.ID);
		ISPConsole ispCon = (ISPConsole) ISPViewPart;

		try {
			Process p = Runtime.getRuntime().exec(command);
			String stdIn = ""; //$NON-NLS-1$
			String stdInResult = ""; //$NON-NLS-1$
			BufferedReader inReader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			// read stdin for the command process
			while ((stdIn = inReader.readLine()) != null) {
				stdInResult += stdIn + "\n"; //$NON-NLS-1$
			}
			ispCon.write(stdInResult);

			return 1;
		} catch (Exception e) {
			String message = Messages.IspUtilities_0;
			showExceptionDialog(message, e);
			logError(Messages.IspUtilities_1, e);
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Compiles the specified file with ispcc.
	 * 
	 * @param sourceFilePath
	 *            The source file with absolute path.
	 * @return int 1 if everything went smoothly, -1 otherwise.
	 */
	public static int doIspcc(String sourceFilePath) {
		// Create our isp directory to hold the generated executable
		File ispdir = new File(getFilePath(sourceFilePath) + "/isp"); //$NON-NLS-1$
		ispdir.mkdir();

		// Build up command line String
		String exePath = ispdir.getAbsolutePath() + "/" //$NON-NLS-1$
				+ getFileName(sourceFilePath);

		// Get the ispcc path from the preference store
		String ispcc = ISPPlugin.getDefault().getPreferenceStore().getString(
				PreferenceConstants.ISP_PREF_ISPCC_PATH);
		if (ispcc != "") { //$NON-NLS-1$
			ispcc += "/"; //$NON-NLS-1$
		}
		ispcc += "ispcc -o " + exePath + ".isp " + sourceFilePath; //$NON-NLS-1$ //$NON-NLS-2$

		// Run the command and return whether or not it worked
		int error = runCommand(ispcc);

		// if ispcc ran smoothly
		if (error == 1) {
			File executable = new File(exePath + ".isp"); //$NON-NLS-1$
			if (!executable.canRead()) {
				showErrorDialog(Messages.IspUtilities_2,
						Messages.IspUtilities_3);
				return -1;
			}
		}
		return error;
	}

	/**
	 * Runs ISP on the specified file.
	 * 
	 * @param sourceFilePath
	 *            The source file with absolute path.
	 * @return void
	 */
	public static void doIsp(String sourceFilePath) {

		// Get all the current preferences
		IPreferenceStore pstore = ISPPlugin.getDefault().getPreferenceStore();

		// Create our isp directory to hold the generated log file
		File ispdir = new File(getFilePath(sourceFilePath) + "/isp"); //$NON-NLS-1$
		ispdir.mkdir();

		File executable = null;
		String filename = ""; //$NON-NLS-1$

		if (!sourceFilePath.endsWith(".isp")) { //$NON-NLS-1$
			// doIspcc(sourceFilePath);
			filename = getFileName(sourceFilePath);
			executable = new File(ispdir.getAbsolutePath() + "/" + filename //$NON-NLS-1$
					+ ".isp"); //$NON-NLS-1$
			filename += ".isp"; //$NON-NLS-1$
		} else {
			filename = getFullFileName(sourceFilePath);
			executable = new File(sourceFilePath);
		}

		// Start building our string to pass to ISP
		String exePath = executable.toString();

		int numprocs = pstore.getInt(PreferenceConstants.ISP_PREF_NUMPROCS);
		int portnum = pstore.getInt(PreferenceConstants.ISP_PREF_PORTNUM);
		int reportnum = pstore.getInt(PreferenceConstants.ISP_PREF_REPORTNUM);
		boolean fibPreference = pstore
				.getBoolean(PreferenceConstants.ISP_PREF_FIB_OPTION);
		boolean mpicallsPreference = pstore
				.getBoolean(PreferenceConstants.ISP_PREF_MPICALLS_OPTION);
		boolean openmpPreference = pstore
				.getBoolean(PreferenceConstants.ISP_PREF_OPENMP_OPTION);
		boolean blockingsendsPreference = pstore
				.getBoolean(PreferenceConstants.ISP_PREF_BLOCK_OPTION);
		boolean reportPreference = pstore
				.getBoolean(PreferenceConstants.ISP_PREF_REPORT_OPTION);
		boolean verbosePreference = pstore
				.getBoolean(PreferenceConstants.ISP_PREF_VERBOSE);
		String isp = pstore.getString(PreferenceConstants.ISP_PREF_ISPEXE_PATH);
		if (isp != "") { //$NON-NLS-1$
			isp += "/"; //$NON-NLS-1$
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
		pstore.setValue(PreferenceConstants.ISP_PREF_PORTNUM, portnum);

		// Build up command line String
		String ispCmd = isp + "isp -n " + numprocs + " -p " + portnum; //$NON-NLS-1$ //$NON-NLS-2$

		// Now add command line options
		ispCmd = ispCmd
				+ " " + ((blockingsendsPreference) ? "-b " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ ((mpicallsPreference) ? "-m " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((verbosePreference) ? "-O " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((openmpPreference) ? "-s " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((reportPreference) ? "-r " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((reportPreference) ? reportnum + " " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((fibPreference) ? "-f " : "") + "-l " + ispdir.getAbsolutePath() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "/" + filename + ".log " + exePath; //$NON-NLS-1$ //$NON-NLS-2$

		runCommand(ispCmd);
	}

	/**
	 * Runs the ISP Java GUI on the log file associated with the specified
	 * source file.
	 * 
	 * @param sourceFilePath
	 *            The source file with absolute path.
	 * @return void
	 */
	public static void doJavaGUI(String sourceFilePath) {

		// Check if the directory exists
		File ispdir = new File(getFilePath(sourceFilePath) + "/isp"); //$NON-NLS-1$
		if (!ispdir.exists()) {
			showInformationDialog(Messages.IspUtilities_4,
					Messages.IspUtilities_5);
			ispdir.mkdir();
			if (doIspcc(sourceFilePath) != -1) {
				doIsp(sourceFilePath);
			}
		}

		// Check if the file exists
		String filename = getFileName(sourceFilePath);
		File logfile = new File(ispdir.getAbsolutePath() + "/" + filename //$NON-NLS-1$
				+ ".isp.log"); //$NON-NLS-1$
		String logpath = logfile.getAbsolutePath();
		if (!logfile.exists()) {
			showInformationDialog(Messages.IspUtilities_4,
					Messages.IspUtilities_5);
			if (doIspcc(sourceFilePath) == -1) {
				return;
			}
			doIsp(sourceFilePath);
		}

		// Get the ispui.jar path from the preference store
		String ispui = ISPPlugin.getDefault().getPreferenceStore().getString(
				PreferenceConstants.ISP_PREF_UI_PATH);
		if (ispui != "") { //$NON-NLS-1$
			ispui += "/"; //$NON-NLS-1$
		}

		// Now build up command line String and run as thread
		ispui += "ispUI " + logpath; //$NON-NLS-1$
		runCommandAsThread(ispui);
	}

	/**
	 * Returns the absolute path to the specified file.
	 * 
	 * @param sourceFileWithPath
	 *            The source file with absolute path.
	 * @return String The absolute path of the source code file.
	 */
	public static String getFilePath(String fileWithPath) {
		int index = fileWithPath.lastIndexOf('/');
		return fileWithPath.substring(0, index);
	}

	/**
	 * Returns only the base name of the file (without extension).
	 * 
	 * @param sourceFilePath
	 *            The source file with absolute path.
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
	 * @param sourceFilePath
	 *            The source file with absolute path.
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
	 * Returns the path
	 * 
	 */
	public static String getSourcePathFromLog(String logFilePath) {
		// Check if the log file is where we expect it to be
		Scanner scanner = null;
		String sourceFilePath = ""; //$NON-NLS-1$

		try {
			scanner = new Scanner(new File(logFilePath));
		} catch (FileNotFoundException pie) {
			IspUtilities.showExceptionDialog(
					Messages.AnalyzeLogFilePopUpAction_0, pie);
			IspUtilities.logError(Messages.AnalyzeLogFilePopUpAction_0, pie);
		}
		scanner.nextLine();
		sourceFilePath = scanner.nextLine();
		sourceFilePath = sourceFilePath.substring(sourceFilePath.indexOf("/"), //$NON-NLS-1$
				sourceFilePath.lastIndexOf(".") + 2); //$NON-NLS-1$
		File file = new File(sourceFilePath);
		if (!file.exists()) {
			IspUtilities.showExceptionDialog(Messages.IspUtilities_14, null);
		}
		return sourceFilePath;
	}

	/**
	 * Runs the specified command as a thread.
	 * 
	 * @param command
	 *            The command to run as a thread.
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
		IPreferenceStore pstore = ISPPlugin.getDefault().getPreferenceStore();
		Integer numProcs = pstore.getInt(PreferenceConstants.ISP_PREF_NUMPROCS);
		String title = Messages.IspUtilities_6;
		String message = Messages.IspUtilities_7;
		InputDialog dlg = new InputDialog(shell, title, message, numProcs
				.toString(), new NumProcsValidator());
		Window.setDefaultImage(ISPPlugin.getImageDescriptor(
				"icons/processes.gif").createImage()); //$NON-NLS-1$
		dlg.open();
		numProcs = Integer.parseInt(dlg.getValue());
		pstore.setValue(PreferenceConstants.ISP_PREF_NUMPROCS, dlg.getValue());

		// Update the drop down in the Analyzer
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IViewPart ISPViewPart = window.getActivePage().findView(ISPAnalyze.ID);
		ISPAnalyze analyzer = (ISPAnalyze) ISPViewPart;
		analyzer.updateDropDown();
	}

	/**
	 * Activates the ISP Analyzer by sending it the source and log files
	 * 
	 * @param src
	 *            The source file.
	 * @param log
	 *            The log file.
	 * @return void
	 */
	public static void activateAnalyzer(String sourceFilePath,
			String logFilePath, boolean compile, boolean runIsp) {

		// Tell the Analyzer where to find the source and log files
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IViewPart ISPViewPart = window.getActivePage().findView(ISPAnalyze.ID);
		ISPAnalyze analyzer = (ISPAnalyze) ISPViewPart;
		analyzer.start(sourceFilePath, logFilePath, compile, runIsp);
	}

	/**
	 * Given the absolute path of the source file, this returns where the log
	 * file should be located.
	 * 
	 * @param location
	 *            The location of the log file.
	 * @return String The log file with absolute path.
	 */
	public static String getLogFile(String sourceFilePath) {

		// get the name of the file
		String name = getFileName(sourceFilePath);
		String path = getFilePath(sourceFilePath);
		return path + "/isp/" + name + ".isp.log"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Logs the specified error.
	 * 
	 * @param message
	 *            The error message to log.
	 * @param exception
	 *            The Exception that occurred.
	 * @return void
	 */
	public static void logError(String message, Throwable exception) {
		IStatus status = new Status(IStatus.ERROR, ISPPlugin.PLUGIN_ID,
				IStatus.OK, message, exception);
		ISPPlugin.getDefault().getLog().log(status);
	}

	/**
	 * Simply open an ExceptionDetailsDialog for the specified exception.
	 * 
	 * @param message
	 *            The message body.
	 * @param e
	 *            The title Exception.
	 * @return void
	 */
	public static void showExceptionDialog(String message, Exception e) {
		String title = Messages.IspUtilities_8;
		showErrorDialog(message, title);
		e.printStackTrace();
	}

	/**
	 * Simply opens an error message dialog with the specified strings.
	 * 
	 * @param message
	 *            The message body.
	 * @param title
	 *            The title string.
	 * @return void
	 */
	public static void showErrorDialog(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Simply opens a dialog with the specified strings.
	 * 
	 * @param message
	 *            The message body.
	 * @param title
	 *            The title string.
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
	 * @return The path of the last used file relative to the workspace.
	 */
	public static String getLastFile() {
		IPreferenceStore pstore = ISPPlugin.getDefault().getPreferenceStore();
		return pstore.getString(PreferenceConstants.ISP_PREF_LAST_FILE);
	}

	/**
	 * Saves the full path of the last used file to the preference store.
	 * 
	 * @param none
	 * @return The path of the last used file inthe workspace.
	 */
	public static void saveLastFile(String relativePath) {
		IPreferenceStore pstore = ISPPlugin.getDefault().getPreferenceStore();
		pstore.setValue(PreferenceConstants.ISP_PREF_LAST_FILE, relativePath);
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
			String msg = Messages.IspUtilities_9;
			logError(msg, e);
			return false;
		}
	}

}