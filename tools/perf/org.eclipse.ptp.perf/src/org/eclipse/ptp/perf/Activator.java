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
package org.eclipse.ptp.perf;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.perf.internal.BuildLaunchUtils;
import org.eclipse.ptp.perf.toolopts.BuildTool;
import org.eclipse.ptp.perf.toolopts.ExecTool;
import org.eclipse.ptp.perf.toolopts.PerformanceProcess;
import org.eclipse.ptp.perf.toolopts.PerformanceTool;
import org.eclipse.ptp.perf.toolopts.PostProcTool;
import org.eclipse.ptp.perf.toolopts.ToolApp;
import org.eclipse.ptp.perf.toolopts.ToolMaker;
import org.eclipse.ptp.perf.toolopts.ToolPane;
import org.eclipse.ptp.perf.ui.AbstractPerformanceConfigurationTab;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.perf";

	// The shared instance
	private static Activator plugin;
	
	private static PerformanceProcess[] tools=null;
	
	public static PerformanceProcess[] getTools()
	{
		return tools;
	}
	
	/**
	 * Returns the performance tool with the given name from the performance tools array, or null if not found
	 * @param toolName
	 * @return
	 */
	public static PerformanceProcess getTool(String toolName)
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
	public static PerformanceProcess getTool(int dex)
	{
		if(dex<tools.length)
		{
			return tools[dex];
		}
		else return null;
	}
	
	/**
	 * Inserts the tool panes located in the array panes into the List paneList
	 * @param panes
	 * @param paneList
	 */
	private static void insertPanes(ToolPane[] panes, List<ToolPane> paneList)
	{
		if(panes!=null&&panes.length>0)
		{
			for(int k=0;k<panes.length;k++)
			{
				if(!panes[k].virtual)
					paneList.add(panes[k]);
			}
		}
	}
	
	/**
	 * Returns an array of all of the non-virtual tool panes defined in available tool definition xml files
	 * Panes are ordered by tool, and within each tool by compilation, execution and analysis step
	 * @return
	 */
	public static ToolPane[] getToolPanes()
	{
		ArrayList<ToolPane> paneList = new ArrayList<ToolPane>();
		ToolPane[] panes = null;
		
		if(tools.length<=0)
			return null;
		
		for(int i=0;i<tools.length;i++)
		{
			for(int j=0;j<tools[i].perfTools.size();j++){
				PerformanceTool t = tools[i].perfTools.get(j);
				if(t instanceof BuildTool)
				{
					BuildTool bt = (BuildTool)t;
					ToolApp compApp=bt.getCcCompiler();
					if(compApp!=null)
					{
						insertPanes(compApp.toolPanes,paneList);
					}
					compApp=bt.getCxxCompiler();
					if(compApp!=null)
					{
						insertPanes(compApp.toolPanes,paneList);
					}
					compApp=bt.getF90Compiler();
					if(compApp!=null)
					{
						insertPanes(compApp.toolPanes,paneList);
					}
					compApp=bt.getGlobalCompiler();
					if(compApp!=null)
					{
						insertPanes(compApp.toolPanes,paneList);
					}
				}
				else if(t instanceof ExecTool)  
				{
					ExecTool et = (ExecTool)t;
					for(int k=0;k<et.execUtils.length;k++)
					{
						insertPanes(et.execUtils[k].toolPanes,paneList);
					}
				}
				else if(t instanceof PostProcTool)
				{
					PostProcTool pt = (PostProcTool)t;
					for(int k=0;k<pt.analysisCommands.length;k++)
					{
						insertPanes(pt.analysisCommands[k].toolPanes,paneList);
					}
				}
				if(t.global!=null){
					insertPanes(t.global.toolPanes,paneList);
				}
			}
		}
		
		panes=new ToolPane[paneList.size()];
		paneList.toArray(panes);
		
		return panes;
	}
	
	private static ArrayList<File> workflowList =null;
	
	private ArrayList<File> getInternalXMLWorkflows()
	{
		if (workflowList != null) {
			return workflowList;
		}
		
		workflowList = new ArrayList<File>();
	
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.perf.workflows");
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];
			
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (int i=0; i< elements.length; i++)
			{
				IConfigurationElement ce = elements[i];
				try {
					String plugspace=ext.getNamespaceIdentifier();
					String aGetter = ce.getAttribute("XMLFile");
					
					//elements[i].
					//aGetter.setId(ce.getAttribute("id"));
					//System.out.println(plugspace+" "+aGetter);
					workflowList.add(new File(new URI(FileLocator.toFileURL((Platform.getBundle(plugspace).getEntry(aGetter))).toString().replaceAll(" ", "%20"))));
				} 
				catch (Exception e) {
					e.printStackTrace();
					//PTPCorePlugin.log(e);
				}
			}
		}
