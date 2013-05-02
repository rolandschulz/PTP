/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.internal.etfw;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;
import org.eclipse.ptp.internal.etfw.messages.Messages;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class BuildLaunchUtils implements IBuildLaunchUtils {

	public String getWorkingDirectory() {
		return null;
	}

	Shell selshell = null;

	public BuildLaunchUtils() {
		try {
			this.selshell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		} catch (Exception e) {
			selshell = null;
		}
	}

	/**
	 * Returns the directory containing the tool's executable file. Prompts the
	 * user for the location if it is not found. Returns the empty string if no
	 * selection is made
	 * 
	 * @param toolfind
	 *            The name of the executable being sought
	 * @param suggPath
	 *            The suggested path upon which to focus the directory locator
	 *            window
	 * @param queryText
	 *            The text asking the user to search for the binary
	 * @param queryMessage
	 *            The text providing more detail on the search task
	 * @param selshell
	 *            The shell in which to launch the directory locator window
	 * @return
	 */
	public String findToolBinPath(String toolfind, String suggPath, String queryText, String queryMessage) {
		String vtbinpath = BuildLaunchUtils.checkLocalToolEnvPath(toolfind);
		if (vtbinpath == null || vtbinpath.equals("")) //$NON-NLS-1$
		{
			vtbinpath = askToolPath(suggPath, queryText, queryMessage);
			if (vtbinpath == null) {
				vtbinpath = ""; //$NON-NLS-1$
			}
		}

		return vtbinpath;
	}

	/**
	 * Returns the directory containing the tool's executable file. Prompts the
	 * user for the location if it is not found. Returns the empty string if no
	 * selection is made
	 * 
	 * @param toolfind
	 *            The name of the executable being sought
	 * @param suggPath
	 *            The suggested path upon which to focus the directory locator
	 *            window
	 * @param toolName
	 *            The name of the tool used when prompting the user for its
	 *            location
	 * @param selshell
	 *            The shell in which to launch the directory locator window
	 * @return
	 */
	public String findToolBinPath(String toolfind, String suggPath, String toolName) {
		String vtbinpath = BuildLaunchUtils.checkLocalToolEnvPath(toolfind);
		if (vtbinpath == null || vtbinpath.equals("")) //$NON-NLS-1$
		{
			vtbinpath = askToolPath(suggPath, toolName);
			if (vtbinpath == null) {
				vtbinpath = ""; //$NON-NLS-1$
			}
		}

		return vtbinpath;
	}

	/**
	 * Given a tool's ID, returns the path to that tool's bin directory if
	 * already known and stored locally, otherwise returns the empty string
	 * 
	 * @param toolID
	 * @return
	 */
	public String getToolPath(String toolID) {
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
		String toolBinID = IToolLaunchConfigurationConstants.TOOL_BIN_ID + "." + toolID; //$NON-NLS-1$
		String path = pstore.getString(toolBinID);
		if (path != null) {
			return path;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Iterates through an array of tools, populating the preference store with
	 * their binary directory locations
	 * 
	 * @param tools
	 *            The array of tools to be checked
	 * @param force
	 *            If true existing values will be overridden.
	 */
	public void getAllToolPaths(ExternalToolProcess[] tools, boolean force) {
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();

		// Shell ourshell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		Iterator<Map.Entry<String, String>> eIt = null;
		Map.Entry<String, String> me = null;
		String entry = null;

		for (ExternalToolProcess tool : tools) {
			eIt = tool.groupApp.entrySet().iterator();
			while (eIt.hasNext()) {
				me = eIt.next();
				entry = me.getKey();

				if (entry.equals("internal")) {
					continue;
				}

				String toolBinID = IToolLaunchConfigurationConstants.TOOL_BIN_ID + "." + entry; //$NON-NLS-1$
				if (force || pstore.getString(toolBinID).equals("")) //$NON-NLS-1$
				{
					pstore.setValue(toolBinID, findToolBinPath(me.getValue(), null, entry));// findToolBinPath(tools[i].pathFinder,null,tools[i].queryText,tools[i].queryMessage)
				}
			}
		}
	}

	public void verifyRequestToolPath(ExternalToolProcess tool, boolean force) {
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();

		// Shell ourshell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		Iterator<Map.Entry<String, String>> eIt = null;
		Map.Entry<String, String> me = null;
		String entry = null;

		eIt = tool.groupApp.entrySet().iterator();
		while (eIt.hasNext()) {
			me = eIt.next();
			entry = me.getKey();

			if (entry.equals("internal")) {
				continue;
			}

			String toolBinID = IToolLaunchConfigurationConstants.TOOL_BIN_ID + "." + entry; //$NON-NLS-1$
			if (force || pstore.getString(toolBinID).equals("")) //$NON-NLS-1$
			{
				pstore.setValue(toolBinID, findToolBinPath(me.getValue(), null, entry));// findToolBinPath(tools[i].pathFinder,null,tools[i].queryText,tools[i].queryMessage)
			}
		}
	}

	public static void verifyLocalEnvToolPath(ExternalToolProcess tool) {
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();

		Iterator<Map.Entry<String, String>> eIt = null;
		Map.Entry<String, String> me = null;
		String entry = null;
		String toolBinID = null;
		String curTool = null;

		eIt = tool.groupApp.entrySet().iterator();
		while (eIt.hasNext()) {
			me = eIt.next();
			entry = me.getKey();

			if (entry.equals("internal")) {
				continue;
			}

			toolBinID = IToolLaunchConfigurationConstants.TOOL_BIN_ID + "." + entry; //$NON-NLS-1$
			curTool = pstore.getString(toolBinID);

			if (curTool == null || curTool.equals("") || !(new File(curTool).exists())) //$NON-NLS-1$
			{
				String gVal = me.getValue();
				if (gVal != null && gVal.trim().length() > 0) {
					curTool = BuildLaunchUtils.checkLocalToolEnvPath(gVal);
					if (curTool != null) {
						pstore.setValue(toolBinID, curTool);// findToolBinPath(tools[i].pathFinder,null,tools[i].queryText,tools[i].queryMessage)
					}
				}
			}
		}
	}

	public String checkToolEnvPath(String toolname) {
		return checkLocalToolEnvPath(toolname);
	}

	/**
	 * This locates name of the parent of the directory containing the given
	 * tool.
	 * 
	 * @param The
	 *            name of the tool whose directory is being located
	 * @return The location of the tool's arch directory, or null if it is not
	 *         found or if the architecture is windows
	 * 
	 */
	public static String checkLocalToolEnvPath(String toolname) {
		if (org.eclipse.cdt.utils.Platform.getOS().toLowerCase().trim().indexOf("win") >= 0) {
			return null;
		}
		String pPath = null;
		try {
			Process p = new ProcessBuilder("which", toolname).start();//Runtime.getRuntime().exec("which "+toolname); //$NON-NLS-1$
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			pPath = reader.readLine();
			while (reader.readLine() != null) {
			}
			reader.close();
			reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while (reader.readLine() != null) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (pPath == null) {
			return null;
		}
		File test = new File(pPath);
		File toolin = new File(toolname);

		if (test.getPath().equals(toolin.getPath())) {
			return null;// TODO: Make sure this is the right behavior when the
		}
		// full path is provided
		if (test.exists()) {
			return test.getParentFile().getPath();
		} else {
			return null;
		}
	}

	/**
	 * Given a string as a starting point, this asks the user for the location
	 * of a tool's directory
	 * */
	public String askToolPath(String archpath, String toolText, String toolMessage) {
		// Shell
		// ourshell=PlatformUI.getWorkbench().getDisplay().getActiveShell();
		if (selshell == null) {
			return null;
		}
		DirectoryDialog dialog = new DirectoryDialog(selshell);
		dialog.setText(toolText);
		dialog.setMessage(toolMessage);
		if (archpath != null) {
			// File path = new File(archpath);
			IFileStore path = EFS.getLocalFileSystem().getStore(new Path(archpath));
			IFileInfo finf = path.fetchInfo();
			if (finf.exists()) {
				dialog.setFilterPath(!finf.isDirectory() ? archpath : path.getParent().toURI().getPath());
			}
		}
		return dialog.open();
	}

	/**
	 * Given a tool's name, ask the user for the location of the tool
	 * */
	public String askToolPath(String archpath, String toolName) {

		return askToolPath(archpath, Messages.BuildLaunchUtils_Select + toolName + Messages.BuildLaunchUtils_BinDir,
				Messages.BuildLaunchUtils_PleaseSelectDir + toolName + ""); //$NON-NLS-1$
	}

	/**
	 * Get the current timestamp
	 * 
	 * @return A formatted representation of the current time
	 */
	public static String getNow() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		String DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"; //$NON-NLS-1$
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(cal.getTime());
	}

	/**
	 * Launches a command on the local system.
	 * 
	 * @param tool
	 *            The command to be run
	 * @param env
	 *            A list of environment variables to associate with the tool
	 * @param directory
	 *            The directory where the tool is invoked
	 */
	public boolean runTool(List<String> tool, Map<String, String> env, String directory) {
		return runTool(tool, env, directory, null);
	}

	public boolean runTool(List<String> tool, Map<String, String> env, String directory, String output) {
		int eval = -1;
		try {

			OutputStream fos = null;
			if (output != null) {
				File test = new File(output);
				File parent = test.getParentFile();
				if (parent == null || !parent.canRead()) {
					output = directory + File.separator + output;
				}
				fos = new FileOutputStream(output);
			}

			ProcessBuilder pb = new ProcessBuilder(tool);
			if (directory != null) {
				pb.directory(new File(directory));
			}
			if (env != null) {
				pb.environment().putAll(env);
			}

			Process p = pb.start();// Runtime.getRuntime().exec(tool, env,
									// directory);
			StreamRunner outRun = new StreamRunner(p.getInputStream(), "out", fos); //$NON-NLS-1$
			StreamRunner errRun = new StreamRunner(p.getErrorStream(), "err", null); //$NON-NLS-1$
			outRun.start();
			errRun.start();
			outRun.join();
			eval = p.waitFor();
			if (fos != null) {
				fos.flush();
				fos.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return (eval == 0);// true;
	}

	public void runVis(List<String> tool, Map<String, String> env, String directory) {
		// int eval = -1;
		try {

			ProcessBuilder pb = new ProcessBuilder(tool);
			pb.directory(new File(directory));
			if (env != null) {
				pb.environment().putAll(env);
			}

			// Process p =
			pb.start();

		} catch (Exception e) {
			e.printStackTrace();
			// return false;
		}
		// return (eval == 0);// true;
	}

	static class StreamRunner extends Thread {
		InputStream is;
		OutputStream os;
		String type;

		StreamRunner(InputStream is, String type, OutputStream os) {
			this.is = is;
			this.os = os;
			this.type = type;
		}

		@Override
		public void run() {
			try {
				PrintWriter pw = null;
				if (os != null) {
					pw = new PrintWriter(os);
				}

				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (pw != null) {
						pw.println(line);
					} else {
						System.out.println(line);
					}
				}
				if (pw != null) {
					pw.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public IFileStore getFile(String path) {

		return EFS.getLocalFileSystem().getStore(URI.create(path));
	}

	public byte[] runToolGetOutput(List<String> tool, Map<String, String> env, String directory, boolean showErr) {
		int eval = -1;
		byte[] out = null;
		try {

			ByteArrayOutputStream fos = new ByteArrayOutputStream();

			ProcessBuilder pb = new ProcessBuilder(tool);
			if (directory != null) {
				pb.directory(new File(directory));
			}
			if (env != null) {
				pb.environment().putAll(env);
			}
			pb.redirectErrorStream(showErr);
			Process p = pb.start();// Runtime.getRuntime().exec(tool, env,
									// directory);
			StreamRunner outRun = new StreamRunner(p.getInputStream(), "out", fos); //$NON-NLS-1$
			StreamRunner errRun = new StreamRunner(p.getErrorStream(), "err", null); //$NON-NLS-1$
			outRun.start();
			errRun.start();
			outRun.join();
			eval = p.waitFor();
			if (fos != null) {
				fos.flush();
				out = fos.toByteArray();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (eval != 0) {
			return null;
		}
		return out;// true;
	}

	public boolean isRemote() {

		return false;
	}

	public byte[] runToolGetOutput(List<String> tool, Map<String, String> env, String directory) {
		return runToolGetOutput(tool, env, directory, false);
	}

}
