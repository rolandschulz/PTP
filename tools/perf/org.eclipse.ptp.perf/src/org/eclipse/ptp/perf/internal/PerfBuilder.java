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
import java.util.Map;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.core.MakeTargetManager;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.toolopts.BuildTool;

public class PerfBuilder extends PerfStep implements IPerformanceLaunchConfigurationConstants{
	


	/**
	 * The location of the binary rebuilt with performance instrumentation
	 */
	private String progPath=null;
	

	
	/**
	 * False implies that no execution is to take place (either because of an error, or user request)
	 * */
	private boolean runbuilt=false;
	

	/** Executable (application) path attribute name */
	//private String apppathattrib=null;
	private IManagedProject managedBuildProj=null;
	private IConfiguration newBuildConfig=null;
	private String buildConf = null;
	//private String projectLocation=null;
	private IConfiguration olddefbuildconf=null;
	//private ICProject thisCProject=null;
	//private IProject thisProject = null;
	private String outputLocation=null;
	private Map<String,String> buildMods=null;
	
	private String newname=null;
	private String binary=null;
	private BuildTool tool=null;
	
	public PerfBuilder(ILaunchConfiguration conf, BuildTool btool,Map<String,String> buildMods) throws CoreException{
		super(conf,"Instrumenting/Building");
		this.buildMods=buildMods;
		tool=btool;
		initBuild(conf);
	}
	
	
	public PerfBuilder(ILaunchConfiguration conf, BuildTool btool) throws CoreException{
		super(conf,"Instrumenting/Building");
		tool=btool;
		initBuild(conf);
	}
	
	private void initBuild(ILaunchConfiguration conf)throws CoreException{
		//apppathattrib=apa;
		outputLocation=projectLocation;
		buildConf=configuration.getAttribute(ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME,(String)null);
		if(tool==null)return;
		if(ManagedBuildManager.canGetBuildInfo(thisCProject.getResource()))
		{
			runbuilt=initMMBuild();
		}
	}
	
	public boolean getBuildSuccessful(){
		return runbuilt;
	}
	public String getOutputLocation(){
		return outputLocation;
	}
	public String getProgramPath(){
		return progPath;
	}
	
