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
package org.eclipse.ptp.perf.tau;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.perf.AbstractPerformanceDataManager;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.internal.BuildLaunchUtils;
import org.eclipse.ptp.perf.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.ptp.perf.tau.perfdmf.views.PerfDMFView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class TAUPerformanceDataManager extends AbstractPerformanceDataManager{
	//private static final IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
	
	private static String tbpath = null;
	
	/**
	 * Returns the TAU makefile associated with the supplied launch configuration
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private static String getTauMakefile(ILaunchConfiguration configuration) throws CoreException
	{
		return configuration.getAttribute(ITAULaunchConfigurationConstants.TAU_MAKEFILE, (String)null);
	}
	
//	/**
//	 * Gets the path to the TAU arch directory, as stored in the eclipse workspace
//	 * @return the path to the TAU arch directory
//	 */
//	protected static String getTauArchPath(){
//		File binPath=new File(pstore.getString(ITAULaunchConfigurationConstants.TAU_BIN_PATH));
//		if(binPath.canRead())
//			return binPath.getParent().toString();
//		
//		return "";
//		//return pstore.getString(ITAULaunchConfigurationConstants.TAU_ARCH_PATH);
//	}
	
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
	boolean useExt=false;
	public void setExternalTarget(boolean useExt){
		this.useExt=useExt;
	}

	public void process(String projname, ILaunchConfiguration configuration,String directory) throws CoreException {
		tbpath=BuildLaunchUtils.getToolPath("tau");
		if(useExt||projname==null){
			boolean usePortal=configuration.getAttribute(ITAULaunchConfigurationConstants.PORTAL, false);
			String[] profIDs = getProfIDs();//manageProfiles(projectLocation,dbname,usePortal);
			
			if(profIDs==null){
				return;
			}
			
			File[] profs = getProfiles(directory);
			if(profs==null||profs.length==0)
			{
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog
						.openInformation(
								PlatformUI.getWorkbench()
								.getDisplay()
								.getActiveShell(),
								"TAU Warning",
						"No profile data was located.  Please make sure profile data is available");
					}});
				
				
				return;
			}
			
			projname=profIDs[0];
			String projtype=profIDs[1];
			String projtrial=profIDs[2];
			
			String xmlMetaData=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.PERF_XML_METADATA, (String)null);
			String database= PerfDMFView.extractDatabaseName(configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB,(String)null));
			boolean hasdb=addToDatabase(directory,database,projname,projtype,projtrial,xmlMetaData);
			if (!hasdb) {
				
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog
						.openInformation(
								PlatformUI.getWorkbench()
								.getDisplay()
								.getActiveShell(),
								"TAU Warning",
						"Adding data to your perfdmf database failed.  Please make sure that you have successfully run perfdmf_configure with your selected TAU installation.");
					}});
				
				
				return;
			}
			
			if(usePortal){
				File ppkfile=getPPKFile(directory,projname,projtype,projtrial);
				if(ppkfile==null||!ppkfile.canRead())
				{
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog
							.openInformation(
									PlatformUI.getWorkbench()
									.getDisplay()
									.getActiveShell(),
									"TAU Warning",
							"Could not generate ppk file from profile data");
						}});
					
					
				}
				else{
					
						runPortal(ppkfile);
					
						
				}
				ppkfile.delete();
			}
			
			return;
		}
		
		//System.out.println("HEY!"+projectLocation+" vs "+thisCProject.getResource().getLocation().toOSString());
		//String projname = thisCProject.getElementName();
		
		
		
		/*Contains all tau configuration options in the makefile name, except pdt*/
		String makename=getTauMakefile(configuration);
		String projtype = makename.substring(makename.lastIndexOf("tau-")+4);
		
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
		//+File.separator+"bin";
		String projtrial = BuildLaunchUtils.getNow();

		//TODO:  Test this and repace the configuration with makefile check
		if(projtype.indexOf("-perf")>0)
			managePerfFiles(directory);
		
		//Put the profile data in the database and delete any profile files
		//Also generate MPI include list if specified (@author: raportil)
		if(haveprofiles)
		{
			String expAppend=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.PERF_EXPERIMENT_APPEND, (String)null);
			boolean useP=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.PARA_USE_PARAMETRIC,false);
			
			
			if(expAppend!=null&&expAppend.length()>0)
			{
				projtype+="_"+expAppend;
			}
			
			File[] profs = getProfiles(directory);
			if(!useP&&profs==null||profs.length==0)
			{
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog
						.openInformation(
								PlatformUI.getWorkbench()
								.getDisplay()
								.getActiveShell(),
								"TAU Warning",
						"No profile data was located.  Please make sure profile data is available");
					}});
			}
			
			boolean runtauinc = configuration.getAttribute(ITAULaunchConfigurationConstants.TAUINC, false);
			if(runtauinc){
				runTAUInc(directory,projname,projtype,projtrial);
			}
			
			
			String xmlMetaData=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.PERF_XML_METADATA, (String)null);
			String database= PerfDMFView.extractDatabaseName(configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB,(String)null));
			boolean hasdb=addToDatabase(directory,database,projname,projtype,projtrial,xmlMetaData);
			if (!hasdb&&!useP) {
				
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog
						.openInformation(
								PlatformUI.getWorkbench()
								.getDisplay()
								.getActiveShell(),
								"TAU Warning",
						"Adding data to your perfdmf database failed.  Please make sure that you have successfully run perfdmf_configure with your selected TAU installation.");
					}});
				
				
			}
			
			boolean keepprofs = configuration.getAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, false);
			boolean useportal=configuration.getAttribute(ITAULaunchConfigurationConstants.PORTAL, false);//TODO: Enable portal use via external data interface
			
			if(keepprofs||!hasdb||useportal){
				File ppkfile=getPPKFile(directory,projname,projtype,projtrial);
				if(ppkfile==null||!ppkfile.canRead())
				{
					
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog
							.openInformation(
									PlatformUI.getWorkbench()
									.getDisplay()
									.getActiveShell(),
									"TAU Warning",
							"Could not generate ppk file from profile data");
						}});
					
					
				}
				else{
					if(useportal){
						runPortal(ppkfile);
					}
					if(keepprofs||!hasdb){
						movePakFile(directory,projtype,projtrial,ppkfile);
					}
					else{
						ppkfile.delete();
					}
				}
			}
			
			removeProfiles(profs);
			
			boolean useperfex=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.PERF_LAUNCH_PERFEX, false);
			if(useperfex){
				String perfexScript=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.PARA_PERF_SCRIPT,(String)null);
				runPerfEx(directory,database,projname,projtype,perfexScript);
			}
		}
		
		//TODO: Enable tracefile management
		//if(tracout||configuration.getAttribute(ITAULaunchConfigurationConstants.TRACE, false))
		//manageTraceFiles(directory, projtype,now);
		
	}

	@Override
	public void view() {
		// TODO Auto-generated method stub
		
	}
	
	
	private static String[] getProfIDs(){// manageProfiles(final String directory,final String database, final boolean useportal) throws CoreException{
		
//		String pName="a";//null;
//		String pType="b";//null;
//		String pTrial="c";//null;

		final String queries[]=new String[3];
		queries[0]="Application Name:";//pName;
		queries[1]="Experiment Name:";//pType;
		queries[2]="Trial Name:";//pTrial;
		
		final String values[] = new String[3];
		
//		class MFDRunner implements Runnable{
//			MFDRunner(String[] queries){
//				this.queries=queries;
//			}
//			public String[] values=null;
//			public String[] queries=null;
//			public void run() {
//				Shell shell=PlatformUI.getWorkbench().getDisplay().getActiveShell();
//				MultiFieldDialog mfd = new MultiFieldDialog(shell,queries);
//				values=mfd.open();
//			}
//		}
//		
//		final MFDRunner mfdr=new MFDRunner(ids);

		Display.getDefault().syncExec(new Runnable(){

			public void run() {
				Shell shell=PlatformUI.getWorkbench().getDisplay().getActiveShell();
				MultiFieldDialog mfd = new MultiFieldDialog(shell,queries);
				mfd.open();
				String[] outvals=mfd.getValues();
				for(int i=0;i<values.length;i++){
					values[i]=outvals[i];
				}
				
			}
			
		});
		return values;
		
	}
	
	private static File[] getProfiles(String directory){
		class Profilefilter implements FilenameFilter {
			public boolean accept(File dir, String name) {
				if (name.indexOf("profile.") != 0)
					return false;
				return true;
			}
		}

		class Counterfilter implements FileFilter {

			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					if (pathname.getName().indexOf("MULTI__") == 0)
						return true;
				}
				return false;
			}

		}

		File[] profiles = null;
		File dir = new File(directory);
		Profilefilter profil = new Profilefilter();
		profiles = dir.listFiles(profil);
		
		if(profiles.length>0)
		{
			return profiles;
		}

		//final boolean multipapi = (projtype.indexOf("multiplecounters") >= 0 && projtype.indexOf("papi") >= 0);// configuration.getAttribute(ITAULaunchConfigurationConstants.PAPI,false);
		//File[] counterdirs = null;
		Counterfilter countfil = new Counterfilter();
		//if (multipapi) {
			
		profiles = dir.listFiles(countfil);
		if(profiles.length>0)
		{
			return profiles;
		}

		return null;
	}
	
	private static File getPPKFile(final String directory, final String projname, final String projtype, final String projtrial){
		
		class ppPaker implements Runnable {
			public String ppk;
			public void run() {
				//String tbpath = BuildLaunchUtils.getToolPath("tau");


				String ppkname = projname + "_" + projtype + "_" + projtrial+ ".ppk";
				String paraprof = "";
				if(tbpath!=null && tbpath.length()>0)
					paraprof+= tbpath + File.separator;
				paraprof+= "paraprof --pack " + ppkname;
				System.out.println(paraprof);
				ppk = directory + File.separator + ppkname;

				BuildLaunchUtils.runTool(paraprof, null,new File(directory));
			}}
		
		ppPaker ppp=new ppPaker();
		Display.getDefault().syncExec(ppp);

		return new File(ppp.ppk);
	}
	
	private static boolean addToDatabase(final String directory, final String database, final String projname, final String projtype, final String projtrial, String xmlMetaData){

		boolean hasdb=false;
		try {
			//if(usingParameters)
			//{
			
			if(database!=null&&!database.equals(ITAULaunchConfigurationConstants.NODB))
			{
				/* TODO: Re-enable this or find another way to support metadata uploading!
				String metaSpec=" ";
				File xmlFi=null;
				if(xmlMetaData!=null&&xmlMetaData.length()>0){
					String xmlLoc=directory+File.separator+"perfMetadata.xml";
					xmlFi=new File(xmlLoc);
					FileOutputStream o;
					PrintStream p;
					try {
						o=new FileOutputStream(xmlFi);
						p=new PrintStream(o);

						p.println(xmlMetaData);
						p.close();
						if(xmlFi.exists()&&xmlFi.canRead()){
							metaSpec=" -m "+xmlLoc+" ";
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				String db="";
				if(!database.equals("Default")){
					db=" -c "+database;
				}

				String ex = "";
				if(tbpath!=null && tbpath.length()>0)
					ex+=tbpath+File.separator;
				ex+="perfdmf_loadtrial -a "+projname+" -x "+projtype+" -n "+projtrial+db+metaSpec+ directory;//-m metaDataFile
				hasdb=BuildLaunchUtils.runTool(ex, null,new File(directory));
				
				
				if(hasdb==false)
				{
					return false;
				}
				
				//hasdb=true;
				if(xmlFi!=null){
					xmlFi.delete();
				}
				
				
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						//}
						//else{
						//if(database!=null&&!database.equals(ITAULaunchConfigurationConstants.NODB))
						//{
						PerfDMFUIPlugin.displayPerformanceData(projname,projtype,projtrial);//,directory, database
						//}
					}});
				*/
				
				class DBView implements Runnable{
					boolean hasdb=false;
					public void run() {
						hasdb = PerfDMFUIPlugin.addPerformanceData(projname,projtype, projtrial, directory, database);
						
					}
					
				}
				DBView dbv=new DBView();
				Display.getDefault().syncExec(dbv);
				hasdb=dbv.hasdb;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hasdb;
	}

	
	private static void movePakFile(String directory, String projtype, String projtrial, File ppkFile){
		File profdir = new File(directory + File.separator
				+ "Profiles" + File.separator + projtype
				+ File.separator + projtrial);
		profdir.mkdirs();

		ppkFile.renameTo(new File(profdir + File.separator
				+ ppkFile.getName()));
	}
	
	private static void removeProfiles(File[] remprofs){
		
		if(remprofs==null||remprofs.length==0)
			return;
		
		boolean multipapi=remprofs[0].isDirectory();
		
		for (int i = 0; i < remprofs.length; i++) {
			if (multipapi) {
				File[] profs = remprofs[i].listFiles();
				for (int j = 0; j < profs.length; j++)
					profs[j].delete();
			}
			remprofs[i].delete();
		}
	}
	
	private static void runTAUInc(final String directory, final String projname, final String projtype, final String projtrial){
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					
					//String tbpath = BuildLaunchUtils.getToolPath("tau");
					
					/*
					* Produce MPI include list if specified.
					* FIXME: Running getRuntime directly instead of through runTool to
					* pipe include list to a file
					* @author raportil
					*/					
					
						String mpilistname = projname + "_" + projtype + "_" + projtrial+ ".includelist";
						String tauinc = "cd " + directory + " ; ";
						if(tbpath!=null && tbpath.length()>0)
							tauinc+= tbpath + File.separator;
						tauinc+= "tauinc.sh > " + mpilistname;
						System.out.println(tauinc);
						try{
							String[] cmd = {"sh", "-c", tauinc};
							//Process p = 
							Runtime.getRuntime().exec(cmd);
						}
						catch (Exception e) {e.printStackTrace();}
					}});
	}
	
	private static void runPerfEx(String directory, String database, String projname, String projtype, String perfExScript){
		
		String script=perfExScript+" -c "+database+" -p app="+projname+",exp="+projtype;

		BuildLaunchUtils.runTool(script, null,new File(directory));
	}
	
	private static boolean runPortal(final File ppkFile){
		boolean hasdb = true;
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
		
		try {
			TAUPortalUploadDialog pwDialog = new TAUPortalUploadDialog(
					PlatformUI.getWorkbench().getDisplay()
							.getActiveShell(), ppkFile);
			int result = pwDialog.open();
			if (result != TAUPortalUploadDialog.OK && result != TAUPortalUploadDialog.CANCEL) {
				MessageDialog
						.openInformation(
								PlatformUI.getWorkbench()
										.getDisplay()
										.getActiveShell(),
								"TAU Warning",
								"Adding data to your online database failed.  Please make sure that the given URL, username and password are correct.");
				//hasdb = false;
			}
		} catch (Exception e) {
			MessageDialog
					.openInformation(
							PlatformUI.getWorkbench()
									.getDisplay()
									.getActiveShell(),
							"TAU Warning",
							"Adding data to your online database failed.  Please make sure that the given URL, username and password are correct.");
			//hasdb = false;
		}
		
			}});
		return hasdb;
	}
	
	/**
	 * Handle files produced by 'perflib' instrumentation by converting them to the TAU format and moving them to an
	 * appropriate directory
	 * @param directory The path to the directory containing the performance data
	 * @param tbpath The path to the TAU bin directory
	 * @param monitor
	 * @throws CoreException
	 */
	private static void managePerfFiles(String directory) throws CoreException
	{
		class perffilter implements FilenameFilter{
			public boolean accept(File dir, String name) {
				if(name.indexOf("perf_data.")!=0)
					return false;
				return true;
			}
		}
		
		//String tbpath = BuildLaunchUtils.getToolPath("tau");
		
		perffilter seekdir = new perffilter();
		File dir = new File(directory);
		File[] perfdir = dir.listFiles(seekdir);

		if(perfdir==null||perfdir.length<1)
		{
			return;
		}

		String perf2tau = "";
		if(tbpath!=null && tbpath.length()>0)
			perf2tau+=tbpath+File.separator;
		perf2tau+="perf2tau";
		
		BuildLaunchUtils.runTool(perf2tau,null,dir);
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "process-TAU-data";
	}
	
	

}
