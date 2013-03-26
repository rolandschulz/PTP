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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.etfw.AbstractToolDataManager;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.internal.BuildLaunchUtils;
import org.eclipse.ptp.etfw.internal.PostlaunchTool;
import org.eclipse.ptp.etfw.internal.RemoteBuildLaunchUtils;
import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.ptp.etfw.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.ptp.etfw.tau.perfdmf.views.PerfDMFView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;

public class TAUPerformanceDataManager extends AbstractToolDataManager {
	// private static final IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();

	private static String tbpath = null;

	/**
	 * Returns the TAU makefile associated with the supplied launch configuration
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private static String getTauMakefile(ILaunchConfiguration configuration) throws CoreException {
		String etfwVersion = configuration.getAttribute(IToolLaunchConfigurationConstants.ETFW_VERSION,
				IToolLaunchConfigurationConstants.EMPTY);
		String attributeKey = ITAULaunchConfigurationConstants.TAU_MAKEFILE;
		if (!etfwVersion.equals(IToolLaunchConfigurationConstants.USE_SAX_PARSER)) {
			String controlId = configuration.getAttribute("org.eclipse.ptp.launch.RESOURCE_MANAGER_NAME", //$NON-NLS-1$
					IToolLaunchConfigurationConstants.EMPTY);
			attributeKey = controlId + IToolLaunchConfigurationConstants.DOT + attributeKey;
		}
		return configuration.getAttribute(attributeKey, (String) null);
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	boolean useExt = false;

	@Override
	public void setExternalTarget(boolean useExt) {
		this.useExt = useExt;
	}

	private IBuildLaunchUtils utilBlob = null;

	private IFileStore ppkFile = null;
	private IFileStore xmlFile = null;
	private List<IFileStore> profiles = null;

	// private void userSelectData(ILaunchConfiguration configuration, String directory) throws CoreException{
	//
	// String[] profIDs = requestProfIDs();
	//
	// if(profIDs==null){
	// return;
	// }
	//
	// //profs = getProfiles(directory);
	// if(profiles==null||profiles.size()==0)
	// {
	// TAUPerformanceDataManager.printNoProfsError();
	//
	// return;
	// }
	//
	// String projname=profIDs[0];
	// String projtype=profIDs[1];
	// String projtrial=profIDs[2];
	//
	// String xmlMetaData=configuration.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_XML_METADATA, (String)null);
	// String database=
	// PerfDMFView.extractDatabaseName(configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB,(String)null));
	// boolean hasdb=addToDatabase(utilBlob.getFile(directory),database,projname,projtype,projtrial,xmlMetaData);
	// if (!hasdb) {
	//
	// Display.getDefault().syncExec(new Runnable() {
	// public void run() {
	// MessageDialog
	// .openInformation(
	// PlatformUI.getWorkbench()
	// .getDisplay()
	// .getActiveShell(),
	// Messages.TAUPerformanceDataManager_TAUWarning,
	// Messages.TAUPerformanceDataManager_AddingDataPerfDBFailed);
	// }});
	//
	//
	// return;
	// }
	//
	// if(usePortal){
	// IFileStore ppkfile=getPPKFile(directory,projname,projtype,projtrial);
	// if(ppkfile==null||!ppkfile.fetchInfo().exists())
	// {
	// Display.getDefault().syncExec(new Runnable() {
	// public void run() {
	// MessageDialog
	// .openInformation(
	// PlatformUI.getWorkbench()
	// .getDisplay()
	// .getActiveShell(),
	// Messages.TAUPerformanceDataManager_TAUWarning,
	// Messages.TAUPerformanceDataManager_CouldNotGenPPK);
	// }});
	//
	//
	// }
	// else{
	//
	// runPortal(ppkfile);
	//
	//
	// }
	// ppkfile.delete(EFS.NONE,null);
	// }
	// }

	private static boolean madeProfiles(ILaunchConfiguration configuration, String projtype) throws CoreException {
		// TODO: replace config check with makefile check
		boolean tracout = (configuration.getAttribute(ITAULaunchConfigurationConstants.EPILOG, false)
				|| configuration.getAttribute(ITAULaunchConfigurationConstants.VAMPIRTRACE, false)
				|| configuration.getAttribute(ITAULaunchConfigurationConstants.TRACE, false)
				|| configuration.getAttribute(ITAULaunchConfigurationConstants.PERF, false) || projtype.indexOf("-trace") >= 0); //$NON-NLS-1$
		boolean profout = (configuration.getAttribute(ITAULaunchConfigurationConstants.CALLPATH, false)
				|| configuration.getAttribute(ITAULaunchConfigurationConstants.PHASE, false)
				|| configuration.getAttribute(ITAULaunchConfigurationConstants.MEMORY, false) || projtype.indexOf("-profile") >= 0 || projtype.indexOf("-headroom") >= 0); //$NON-NLS-1$ //$NON-NLS-2$

		// if we have trace output but no profile output it means we don't need to process profiles at all...
		return (!tracout || (profout));
	}

	private String[] findProfIDs(ILaunchConfiguration configuration) throws CoreException {

		/* Contains all tau configuration options in the makefile name, except pdt */
		String makename = getTauMakefile(configuration);
		int tauDex = makename.lastIndexOf("tau-"); //$NON-NLS-1$
		String projtype = null;
		if (tauDex < 0) {
			projtype = "tau"; //$NON-NLS-1$
		} else {
			projtype = makename.substring(tauDex + 4);
		}
		String projtrial = BuildLaunchUtils.getNow();
		String expAppend = configuration.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_EXPERIMENT_APPEND, (String) null);
		if (expAppend != null && expAppend.length() > 0) {
			projtype += "_" + expAppend; //$NON-NLS-1$
		}
		String[] IDs = { projtype, projtrial };
		return IDs;
	}

	/**
	 * Returns true if the directory exists and contains a tau profile or tau xml file.
	 * 
	 * @param directory
	 * @param utils
	 * @return
	 */
	private boolean checkDirectory(String directory, IBuildLaunchUtils utils) {
		IFileStore d = utils.getFile(directory);
		boolean check = d.fetchInfo().exists();
		if (!check)
			return false;

		try {
			String[] children = d.childNames(EFS.NONE, null);
			for (int i = 0; i < children.length; i++)
			{
				if (children[i].contains(PROFXML) || children[i].contains("profile.0.0.0"))
					return true;
			}

		} catch (CoreException e) {

			e.printStackTrace();
			return false;
		}

		return false;
	}

	@Override
	public void process(String projname, ILaunchConfiguration configuration, String directory) throws CoreException {
		// String projectDirectory = directory;
		boolean profsummary = configuration.getAttribute(ITAULaunchConfigurationConstants.PROFSUMMARY, false);
		IBuildLaunchUtils tmpub = new BuildLaunchUtils();
		String pppath = tmpub.checkToolEnvPath("paraprof");
		boolean hasLocalParaprof = true;
		if (pppath != null && tmpub.getFile(pppath).fetchInfo().exists()) {

		} else {// If we can't find a local copy of paraprof we *have* to show a pprof summary of the data instead.
			profsummary = true;
			hasLocalParaprof = false;
		}

		/*
		 * Determining if the job was local or remote at this point is tricky. It's safest to see if profiles are available locally
		 * and if not use the remote location.
		 */
		boolean dirgood = checkDirectory(directory, tmpub);

		if (!dirgood && LaunchUtils.getRemoteServicesId(configuration) != null) {
			utilBlob = new RemoteBuildLaunchUtils(configuration);
		} else {
			utilBlob = tmpub;
		}

		String tmpDir = null;
		
		/*
		* Now check the entered directory on the remote connection, if any.
		*/
		if(!dirgood)
		{
			dirgood=checkDirectory(directory,utilBlob);
		}	
		
		if (!dirgood) {
			tmpDir = utilBlob.getWorkingDirectory();
			if (tmpDir != null) {
				directory = tmpDir;
			}
		}

		if(!dirgood){
			dirgood=checkDirectory(directory,utilBlob);
		}
		
		if (!dirgood) {
			tmpDir = configuration.getAttribute(IToolLaunchConfigurationConstants.PROJECT_DIR, "");
			if (tmpDir != null)
				directory = tmpDir;
		}

		tbpath = utilBlob.getToolPath(Messages.TAUPerformanceDataManager_0);
		if(tbpath==null||tbpath.length()==0){
			tbpath=utilBlob.findToolBinPath("paraprof",null,"paraprof");
		}

		profiles = getProfiles(directory);
		xmlFile = utilBlob.getFile(directory).getChild(PROFXML);
		if (!xmlFile.fetchInfo().exists()) {
			xmlFile = null;
		}
			
		if(profiles==null&&xmlFile==null){
			printNoProfsError();
			return;
		}
		
		String projtype = null;
		String projtrial = null;

		if (useExt || projname == null) {
			String[] profIDs = requestProfIDs();

			if (profIDs == null) {
				return;
			}

			projname = profIDs[0];
			projtype = profIDs[1];
			projtrial = profIDs[2];
		} else {
			String[] IDs = this.findProfIDs(configuration);
			projtype = IDs[0];
			projtrial = IDs[1];
		}

		boolean runtauinc = configuration.getAttribute(ITAULaunchConfigurationConstants.TAUINC, false);
		boolean usePortal = configuration.getAttribute(ITAULaunchConfigurationConstants.PORTAL, false);
		if (xmlFile == null && profiles != null && profiles.size() > 0 || usePortal) {
			ppkFile = getPPKFile(directory, projname, projtype, projtrial);
			if(!ppkFile.fetchInfo().exists())
			{
				ppkFile=null;			
			}
		}

		// If we are doing a profile summary but have no 'exposed' profiles we must expose them.
		if ((runtauinc || profsummary) && xmlFile != null && profiles != null && profiles.size() < 1) {
			List<String> xcon = new ArrayList<String>();
			xcon.add("paraprof");
			xcon.add("--dump");
			xcon.add(xmlFile.toURI().getPath());
			utilBlob.runToolGetOutput(xcon, null, directory);
			profiles = getProfiles(directory);
		}

		// Either the user is assuming profiles are available, or we are using a configuration that generated them.
		boolean haveprofiles = useExt || madeProfiles(configuration, projtype);

		// TODO: Test this and repace the configuration with makefile check
		if (!useExt && projtype.indexOf("-perf") > 0) //$NON-NLS-1$
		{
			managePerfFiles(directory);
		}

		if (!haveprofiles) {

			return;
		}

		if ((profiles == null || profiles.size() < 1) && xmlFile == null && ppkFile == null) {
			printNoProfsError();
			return;
		}

		// Put the profile data in the database and delete any profile files
		// Also generate MPI include list if specified (@author: raportil)
		boolean keepprofs = configuration.getAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, false);
		boolean useParametric = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_USE_PARAMETRIC, false);
		// IFileStore xmlprof=null;
		// TODO: Return support for regular profiles/ppk files
		// profs = getProfiles(directory);
		// if(!useP&&profs==null||profs.size()==0)
		// {
		// new File(directory+File.separatorChar+PROFXML);

		if (profsummary||(profiles!=null&&profiles.size()>0&&xmlFile==null&&ppkFile==null)) {
			displayProfileSummary(directory);
		}

		if (runtauinc) {
			// TODO: This will not work if the profiles are not in the working directory (eg papi) or they are not in expanded
			// profile form.
			runTAUInc(directory, directory, projname, projtype, projtrial);
		}
		boolean hasdb = false;
		String database = null;
		if (hasLocalParaprof) {
			String xmlMetaData = configuration.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_XML_METADATA, (String) null);
			database = PerfDMFView.extractDatabaseName(configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB,
					(String) null));

			if (xmlFile != null) {
				hasdb = addToDatabase(xmlFile, database, projname, projtype, projtrial, xmlMetaData);
			} else if (ppkFile != null) {
				hasdb = addToDatabase(ppkFile, database, projname, projtype, projtrial, xmlMetaData);
			}
			if (!hasdb && !useParametric) {

				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
								Messages.TAUPerformanceDataManager_TAUWarning,
								Messages.TAUPerformanceDataManager_AddingDataPerfDBFailed);
					}
				});

			}

		}

		// boolean useportal=configuration.getAttribute(ITAULaunchConfigurationConstants.PORTAL, false);//TODO: Enable portal use
		// via external data interface

		// if(keepprofs||!hasdb||usePortal){
		// IFileStore ppkfile=getPPKFile(directory,projname,projtype,projtrial);
		// if(ppkfile==null||!ppkfile.fetchInfo().exists())
		// {
		//
		// Display.getDefault().syncExec(new Runnable() {
		// public void run() {
		// MessageDialog
		// .openInformation(
		// PlatformUI.getWorkbench()
		// .getDisplay()
		// .getActiveShell(),
		// Messages.TAUPerformanceDataManager_TAUWarning,
		// Messages.TAUPerformanceDataManager_CouldNotGeneratePPK);
		// }});
		//
		//
		// }
		// else{
		if (usePortal && ppkFile != null && ppkFile.fetchInfo().exists()) {
			runPortal(ppkFile);
		}

		if (keepprofs || !hasdb || profsummary) {
			if (ppkFile != null) {
				movePakFile(directory, projtype, projtrial, ppkFile);
			}
			if (xmlFile != null) {
				movePakFile(directory, projtype, projtrial, xmlFile);
			}
		} else {
			if (ppkFile != null) {
				ppkFile.delete(EFS.NONE, null);
			}
			if (xmlFile != null) {
				xmlFile.delete(EFS.NONE, null);
			}
		}
		// }
		if(xmlFile!=null||ppkFile!=null)
		{
			removeProfiles(profiles);// TODO: xml profiles don't make a mess, so save?
		}

		// TODO: This needs to be tested remotely.
		if (hasLocalParaprof && database != null) {
			boolean useperfex = configuration.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_LAUNCH_PERFEX, false);
			if (useperfex) {
				String perfexScript = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_PERF_SCRIPT, (String) null);

				if (perfexScript != null && perfexScript.length() > 0) {
					runPerfEx(directory, database, projname, projtype, perfexScript);
				}
			}
		}

		// TODO: Enable tracefile management
		// if(tracout||configuration.getAttribute(ITAULaunchConfigurationConstants.TRACE, false))
		// manageTraceFiles(directory, projtype,now);

	}

	private static void printNoProfsError() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						Messages.TAUPerformanceDataManager_TAUWarning, Messages.TAUPerformanceDataManager_NoProfData);
			}
		});
	}

	private void displayProfileSummary(String directory) {
		// if(profs==null){
		// profs = getProfiles(directory);
		// }

		if (profiles == null || profiles.size() < 1) {
			printNoProfsError();
			return;
		}
		List<String> ppl = new ArrayList<String>();
		ppl.add("pprof");
		ppl.add("-s");
		// IFileStore ppk = getPPKFile(directory, projname, projtype, projtrial);
		byte[] ppout = null;

		MessageConsole mc = PostlaunchTool.findConsole(PARAPROFCONSOLE);
		mc.clearConsole();
		OutputStream os = mc.newOutputStream();

		if (!profiles.get(0).fetchInfo().isDirectory()) {
			ppout = utilBlob.runToolGetOutput(ppl, null, directory);

			try {
				os.write(ppout);
				// os.write(("Moving profile data to: "+ppkFile.toString()).getBytes());
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			try {

				for (int i = 0; i < profiles.size(); i++) {
					os.write(profiles.get(i).getName().getBytes());
					ppout = utilBlob.runToolGetOutput(ppl, null, profiles.get(i).toURI().getPath());
					os.write(ppout);
				}

				// os.write(("Moving profile data to: "+ppk.toString()).getBytes());
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		mc.activate();

		// IFileStore projDir = utilBlob.getFile(projectDirectory);
		// if(projDir.fetchInfo().exists()&&projDir.fetchInfo().isDirectory()){
		// projDir=projDir.getChild(ppk.getName());
		// ppk.move(projDir, EFS.NONE, null);
		// }
		//
		// removeProfiles(profs);

		// if(!ppk.fetchInfo().exists()||!ppk.fetchInfo().getAttribute(EFS.ATTRIBUTE_OWNER_READ)||!ppk.fetchInfo().getAttribute(EFS.ATTRIBUTE_OWNER_WRITE)){
		// return;
		// }

		// IFileStore wdir=utilBlob.getFile(utilBlob.getWorkingDirectory());
		// String tname = ppk.getName();
		// wdir = wdir.getChild(tname);
		// if(!wdir.fetchInfo().exists()||!wdir.fetchInfo().isDirectory()||!wdir.fetchInfo().getAttribute(EFS.ATTRIBUTE_OWNER_READ)||!wdir.fetchInfo().getAttribute(EFS.ATTRIBUTE_OWNER_WRITE)){
		// return;
		// }
		// ppk.move(wdir, EFS.OVERWRITE, null);

		// IConsole myConsole = mc;//...;// your console instance
		// IWorkbench wb = PlatformUI.getWorkbench();
		// IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		// IWorkbenchPage page = win.getActivePage();
		// String id = IConsoleConstants.ID_CONSOLE_VIEW;
		// IConsoleView view = (IConsoleView) page.showView(id);
		// view.display(myConsole);

		return;
	}

	private static final String PARAPROFCONSOLE = "TAU Profile Output";

	@Override
	public void view() {
		// TODO Auto-generated method stub

	}

	/**
	 * Asks the user for the tags to use on the profile data when uploading to a database.
	 * 
	 * @return Array containing three Strings; User provided names for the application, experiment and trial.
	 */
	private static String[] requestProfIDs() {

		final String queries[] = new String[3];
		queries[0] = Messages.TAUPerformanceDataManager_AppName;
		queries[1] = Messages.TAUPerformanceDataManager_ExpName;
		queries[2] = Messages.TAUPerformanceDataManager_TrialName;

		final String values[] = new String[3];

		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
				MultiFieldDialog mfd = new MultiFieldDialog(shell, queries);
				mfd.open();
				String[] outvals = mfd.getValues();
				for (int i = 0; i < values.length; i++) {
					values[i] = outvals[i];
				}

			}

		});
		return values;

	}

	private static final String PROFDOT = "profile.";
	private static final String PROFXML = "tauprofile.xml";
	private static final String MULTI = "MULTI__";

	/**
	 * Checks the directory for MULTI directories or profile. files. Returns a list containing all of either type found, but not
	 * both. If a directory contains both only profile. files will be returned.
	 * 
	 * @param directory
	 *            The main/output directory
	 * @return List of file objects representing profiles or MULTI directories containing profiles
	 */
	private List<IFileStore> getProfiles(String directory) {
		IFileStore[] profiles = null;
		IFileStore dir = utilBlob.getFile(directory);
		try {
			profiles = dir.childStores(EFS.NONE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		ArrayList<IFileStore> profs = new ArrayList<IFileStore>();
		ArrayList<IFileStore> dirs = new ArrayList<IFileStore>();
		for (int i = 0; i < profiles.length; i++) {

			IFileInfo proinfo = profiles[i].fetchInfo();
			if (!proinfo.isDirectory() && proinfo.getName().startsWith(PROFDOT)) {
				profs.add(profiles[i]);
			} else if (proinfo.isDirectory() && proinfo.getName().startsWith(MULTI)) {
				dirs.add(profiles[i]);
			}
		}

		if (profs.size() > 0) {
			return profs;
		}

		if (dirs.size() > 0) {
			return dirs;
		}

		return null;
	}

	private static final String UNIX_SLASH = "/";

	private IFileStore getPPKFile(final String directory, final String projname, final String projtype, final String projtrial) {

		class ppPaker implements Runnable {
			public String ppk;

			public void run() {

				List<String> paraCommand = new ArrayList<String>();

				String ppkname = projname + "_" + projtype + "_" + projtrial + ".ppk"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String paraprof = ""; //$NON-NLS-1$

				if (tbpath != null && tbpath.length() > 0) {
					utilBlob.getFile(tbpath);
					paraprof += tbpath + UNIX_SLASH;
				}
				paraprof += "paraprof"; //$NON-NLS-1$
				String pack = "--pack ";// + ppkname; //$NON-NLS-1$
				System.out.println(paraprof + " " + pack); //$NON-NLS-1$
				ppk = directory + UNIX_SLASH + ppkname;

				paraCommand.add(paraprof);
				paraCommand.add(pack);
				paraCommand.add(ppkname);

				utilBlob.runTool(paraCommand, null, directory);
			}
		}

		ppPaker ppp = new ppPaker();
		Display.getDefault().syncExec(ppp);

		return utilBlob.getFile(ppp.ppk);
	}

	private static boolean addToDatabase(final IFileStore profileFile, final String database, final String projname,
			final String projtype, final String projtrial, String xmlMetaData) {

		boolean hasdb = false;
		try {
			// if(usingParameters)
			// {

			if (database != null && !database.equals(ITAULaunchConfigurationConstants.NODB)) {
				/*
				 * TODO: Re-enable this or find another way to support metadata uploading! String metaSpec=" "; File xmlFi=null;
				 * if(xmlMetaData!=null&&xmlMetaData.length()>0){ String xmlLoc=directory+File.separator+"perfMetadata.xml";
				 * xmlFi=new File(xmlLoc); FileOutputStream o; PrintStream p; try { o=new FileOutputStream(xmlFi); p=new
				 * PrintStream(o);
				 * 
				 * p.println(xmlMetaData); p.close(); if(xmlFi.exists()&&xmlFi.canRead()){ metaSpec=" -m "+xmlLoc+" "; } } catch
				 * (FileNotFoundException e) { // TODO Auto-generated catch block e.printStackTrace(); } }
				 * 
				 * String db=""; if(!database.equals("Default")){ db=" -c "+database; }
				 * 
				 * String ex = ""; if(tbpath!=null && tbpath.length()>0) ex+=tbpath+File.separator;
				 * ex+="perfdmf_loadtrial -a "+projname+" -x "+projtype+" -n "+projtrial+db+metaSpec+ directory;//-m metaDataFile
				 * hasdb=BuildLaunchUtils.runTool(ex, null,new File(directory));
				 * 
				 * 
				 * if(hasdb==false) { return false; }
				 * 
				 * //hasdb=true; if(xmlFi!=null){ xmlFi.delete(); }
				 * 
				 * 
				 * Display.getDefault().syncExec(new Runnable() { public void run() { //} //else{
				 * //if(database!=null&&!database.equals(ITAULaunchConfigurationConstants.NODB)) //{
				 * PerfDMFUIPlugin.displayPerformanceData(projname,projtype,projtrial);//,directory, database //} }});
				 */
				final IFileStore local;
				// if(xmlFile!=null)
				// IFileStore profileFile =xmlFile; //directory.getChild("tauprofile.xml");
				if (!profileFile.fetchInfo().exists()) {
					return false;
				}
				File tmpprof = null;
				if (!profileFile.getFileSystem().equals(EFS.getLocalFileSystem())) {
					File f = profileFile.toLocalFile(EFS.CACHE, null);
					if (!f.exists()) {
						return false;
					}
					tmpprof = new File(System.getProperty("java.io.tmpdir") + File.separator + profileFile.getName());
					copyFile(f, tmpprof);
					// f.renameTo(new File(f.getParent()+File.separator+profileFile.getName()));
					String s = tmpprof.getCanonicalPath();
					URI suri = URI.create(s);
					local = EFS.getLocalFileSystem().getStore(suri);
					// tmp.move(tmp.getParent().getChild(profileFile.getName()), EFS.OVERWRITE, null);
					// local =tmp;

				} else {
					local = profileFile;
				}
				class DBView implements Runnable {
					boolean hasdb = false;

					public void run() {
						hasdb = PerfDMFUIPlugin.addPerformanceData(projname, projtype, projtrial, local, database);

					}

				}
				DBView dbv = new DBView();
				Display.getDefault().syncExec(dbv);
				hasdb = dbv.hasdb;
				if (tmpprof != null) {
					tmpprof.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			hasdb = false;
		}
		return hasdb;
	}

	// TODO: Fine a better way to change the filenames of these tmp files so I can get rid of this!
	private static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	/**
	 * Moves the ppk file to an approriately named subdirectory of a Profiles directory in the top-level output directory
	 * 
	 * @param directory
	 *            The top level output directory, where the Profiles directory is created
	 * @param projtype
	 *            Created a subdirectory of Profiles
	 * @param projtrial
	 *            Created as a subdirectory of projtype
	 * @param profileFile
	 *            This file is moved to the projtrial directory
	 */
	private void movePakFile(String destDirectory, String projtype, String projtrial, IFileStore profileFile) {
		IFileStore profdir = utilBlob.getFile(destDirectory).getChild("Profiles").getChild(projtype).getChild(projtrial);

		try {
			profdir.mkdir(EFS.OVERWRITE, null);
			IFileStore dest = profdir.getChild(profileFile.getName());
			profileFile.move(dest, EFS.OVERWRITE, null);
			MessageConsole mc = PostlaunchTool.findConsole(PARAPROFCONSOLE);
			OutputStream os = mc.newOutputStream();
			os.write(("Moving profile data to: " + profileFile.toString()).getBytes());
			os.close();
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Deletes either specified profiles or MULTI profile directories and the profiles they contain
	 * 
	 * @param remprofs
	 *            List of either profile files or MULTI profile directories
	 */
	private static void removeProfiles(List<IFileStore> remprofs) {

		if (remprofs == null || remprofs.size() == 0) {
			return;
		}

		boolean multipapi = remprofs.get(0).fetchInfo().isDirectory();
		try {
			for (int i = 0; i < remprofs.size(); i++) {
				if (multipapi) {
					IFileStore[] profs;

					profs = remprofs.get(i).childStores(EFS.NONE, null);

					for (int j = 0; j < profs.length; j++) {
						profs[j].delete(EFS.NONE, null);
					}
				}
				remprofs.get(i).delete(EFS.NONE, null);

			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void runTAUInc(final String projDirectory, final String runDirectory, final String projname, final String projtype,
			final String projtrial) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				/*
				 * Produce MPI include list if specified. FIXME: Running getRuntime directly instead of through runTool to pipe
				 * include list to a file
				 * 
				 * @author raportil
				 */

				String mpilistname = projname + "_" + projtype + "_" + projtrial + ".includelist"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String tauinc = "cd " + runDirectory + " ; "; //$NON-NLS-1$ //$NON-NLS-2$
				if (tbpath != null && tbpath.length() > 0) {
					tauinc += tbpath + UNIX_SLASH;
				}
				tauinc += "tauinc.pl > " + mpilistname + " ; "; //$NON-NLS-1$
				tauinc += "cp " + mpilistname + " projDirectory";
				System.out.println(tauinc);
				try {
					List<String> cmd = new ArrayList<String>();// {"sh", "-c", tauinc}; //$NON-NLS-1$ //$NON-NLS-2$
					cmd.add("sh");
					cmd.add("-c");
					cmd.add(tauinc);
					utilBlob.runTool(cmd, null, runDirectory);// TODO: Test This!
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void runPerfEx(String directory, String database, String projname, String projtype, String perfExScript) {

		List<String> script = new ArrayList<String>();
		script.add(perfExScript);
		script.add(" -c " + database); //$NON-NLS-1$
		script.add(" -p app=" + projname + ",exp=" + projtype); //$NON-NLS-1$ //$NON-NLS-2$

		// String script=perfExScript+" -c "+database+" -p app="+projname+",exp="+projtype;

		utilBlob.runTool(script, null, directory);
	}

	private static boolean runPortal(final IFileStore ppkFile) {
		boolean hasdb = true;

		Display.getDefault().syncExec(new Runnable() {
			public void run() {

				try {
					TAUPortalUploadDialog pwDialog = new TAUPortalUploadDialog(PlatformUI.getWorkbench().getDisplay()
							.getActiveShell(), ppkFile);
					int result = pwDialog.open();
					if (result != TAUPortalUploadDialog.OK && result != TAUPortalUploadDialog.CANCEL) {
						MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
								Messages.TAUPerformanceDataManager_TAUWarning,
								Messages.TAUPerformanceDataManager_AddingOnlineDBFailed);
						// hasdb = false;
					}
				} catch (Exception e) {
					MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
							Messages.TAUPerformanceDataManager_TAUWarning, Messages.TAUPerformanceDataManager_AddingOnlineDBFailed);
					// hasdb = false;
				}

			}
		});
		return hasdb;
	}

	/**
	 * Handle files produced by 'perflib' instrumentation by converting them to the TAU format and moving them to an appropriate
	 * directory
	 * 
	 * @param directory
	 *            The path to the directory containing the performance data
	 * @param tbpath
	 *            The path to the TAU bin directory
	 * @param monitor
	 * @throws CoreException
	 */
	private void managePerfFiles(String directory) throws CoreException {

		IFileStore dir = utilBlob.getFile(directory);
		IFileStore[] perfdir = dir.childStores(EFS.NONE, null);

		if (perfdir == null || perfdir.length < 1) {
			return;
		}

		int count = 0;
		for (int i = 0; i < perfdir.length; i++) {
			if (perfdir[i].fetchInfo().getName().startsWith("perf_data.")) {
				count++;
			}
		}

		if (count == 0) {
			return;
		}

		List<String> command = new ArrayList<String>();
		String perf2tau = ""; //$NON-NLS-1$
		if (tbpath != null && tbpath.length() > 0) {
			perf2tau += UNIX_SLASH;
		}
		perf2tau += "perf2tau"; //$NON-NLS-1$
		command.add(perf2tau);
		utilBlob.runTool(command, null, directory);

	}

	@Override
	public String getName() {
		return "process-TAU-data"; //$NON-NLS-1$
	}

}
