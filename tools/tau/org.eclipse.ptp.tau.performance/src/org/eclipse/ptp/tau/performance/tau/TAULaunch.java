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
package org.eclipse.ptp.tau.performance.tau;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.ptp.tau.performance.Activator;
import org.eclipse.ptp.tau.performance.internal.BuildLaunchUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Defines TAU-specific launch configuration/build varaibles.  TODO: This should eventually be merged
 * into the generic xml-driven analysis system
 * @author wspear
 *
 */
public class TAULaunch{

	private static final IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
	
	public static void adjustBuild(IConfiguration buildConf){
		if(Activator.getDefault().getPluginPreferences().getBoolean("TAUCheckForAIXOptions"))
			((Configuration)buildConf).enableInternalBuilder(true);
	}
	
	/**
	 * Gets the path to the TAU arch directory, as stored in the eclipse workspace
	 * @return the path to the TAU arch directory
	 */
	protected static String getTauArchPath(){
		File binPath=new File(pstore.getString(ITAULaunchConfigurationConstants.TAU_BIN_PATH));
		if(binPath.canRead())
			return binPath.getParent().toString();
		
		return "";
		//return pstore.getString(ITAULaunchConfigurationConstants.TAU_ARCH_PATH);
	}
	
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
	
	public static String getConfigurationName(String basename, ILaunchConfiguration configuration) throws CoreException
	{
		String tauMakeName = getTauMakefile(configuration);
		String taunameappend=("_"+tauMakeName.substring(tauMakeName.lastIndexOf(".")+1));
		
		if(basename.indexOf(taunameappend)<0)
			basename += ("_"+tauMakeName.substring(tauMakeName.lastIndexOf(".")+1));
		
		return basename;
	}
	
	
	public static void toolClean(String projname, ILaunchConfiguration configuration, String projectLocation) throws CoreException
	{
		//System.out.println("HEY!"+projectLocation+" vs "+thisCProject.getResource().getLocation().toOSString());
		String directory=projectLocation;
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
		String tbpath = getTauArchPath()+File.separator+"bin";
		String now = BuildLaunchUtils.getNow();

		//TODO:  Test this and repace the configuration with makefile check
		if(projtype.indexOf("-perf")>0)
			managePerfFiles(directory,tbpath);
		
		//Put the profile data in the database and delete any profile files
		if(haveprofiles)
		{
			boolean keepprofs = configuration.getAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, false);
			boolean useportal=configuration.getAttribute(ITAULaunchConfigurationConstants.PORTAL, false);
			manageProfiles(directory, projname, projtype, tbpath, configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB,(String)null), now, keepprofs,useportal);
			
		}
		
		//TODO: Enable tracefile management
		//if(tracout||configuration.getAttribute(ITAULaunchConfigurationConstants.TRACE, false))
		//manageTraceFiles(directory, projtype,now);
	}

