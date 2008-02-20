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
package org.eclipse.ptp.perf.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.perf.Activator;
import org.eclipse.ptp.perf.toolopts.PerformanceTool;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class BuildLaunchUtils {

	/**
	 * Returns the directory containing the tool's executable file.  
	 * Prompts the user for the location if it is not found.  Returns the empty string if no selection is made
	 * @param toolfind The name of the executable being sought
	 * @param suggPath The suggested path upon which to focus the directory locator window
	 * @param queryText The text asking the user to search for the binary
	 * @param queryMessage The text providing more detail on the search task
	 * @param selshell The shell in which to launch the directory locator window
	 * @return
	 */
	public static String findToolBinPath(String toolfind, String suggPath, String queryText, String queryMessage, Shell selshell)
	{
			String vtbinpath=BuildLaunchUtils.checkToolEnvPath(toolfind);
			if(vtbinpath==null||vtbinpath.equals(""))
			{
				vtbinpath=BuildLaunchUtils.askToolPath(suggPath, queryText, queryMessage, selshell);
				if(vtbinpath==null)
					vtbinpath="";
			}

		return vtbinpath;
	}
	
	/**
	 * Returns the directory containing the tool's executable file.  
	 * Prompts the user for the location if it is not found.  Returns the empty string if no selection is made
	 * @param toolfind  The name of the executable being sought
	 * @param suggPath  The suggested path upon which to focus the directory locator window
	 * @param toolName  The name of the tool used when prompting the user for its location
	 * @param selshell  The shell in which to launch the directory locator window
	 * @return
	 */
	public static String findToolBinPath(String toolfind, String suggPath, String toolName, Shell selshell)
	{
			String vtbinpath=BuildLaunchUtils.checkToolEnvPath(toolfind);
			if(vtbinpath==null||vtbinpath.equals(""))
			{
				vtbinpath=BuildLaunchUtils.askToolPath(suggPath, toolName, selshell);
				if(vtbinpath==null)
					vtbinpath="";
			}

		return vtbinpath;
	}
	
	/**
	 * Given a tool's ID, returns the path to that tool's bin directory if 
	 * already known and stored locally, otherwise returns the empty string
	 * @param toolID
	 * @return
	 */
	public static String getToolPath(String toolID)
	{
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
		String toolBinID=IPerformanceLaunchConfigurationConstants.TOOL_BIN_ID+ "."+toolID;
		String path=pstore.getString(toolBinID);
		if(path!=null)
			return path;
		return "";
	}
	
	/**
	 * Iterates through an array of tools, populating the preference store with their binary directory locations
	 * @param tools The array of tools to be checked
	 * @param force If true existing values will be overridden.
	 */
	public static void getAllToolPaths(PerformanceTool[] tools,boolean force)
	{
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();

		Shell ourshell = PlatformUI.getWorkbench().getDisplay()
				.getActiveShell();
		Iterator eIt = null;
		Map.Entry me = null;
		String entry=null;

		for (int i = 0; i < tools.length; i++) 
		{
			eIt = tools[i].groupApp.entrySet().iterator();
			while (eIt.hasNext()) 
			{
				me = (Map.Entry) eIt.next();
				entry=(String) me.getKey();
				
				if(entry.equals("internal"))
					continue;
				
				String toolBinID=IPerformanceLaunchConfigurationConstants.TOOL_BIN_ID+ "." + entry;
				if (force||pstore.getString(toolBinID).equals("")) 
				{
					pstore.setValue(toolBinID,
									BuildLaunchUtils.findToolBinPath((String) me.getValue(), null,entry, ourshell));//findToolBinPath(tools[i].pathFinder,null,tools[i].queryText,tools[i].queryMessage)
				}
			}
		}
	}
	
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
		File toolin=new File(toolname);
		
		if(test.getPath().equals(toolin.getPath()))
			return null;//TODO:  Make sure this is the right behavior when the full path is provided
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

		return askToolPath(archpath,"Select "+toolName+" Bin Directory","Please select the directory containing "+toolName+"",selshell);
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
