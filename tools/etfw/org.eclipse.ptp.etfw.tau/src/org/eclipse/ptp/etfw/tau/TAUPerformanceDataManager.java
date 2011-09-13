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
package org.eclipse.ptp.etfw.tau;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.etfw.AbstractToolDataManager;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.internal.BuildLaunchUtils;
import org.eclipse.ptp.etfw.internal.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.internal.RemoteBuildLaunchUtils;
import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.ptp.etfw.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.ptp.etfw.tau.perfdmf.views.PerfDMFView;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class TAUPerformanceDataManager extends AbstractToolDataManager{
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
	
	
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
	boolean useExt=false;
	public void setExternalTarget(boolean useExt){
		this.useExt=useExt;
	}
	private IBuildLaunchUtils utilBlob=null;
	public void process(String projname, ILaunchConfiguration configuration,String directory) throws CoreException {
		IResourceManager rm = RemoteBuildLaunchUtils.getResourceManager(configuration);
		if(rm!=null)
			utilBlob=new RemoteBuildLaunchUtils(rm);
		else
			utilBlob = new BuildLaunchUtils();
		tbpath=utilBlob.getToolPath(Messages.TAUPerformanceDataManager_0);
		List<IFileStore> profs=null;
		if(useExt||projname==null){
			boolean usePortal=configuration.getAttribute(ITAULaunchConfigurationConstants.PORTAL, false);
			String[] profIDs = requestProfIDs();
			
			if(profIDs==null){
				return;
			}
			
			profs = getProfiles(directory);
			if(profs==null||profs.size()==0)
			{
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog
						.openInformation(
								PlatformUI.getWorkbench()
								.getDisplay()
								.getActiveShell(),
								Messages.TAUPerformanceDataManager_TAUWarning,
						Messages.TAUPerformanceDataManager_NoProfData);
					}});
				
				
				return;
			}
			
			projname=profIDs[0];
			String projtype=profIDs[1];
			String projtrial=profIDs[2];
			
			String xmlMetaData=configuration.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_XML_METADATA, (String)null);
			String database= PerfDMFView.extractDatabaseName(configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB,(String)null));
			boolean hasdb=addToDatabase(utilBlob.getFile(directory),database,projname,projtype,projtrial,xmlMetaData);
			if (!hasdb) {
				
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog
						.openInformation(
								PlatformUI.getWorkbench()
								.getDisplay()
								.getActiveShell(),
								Messages.TAUPerformanceDataManager_TAUWarning,
						Messages.TAUPerformanceDataManager_AddingDataPerfDBFailed);
					}});
				
				
				return;
			}
			
			if(usePortal){
				IFileStore ppkfile=getPPKFile(directory,projname,projtype,projtrial);
				if(ppkfile==null||!ppkfile.fetchInfo().exists())
				{
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog
							.openInformation(
									PlatformUI.getWorkbench()
									.getDisplay()
									.getActiveShell(),
									Messages.TAUPerformanceDataManager_TAUWarning,
							Messages.TAUPerformanceDataManager_CouldNotGenPPK);
						}});
					
					
				}
				else{
					
						runPortal(ppkfile);
					
						
				}
				ppkfile.delete(EFS.NONE,null);
			}
			
			return;
		}
		
		
		/*Contains all tau configuration options in the makefile name, except pdt*/
		String makename=getTauMakefile(configuration);
		int tauDex=makename.lastIndexOf("tau-"); //$NON-NLS-1$
		String projtype=null;
		if(tauDex<0){
			projtype="tau"; //$NON-NLS-1$
		}
		else
		projtype = makename.substring(tauDex+4);
		
		//TODO: replace config check with makefile check
		boolean tracout=(configuration.getAttribute(ITAULaunchConfigurationConstants.EPILOG, false)||
						 configuration.getAttribute(ITAULaunchConfigurationConstants.VAMPIRTRACE, false)||
						 configuration.getAttribute(ITAULaunchConfigurationConstants.TRACE, false)||
						 configuration.getAttribute(ITAULaunchConfigurationConstants.PERF, false)||
						 projtype.indexOf("-trace")>=0); //$NON-NLS-1$
		boolean profout=(configuration.getAttribute(ITAULaunchConfigurationConstants.CALLPATH, false)||
						configuration.getAttribute(ITAULaunchConfigurationConstants.PHASE, false)||
						configuration.getAttribute(ITAULaunchConfigurationConstants.MEMORY, false)||
						projtype.indexOf("-profile")>=0||projtype.indexOf("-headroom")>=0); //$NON-NLS-1$ //$NON-NLS-2$
		
		//if we have trace output but no profile output it means we don't need to process profiles at all...
		boolean haveprofiles = (!tracout||(profout));
		//+File.separator+"bin";
		String projtrial = BuildLaunchUtils.getNow();

		//TODO:  Test this and repace the configuration with makefile check
		if(projtype.indexOf("-perf")>0) //$NON-NLS-1$
			managePerfFiles(directory);
		
		//Put the profile data in the database and delete any profile files
		//Also generate MPI include list if specified (@author: raportil)
		if(haveprofiles)
		{
			String expAppend=configuration.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_EXPERIMENT_APPEND, (String)null);
			boolean useP=configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_USE_PARAMETRIC,false);
			
			
			if(expAppend!=null&&expAppend.length()>0)
			{
				projtype+="_"+expAppend; //$NON-NLS-1$
			}
			
			IFileStore xmlprof=null;
			//TODO: Return support for regular profiles/ppk files
			//profs = getProfiles(directory);
			//if(!useP&&profs==null||profs.size()==0)
			//{
				xmlprof = utilBlob.getFile(directory).getChild(PROFXML);//new File(directory+File.separatorChar+PROFXML);
				if(!xmlprof.fetchInfo().exists()){
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog
							.openInformation(
									PlatformUI.getWorkbench()
									.getDisplay()
									.getActiveShell(),
									Messages.TAUPerformanceDataManager_TAUWarning,
							Messages.TAUPerformanceDataManager_NoProfData);
						}});
					return;
				}
			//}
			
			boolean runtauinc = configuration.getAttribute(ITAULaunchConfigurationConstants.TAUINC, false);
			if(runtauinc){
				runTAUInc(directory,projname,projtype,projtrial);
			}
			
			
			String xmlMetaData=configuration.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_XML_METADATA, (String)null);
			String database= PerfDMFView.extractDatabaseName(configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB,(String)null));
			boolean hasdb=addToDatabase(utilBlob.getFile(directory),database,projname,projtype,projtrial,xmlMetaData);
			if (!hasdb&&!useP) {
				
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog
						.openInformation(
								PlatformUI.getWorkbench()
								.getDisplay()
								.getActiveShell(),
								Messages.TAUPerformanceDataManager_TAUWarning,
						Messages.TAUPerformanceDataManager_AddingDataPerfDBFailed);
					}});
				
				
			}
			
			boolean keepprofs = configuration.getAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, false);
			boolean useportal=configuration.getAttribute(ITAULaunchConfigurationConstants.PORTAL, false);//TODO: Enable portal use via external data interface
			
			if(keepprofs||!hasdb||useportal){
				IFileStore ppkfile=getPPKFile(directory,projname,projtype,projtrial);
				if(ppkfile==null||!ppkfile.fetchInfo().exists())
				{
					
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog
							.openInformation(
									PlatformUI.getWorkbench()
									.getDisplay()
									.getActiveShell(),
									Messages.TAUPerformanceDataManager_TAUWarning,
							Messages.TAUPerformanceDataManager_CouldNotGeneratePPK);
						}});
					
					
				}
				else{
					if(useportal){
						runPortal(ppkfile);
					}
					if(keepprofs||!hasdb){
						if(ppkfile!=null)
							movePakFile(directory,projtype,projtrial,ppkfile);
						if(xmlprof!=null)
							movePakFile(directory,projtype,projtrial,xmlprof);
					}
					else{
						ppkfile.delete(EFS.NONE,null);
					}
				}
			}
			
			removeProfiles(profs);//TODO: xml profiles don't make a mess, so save?
			if(xmlprof!=null)
				xmlprof.delete(EFS.NONE,null);
			
			boolean useperfex=configuration.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_LAUNCH_PERFEX, false);
			if(useperfex){
				String perfexScript=configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_PERF_SCRIPT,(String)null);
				
				if(perfexScript!=null&&perfexScript.length()>0){
					runPerfEx(directory,database,projname,projtype,perfexScript);
				}
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
	
	/**
	 * Asks the user for the tags to use on the profile data when uploading to a database.
	 * @return Array containing three Strings; User provided names for the application, experiment and trial.
	 */
	private static String[] requestProfIDs(){

		final String queries[]=new String[3];
		queries[0]=Messages.TAUPerformanceDataManager_AppName;
		queries[1]=Messages.TAUPerformanceDataManager_ExpName;
		queries[2]=Messages.TAUPerformanceDataManager_TrialName;
		
		final String values[] = new String[3];

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
	
	private static final String PROFDOT="profile.";
	private static final String PROFXML="tauprofile.xml";
	private static final String MULTI="MULTI__";
	
	/**
	 * Checks the directory for MULTI directories or profile. files.  Returns a list containing all of either type found, but not both. If a directory contains both only profile. files will be returned.
	 * @param directory The main/output directory
	 * @return List of file objects representing profiles or MULTI directories containing profiles
	 */
	private List<IFileStore> getProfiles(String directory){
		IFileStore[] profiles = null;
		IFileStore dir = utilBlob.getFile(directory);
		try {
			profiles = dir.childStores(EFS.NONE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		ArrayList<IFileStore> profs=new ArrayList<IFileStore>();
		ArrayList<IFileStore> dirs=new ArrayList<IFileStore>();
		for(int i=0;i<profiles.length;i++){
			IFileInfo proinfo=profiles[i].fetchInfo();
			if(!proinfo.isDirectory()&&proinfo.getName().startsWith(PROFDOT)){
				profs.add(profiles[i]);
			}
			else if(proinfo.isDirectory()&&proinfo.getName().startsWith(MULTI)){
				dirs.add(profiles[i]);
			}
		}
		
		
		if(profiles.length>0)
		{
			return profs;
		}

		if(dirs.size()>0)
		{
			return dirs;
		}

		return null;
	}
	private static final String UNIX_SLASH="/";
	
	
	
	private IFileStore getPPKFile(final String directory, final String projname, final String projtype, final String projtrial){
		
		class ppPaker implements Runnable {
			public String ppk;
			public void run() {

				List<String> paraCommand = new ArrayList<String>();

				String ppkname = projname + "_" + projtype + "_" + projtrial+ ".ppk"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String paraprof = ""; //$NON-NLS-1$
				
				if(tbpath!=null && tbpath.length()>0){
					paraprof+= tbpath + UNIX_SLASH;
				}
				paraprof+= "paraprof";  //$NON-NLS-1$
				String pack = "--pack " + ppkname; //$NON-NLS-1$
				System.out.println(paraprof+" "+pack); //$NON-NLS-1$
				ppk = directory + UNIX_SLASH + ppkname;
				
				paraCommand.add(paraprof);
				paraCommand.add(pack);

				utilBlob.runTool(paraCommand, null,directory);
			}}
		
		ppPaker ppp=new ppPaker();
		Display.getDefault().syncExec(ppp);

		return utilBlob.getFile(ppp.ppk);
	}
	
	private static boolean addToDatabase(final IFileStore directory, final String database, final String projname, final String projtype, final String projtrial, String xmlMetaData){

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
				final IFileStore local;
				IFileStore xml = directory.getChild("tauprofile.xml");
				if(!xml.fetchInfo().exists()){
					return false;
				}
				if(!xml.getFileSystem().equals(EFS.getLocalFileSystem())){
					File f = xml.toLocalFile(EFS.CACHE,null);
					if(!f.exists()){
						return false;
					}
					String s = f.getCanonicalPath();
					local =EFS.getLocalFileSystem().getStore(URI.create(s));
				
				}
				else{
				local =xml;
				}
				class DBView implements Runnable{
					boolean hasdb=false;
					public void run() {
						hasdb = PerfDMFUIPlugin.addPerformanceData(projname,projtype, projtrial, local, database);
						
					}
					
				}
				DBView dbv=new DBView();
				Display.getDefault().syncExec(dbv);
				hasdb=dbv.hasdb;
			}
		} catch (Exception e) {
			e.printStackTrace();
			hasdb=false;
		}
		return hasdb;
	}

	/**
	 * Moves the ppk file to an approriately named subdirectory of a Profiles directory in the top-level output directory
	 * @param directory The top level output directory, where the Profiles directory is created
	 * @param projtype Created a subdirectory of Profiles
	 * @param projtrial Created as a subdirectory of projtype
	 * @param ppkFile This file is moved to the projtrial directory
	 */
	private void movePakFile(String directory, String projtype, String projtrial, IFileStore ppkFile){
		IFileStore profdir = utilBlob.getFile(directory).getChild("Profiles").getChild(projtype).getChild(projtrial);
				
		try {
			profdir.mkdir(EFS.NONE,null);
			ppkFile.move(profdir.getChild(ppkFile.fetchInfo().getName()), EFS.NONE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Deletes either specified profiles or MULTI profile directories and the profiles they contain
	 * @param remprofs List of either profile files or MULTI profile directories
	 */
	private static void removeProfiles(List<IFileStore> remprofs){
		
		if(remprofs==null||remprofs.size()==0)
			return;
		
		boolean multipapi=remprofs.get(0).fetchInfo().isDirectory();
		try {
		for (int i = 0; i < remprofs.size(); i++) {
			if (multipapi) {
				IFileStore[] profs;
				
					profs = remprofs.get(i).childStores(EFS.NONE, null);
				
				for (int j = 0; j < profs.length; j++)
					profs[j].delete(EFS.NONE,null);
			}
			remprofs.get(i).delete(EFS.NONE,null);
		
		}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void runTAUInc(final String directory, final String projname, final String projtype, final String projtrial){
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					/*
					* Produce MPI include list if specified.
					* FIXME: Running getRuntime directly instead of through runTool to
					* pipe include list to a file
					* @author raportil
					*/					
					
						String mpilistname = projname + "_" + projtype + "_" + projtrial+ ".includelist"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						String tauinc = "cd " + directory + " ; "; //$NON-NLS-1$ //$NON-NLS-2$
						if(tbpath!=null && tbpath.length()>0)
							tauinc+= tbpath +UNIX_SLASH;
						tauinc+= "tauinc.sh > " + mpilistname; //$NON-NLS-1$
						System.out.println(tauinc);
						try{
							List<String> cmd = new ArrayList<String>();// {"sh", "-c", tauinc}; //$NON-NLS-1$ //$NON-NLS-2$
							cmd.add("sh");
							cmd.add("-c");
							cmd.add(tauinc);
							utilBlob.runTool(cmd, null, directory);//TODO: Test This!
						}
						catch (Exception e) {e.printStackTrace();}
					}});
	}
	
	private void runPerfEx(String directory, String database, String projname, String projtype, String perfExScript){
		
		
		List<String> script = new ArrayList<String>();
		script.add(perfExScript);
		script.add(" -c "+database); //$NON-NLS-1$
		script.add(" -p app="+projname+",exp="+projtype); //$NON-NLS-1$ //$NON-NLS-2$
		
		//String script=perfExScript+" -c "+database+" -p app="+projname+",exp="+projtype;

		
		
		utilBlob.runTool(script, null,directory);
	}
	
	private static boolean runPortal(final IFileStore ppkFile){
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
								Messages.TAUPerformanceDataManager_TAUWarning,
								Messages.TAUPerformanceDataManager_AddingOnlineDBFailed);
				//hasdb = false;
			}
		} catch (Exception e) {
			MessageDialog
					.openInformation(
							PlatformUI.getWorkbench()
									.getDisplay()
									.getActiveShell(),
							Messages.TAUPerformanceDataManager_TAUWarning,
							Messages.TAUPerformanceDataManager_AddingOnlineDBFailed);
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
	private void managePerfFiles(String directory) throws CoreException
	{

		IFileStore dir = utilBlob.getFile(directory);
		IFileStore[] perfdir = dir.childStores(EFS.NONE, null);

		if(perfdir==null||perfdir.length<1)
		{
			return;
		}
		
		int count=0;
		for(int i=0;i<perfdir.length;i++){
			if(perfdir[i].fetchInfo().getName().startsWith("perf_data."))
				count++;
		}

		if(count==0)
			return;
		
		List<String> command = new ArrayList<String>();
		String perf2tau=""; //$NON-NLS-1$
		if(tbpath!=null && tbpath.length()>0)
			perf2tau+=UNIX_SLASH;
		perf2tau+="perf2tau"; //$NON-NLS-1$
		command.add(perf2tau);
		utilBlob.runTool(command,null,directory);
		
	}

	@Override
	public String getName() {
		return "process-TAU-data"; //$NON-NLS-1$
	}
	
	

}
