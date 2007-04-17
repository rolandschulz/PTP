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
package org.eclipse.ptp.tau.core.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.tau.options.TAUOptionsPlugin;
import org.eclipse.ptp.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;


/**
 * 
 */
public class TAULaunchManage { //extends ParallelLaunchConfigurationDelegate 
	
	private IManagedProject managedBuildProj=null;
	private IConfiguration newBuildConfig=null;
	private IConfiguration olddefbuildconf=null;
	private ICProject thisCProject=null;
	private IProject thisProject = null;
	
	private String tbpath = null;
	private String tmakepath = null;
	private String tauoptchunk = null;
	private String application = null;
	
	private boolean managed = false;
	//private String askarchdir=null;
	private File askmakefile=null;
	private int numprocs = 1;
	private String appnameattrib=null;
	private String projnameattrib=null;
	private ILaunchConfiguration configuration=null;
	IProgressMonitor monitor = null;
	
	public TAULaunchManage(ILaunchConfiguration conf, IProgressMonitor mon, String ana, String apn, int procs) throws CoreException{
		appnameattrib = ana;
		numprocs = procs;
		configuration=conf;
		monitor=mon;
		projnameattrib=apn;
		setupProject();
	}
	
	public TAULaunchManage(ILaunchConfiguration conf, IProgressMonitor mon, String ana, String apn) throws CoreException{
		appnameattrib=ana;
		numprocs = 1;
		configuration=conf;
		monitor=mon;
		projnameattrib=apn;
		setupProject();
	}
	
