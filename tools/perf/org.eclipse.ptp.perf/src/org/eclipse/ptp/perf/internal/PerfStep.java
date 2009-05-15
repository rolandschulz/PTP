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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.toolopts.ToolApp;
import org.eclipse.ptp.perf.toolopts.ToolIO;
import org.eclipse.ptp.perf.toolopts.ToolsOptionsConstants;


/**
 * Manages the process of building instrumented applications and collecting the resulting data
 * @author wspear
 *
 */
public abstract class PerfStep extends Job implements IPerformanceLaunchConfigurationConstants {
	

	//private IConfiguration olddefbuildconf=null;
	
	//private boolean useTau=false;

//	/**
//	 * The name of the original application in the launch configuration
//	 */
//	private String application = null;
//
//	
//	
//
//	
//	private String saveApp=null;
//	private String saveArgs=null;
//	private boolean swappedArgs=false;
	
//	/**
//	 * False implies that no execution is to take place (either because of an error, or user request)
//	 * */
//	private boolean runbuilt=false;
//	
//	//=null;//Activator.getTool();// .tools[0].toolPanes[0];;
//	
//	/**  Executable (application) attribute name 	 */
//	private String appnameattrib=null;
//	/** Executable (application) path attribute name */
//	private String apppathattrib=null;
//	
	
	protected String projectLocation=null;
	protected ICProject thisCProject=null;
	protected IProject thisProject = null;
	//protected String outputLocation=null;
	protected ILaunchConfiguration configuration=null;
	//protected final PerformanceProcess tool;
	protected Map<String, String> IOMap=null;
	
	protected PerfStep(ILaunchConfiguration conf,String name) throws CoreException{
		super(name);
		configuration=conf;
		
		thisProject = getProject(configuration);
		thisCProject = CCorePlugin.getDefault().getCoreModel().create(thisProject);
		projectLocation=thisCProject.getResource().getLocation().toOSString();
		
		IOMap=new HashMap<String,String>();
		//this.tool=Activator.getTool(configuration.getAttribute(SELECTED_TOOL, (String)null));
	}
//	
//	/**
//	 * Creates a new LaunchManage object with the information necessary to instrument a program for performance analysis
//	 * @param conf The Launch configuration being adjusted for performance analysis
//	 * @param ana The application name attribute used by the underlying launch configuration delegate (differs between CDT and PTP)
//	 * @param projnameattrib The project name attribute used by the underlying launch configuration delegate (differs between CDT and PTP)
//	 * @throws CoreException
//	 */
//	public PerfStep(ILaunchConfiguration conf, String ana, String projnameattrib, String apa, String progPath, String outLoc) throws CoreException{//, TAULaunch tool
//		
//		super("Performance Analysis");
//		
//		appnameattrib=ana;
//		apppathattrib=apa;
//		configuration=conf;
//		//this.progPath=progPath;
//		//useTau=configuration.getAttribute(TAULAUNCH, false);
//		thisProject = getProject(projnameattrib, configuration);
//		thisCProject = CCorePlugin.getDefault().getCoreModel().create(thisProject);
//		projectLocation=thisCProject.getResource().getLocation().toOSString();
//		if(outputLocation==null){
//			outputLocation=projectLocation;
//		}
//		else{
//			outputLocation=outLoc;
//		}
//		
//		
//		IOMap=new HashMap<String, String>();
//		
//	}

	public void setConfiguration(ILaunchConfiguration conf){
		configuration =conf;
	}
	
	public ILaunchConfiguration getConfiguration(){
		return configuration;
	}
	
	/**
	 * Gets the project associated with the launch configuration held by this manager
	 * @param configuration
	 * @return The project associated with this manager's launch configuration, or null if no project is found
	 * @throws CoreException
	 */
	protected static IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(configuration.getAttribute(PERF_PROJECT_NAME_TAG, (String)null), (String)null);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
		}
		return null;
	}
	
