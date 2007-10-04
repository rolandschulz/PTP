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
package org.eclipse.ptp.tau.toolopts;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public class ToolApp {
	/**
	 * The unique ID associated with this tool
	 */
	public String toolID=null;
	
	/**
	 * The name of this tool
	 */
	public String toolName=null;
	
	/**
	 * The command used to invoke this tool
	 */
	public String toolCommand=null;
	
	/**
	 * The array of string arguments assoicated with this tool
	 */
	public String[] arguments=null;
	
//	public String queryText=null;
//	public String queryMessage=null;
	
	/**
	 * The array of toolPanes associated with this tool
	 */
	public ToolPane[] toolPanes=null;
	
	/**
	 * Returns the tool command followed by any arguments for this tool
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public String getCommand(ILaunchConfiguration configuration) throws CoreException
	{
		if(toolCommand==null || toolCommand.length()<1)
			return null;
		
		String command = toolCommand+" "+getArgs()+" "+getEnvArgs(configuration);
		
		return command;
	}
	
	/**
	 * Returns the space-seperated string of arguments stored by this tool 
	 * @return
	 */
	public String getArgs()
	{
		String args="";
		
		if(arguments!=null)
			for(int i =0;i<arguments.length;i++)
			{
				args+=" "+arguments[i];
			}
		
		return args;
	}
	
	/**
	 * Retrieves the space-seperated string of arguments stored as configuration variables by this tool's associated tool panes
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public String getEnvArgs(ILaunchConfiguration configuration) throws CoreException
	{
		String envArgs="";
		if(toolPanes!=null)
		{
			for(int i=0;i<toolPanes.length;i++)
			{
				envArgs+=" "+configuration.getAttribute(toolPanes[i].configID,"");
			}
		}
		return envArgs;
	}
}