	private void setupProject() throws CoreException
	{
		thisProject = getProject(configuration);
		thisCProject = CCorePlugin.getDefault().getCoreModel().create(thisProject);
		
		application=configuration.getAttribute(appnameattrib, (String)null);
		//IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, (String)null
		
	}
	
	
	private IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(projnameattrib, (String)null);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				//ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
				//if (cProject != null && cProject.exists()) {
					//return cProject;
				//}
			}
		}
		return null;
	}
	
	public boolean TAUBuild() throws CoreException, FileNotFoundException
	{
		initTauCompilers();
		boolean runbuilt = true;
		//Rebuild project with TAU compiler scripts according to the project's nature
		if(!ManagedBuildManager.canGetBuildInfo(thisCProject.getResource()))
		{
			runbuilt = standardMakeBuild();
		}
		else
		{
			managed=true;
			runbuilt = managedMakeBuild();
		}
		return runbuilt;
	}
	
	public void initTauCompilers() throws CoreException, FileNotFoundException
	{
		//Build the tau compiler run string based on the TAU preferences
		IPreferenceStore pstore = TAUOptionsPlugin.getDefault().getPreferenceStore();
		String archpath=pstore.getString("TAUCDTArchPath");
		tbpath = archpath+File.separator+"bin";
		final String tlpath = archpath+File.separator+"lib";
		File test = new File(tlpath);
		String tmakefile=configuration.getAttribute(ITAULaunchConfigurationConstants.MAKEFILE, (String)null);
		//tmakepath=
		
		tmakepath=tlpath+File.separator+tmakefile;
		
		File testmf = new File(tmakepath);
		//if(!test.canRead())
		//	System.out.println("Error: please make sure that "+tmakepath+" is a valid TAU makefile.");
		
		if(!test.canRead()||!testmf.canRead())
		{
			//final ILaunchConfiguration finconf = configuration;
			Display.getDefault().syncExec(
					  new Runnable() 
					  {
					    public void run()
					    {
					    	//System.out.println("Error: Please specify a valid TAU arch directory");  //PlatformUI.getWorkbench().getDisplay()
							FileDialog dialog=null;
							try{
							//Shell sgame = new Shell(Display.getDefault());
							dialog = new FileDialog(Display.getCurrent().getActiveShell());//.getActiveShell());
							}catch(Exception e){e.printStackTrace();}
							//if(new File(tlpath).canRead())
								//dialog.setFilterPath(tlpath);
							String[] filt = {"Makefile.tau-*pdt*"};
							dialog.setFilterExtensions(filt);
							dialog.setText("Select a valid TAU Makefile from your <tau>/<arch>/lib directory");
							//dialog.setMessage("You must select a valid TAU architecture directory and stub Makefile.  Such a directory should be created when you configure and install TAU.  If no Makefiles are avilable, be sure you have configured TAU with -pdt=[dir] and any other relevant options.");
							
							//String correctPath = getFieldContent(tauArch.getText());
							if (tlpath != null) {
								File path = new File(tlpath);
								if (path.exists())
									dialog.setFilterPath(path.isFile() ? tlpath : path.getParent());
							}
							String selectedFile=null;
							while(true){
								selectedFile = dialog.open();
								if(selectedFile==null)
									return;
									//throw new FileNotFoundException("Invalid TAU Arch Directory");
								
								//String newtlpath=selectedPath+File.separator+"lib";
								File test = new File(selectedFile);
								//IPreferenceStore pstore = TAUOptionsPlugin.getDefault().getPreferenceStore();
								if (test.canRead())
								{
									askmakefile=test;
									//pstore.setValue("TAUCDTArchPath", selectedPath);
									break;
								}
								//dialog.setText("Select")
							}
					    }
					  });
			if(askmakefile==null)
				throw new FileNotFoundException("No MakefileSelected");
			tmakepath=askmakefile.getAbsolutePath();
			pstore.setValue("TAUCDTArchPath", askmakefile.getParentFile().getParent());
			
		}
		
		int selopt = configuration.getAttribute(ITAULaunchConfigurationConstants.SELECT, 0);
		String selcommand="";
		String selpath="";
		if(selopt==1)
		{
			selpath=thisCProject.getResource().getLocation().toOSString()+File.separator+"tau.selective";
			selcommand="-optTauSelectFile="+selpath;//+" -optKeepFiles";
		}
		else
		if(selopt==2)
		{
			selpath=configuration.getAttribute(ITAULaunchConfigurationConstants.SELECT_FILE, "");
			if(!selpath.equals(""))
				selcommand="-optTauSelectFile="+selpath;//+" -optKeepFiles"
		}
		
		tauoptchunk="";
		String tauopts = configuration.getAttribute(ITAULaunchConfigurationConstants.TAU_RUN_OPTS,"")+selcommand;//pstore.getString("TAUCDTOpts");
		if(!tauopts.equals("")){
			tauoptchunk=" -tau_options='"+tauopts+"'";
		}
	}
	
	
	public boolean standardMakeBuild() throws CoreException{
		
		File tauinc = new File(thisCProject.getResource().getLocation().toOSString()+File.separator+"eclipse.inc");
		File taudef = new File(thisCProject.getResource().getLocation().toOSString()+File.separator+"eclipse.inc.default");
		try{
			if(tauinc.exists())
			{
				InputStream in = new FileInputStream(tauinc);
				OutputStream out = new FileOutputStream(taudef);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}

			BufferedWriter tauout = new BufferedWriter(new FileWriter(tauinc));

			tauout.write("ECLIPSE_CC=" +tbpath+File.separator+"tau_cc.sh" +" -tau_makefile="+tmakepath+tauoptchunk+"\n");
			tauout.write("ECLIPSE_CXX="+tbpath+File.separator+"tau_cxx.sh"+" -tau_makefile="+tmakepath+tauoptchunk+"\n");
			tauout.write("ECLIPSE_FC="+tbpath+File.separator+"tau_f90.sh"+" -tau_makefile="+tmakepath+tauoptchunk+"\n");
			tauout.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(makefile.getContents().);
		//IMakeTarget selected = new IMakeTarget(); //= targetPart.getSelectedTarget();
		MakeTargetManager targetMan = new MakeTargetManager();
		targetMan.startup();
		//System.out.println(targetMan.hasTargetBuilder(thisProject));
		//targetMan.
		//String[] targbs = targetMan.getTargetBuilders(thisProject);
		//for(int i=0; i<targbs.length;i++)
		//	System.out.println(targbs[i]);

		IMakeTarget[] targs = targetMan.getTargets(thisProject);
		IMakeTarget select = null;
		//System.out.println(targs.length);
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
			return false;
		}

		System.out.println(select.getBuildLocation());
		//System.out.println(select.);

		select.build(new NullProgressMonitor());
		//targbs[0].
		//IMakeTarget target = targetMan.createTarget(thisProject, "all", "all");

		//super.okPressed();
		//if (selected != null) {
		//TargetBuild.buildTargets(null, new IMakeTarget[] { select });//getParentShell()  
		//}
		targetMan.shutdown();
		//System.out.println("NOT MANAGED!!!");
		//return;
		if(taudef.exists())
		{
			InputStream in;
			try {
				in = new FileInputStream(taudef);

				OutputStream out = new FileOutputStream(tauinc);

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
		return true;
	}
	/*False implies that no execution is to take place (either because of an error, or user request)*/
	public boolean managedMakeBuild() throws CoreException
	{
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(thisCProject.getResource());
		if (info == null||!info.isValid()){
			System.out.println("No info!!!");
			return false;
		}
		//Get the managed project object
		managedBuildProj = info.getManagedProject();
		if (managedBuildProj == null){
			System.out.println("No managed project!!!");
			return false;
		}
		olddefbuildconf=info.getDefaultConfiguration();//(IConfiguration) configlist.get(0);///chooseBuildConfiguration(configlist); ---FIX THIS---
		String binary = application.substring(application.indexOf(File.separator));
		String useconf= application.substring(0,application.indexOf(File.separator));
		//Make a list of the configurations already within the project
		IConfiguration[] buildconfigs = info.getManagedProject().getConfigurations();
		IConfiguration selectedconf = null;
		for(int i=0;i<buildconfigs.length;i++){
			if((buildconfigs[i].getName()).equals(useconf))
			{
				selectedconf=buildconfigs[i];
				break;
			}
		}
		
		if(selectedconf==null)
		{System.out.println("No Conf Selected");return false;}
		if(selectedconf.getName()==null)
		{System.out.println("Uh Oh!");return false;}
		
		//Make the new configuration name, and if there is already a configuration with that name, remove it.
		String taunameappend=("_"+tmakepath.substring(tmakepath.lastIndexOf(".")+1));
		String newname=selectedconf.getName();
		if(newname.indexOf(taunameappend)<0)
			newname += ("_"+tmakepath.substring(tmakepath.lastIndexOf(".")+1));
		
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
			//return;
		}
		newBuildConfig.setName(newname);
		IToolChain chain = newBuildConfig.getToolChain();
		ITool[] tools = chain.getTools();
		//Replace the compiler/linker commands with the correct tau compiler scripts and arguments.
		for(int i =0;i<tools.length;i++){
			String toolid=tools[i].getId();
			if(toolid.indexOf(".c.")>=0)
			{
				tools[i].setToolCommand(tbpath+File.separator+"tau_cc.sh"+" -tau_makefile="+tmakepath+tauoptchunk);
			}
			if(toolid.indexOf(".cpp.")>=0)
			{
				tools[i].setToolCommand(tbpath+File.separator+"tau_cxx.sh"+" -tau_makefile="+tmakepath+tauoptchunk);
			}
			if(toolid.indexOf(".fortran.")>=0)
			{
				tools[i].setToolCommand(tbpath+File.separator+"tau_f90.sh"+" -tau_makefile="+tmakepath+tauoptchunk);
			}
		}
		System.out.println(tbpath+File.separator+"tau_cc.sh"+" -tau_makefile="+tmakepath+tauoptchunk);
		ManagedBuildManager.saveBuildInfo(thisCProject.getProject(),true);
		//Build set the new configuration to default so we can build it.
		ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),newBuildConfig);
		SubProgressMonitor buildmonitor = new SubProgressMonitor(monitor,10);
		//NullProgressMonitor nullmon = new NullProgressMonitor();
		try {
			thisProject.build(IncrementalProjectBuilder.FULL_BUILD, buildmonitor);//CLEAN_BUILD

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		IFile programPath = thisProject.getFile(newname+binary);
		while(!programPath.exists() || !programPath.getLocation().toFile().exists())
		{
			//System.out.println("Looping Out!");
			if(monitor!=null&&buildmonitor.isCanceled())
			{
				ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
				throw new OperationCanceledException();
				//abort(LaunchMessages.getResourceString("AbstractTAULaunchConfigurationDelegate.Application_file_does_not_exist"), new FileNotFoundException(LaunchMessages.getFormattedResourceString("AbstractTAULaunchConfigurationDelegate.Application_path_not_found", programPath.getLocation().toString())), IStatus.INFO);
			}
			long numMillisecondsToSleep = 1000; // 1 seconds
			try {
				Thread.sleep(numMillisecondsToSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			programPath = thisProject.getFile(newname+binary);
		}
		
		if(configuration.getAttribute(ITAULaunchConfigurationConstants.BUILDONLY, false))
		{
			ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
			return false;
		}
		
		ILaunchConfigurationWorkingCopy confWC = configuration.getWorkingCopy();
		confWC.setAttribute(appnameattrib, newname+binary);//IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME
		configuration = confWC.doSave();
		return true;
	}
	
	public void cleanup() throws CoreException
	{
		if(managed)
		{
			ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
			if(!configuration.getAttribute(ITAULaunchConfigurationConstants.NOCLEAN, false))
				managedBuildProj.removeConfiguration(newBuildConfig.getId());
		}
		ILaunchConfigurationWorkingCopy confWC = configuration.getWorkingCopy();
		confWC.setAttribute(appnameattrib, application);//IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME
		configuration = confWC.doSave();
	}
	
	public void postlaunch() throws CoreException{
		
		if (monitor.isCanceled()) {
			cleanup();
			throw new OperationCanceledException();
		}
		
		final String directory=thisCProject.getUnderlyingResource().getLocation().toOSString();
		final String projname = thisCProject.getElementName();
		/*Contains all tau configuration options in the makefile name, except pdt*/
		final String projtype = tmakepath.substring(tmakepath.lastIndexOf("tau-")+4);

		class profilefilter implements FilenameFilter{
			public boolean accept(File dir, String name) {
				if(name.indexOf("profile.")!=0)
					return false;
				return true;
			}
		}
		
		class counterfilter implements FileFilter{

			public boolean accept(File pathname) {
				if(pathname.isDirectory())
				{
					if(pathname.getName().indexOf("MULTI__")==0)
						return true;
				}

				return false;
			}
			
		}
		
		class tracefilter implements FilenameFilter{
			public boolean accept(File dir, String name) {
				if(name.indexOf(".trc")>0||name.indexOf(".edf")>0)
					return true;
				return false;
			}
		}

		class perffilter implements FilenameFilter{
			public boolean accept(File dir, String name) {
				if(name.indexOf("perf_data.")!=0)
					return false;
				return true;
			}
		}

		File[] profiles = null;
		File dir = new File(directory);
		profilefilter profil=new profilefilter();
		profiles = dir.listFiles(profil);
		


		//perf_data.*
		//perf2tau
		boolean profilesdone = false;
		//String tmakefile=configuration.getAttribute(ITAULaunchConfigurationConstants.MAKEFILE, (String)null);
		//TODO: replace config check with makefile check
		boolean tracout=(configuration.getAttribute(ITAULaunchConfigurationConstants.EPILOG, false)||
						 configuration.getAttribute(ITAULaunchConfigurationConstants.VAMPIRTRACE, false)||
						 configuration.getAttribute(ITAULaunchConfigurationConstants.TRACE, false)||
						 configuration.getAttribute(ITAULaunchConfigurationConstants.PERF, false)||
						 projtype.indexOf("-trace")>=0);
		boolean profout=(configuration.getAttribute(ITAULaunchConfigurationConstants.CALLPATH, false)||
						configuration.getAttribute(ITAULaunchConfigurationConstants.PHASE, false)||
						configuration.getAttribute(ITAULaunchConfigurationConstants.MEMORY, false)||
						projtype.indexOf("-profile")>=0||projtype.indexOf("-headroom")>=0);
		
		//if we have trace output but no profile output it means we don't need to process profiles at all...
		
		boolean haveprofiles = (!tracout||(profout));
		
		if(!haveprofiles)
			profilesdone=true;
		//TODO:  Test this and repace the configuration with makefile check
		if(projtype.indexOf("-perf")>0)//configuration.getAttribute(ITAULaunchConfigurationConstants.PERF, false)
		{
			perffilter seekdir = new perffilter();
			File[] perfdir = dir.listFiles(seekdir);
			while(perfdir==null || perfdir.length<1)
			{
				perfdir = dir.listFiles(seekdir);
				try {
					if(monitor.isCanceled())
					{
						cleanup();
						throw new OperationCanceledException();
					}
					//long numMillisecondsToSleep = 1000; // 1 seconds
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}

			String perf2tau = tbpath+File.separator+"perf2tau";
			String s = null;
			try {
				Process p = Runtime.getRuntime().exec(perf2tau, null, dir);
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
			catch (Exception e) {System.out.println(e);}
			//profilesdone = true;
		}//Done with perf2tau

		final boolean multipapi=(projtype.indexOf("multiplecounters")>=0&&projtype.indexOf("papi")>=0);//configuration.getAttribute(ITAULaunchConfigurationConstants.PAPI, false);
		File[] counterdirs = null;
		counterfilter countfil=null;
		if(multipapi)
		{
			countfil=new counterfilter();
			counterdirs = dir.listFiles(countfil);
		}
		
		int nprocs = numprocs;//(new Integer(getNumberOfProcesses(configuration))).intValue();
		
		if(haveprofiles)
		while((profiles.equals(null) || profiles.length<nprocs)&&!profilesdone)
		{
			if(multipapi)
			{
				if(counterdirs!=null&&counterdirs.length>0)
				{
					profiles=counterdirs[counterdirs.length-1].listFiles(profil);
					//System.out.println(counterdirs[0].getName()+" "+counterdirs[0].list()[0]+" "+profiles.length);
				}
				counterdirs=dir.listFiles(countfil);
			}
			else
				profiles = dir.listFiles(profil);
			try {
				if(monitor.isCanceled())
				{
					cleanup();
					throw new OperationCanceledException();
				}
				//long numMillisecondsToSleep = 1000; // 1 seconds
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		
		//Put the profile data in the database and delete any profile files
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        String DATE_FORMAT = "yyyy-MM-dd_HH:mm:ss";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault());
        final String now = sdf.format(cal.getTime());
		
		
		
		File[] rem=null;
		if(multipapi)
			rem=counterdirs;
			else
				rem=profiles;
		final File[] remprofs = rem;
		final boolean keepprofs=configuration.getAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, false);
		if(haveprofiles)
		Display.getDefault().asyncExec(new Runnable() 
		{
			public void run() 
			{	
				boolean hasdb = false;
				try{
					hasdb = PerfDMFUIPlugin.addPerformanceData(projname, projtype, directory);
					if(!hasdb)
					{
						MessageDialog.openInformation(
								PlatformUI.getWorkbench().getDisplay().getActiveShell(),
								"TAU Warning",
								"Adding data to your perfdmf database failed.  Please make sure that you have successfully run perfdmf_configure with your selected TAU installation.");
						
					}
				}catch(Exception e){
					e.printStackTrace();
					}
				if(!keepprofs&&hasdb)
					for(int i=0;i<remprofs.length;i++)
					{
						//System.out.println(remprofs[i].getName());
						if(multipapi)
						{
							File[] profs = remprofs[i].listFiles();
							for(int j=0;j<profs.length;j++)
								profs[j].delete();
						}
						remprofs[i].delete();
					}
				else
				{
					File profdir = new File(directory+File.separator+"Profiles"+File.separator+projtype+File.separator+now);
					profdir.mkdirs();
					for(int i=0;i<remprofs.length;i++)
					{
						remprofs[i].renameTo(new File(profdir+File.separator+remprofs[i].getName()));
					}
				}
			}
		});
		/*
		if(configuration.getAttribute(ITAULaunchConfigurationConstants.TRACE, false))
		{
            //trial.setName();
			tracefilter tracefind = new tracefilter();
			File[] mvtrc=dir.listFiles(tracefind);
			while((mvtrc.equals(null) || mvtrc.length<nprocs*2))
			{
				mvtrc = dir.listFiles(tracefind);
				try {
					if(monitor.isCanceled())
					{
						//cleanup(configuration);
						throw new OperationCanceledException();
					}
					//long numMillisecondsToSleep = 1000; // 1 seconds
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			
			File trcdir = new File(directory+File.separator+"Traces"+File.separator+projtype+File.separator+now);
			trcdir.mkdirs();
			for(int i=0;i<mvtrc.length;i++)
			{
				mvtrc[i].renameTo(new File(trcdir+File.separator+remprofs[i].getName()));
			}
		}*/
		cleanup();
	}
}