//	boolean isManagedProject(IProject prj) {
//		if(prj==null)
//			return false;
//		ICProjectDescription prjd =CoreModel.getDefault().getProjectDescription(prj, false);
//			if(prjd==null)
//				return false;
//		IConfiguration cfg=ManagedBuildManager.getConfigurationForDescription(prjd.getDefaultSettingConfiguration());
//			return ( cfg.getBuilder().isManagedBuildOn() );
//	} 
	

	

	

	
//	private boolean doclean = true;
//	
//	public void setDoClean(boolean doit){
//		doclean=doit;
//	}
	

	
//	protected String getToolArguments(ToolApp app, ILaunchConfiguration configuration) throws CoreException
//	{
//		return getToolArguments(app,configuration,"");
//	}
	
	
	protected List<String> getToolArgumentList(ToolApp app, ILaunchConfiguration configuration, String buildDir, String rootDir) throws CoreException
	{
		if(app==null)
			return null;
		//Formerly replaced with projectLocation global variable.  May be the same?
		List<String> allargs=app.getArguments(configuration);
		
		for(int i=0;i<allargs.size();i++){
			String tmp = allargs.get(i);
			tmp=tmp.replaceAll(ToolsOptionsConstants.PROJECT_BUILD, buildDir);
			tmp=tmp.replaceAll(ToolsOptionsConstants.PROJECT_ROOT, rootDir);
			allargs.set(i, tmp);
		}
		String io = parseInput(app).trim();
		if(io.length()>0)
			allargs.add(io);
		io = parseOutput(app).trim();
		if(io.length()>0)
			allargs.add(io);
		return allargs;
	}
	
	protected String getToolArguments(ToolApp app, ILaunchConfiguration configuration, String buildDir, String rootDir) throws CoreException
	{
		List<String> argList = getToolArgumentList(app,configuration,buildDir,rootDir);
		String args="";
		if(argList==null)
			return(args);
		
		for(String a : argList){
			args+=a+" ";
		}
		
		//Formerly replaced with projectLocation global variable.  May be the same?
//		String allargs=app.getArguments(configuration);
//		allargs=allargs.replaceAll(ToolsOptionsConstants.PROJECT_BUILD, buildDir);
//		allargs=allargs.replaceAll(ToolsOptionsConstants.PROJECT_ROOT, rootDir);
//		allargs=allargs+parseInput(app)+" "+parseOutput(app);
		return args;
	}
	
	protected Map<String,String> getToolEnvVars(ToolApp app, ILaunchConfiguration configuration, String buildDir, String rootDir) throws CoreException
	{
		if(app==null){
			return null;
		}
		Map<String,String> map = app.getEnvVars(configuration);
		
		Iterator<Entry<String,String>> mapIt = map.entrySet().iterator();
		Entry<String,String> ent;
		while(mapIt.hasNext()){
			ent=mapIt.next();
			ent.setValue(ent.getValue().replaceAll(ToolsOptionsConstants.PROJECT_BUILD, buildDir).replaceAll(ToolsOptionsConstants.PROJECT_ROOT, rootDir));
		}
		
		return map;
	}
	
	private String parseInput(ToolApp app)
	{
		String input="";
		String oneIn="";
		if(app.inputArgs!=null)
		for(int i=0;i<app.inputArgs.length;i++)
		{
			oneIn="";
			if(app.inputArgs[i].pathFlag!=null)
				oneIn+=app.inputArgs[i].pathFlag+" ";
			if(IOMap.containsKey(app.inputArgs[i].ID))
			{
				input+=IOMap.get(app.inputArgs[i].ID);
				/*
				 * If input type is not directory, get a file list from the specified directory
				 */
			}
			else
			{
				return "";
			}
			/*
			 * 
			 */
			//if(app.inputArgs[i].)
			
		}
		return input;
	}
	
	private String parseOutput(ToolApp app)
	{
		String output="";
		
		/*
		 * Make and the new directory, associate it with the ID and stick -that- in the iomap!
		 */
		if(app.outputArgs!=null)
			for(int i=0;i<app.outputArgs.length;i++)
			{
				if(app.outputArgs[i].pathFlag!=null)
					output+=app.outputArgs[i].pathFlag+" ";
				if(IOMap.containsKey(app.outputArgs[i].ID))
				{
					output+=IOMap.get(app.outputArgs[i].ID);
					/*
					 * If input type is not directory, get a file list from the specified directory
					 */
				}
				else
				{
					output+=createOutputPath(app.outputArgs[i]);
				}
				/*
				 * 
				 */
				//if(app.inputArgs[i].)
				
			}
		
		return output;
	}
	
	private String createOutputPath(ToolIO IO)
	{
		//String ostring="";
		String opath=projectLocation+File.separator+IO.ID+File.separator+BuildLaunchUtils.getNow();
		File ofile=new File(opath);
		ofile.mkdirs();
		/*
		 * Create the opath.  Do something smart if it is impossible.
		 */
		IOMap.put(IO.ID, opath);
		
		return opath;
	}
	
	/**
	 * Given a ToolApp app finds the full path to the associated executable if possible and returns it.  If not it just returns the executable name
	 * @param app
	 * @return
	 */
	protected static String getToolExecutable(ToolApp app)
	{
		String command=app.toolCommand;
		
		String toolPath=BuildLaunchUtils.getToolPath(app.toolGroup);  //checkToolEnvPath(app.toolCommand);
		if(toolPath!=null)
		{
			command=toolPath+File.separator+command;
		}
		
		return command;
	}
	
//	protected String getToolCommand(ToolApp app, ILaunchConfiguration configuration) throws CoreException
//	{
//		return getToolCommand(app,configuration,"");
//	}
	
	/**
	 * Returns the full tool command; the full path to the executable used by  app followed by any arguments, replacing the 
	 * output location with the string provided by outputloc if necessary
	 */
	protected String getToolCommand(ToolApp app, ILaunchConfiguration configuration,String buildDir, String rootDir) throws CoreException
	{
		String command=getToolExecutable(app);
		if (command==null)
			return null;
		
		return command+" "+getToolArguments(app,configuration,buildDir,rootDir);
	}
	
	/**
	 * Returns the full tool command; the full path to the executable used by  app followed by any arguments, replacing the 
	 * output location with the string provided by outputloc if necessary
	 */
	protected List<String> getToolCommandList(ToolApp app, ILaunchConfiguration configuration,String buildDir, String rootDir) throws CoreException
	{
		List<String> command= new ArrayList<String>();
		String exec = getToolExecutable(app);
		if(exec==null)
			return null;
		
		command.add(exec.trim());
		
		List<String> args = getToolArgumentList(app,configuration,buildDir,rootDir);
		if(args==null){
			return command;
		}
		command.addAll(args);
		return command;
	}
}