//	/**
//	 * Collects generated profile files and either stores them in a ppk file or, if possible, in a local database.  Optionally uploads them to TAU's 
//	 * web portal system.  Optionally deletes local files after successfully inserted into the database.
//	 * @param directory The directory containing the profile files
//	 * @param projname The name of the project
//	 * @param projtype The options used on this project
//	 * @param tbpath The path to the TAU bin directory
//	 * @param now The current time
//	 * @throws CoreException
//	 */
//	private static void manageProfiles(final File ppkFile, final String directory, final String projname, final String projtype, final String now, final boolean keepprofs, final boolean useportal) throws CoreException
//	{
//
//		class Profilefilter implements FilenameFilter {
//			public boolean accept(File dir, String name) {
//				if (name.indexOf("profile.") != 0)
//					return false;
//				return true;
//			}
//		}
//
//		class Counterfilter implements FileFilter {
//
//			public boolean accept(File pathname) {
//				if (pathname.isDirectory()) {
//					if (pathname.getName().indexOf("MULTI__") == 0)
//						return true;
//				}
//				return false;
//			}
//
//		}
//
//		File[] profiles = null;
//		File dir = new File(directory);
//		Profilefilter profil = new Profilefilter();
//		profiles = dir.listFiles(profil);
//
//		final boolean multipapi = (projtype.indexOf("multiplecounters") >= 0 && projtype
//				.indexOf("papi") >= 0);// configuration.getAttribute(ITAULaunchConfigurationConstants.PAPI,
//		// false);
//		File[] counterdirs = null;
//		Counterfilter countfil = null;
//		if (multipapi) {
//			countfil = new Counterfilter();
//			counterdirs = dir.listFiles(countfil);
//		}
//
//		File[] rem = null;
//		if (multipapi)
//			rem = counterdirs;
//		else
//			rem = profiles;
//		final File[] remprofs = rem;
//		
//		//final File ppkFile=makePPK(projname, projtype, now, tbpath, directory);
//		
////		final boolean keepprofs = configuration.getAttribute(
////				ITAULaunchConfigurationConstants.KEEPPROFS, false);
////		final boolean useportal = configuration.getAttribute(
////				ITAULaunchConfigurationConstants.PORTAL, false);
//		if (rem.length > 0)
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
//
//
//					boolean hasdb = false;
//
//					try {
//						hasdb = PerfDMFUIPlugin.addPerformanceData(projname,
//								projtype, directory);
//						if (!hasdb) {
//							MessageDialog
//									.openInformation(
//											PlatformUI.getWorkbench()
//													.getDisplay()
//													.getActiveShell(),
//											"TAU Warning",
//											"Adding data to your perfdmf database failed.  Please make sure that you have successfully run perfdmf_configure with your selected TAU installation.");
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//
//					
//
//					for (int i = 0; i < remprofs.length; i++) {
//						if (multipapi) {
//							File[] profs = remprofs[i].listFiles();
//							for (int j = 0; j < profs.length; j++)
//								profs[j].delete();
//						}
//						remprofs[i].delete();
//					}
//
//					if (!keepprofs && hasdb) {
//						ppkFile.delete();
//					} else {
//						File profdir = new File(directory + File.separator
//								+ "Profiles" + File.separator + projtype
//								+ File.separator + now);
//						profdir.mkdirs();
//
//						ppkFile.renameTo(new File(profdir + File.separator
//								+ ppkFile.getName()));
//					}
//				}
//			});
//
//		if (rem.length <= 0) {
//			System.out
//					.println("No profile data generated!  Check for build and runtime errors!");
//		}
//	}
	
	
//	private static File makePPK(String projname, String projtype, String now, String tbpath, String directory)
//	{
//		String ppkname = projname + "_" + projtype + "_" + now+ ".ppk";
//		String paraprof = tbpath + File.separator+ "paraprof --pack " + ppkname;
//		System.out.println(paraprof);
//
//		String ppk = directory + File.separator + ppkname;
//
//		BuildLaunchUtils.runTool(paraprof, null,new File(directory));
//
//		File ppkFile = new File(ppk);
//		return ppkFile;
//	}
	
	/**
	 * Collects generated profile files and either stores them in a ppk file or, if possible, in a local database.  Optionally uploads them to TAU's 
	 * web portal system.  Optionally deletes local files after successfully inserted into the database.
	 * @param directory The directory containing the profile files
	 * @param projname The name of the project
	 * @param projtype The options used on this project
	 * @param tbpath The path to the TAU bin directory
	 * @param database The name of the database recieving the profiles
	 * @param now The current time
	 * @throws CoreException
	 */
	private static void manageProfiles(final String directory, final String projname, final String projtype, final String tbpath, final String database, final String now, final boolean keepprofs, final boolean useportal) throws CoreException
	{

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

		final boolean multipapi = (projtype.indexOf("multiplecounters") >= 0 && projtype
				.indexOf("papi") >= 0);// configuration.getAttribute(ITAULaunchConfigurationConstants.PAPI,
		// false);
		File[] counterdirs = null;
		Counterfilter countfil = null;
		if (multipapi) {
			countfil = new Counterfilter();
			counterdirs = dir.listFiles(countfil);
		}

		File[] rem = null;
		if (multipapi)
			rem = counterdirs;
		else
			rem = profiles;
		final File[] remprofs = rem;

		if (rem.length > 0)
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					String ppkname = projname + "_" + projtype + "_" + now
							+ ".ppk";
					String paraprof = tbpath + File.separator
							+ "paraprof --pack " + ppkname;
					System.out.println(paraprof);

					String ppk = directory + File.separator + ppkname;

					BuildLaunchUtils.runTool(paraprof, null,
							new File(directory));

					File ppkFile = new File(ppk);

					boolean hasdb = false;

					try {
						if(database!=null&&!database.equals(ITAULaunchConfigurationConstants.NODB))
							hasdb = PerfDMFUIPlugin.addPerformanceData(projname,projtype, directory, database);
						else
							hasdb=false;
						if (!hasdb) {
							MessageDialog
									.openInformation(
											PlatformUI.getWorkbench()
													.getDisplay()
													.getActiveShell(),
											"TAU Warning",
											"Adding data to your perfdmf database failed.  Please make sure that you have successfully run perfdmf_configure with your selected TAU installation.");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (useportal) {
						hasdb = true;
						try {
							TAUPortalUploadDialog pwDialog = new TAUPortalUploadDialog(
									PlatformUI.getWorkbench().getDisplay()
											.getActiveShell(), ppkFile);
							if (pwDialog.open() != TAUPortalUploadDialog.OK
									&& pwDialog.open() != TAUPortalUploadDialog.CANCEL) {
								MessageDialog
										.openInformation(
												PlatformUI.getWorkbench()
														.getDisplay()
														.getActiveShell(),
												"TAU Warning",
												"Adding data to your online database failed.  Please make sure that the given URL, username and password are correct.");
								hasdb = false;
							}
						} catch (Exception e) {
							MessageDialog
									.openInformation(
											PlatformUI.getWorkbench()
													.getDisplay()
													.getActiveShell(),
											"TAU Warning",
											"Adding data to your online database failed.  Please make sure that the given URL, username and password are correct.");
							hasdb = false;
						}
					}

					for (int i = 0; i < remprofs.length; i++) {
						if (multipapi) {
							File[] profs = remprofs[i].listFiles();
							for (int j = 0; j < profs.length; j++)
								profs[j].delete();
						}
						remprofs[i].delete();
					}

					if (!keepprofs && hasdb) {
						ppkFile.delete();
					} else {
						File profdir = new File(directory + File.separator
								+ "Profiles" + File.separator + projtype
								+ File.separator + now);
						profdir.mkdirs();

						ppkFile.renameTo(new File(profdir + File.separator
								+ ppkFile.getName()));
					}
				}
			});

		if (rem.length <= 0) {
			System.out
					.println("No profile data generated!  Check for build and runtime errors!");
		}
	}
	