//		xmlWorkflows =
//			(File[]) getterList.toArray(
//					new File[getterList.size()]);
//	
		return workflowList;
	}
	
	/**
	 * Reinitializes the performance tool data structures from the given XML definition file(s). 
	 *
	 */
	public void refreshTools()
	{
		getInternalXMLWorkflows();
//		File tauToolXML=null;
//		URL testURL=Activator.getDefault().getBundle().getEntry("toolxml"+File.separator+"tau_tool.xml");
//		try {
//			tauToolXML = new File(new URI(FileLocator.toFileURL(testURL).toString().replaceAll(" ", "%20")));
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		PerformanceProcess[] tauTool=null;
		ArrayList<PerformanceProcess> theTools=new ArrayList<PerformanceProcess>();//null;
//		if(tauToolXML!=null&&tauToolXML.canRead())
//		{
//			tauTool=ToolMaker.makeTools(tauToolXML);
//		}
		
		
		String fiList=getPreferenceStore().getString(IPerformanceLaunchConfigurationConstants.XMLLOCID);
		String[] fiLocs = fiList.split(",,,");
		
		ArrayList<File> files = new ArrayList<File>();
		File fi=null;
		for(int i=0;i<fiLocs.length;i++){
			fi=new File(fiLocs[i]);
			if(fi.canRead()&&fi.isFile()){
				files.add(fi);
			}
		}
		
		
		if(files.size()==0)
		{
			//File toolxml= new File();
			String epath=BuildLaunchUtils.checkToolEnvPath("eclipse");
			if(epath!=null)
			{
				File toolxml=new File(epath);
				if(toolxml.canRead()&&toolxml.exists())
				{
					toolxml=new File(toolxml.getPath()+File.separator+"tool.xml");
					if(toolxml.canRead()&&toolxml.isFile())
					{
						files.add(toolxml);
						//tools=ToolMaker.makeTools(toolxml);
						this.getPreferenceStore().setValue(IPerformanceLaunchConfigurationConstants.XMLLOCID, toolxml.getPath());
					}
				}
			}
		}
		//int numOTools=0;
		
		for(int i=0;i<workflowList.size();i++)
		{
			tools=ToolMaker.makeTools(workflowList.get(i));
			if(tools!=null)
				for(int j=0;j<tools.length;j++)
				{
					theTools.add(tools[j]);
				}
		}
		
		
		for(int i=0;i<files.size();i++)
		{
			try{
			tools=ToolMaker.makeTools(files.get(i)); //PerformanceProcess.getSample();//new PerformanceProcess[1];;
			}catch(Exception e){
				tools=null;
				e.printStackTrace();
				System.out.println("Problem reading "+files.get(i).toString());
			}
			//numOTools=otherTools.length;
			if(tools!=null)
				for(int j=0;j<tools.length;j++)
				{
					theTools.add(tools[j]);
				}
			tools=null;
		}
		
			
		
		tools =
			(PerformanceProcess[]) theTools.toArray(
					new PerformanceProcess[theTools.size()]);
		
//		tools=new PerformanceProcess[numOTools];
//		//tools[0]=tauTool[0];
//		for(int i=0;i<numOTools;i++)
//		{
//			tools[i]=otherTools[i];
//		}
	}
	
	private static ArrayList<AbstractPerformanceConfigurationTab> perfConfTabs=null;
	
	public static ArrayList<AbstractPerformanceConfigurationTab> getPerfTabs()
	{
		if (perfConfTabs != null) {
			return perfConfTabs;
		}
		
		perfConfTabs = new ArrayList<AbstractPerformanceConfigurationTab>();
	
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.perf.configurationTabs");
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];
			
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (int i=0; i< elements.length; i++)
			{
				IConfigurationElement ce = elements[i];
				try {
					AbstractPerformanceConfigurationTab aGetter = (AbstractPerformanceConfigurationTab) ce.createExecutableExtension("class");
					//aGetter.setId(ce.getAttribute("id"));
					perfConfTabs.add(aGetter);
				} catch (CoreException e) {
					e.printStackTrace();
					//PTPCorePlugin.log(e);
				}
			}
		}
		
		return perfConfTabs;
	}
	
	
	private static ArrayList<AbstractPerformanceDataManager> perfConfManagers=null;
	
	private ArrayList<AbstractPerformanceDataManager> getPerfConfManagers()
	{
		if (perfConfManagers != null) {
			return perfConfManagers;
		}
		
		perfConfManagers = new ArrayList<AbstractPerformanceDataManager>();
	
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.perf.dataManagers");
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];
			
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (int i=0; i< elements.length; i++)
			{
				IConfigurationElement ce = elements[i];
				try {
					AbstractPerformanceDataManager aGetter = (AbstractPerformanceDataManager) ce.createExecutableExtension("class");
					//aGetter.setId(ce.getAttribute("id"));
					perfConfManagers.add(aGetter);
				} catch (CoreException e) {
					e.printStackTrace();
					//PTPCorePlugin.log(e);
				}
			}
		}
		
		return perfConfManagers;
	}
	
	public static AbstractPerformanceDataManager getPerfDataManager(String name)
	{
		if(name==null)
			return null;
		AbstractPerformanceDataManager check=null;
		Iterator<AbstractPerformanceDataManager> perfit=perfConfManagers.iterator();
		while(perfit.hasNext())
		{
			check=perfit.next();
			if(check.getName().equals(name))
			{
				return(check);
			}
		}
		return null;
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
		getPerfTabs();
		getPerfConfManagers();
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
