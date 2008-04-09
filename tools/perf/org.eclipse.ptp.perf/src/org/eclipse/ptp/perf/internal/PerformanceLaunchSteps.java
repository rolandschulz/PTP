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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.core.MakeTargetManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.perf.AbstractPerformanceDataManager;
import org.eclipse.ptp.perf.Activator;
import org.eclipse.ptp.perf.toolopts.PerformanceTool;
import org.eclipse.ptp.perf.toolopts.ToolApp;
import org.eclipse.ptp.perf.toolopts.ToolIO;
//import org.eclipse.ptp.perf.tau.TAULaunch;


/**
 * Manages the process of building instrumented applications and collecting the resulting data
 * @author wspear
 *
 */
public class PerformanceLaunchSteps implements IPerformanceLaunchConfigurationConstants{
	
	private IManagedProject managedBuildProj=null;
	private IConfiguration newBuildConfig=null;
	private IConfiguration olddefbuildconf=null;
	private ICProject thisCProject=null;
	private IProject thisProject = null;
	//private boolean useTau=false;
	
	/**
	 * The location of the binary rebuilt with performance instrumentation
	 */
	private String progPath=null;
	
	/**
	 * The name of the original application in the launch configuration
	 */
	private String application = null;
	private String buildConf = null;
	
	private String projectLocation=null;
	private String outputLocation=null;
	
	private String saveApp=null;
	private String saveArgs=null;
	private boolean swappedArgs=false;
	
	/**
	 * False implies that no execution is to take place (either because of an error, or user request)
	 * */
	private boolean runbuilt=false;
	
	private final PerformanceTool tool;//=null;//Activator.getTool();// .tools[0].toolPanes[0];;
	
	/**  Executable (application) attribute name 	 */
	private String appnameattrib=null;
	/** Executable (application) path attribute name */
	private String apppathattrib=null;
	private ILaunchConfiguration configuration=null;
	