	/**
	 * Builds the project with managed make if supported, otherwise with standard make
	 * @param monitor 
	 * @throws Exception 
	 */
	public void buildIndstrumented(IProgressMonitor monitor) throws Exception
	{			
		//if(tool==null)
		//	throw new Exception("No valid tool configuration found");
		//runbuilt = true;
		if(tool!=null)
		{
			if(!ManagedBuildManager.canGetBuildInfo(thisCProject.getResource()))
			{
				standardMakeBuild(monitor);
			}
			else
			{
				if(runbuilt)
				{
					runbuilt=initMMBuildConf();
					if(runbuilt){
						runbuilt=managedMakeBuild(monitor);
					}
				}
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
			String allargs=getToolArguments(tool.getGlobalCompiler(),configuration, outputLocation);
			makeOut.write(getToolCommand(tool.getCcCompiler(),configuration, outputLocation)+" "+allargs+"\n");
			makeOut.write(getToolCommand(tool.getCxxCompiler(),configuration, outputLocation)+" "+allargs+"\n");
			makeOut.write(getToolCommand(tool.getF90Compiler(),configuration, outputLocation)+" "+allargs+"\n");
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
			//System.out.println(targs[i].getName()+" "+targs[i].getTargetBuilderID());
		}
		if(select==null)
		{
			System.out.println("No Make Target: all");
			runbuilt = false;
			return;
		}

		//System.out.println(select.getBuildLocation());

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
	
	
	private boolean initMMBuild() throws CoreException{
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(thisCProject.getResource());
		if (info == null||!info.isValid()){
			System.out.println("No info!!!");
			return false;
		}
		
		managedBuildProj = info.getManagedProject();
		if (managedBuildProj == null){
			System.out.println("No managed project!!!");
			return false;
		}
		olddefbuildconf=info.getDefaultConfiguration();//TODO: Make sure default configuration always works.  Prompt user?
		binary = info.getBuildArtifactName();
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
		{System.out.println("No Conf Selected");return false;}
		if(selectedconf.getName()==null)
		{System.out.println("Selected conf has no name");return false;}
		
		//Make the new configuration name, and if there is already a configuration with that name, remove it.
		String basename=selectedconf.getName();
        newname=null;//=basename+"_PerformanceAnalysis"; //TODO:  FIX RECOVERY OF TOOLID!!!
		
        String addname=configuration.getAttribute(TOOLCONFNAME+tool.toolID, DEFAULT_TOOLCONFNAME);
        //if(basename.indexOf(addname)<0)
        newname=basename+"_"+addname;

        if(addname.equals(DEFAULT_TOOLCONFNAME))
        	newname+="_"+tool.toolName;

        
        progPath=newname+File.separator+binary;
		//System.out.println(progPath);
		
		//TODO: We have to do this because PTP puts its output in the build directory
		if(configuration.getAttribute(PERF_EXECUTABLE_PATH_TAG, (String)null)!=null)
		{
			outputLocation=thisProject.getFile(newname).getLocation().toOSString();
		}
        
		boolean confExists=false;
		IConfiguration[] confs = managedBuildProj.getConfigurations();
		for(int i =0; i<confs.length;i++)
		{
			if(confs[i].getName().equals(newname)||confs[i].getName().indexOf(newname)>=0)
			{
				confExists=true;
				newBuildConfig=confs[i];
				break;
				//managedBuildProj.removeConfiguration(confs[i].getId());
			}
		}
		//Make a copy of the selected configuration(Clone works, basic create does not) and rename it.
		if(!confExists)
		{
			newBuildConfig = managedBuildProj.createConfigurationClone(selectedconf, selectedconf.getId()+"."+ManagedBuildManager.getRandomNumber());
		}
		if (newBuildConfig == null){
			System.out.println("No config!");
			return false;
		}
		return true;
	}
	
	/**
	 * Runs the managed make build system using the performance tool's compilers and compiler options.
	 * This is accomplished by creating a new build configuration and replacing the compiler with the relevant tool commands
	 * @param monitor
	 * @throws CoreException
	 * @throws FileNotFoundException
	 */
	public boolean initMMBuildConf() throws CoreException, FileNotFoundException
	{	

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
		
		for(ITool it : tools){
			for(IOption op: it.getOptions()){
				if(op==null)
				{
					continue;
				}
				if(op.getName()==null){
					continue;
				}
//				if(op.getName().equals("Optimization Level")){
//				System.out.println(op.getName()+" ID: "+op.getBaseId());
//				for(String vals:op.getApplicableValues())
//				{
//					System.out.println(vals);
//				}
//				}
			}
		}
		
		
		//TODO: Make sure this never has side-effects.
		String allargs="";
		if(tool.getGlobalCompiler()!=null && !tool.getGlobalCompiler().equals(tool.getCcCompiler()))
		{
			allargs=getToolArguments(tool.getGlobalCompiler(),configuration, outputLocation);
		}
		int numChanges=0;
		for(int i =0;i<tools.length;i++){

			if(buildMods!=null){
				for(String opName:buildMods.keySet())
				{
					//System.out.println(op.getName()+" ID: "+op.getBaseId());
					for(IOption op:tools[i].getOptions())
					{
						//IOption op=tools[i].getOptionById(opId);
						if(op.getName().equals(opName))//op.getName().equals("Optimization Level"))
						{
//							for(String vals:op.getApplicableValues())
//							{
//								System.out.println(vals);
//							}
							try {
								op.setValue(buildMods.get(opName));
							} catch (BuildException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
			
			String toolid=tools[i].getId();
			if(toolid.indexOf(".c.")>=0)
			{
				numChanges+=modifyCommand(tools[i],getToolCommand(tool.getCcCompiler(),configuration, outputLocation),allargs,tool.replaceCompiler);
			}
			if(toolid.indexOf(".cpp.")>=0)
			{
				numChanges+=modifyCommand(tools[i],getToolCommand(tool.getCxxCompiler(),configuration, outputLocation),allargs,tool.replaceCompiler);
			}
			if(toolid.indexOf(".fortran.")>=0)
			{
				numChanges+=modifyCommand(tools[i],getToolCommand(tool.getF90Compiler(),configuration, outputLocation),allargs,tool.replaceCompiler);
			}
		}
		//System.out.println(tbpath+File.separator+"tau_xxx.sh"+tauCompilerArgs);
		if(numChanges>0)
		{
			ManagedBuildManager.saveBuildInfo(thisCProject.getProject(),true);
		}
		
		return true;
	}
	
	private boolean managedMakeBuild(IProgressMonitor monitor){
		//Build set the new configuration to default so we can build it.
		
		IFile programPath = thisProject.getFile(progPath);
		
		ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),newBuildConfig);
		try {
			thisProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);//.FULL_BUILD //.INCREMENTAL_BUILD

		} catch (Exception e) {
			return false;
		}
		
		//TODO: Find out how to get build progress from within the managed build system!
		while(!programPath.exists() || !programPath.getLocation().toFile().exists())
		{
			if(monitor!=null&&monitor.isCanceled())
			{
				//ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
				restoreBuild();
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
		
		restoreBuild();
		return true;
	}

	private static int modifyCommand(ITool tool, String command, String args, boolean replace){
		int didChange=0;
		if(replace)
		{
			String newcom=command+" "+args;
			if(!newcom.equals(tool.getToolCommand()));
			{
				tool.setToolCommand(command+" "+args);
				didChange= 1;
			}
		}
		else
		{
			String newcom=command+" "+args+" "+tool.getToolCommand();
			if(!newcom.equals(tool.getToolCommand()));
			{
				tool.setToolCommand(newcom);
				didChange= 1;
			}
		}
		return didChange;
	}

	public void restoreBuild(){
		if(ManagedBuildManager.canGetBuildInfo(thisCProject.getResource()))
		{
			ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
//			if(!configuration.getAttribute(NOCLEAN, false)&&managedBuildProj!=null&&newBuildConfig!=null)
//				managedBuildProj.removeConfiguration(newBuildConfig.getId());
		}
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			buildIndstrumented(monitor);
		} catch (Exception e) {
			return new Status(IStatus.ERROR,"com.ibm.jdg2e.concurrency",IStatus.ERROR,"Build Incomplete",e);
		}
		return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"Build Successful",null);
	}
}
