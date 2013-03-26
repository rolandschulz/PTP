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
package org.eclipse.ptp.etfw.internal;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.ems.core.EnvManagerProjectProperties;
import org.eclipse.ptp.ems.core.EnvManagerRegistry;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.ems.core.IEnvManagerConfig;
import org.eclipse.ptp.etfw.Activator;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.messages.Messages;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class RemoteBuildLaunchUtils implements IBuildLaunchUtils {
	Shell selshell = null;
	ILaunchConfiguration config;
	IRemoteConnection conn = null;
	IRemoteServices remoteServices = null;
	IRemoteUIServices remoteUIServices = null;
	IRemoteConnectionManager connMgr = null;
	IRemoteUIFileManager fileManagerUI = null;
	IRemoteFileManager fileManager = null;
	IEnvManagerConfig envMgrConfig = null;
	private IEnvManager envManager = null;

	public RemoteBuildLaunchUtils(ILaunchConfiguration config) {
		this.config = config;
		remoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(LaunchUtils.getRemoteServicesId(config), null);// ,getLaunchConfigurationDialog()
		remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
		connMgr = remoteServices.getConnectionManager();
		conn = connMgr.getConnection(LaunchUtils.getConnectionName(config));
		fileManagerUI = remoteUIServices.getUIFileManager();
		fileManagerUI.setConnection(conn);
		fileManager = remoteServices.getFileManager(conn);
		envMgrConfig = getEnvManagerConfig(config);
		if (envMgrConfig != null) {
			envManager = EnvManagerRegistry.getEnvManager(null, conn);
			// if (envManager != null) {
			// //moduleSetup = envManager.getBashConcatenation(";", false, envMgrConfig, null);
			// moduleSetup = envManager.createBashScript(null, false, config, commandToExecuteAfterward)
			//
			// }
		}

		// this.selshell=PlatformUI.getWorkbench().getDisplay().getActiveShell();
	}

	public RemoteBuildLaunchUtils(IRemoteConnection conn) {
		this.conn = conn;
		remoteServices = conn.getRemoteServices();
		remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
		connMgr = remoteServices.getConnectionManager();
		// conn = connMgr.getConnection(LaunchUtils.getConnectionName(config));
		fileManagerUI = remoteUIServices.getUIFileManager();
		fileManager = remoteServices.getFileManager(conn);

		// Can we get the envManager from the connection if we need it?
		// envManager = null;
	}

	public String getWorkingDirectory() {
		return conn.getWorkingDirectory();
	}

	/**
	 * Returns the directory containing the tool's executable file. Prompts the user for the location if it is not found. Returns
	 * the empty string if no selection is made
	 * 
	 * @param toolfind
	 *            The name of the executable being sought
	 * @param suggPath
	 *            The suggested path upon which to focus the directory locator window
	 * @param queryText
	 *            The text asking the user to search for the binary
	 * @param queryMessage
	 *            The text providing more detail on the search task
	 * @param selshell
	 *            The shell in which to launch the directory locator window
	 * @return
	 */
	public String findToolBinPath(String toolfind, String suggPath, String queryText, String queryMessage) {
		String vtbinpath = checkToolEnvPath(toolfind);
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
	 * Returns the directory containing the tool's executable file. Prompts the user for the location if it is not found. Returns
	 * the empty string if no selection is made
	 * 
	 * @param toolfind
	 *            The name of the executable being sought
	 * @param suggPath
	 *            The suggested path upon which to focus the directory locator window
	 * @param toolName
	 *            The name of the tool used when prompting the user for its location
	 * @param selshell
	 *            The shell in which to launch the directory locator window
	 * @return
	 */
	public String findToolBinPath(String toolfind, String suggPath, String toolName) {
		String vtbinpath = checkToolEnvPath(toolfind);
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
	 * Given a tool's ID, returns the path to that tool's bin directory if already known and stored locally, otherwise returns the
	 * empty string
	 * 
	 * @param toolID
	 * @return
	 */
	public String getToolPath(String toolID) {
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
		String toolBinID = null;
		if (config != null) {

			toolBinID = IToolLaunchConfigurationConstants.TOOL_BIN_ID
					+ "." + toolID + "." + LaunchUtils.getResourceManagerUniqueName(config); //$NON-NLS-1$//$NON-NLS-2$
		} else {
			toolBinID = IToolLaunchConfigurationConstants.TOOL_BIN_ID
					+ "." + toolID + "." + conn.getName(); //$NON-NLS-1$//$NON-NLS-2$
		}
		String path = pstore.getString(toolBinID);
		if (path != null) {
			return path;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Iterates through an array of tools, populating the preference store with their binary directory locations
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

		for (int i = 0; i < tools.length; i++) {
			eIt = tools[i].groupApp.entrySet().iterator();
			while (eIt.hasNext()) {
				me = eIt.next();
				entry = me.getKey();

				if (entry.equals("internal")) { //$NON-NLS-1$
					continue;
				}

				String toolBinID = IToolLaunchConfigurationConstants.TOOL_BIN_ID
						+ "." + entry + "." + LaunchUtils.getResourceManagerUniqueName(config); //$NON-NLS-1$ //$NON-NLS-2$
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

			if (entry.equals("internal")) { //$NON-NLS-1$
				continue;
			}

			String toolBinID = IToolLaunchConfigurationConstants.TOOL_BIN_ID
					+ "." + entry + "." + LaunchUtils.getResourceManagerUniqueName(config); //$NON-NLS-1$ //$NON-NLS-2$
			if (force || pstore.getString(toolBinID).equals("")) //$NON-NLS-1$
			{
				pstore.setValue(toolBinID, findToolBinPath(me.getValue(), null, entry));// findToolBinPath(tools[i].pathFinder,null,tools[i].queryText,tools[i].queryMessage)
			}
		}
	}

	public void verifyEnvToolPath(ExternalToolProcess tool) {
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

			if (entry.equals("internal")) { //$NON-NLS-1$
				continue;
			}

			toolBinID = IToolLaunchConfigurationConstants.TOOL_BIN_ID
					+ "." + entry + "." + LaunchUtils.getResourceManagerUniqueName(config); //$NON-NLS-1$ //$NON-NLS-2$
			curTool = pstore.getString(toolBinID);

			IFileStore ttool = fileManager.getResource(curTool);

			if (curTool == null || curTool.equals("") || !(ttool.fetchInfo().exists())) //$NON-NLS-1$
			{
				String gVal = me.getValue();
				if (gVal != null && gVal.trim().length() > 0) {
					curTool = checkToolEnvPath(gVal);
					if (curTool != null) {
						pstore.setValue(toolBinID, curTool);// findToolBinPath(tools[i].pathFinder,null,tools[i].queryText,tools[i].queryMessage)
					}
				}
			}
		}
	}

	/**
	 * Get the environment manager configuration associated with the project that was specified in the launch configuration. If no
	 * launchConfiguration was specified then this CommandJob does not need to use environment management so we can safely return
	 * null.
	 * 
	 * @return environment manager configuration or null if no configuration can be found
	 */
	private IEnvManagerConfig getEnvManagerConfig(ILaunchConfiguration configuration) {
		try {
			String projectName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
			if (projectName != null) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (project != null) {
					final EnvManagerProjectProperties projectProperties = new EnvManagerProjectProperties(project);
					if (projectProperties.isEnvMgmtEnabled()) {
						return projectProperties;
					}
				}
			}
		} catch (CoreException e) {
			// Ignore
		}
		return null;
	}

	/**
	 * This locates name of the parent of the directory containing the given tool.
	 * 
	 * @param The
	 *            name of the tool whose directory is being located
	 * @return The location of the tool's arch directory, or null if it is not found or if the architecture is windows
	 * 
	 */
	public String checkToolEnvPath(String toolname) {
		if (org.eclipse.cdt.utils.Platform.getOS().toLowerCase().trim().indexOf("win") >= 0 && !this.isRemote()) {//$NON-NLS-1$
			return null;
		}
		String pPath = null;
		try {
			IRemoteProcessBuilder rpb = remoteServices.getProcessBuilder(conn);
			if (envManager != null) {
				String com = "";
				try {
					com = envManager.createBashScript(null, false, envMgrConfig, "which " + toolname);
					IFileStore envScript = fileManager.getResource(com);
					IFileInfo envInfo = envScript.fetchInfo();
					envInfo.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE, true);
					envInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
					envScript.putInfo(envInfo, EFS.SET_ATTRIBUTES, null);

				} catch (RemoteConnectionException e) {
					e.printStackTrace();
					return null;
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				rpb.command(com);
			}
			else {
				rpb.command("which", toolname);//$NON-NLS-1$
			}
			// rpb.
			IRemoteProcess p = rpb.start();
			//Process p = new ProcessBuilder("which", toolname).start();//Runtime.getRuntime().exec("which "+toolname); //$NON-NLS-1$
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;// = reader.readLine();

			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				IFileStore test = fileManager.getResource(line);
				if (test.fetchInfo().exists())
				{
					pPath = line;
				}
			}
			reader.close();
			reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (pPath == null) {
			return null;
		}

		IFileStore test = fileManager.getResource(pPath);
		// File test = new File(pPath);
		// IFileStore toolin = fileManager.getResource(toolname);
		// File toolin = new File(toolname);
		String name = test.fetchInfo().getName();
		if (!name.equals(toolname)) {
			return null;// TODO: Make sure this is the right behavior when the
		}
		// full path is provided
		if (test.fetchInfo().exists()) {
			return test.getParent().toURI().getPath();// pPath;//test.getParent().fetchInfo().getName();
														// //.getParentFile().getPath();
		} else {
			return null;
		}
	}

	/**
	 * Given a string as a starting point, this asks the user for the location of a tool's directory
	 * */
	public String askToolPath(String archpath, String toolText, String toolMessage) {
		// Shell
		// ourshell=PlatformUI.getWorkbench().getDisplay().getActiveShell();
		if (selshell == null||selshell.isDisposed()) {
			selshell = PlatformUI.getWorkbench().getDisplay().getShells()[0];
		}

		// DirectoryDialog dialog = new DirectoryDialog(selshell);
		// dialog.setText(toolText);
		// dialog.setMessage(toolMessage);

		if (archpath != null) {
			// File path = new File(archpath);
			// IFileStore path = fileManager.getResource(archpath);////EFS.getLocalFileSystem().getStore(new Path(archpath));
			// IFileInfo finf=path.fetchInfo();
			// if (finf.exists()) {
			// dialog.setFilterPath(!finf.isDirectory() ? archpath : path.getParent().toURI().getPath());
			// }//TODO: We may actually want to use this initial directory checking
		}
		return fileManagerUI.browseDirectory(selshell, toolMessage, archpath, EFS.NONE);// dialog.open();
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
		String DATE_FORMAT = "yyyy-MM-dd_HH:mm:ss"; //$NON-NLS-1$
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

	public IFileStore getFile(String path) {
		return fileManager.getResource(path);
	}

	public boolean runTool(List<String> tool, Map<String, String> env, String directory, String output) {
		int eval = -1;
		try {

			OutputStream fos = null;
			if (output != null) {
				IFileStore test = fileManager.getResource(output);
				// File test = new File(output);
				IFileStore parent = test.getParent();// fileManager.getResource
				// File parent = test.getParentFile();
				if (parent == null || !parent.fetchInfo().exists()) {
					parent = fileManager.getResource(directory);
					test = parent.getChild(output);
					// output = directory + File.separator + output;
				}
				fos = test.openOutputStream(EFS.NONE, null);// test.openOutputStream(options, monitor)new FileOutputStream(test);
			}

			// IRemoteProcessBuilder pb = remoteServices.getProcessBuilder(conn, tool);//new IRemoteProcessBuilder(tool);
			// pb.directory(fileManager.getResource(directory));
			// if (env != null) {
			// pb.environment().putAll(env);
			// }

			IRemoteProcess p = getProcess(tool, env, directory, false);// pb.start();// Runtime.getRuntime().exec(tool, env,
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

	public byte[] runToolGetOutput(List<String> tool, Map<String, String> env, String directory) {
		return runToolGetOutput(tool, env, directory, false);
	}

	public byte[] runToolGetOutput(List<String> tool, Map<String, String> env, String directory, boolean showErr) {
		int eval = -1;
		byte[] out = null;
		try {

			ByteArrayOutputStream fos = new ByteArrayOutputStream();// null;
			// if (output != null) {
			// IFileStore test = fileManager.getResource(output);
			// //File test = new File(output);
			// IFileStore parent = test.getParent();//fileManager.getResource
			// //File parent = test.getParentFile();
			// if (parent == null || !parent.fetchInfo().exists()) {
			// parent = fileManager.getResource(directory);
			// test=parent.getChild(output);
			// //output = directory + File.separator + output;
			// }
			// fos = test.openOutputStream(EFS.NONE, null);//test.openOutputStream(options, monitor)new FileOutputStream(test);
			// }

			IRemoteProcess p = getProcess(tool, env, directory, showErr);// pb.start();// Runtime.getRuntime().exec(tool, env,
			// directory);
			StreamRunner outRun = new StreamRunner(p.getInputStream(), "out", fos); //$NON-NLS-1$
			StreamRunner errRun = null;

			errRun = new StreamRunner(p.getErrorStream(), "err", null); //$NON-NLS-1$

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

		return out;
	}

	private IRemoteProcess getProcess(List<String> tool, Map<String, String> env, String directory, boolean mergeOutput)
			throws IOException {

		IRemoteProcessBuilder pb;

		if (envManager != null) {
			String com = "";
			String concat = "";
			try {
				concat = "";
				for (int i = 0; i < tool.size(); i++) {
					concat += " " + tool.get(i);
				}
				com = envManager.createBashScript(null, false, envMgrConfig, concat);
				IFileStore envScript = fileManager.getResource(com);
				IFileInfo envInfo = envScript.fetchInfo();
				envInfo.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE, true);
				envScript.putInfo(envInfo, EFS.SET_ATTRIBUTES, null);

			} catch (RemoteConnectionException e) {
				e.printStackTrace();
				return null;
			} catch (CoreException e) {
				e.printStackTrace();
			}
			pb = remoteServices.getProcessBuilder(conn, com);
		}
		else {
			pb = remoteServices.getProcessBuilder(conn, tool);// new IRemoteProcessBuilder(tool);
		}
		if (directory != null) {
			pb.directory(fileManager.getResource(directory));
		}
		if (env != null) {
			pb.environment().putAll(env);
		}

		pb.redirectErrorStream(mergeOutput);

		return pb.start();
	}

	public void runVis(List<String> tool, Map<String, String> env, String directory) {
		// int eval = -1;
		try {
			//
			// IRemoteProcessBuilder pb = remoteServices.getProcessBuilder(conn, tool);//new IRemoteProcessBuilder(tool);
			// pb.directory(fileManager.getResource(directory));
			// if (env != null) {
			// pb.environment().putAll(env);
			// }
			//
			// //Process p =
			// pb.start();

			if (env == null)
				env = new HashMap<String, String>();

			if (env.get("DISPLAY") == null)
				env.put("DISPLAY", ":0.0");

			getProcess(tool, env, directory, false);

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

	public boolean isRemote() {
		return true;
	}

}