	private Map<String, String> IOMap=null;
	
	
	/**
	 * Creates a new LaunchManage object with the information necessary to instrument a program for performance analysis
	 * @param conf The Launch configuration being adjusted for performance analysis
	 * @param ana The application name attribute used by the underlying launch configuration delegate (differs between CDT and PTP)
	 * @param projnameattrib The project name attribute used by the underlying launch configuration delegate (differs between CDT and PTP)
	 * @throws CoreException
	 */
	public PerformanceLaunchSteps(ILaunchConfiguration conf, String ana, String projnameattrib, String apa) throws CoreException{//, TAULaunch tool
		appnameattrib=ana;
		apppathattrib=apa;
		configuration=conf;
		//useTau=configuration.getAttribute(TAULAUNCH, false);
		thisProject = getProject(projnameattrib, configuration);
		thisCProject = CCorePlugin.getDefault().getCoreModel().create(thisProject);
		projectLocation=thisCProject.getResource().getLocation().toOSString();
		outputLocation=projectLocation;
		
		this.tool=Activator.getTool(configuration.getAttribute(SELECTED_TOOL, (String)null));
		IOMap=new HashMap<String, String>();
		buildConf=configuration.getAttribute(ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME,(String)null);
	}

	
	/**
	 * Gets the project associated with the launch configuration held by this manager
	 * @param projnameattrib 
	 * @param configuration
	 * @return The project associated with this manager's launch configuration, or null if no project is found
	 * @throws CoreException
	 */
	private static IProject getProject(String projnameattrib, ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(projnameattrib, (String)null);
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
	
	/**
	 * Builds the project with managed make if supported, otherwise with standard make
	 * @param monitor 
	 * @throws Exception 
	 */
	public void buildIndstrumented(IProgressMonitor monitor) throws Exception
	{			
		if(tool==null)
			throw new Exception("No valid tool configuration found");
		runbuilt = true;
		if(tool.recompile)
		{
			if(!ManagedBuildManager.canGetBuildInfo(thisCProject.getResource()))
			{
				standardMakeBuild(monitor);
			}
			else
			{
				managedMakeBuild(monitor);
			}
		}
	}
	
	/**
	 * Runs the standard make build system using the tool-supplied compiler and compiler options.
	 * This is accomplished by temporarily replacing the default compiler names in a pre-defined makefile inclusion
	 * with the names and arguments of the compilers
	 * @param monitor
	 * @throws CoreException
	 */
	public void standardMakeBuild(IProgressMonitor monitor) throws CoreException{
		
		File compilerInclude = new File(projectLocation+File.separator+"eclipse.inc");
		File compilerDef = new File(projectLocation+File.separator+"eclipse.inc.default");
		try{
			if(compilerInclude.exists())
			{
				InputStream in = new FileInputStream(compilerInclude);
				OutputStream out = new FileOutputStream(compilerDef);

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}
//TODO:  Make this work again (i.e. distinguish between all-compiler and discrete compiler systems)
			BufferedWriter makeOut = new BufferedWriter(new FileWriter(compilerInclude));
			String allargs=getToolArguments(tool.getGlobalCompiler(),configuration);
			makeOut.write(getToolCommand(tool.getCcCompiler(),configuration)+" "+allargs+"\n");
			makeOut.write(getToolCommand(tool.getCxxCompiler(),configuration)+" "+allargs+"\n");
			makeOut.write(getToolCommand(tool.getF90Compiler(),configuration)+" "+allargs+"\n");
			makeOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		MakeTargetManager targetMan = new MakeTargetManager();
		targetMan.startup();

		IMakeTarget[] targs = targetMan.getTargets(thisProject);
		IMakeTarget select = null;
		for(int i=0;i<targs.length;i++)
		{
			if(targs[i].getName().equals("all"))
			{
				select = targs[i];
				break;
			}
			System.out.println(targs[i].getName()+" "+targs[i].getTargetBuilderID());
		}
		if(select==null)
		{
			System.out.println("No Make Target: all");
			runbuilt = false;
			return;
		}

		System.out.println(select.getBuildLocation());

		select.build(monitor);

		targetMan.shutdown();

		if(compilerDef.exists())
		{
			InputStream in;
			try {
				in = new FileInputStream(compilerDef);

				OutputStream out = new FileOutputStream(compilerInclude);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();	
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		runbuilt=true;
		return;
	}
	
//	/**
//	 * Replaces the PROJECT_LOCATION string constant with the location of the project being analyzed
//	 * @param input
//	 * @return
//	 * @throws CoreException
//	 */
//	private String replaceLocalPath(String input) throws CoreException
//	{
//		return input.replaceAll(IPerformanceLaunchConfigurationConstants.PROJECT_LOCATION, projectLocation);
//	}
	
	/**
	 * Runs the managed make build system using the performance tool's compilers and compiler options.
	 * This is accomplished by creating a new build configuration and replacing the compiler with the relevant tool commands
	 * @param monitor
	 * @throws CoreException
	 * @throws FileNotFoundException
	 */
	public void managedMakeBuild(IProgressMonitor monitor) throws CoreException, FileNotFoundException
	{	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(thisCProject.getResource());
		if (info == null||!info.isValid()){
			System.out.println("No info!!!");
			runbuilt=false;
			return;
		}
		
		managedBuildProj = info.getManagedProject();
		if (managedBuildProj == null){
			System.out.println("No managed project!!!");
			runbuilt=false;
			return;
		}
		olddefbuildconf=info.getDefaultConfiguration();//TODO: Make sure default configuration always works.  Prompt user?
		String binary = info.getBuildArtifactName();
		String bextension = info.getBuildArtifactExtension();
		if(bextension.length()>0)
			binary=binary+"."+bextension;
		
		//Make a list of the configurations already within the project
		IConfiguration[] buildconfigs = info.getManagedProject().getConfigurations();
		IConfiguration selectedconf = null;
		for(int i=0;i<buildconfigs.length;i++){
			if((buildconfigs[i].getName()).equals(buildConf))
			{
				selectedconf=buildconfigs[i];
				break;
			}
		}
		
		if(selectedconf==null)
		{System.out.println("No Conf Selected");runbuilt = false; return;}
		if(selectedconf.getName()==null)
		{System.out.println("Selected conf has no name");runbuilt=false; return;}
		
		//Make the new configuration name, and if there is already a configuration with that name, remove it.
		String basename=selectedconf.getName();
        String newname=null;//=basename+"_PerformanceAnalysis";
		
        String addname=configuration.getAttribute(TOOLCONFNAME+tool.toolID, DEFAULT_TOOLCONFNAME);
        //if(basename.indexOf(addname)<0)
        newname=basename+"_"+addname;

        if(addname.equals(DEFAULT_TOOLCONFNAME))
        	newname+="_"+tool.toolName;

		
		IConfiguration[] confs = managedBuildProj.getConfigurations();
		for(int i =0; i<confs.length;i++)
		{
			if(confs[i].getName().equals(newname)||confs[i].getName().indexOf(newname)>=0)
			{
				managedBuildProj.removeConfiguration(confs[i].getId());
			}
		}
		//Make a copy of the selected configuration(Clone works, basic create does not) and rename it.
		newBuildConfig = managedBuildProj.createConfigurationClone(selectedconf, selectedconf.getId()+"."+ManagedBuildManager.getRandomNumber());
		if (newBuildConfig == null){
			System.out.println("No config!");
		}
		newBuildConfig.setName(newname);
		
		//TODO: Restore TAU build configuration adjustment
//		if(useTau)
//		{
//			TAULaunch.adjustBuild(newBuildConfig);
//		}
//		else
		{
			//TODO: Make adjustments based on configuration (map build attribute names to values?)
		}
		
		IToolChain chain = newBuildConfig.getToolChain();
		ITool[] tools = chain.getTools();
		
		String allargs=getToolArguments(tool.getGlobalCompiler(),configuration);
		for(int i =0;i<tools.length;i++){
			String toolid=tools[i].getId();
			if(toolid.indexOf(".c.")>=0)
			{
				{
					if(tool.replaceCompiler)
					{
						tools[i].setToolCommand(getToolCommand(tool.getCcCompiler(),configuration)+" "+allargs);
					}
					else
					{
						tools[i].setToolCommand(getToolCommand(tool.getCcCompiler(),configuration)+" "+allargs+" "+tools[i].getToolCommand());
					}
				}
			}
			if(toolid.indexOf(".cpp.")>=0)
			{
				{
					if(tool.replaceCompiler)
					{
						tools[i].setToolCommand(getToolCommand(tool.getCxxCompiler(),configuration)+" "+allargs);
					}
					else
					{
						tools[i].setToolCommand(getToolCommand(tool.getCxxCompiler(),configuration)+" "+allargs+" "+tools[i].getToolCommand());
					}
				}
			}
			if(toolid.indexOf(".fortran.")>=0)
			{
				{
					if(tool.replaceCompiler)
					{
						tools[i].setToolCommand(getToolCommand(tool.getF90Compiler(),configuration)+" "+allargs);
					}
					else
					{
						tools[i].setToolCommand(getToolCommand(tool.getF90Compiler(),configuration)+" "+allargs+" "+tools[i].getToolCommand());
					}
				}
			}
		}
		//System.out.println(tbpath+File.separator+"tau_xxx.sh"+tauCompilerArgs);
		ManagedBuildManager.saveBuildInfo(thisCProject.getProject(),true);
		//Build set the new configuration to default so we can build it.
		ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),newBuildConfig);
		try {
			thisProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);

		} catch (Exception e) {
			runbuilt=false;
			return;
		}

		progPath=newname+File.separator+binary;
		//System.out.println(progPath);
		IFile programPath = thisProject.getFile(progPath);
		
		
		//TODO: We have to do this because PTP puts its output in the build directory
		if(apppathattrib!=null)
		{
			outputLocation=thisProject.getFile(newname).getLocation().toOSString();
		}
		
		
		//TODO: Find out how to get build progress from within the managed build system!
		while(!programPath.exists() || !programPath.getLocation().toFile().exists())
		{
			if(monitor!=null&&monitor.isCanceled())
			{
				ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
				runbuilt=false;
				throw new OperationCanceledException();
			}
			long numMillisecondsToSleep = 1000;
			try {
				Thread.sleep(numMillisecondsToSleep);
			} catch (InterruptedException e) {
			}
			programPath = thisProject.getFile(progPath);
		}
		if(configuration.getAttribute(BUILDONLY, false))
		{
			ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
			runbuilt= false;
			return;
		}

		runbuilt= true;
		return;
	}
	
	/**
	 * This launches the application and makes and adjustments to the build configuration if necessary
	 * @param paraDel
	 * @param launch
	 * @param monitor
	 * @return True if the launch is attempted, false otherwise
	 * @throws Exception 
	 */
	public boolean performLaunch(LaunchConfigurationDelegate paraDel, ILaunch launch, IProgressMonitor monitor) throws Exception{

		if(tool==null)
			throw new Exception("No valid tool configuration found");
		
		if(!runbuilt)
			return false;
		
		ILaunchConfigurationWorkingCopy confWC = configuration.getWorkingCopy();
		application=confWC.getAttribute(appnameattrib, (String)null);
		
		if(tool.recompile)
		{
			confWC.setAttribute(appnameattrib, progPath);
			if(apppathattrib!=null)
			{
				IFile path = thisProject.getFile(progPath);
				//System.out.println(path.exists());
				//System.out.println(path.getLocation().toString());
				confWC.setAttribute(apppathattrib,  path.getLocation().toString());
			}
		}
		
		if(tool.prependExecution)
		{
			String prog = confWC.getAttribute(appnameattrib, EMPTY_STRING);
			//TODO: This needs to work for PTP too eventually
			String arg = confWC.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, EMPTY_STRING);
			saveApp=prog;
			saveArgs=arg;
			
			//List utilList=tool.execUtils;
			if(tool.execUtils!=null&&tool.execUtils.length>0)
			{
				//Iterator utilIt=utilList.iterator();
				
				String firstExecUtil= getToolExecutable(tool.execUtils[0]);// tool.execUtils[0].toolCommand;// (String)utilIt.next();//confWC.getAttribute(EXEC_UTIL_LIST, (String)null);
				
				//String util1Path=BuildLaunchUtils.checkToolEnvPath(firstExecUtil);
				
				if(firstExecUtil==null)
					throw new Exception("Tool "+firstExecUtil+" not found");
			
				confWC.setAttribute(appnameattrib, firstExecUtil);
				
				String otherUtils=getToolArguments(tool.execUtils[0],configuration);// tool.execUtils[0].getArgs()+" "+tool.execUtils[0].getPaneArgs(configuration);
				
				for(int i=1;i<tool.execUtils.length;i++)
				{
					//TODO: Check paths of other tools
					otherUtils+=" "+getToolCommand(tool.execUtils[i],configuration);//tool.execUtils[i].getCommand(configuration);
				}
				swappedArgs=true;
				//System.out.println(firstExecUtil+otherUtils+" "+prog+" "+arg);
				confWC.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, otherUtils+" "+prog+" "+arg);
			}
		}
		configuration = confWC.doSave();
		

