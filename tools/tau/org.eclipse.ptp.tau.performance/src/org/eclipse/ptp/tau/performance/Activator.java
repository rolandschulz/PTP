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
package org.eclipse.ptp.tau.performance;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ptp.tau.performance.internal.BuildLaunchUtils;
import org.eclipse.ptp.tau.performance.internal.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.tau.toolopts.PerformanceTool;
import org.eclipse.ptp.tau.toolopts.ToolMaker;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.tau.performance";

	// The shared instance
	private static Activator plugin;
	
	private static PerformanceTool[] tools=null;
	
	public static PerformanceTool[] getTools()
	{
		return tools;
	}
	
	/**
	 * Returns the performance tool with the given name from the performance tools array, or null if not found
	 * @param toolName
	 * @return
	 */
	public static PerformanceTool getTool(String toolName)
	{
		for(int i=0;i<tools.length;i++)
		{
			if(tools[i].toolName.equals(toolName))
			{
				return tools[i];
			}
		}
		return null;
	}
	
	/**
	 * Returns the performance tool at index dex in the performance tools array
	 * @param dex
	 * @return
	 */
	public static PerformanceTool getTool(int dex)
	{
		if(dex<tools.length)
			return tools[dex];
		else return null;
	}
	
	/**
	 * Reinitializes the performance tool data structures from the given XML definition file(s). 
	 *
	 */
	public void refreshTools()
	{
		File tauToolXML=null;
		URL testURL=Activator.getDefault().getBundle().getEntry("toolxml"+File.separator+"tau_tool.xml");
		try {
			tauToolXML = new File(new URI(FileLocator.toFileURL(testURL).toString().replaceAll(" ", "%20")));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PerformanceTool[] tauTool=null;
		PerformanceTool[] otherTools=null;
		if(tauToolXML!=null&&tauToolXML.canRead())
		{
			tauTool=ToolMaker.makeTools(tauToolXML);
		}
		
		File toolxml= new File(getPreferenceStore().getString(IPerformanceLaunchConfigurationConstants.XMLLOCID));
		if(!toolxml.canRead())
		{
			String epath=BuildLaunchUtils.checkToolEnvPath("eclipse");
			if(epath!=null)
			{
				toolxml=new File(epath);
				if(toolxml.canRead())
				{
					toolxml=new File(toolxml.getPath()+File.separator+"tool.xml");
					if(toolxml.canRead())
					{
						//tools=ToolMaker.makeTools(toolxml);
						this.getPreferenceStore().setValue(IPerformanceLaunchConfigurationConstants.XMLLOCID, toolxml.getPath());
					}
				}
			}
		}
		
		if(toolxml.canRead())
			otherTools=ToolMaker.makeTools(toolxml); //PerformanceTool.getSample();//new PerformanceTool[1];;
		tools=new PerformanceTool[1+otherTools.length];
		tools[0]=tauTool[0];
		for(int i=0;i<otherTools.length;i++)
		{
			tools[i+1]=otherTools[i];
		}
	}
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		refreshTools();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