//	private static boolean uploadToPortal(File ppkFile)
//	{
//			try {
//				TAUPortalUploadDialog pwDialog = new TAUPortalUploadDialog(
//						PlatformUI.getWorkbench().getDisplay()
//								.getActiveShell(), ppkFile);
//				if (pwDialog.open() != TAUPortalUploadDialog.OK
//						&& pwDialog.open() != TAUPortalUploadDialog.CANCEL) {
//					MessageDialog
//							.openInformation(
//									PlatformUI.getWorkbench()
//											.getDisplay()
//											.getActiveShell(),
//									"TAU Warning",
//									"Adding data to your online database failed.  Please make sure that the given URL, username and password are correct.");
//					return false;
//				}
//			} catch (Exception e) {
//				MessageDialog
//						.openInformation(
//								PlatformUI.getWorkbench()
//										.getDisplay()
//										.getActiveShell(),
//								"TAU Warning",
//								"Adding data to your online database failed.  Please make sure that the given URL, username and password are correct.");
//				return false;
//			}
//			return true;
//	}
	
	
	
	/**
	 * Handle files produced by 'perflib' instrumentation by converting them to the TAU format and moving them to an
	 * appropriate directory
	 * @param directory The path to the directory containing the performance data
	 * @param tbpath The path to the TAU bin directory
	 * @param monitor
	 * @throws CoreException
	 */
	private static void managePerfFiles(String directory, String tbpath) throws CoreException
	{
		class perffilter implements FilenameFilter{
			public boolean accept(File dir, String name) {
				if(name.indexOf("perf_data.")!=0)
					return false;
				return true;
			}
		}
		
		perffilter seekdir = new perffilter();
		File dir = new File(directory);
		File[] perfdir = dir.listFiles(seekdir);

		if(perfdir==null||perfdir.length<1)
		{
			return;
		}

		String perf2tau = tbpath+File.separator+"perf2tau";
		
		BuildLaunchUtils.runTool(perf2tau,null,dir);
		
	}
	
//	//TODO:  Test and enable trace management
//	/**
//	 * Collect and move trace files to an appropriate directory
//	 */
//	private static void manageTraceFiles(String directory, String projtype, String now){
//		class tracefilter implements FilenameFilter{
//			public boolean accept(File dir, String name) {
//				if(name.indexOf(".trc")>0||name.indexOf(".edf")>0)
//					return true;
//				return false;
//			}
//		}
//		tracefilter tracefind = new tracefilter();
//		File dir = new File(directory);
//		File[] mvtrc=dir.listFiles(tracefind);
//		
//		File trcdir = new File(directory+File.separator+"Traces"+File.separator+projtype+File.separator+now);
//		trcdir.mkdirs();
//		for(int i=0;i<mvtrc.length;i++)
//		{
//			mvtrc[i].renameTo(new File(trcdir+File.separator+mvtrc[i].getName()));
//		}
//	}
	
}