		paraDel.launch(configuration, ILaunchManager.RUN_MODE, launch, monitor);
		if(!launch.isTerminated())
		{
//			if(!launch.canTerminate())
//			{
//				System.out.println("Launch can not terminate!  Possible infinite loop!");
//				cleanup();
//				throw new OperationCanceledException();
//			}
			while(!launch.isTerminated())
			{
				if(monitor.isCanceled())
				{
					launch.terminate();
					cleanup();
					throw new OperationCanceledException();
				}
				Thread.sleep(1000);
			}
		}
		return true;
	}
	
	/**
	 * Restore the previous default build configuration and optionally remove the performance tool's build configuration
	 * Restore the previous launch configuration settings
	 * @throws CoreException
	 */
	public void cleanup() throws CoreException
	{
		if(tool.recompile&&ManagedBuildManager.canGetBuildInfo(thisCProject.getResource()))
		{
			ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
//			if(!configuration.getAttribute(NOCLEAN, false)&&managedBuildProj!=null&&newBuildConfig!=null)
//				managedBuildProj.removeConfiguration(newBuildConfig.getId());
		}
		ILaunchConfigurationWorkingCopy confWC = configuration.getWorkingCopy();
		
		if(tool.prependExecution&&swappedArgs)
		{
			confWC.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, saveApp);
			confWC.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, saveArgs);
		}
		
		confWC.setAttribute(appnameattrib, application);
		configuration = confWC.doSave();
	}
	
	/**
	 * Handle data collection and cleanup after an instrumented application has finished running
	 * @param monitor
	 * @throws CoreException
	 */
	public void postlaunch(IProgressMonitor monitor) throws CoreException{
		
		if (monitor.isCanceled()) {
			cleanup();
			throw new OperationCanceledException();
		}
		
		//TODO:  Restore tau performance data management
//		if(useTau)
//		{
//			TAULaunch.toolClean(thisCProject.getElementName(), configuration, outputLocation);
//		}
//		else
		{
			//List toolList=tool.analysisCommands;//configuration.getAttribute(TOOL_LIST, (List)null);
			if(tool.analysisCommands!=null&&tool.analysisCommands.length>0)
			{
				File projectLoc=new File(outputLocation);
				String runTool;
				//String toolPath;
				for(int i=0;i<tool.analysisCommands.length;i++)
				{
					//TODO: put internal in defined strings
					if(tool.analysisCommands[i].toolGroup==null||!tool.analysisCommands[i].toolGroup.equals("internal"))
					{
						runTool=getToolCommand(tool.analysisCommands[i],configuration);//tool.analysisCommands[i].toolCommand;
						//toolPath=BuildLaunchUtils.checkToolEnvPath(runTool);
						if(runTool!=null)
						{
							BuildLaunchUtils.runTool(runTool, null, projectLoc);
						}
						else
						{
							System.out.println("The command "+tool.analysisCommands[i].toolCommand+" could not be run because the application is not in your path.");
						}
					}
					else
					{
						AbstractPerformanceDataManager manager=Activator.getPerfDataManager(tool.analysisCommands[i].toolCommand);
						if(manager!=null)
						{
							manager.process(thisCProject.getElementName(), configuration, outputLocation);
						}
					}
				}
			}
		}
		cleanup();
	}	
	
	
	private String getToolArguments(ToolApp app, ILaunchConfiguration configuration) throws CoreException
	{
		if(app==null)
			return("");
		String allargs=app.getArguments(configuration).replaceAll(IPerformanceLaunchConfigurationConstants.PROJECT_LOCATION, projectLocation)+parseInput(app)+" "+parseOutput(app);
		return allargs;
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
	
	private static String getToolExecutable(ToolApp app)
	{
		String command=app.toolCommand;
		
		String toolPath=BuildLaunchUtils.getToolPath(app.toolGroup);  //checkToolEnvPath(app.toolCommand);
		if(toolPath!=null)
		{
			command=toolPath+File.separator+command;
		}
		
		return command;
	}
	
	private String getToolCommand(ToolApp app, ILaunchConfiguration configuration) throws CoreException
	{
		String command=getToolExecutable(app);
		if (command==null)
			return null;
		
		return command+" "+getToolArguments(app,configuration);
	}
}
