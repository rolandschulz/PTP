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
	
//	public String outputID=null;
//	public String outputType=null;
//	public String outputArg=null;
	
	/**
	 * Matched to available output ID's to determine the path and arguments to files to be read/processed
	 */
	public String inputID=null;
	
	/**
	 * Tools in the same group are assumed to share the same binary directory location
	 */
	public String toolGroup=null;
	
	/**
	 * The array of toolPanes associated with this tool
	 */
	public ToolPane[] toolPanes=null;
	
	public ToolIO[] inputArgs=null;
	public ToolIO[] outputArgs=null;
	
//	/**
//	 * Returns the tool command followed by any arguments for this tool
//	 * @param configuration
//	 * @return
//	 * @throws CoreException
//	 */
//	public String getCommand(ILaunchConfiguration configuration) throws CoreException
//	{
//		if(toolCommand==null || toolCommand.length()<1)
//			return null;
//		
//		String command = toolCommand+" "+getArguments(configuration);// +" "+ getIO();
//		
//		return command;
//	}
	
	public String getArguments(ILaunchConfiguration configuration) throws CoreException
	{
		return getArgs()+" "+getPaneArgs(configuration);
	}
	
//	public String[] getCommandArray(ILaunchConfiguration configuration) throws CoreException
//	{
//		if(toolCommand==null || toolCommand.length()<1)
//			return null;
//		
//		int totargs=1;
//		if(arguments!=null)
//		{
//			totargs+=arguments.length;
//		}
//		if(toolPanes!=null)
//		{
//			totargs+=toolPanes.length;
//		}
//		String[] command=new String[totargs];
//		
//		int i=0;
//		command[i]=toolCommand;
//		i++;
//		if(arguments!=null)
//		{
//			for(int j=0;j<arguments.length;j++)
//			{
//				command[i]=arguments[j];
//				j++;
//			}
//		}
//		
//		if(toolPanes!=null)
//		{
//			for(int j=0;j<toolPanes.length;j++)
//			{
//				command[i]=configuration.getAttribute(toolPanes[j].configID,"");
//				j++;
//			}
//		}
//		
//		return command;
//	}
	
	/**
	 * Returns the space-seperated string of arguments stored by this tool 
	 * @return
	 */
	private String getArgs()
	{
		String args="";
		
		if(arguments!=null)
			for(int i =0;i<arguments.length;i++)
			{
				if(arguments[i]!=null)
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
	private String getPaneArgs(ILaunchConfiguration configuration) throws CoreException
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
	
//	public String getIO()
//	{
//		String args="";
//		
//		if(inputArgs!=null)
//		{
//			for(int i=0;i<inputArgs.length;i++)
//			{
//				args+=inputArgs[i].getInputArg()+" ";
//			}
//		}
//		if(outputArgs!=null)
//		{
//			for(int i=0;i<outputArgs.length;i++)
//			{
//				args+=outputArgs[i].getOutputArg()+" ";
//			}
//		}
//		return args;
//	}
}