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
package org.eclipse.ptp.tau.performance.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class BuildLaunchUtils {

	/**
	 * This locates name of the parent of the directory containing the given tool.
	 * @param The name of the tool whose directory is being located
	 * @return The location of the tool's arch directory, or null if it is not found
	 * or if the architecture is windows
	 * 
	 */
	public static String checkToolEnvPath(String toolname) {
		if(org.eclipse.cdt.utils.Platform.getOS().toLowerCase().trim().indexOf("win")>=0)
			return null;
		String pPath = null;
		try {
			Process p = Runtime.getRuntime().exec("which "+toolname);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			pPath = reader.readLine();
			while (reader.readLine() != null) {
			}
			reader.close();
			reader = new BufferedReader(new InputStreamReader(p
					.getErrorStream()));
			while (reader.readLine() != null) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (pPath == null)
			return null;
		File test = new File(pPath);
		if (test.exists()) {
			return test.getParentFile().getPath();
		} else
			return null;
	}
	
	/**
	 * Given a string as a starting point, this asks the user for
	 * the location of a tool's directory
	 * */
	public static String askToolPath(String archpath, String toolText, String toolMessage, Shell selshell) {
		//Shell ourshell=PlatformUI.getWorkbench().getDisplay().getActiveShell();
		if(selshell==null)
			return null;
		DirectoryDialog dialog = new DirectoryDialog(selshell);
		dialog.setText(toolText);
		dialog.setMessage(toolMessage);
		if (archpath != null) {
			File path = new File(archpath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? archpath : path
						.getParent());
			}
		}
		return dialog.open();
	}
	
	/**
	 * Given a tool's name, ask the user for the location of the tool
	 * */
	public static String askToolPath(String archpath, String toolName, Shell selshell) {

		return askToolPath(archpath,"Select "+toolName+" Bin Directory","Please select the directory containing "+toolName+"'s executable files.",selshell);
	}
	
	/**
	 * Get the current timestamp
	 * @return A formatted representation of the current time
	 */
	public static String getNow(){
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        String DATE_FORMAT = "yyyy-MM-dd_HH:mm:ss";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(cal.getTime());
	}
	
	/**
	 * Launches a command on the local system.
	 * @param tool The command to be run
	 * @param env A list of environment variables to associate with the tool
	 * @param directory The directory where the tool is invoked
	 */
	public static void runTool(String tool, String[] env, File directory)
	{
		String s = new String();
		try {
			Process p = Runtime.getRuntime().exec(tool, env, directory);
			int i = p.waitFor();
			if (i == 0)
			{
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				//read the output from the command
				while ((s = stdInput.readLine()) != null) 
				{
					System.out.println(s);
				}
			}
			else 
			{
				BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				//read the output from the command
				while ((s = stdErr.readLine()) != null) 
				{
					System.out.println(s);
				}
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
}
