/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeTargetManager;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.internal.etfw.jaxb.data.BuildToolType;
import org.eclipse.ptp.internal.etfw.messages.Messages;

/**
 * This class is based on BuilderTool and handles workflow steps that rebuild tools with performance instrumentation.
 * 
 * @see BuilderTool
 * @author "Chris Navarro"
 * 
 */
public class ETFWBuildTool extends ETFWToolStep implements IToolLaunchConfigurationConstants {

	private static final String CComp = "CC";
	private static final String CxxComp = "CXX";
	private static final String FComp = "F90";
	private static final String UPCComp = "UPCC";
	private static final String EQ = ":=";

	private static int modifyCommand(ITool tool, String command, String args, boolean replace) {
		int didChange = 0;
		final String toolCommand = tool.getToolCommand();
		if (replace) {
			final String newcom = command + " " + args; //$NON-NLS-1$
			if (!newcom.equals(toolCommand)) {
				tool.setToolCommand(command + " " + args); //$NON-NLS-1$
				didChange = 1;
			}
		} else {
			String oldcom = toolCommand.trim();
			final int lastspc = oldcom.lastIndexOf(' ');
			if (lastspc >= 0) {
				oldcom = toolCommand.substring(lastspc).trim();
			}
			final String newcom = command + SPACE + args + SPACE + oldcom;
			if (!newcom.equals(toolCommand)) {
				tool.setToolCommand(newcom);
				didChange = 1;
			}
		}
		return didChange;
	}

	/**
	 * The location of the binary rebuilt with performance instrumentation
	 */
	private String progPath = null;

	/**
	 * False implies that no execution is to take place (either because of an
	 * error, or user request)
	 * */
	private boolean runbuilt = false;
	/** Executable (application) path attribute name */
	private IConfiguration newBuildConfig = null;
	private String buildConf = null;
	private IConfiguration olddefbuildconf = null;
	private IManagedBuildInfo buildInfo = null;
	private Map<String, String> buildMods = null;
	private String newname = null;
	private String binary = null;
	private BuildToolType tool = null;
	private IBuildLaunchUtils utilBlob = null;

	public IConfiguration selectedconf = null;

	private boolean isManaged;

	public ETFWBuildTool(ILaunchConfiguration conf, BuildToolType btool, IBuildLaunchUtils utilBlob) throws CoreException {
		super(conf, Messages.BuilderTool_InstrumentingBuilding, utilBlob);
		this.utilBlob = utilBlob;
		tool = btool;
		initBuild(conf);
	}

	public ETFWBuildTool(ILaunchConfiguration conf, BuildToolType btool, Map<String, String> buildMods, IBuildLaunchUtils utilBlob)
			throws CoreException {
		super(conf, Messages.BuilderTool_InstrumentingBuilding, utilBlob);
		this.buildMods = buildMods;
		this.utilBlob = utilBlob;
		tool = btool;

		initBuild(conf);
	}

	/**
	 * Builds the project with managed make if supported, otherwise with
	 * standard make
	 * 
	 * @param monitor
	 * @throws Exception
	 */
	public void buildIndstrumented(IProgressMonitor monitor) throws Exception {
		// if(tool==null)
		// throw new Exception("No valid tool configuration found");
		// runbuilt = true;
		if (tool != null) {
			if (!isManaged) {
				standardMakeBuild(monitor);
			} else {
				if (runbuilt) {
					runbuilt = initMMBuildConf();
					if (runbuilt) {
						runbuilt = managedMakeBuild(monitor);
					}
				}
			}
		}
	}

	public String getOutputLocation() {
		return outputLocation;
	}

	public String getProgramPath() {
		return progPath;
	}

	private String getStandardMakeBuildOps(BuildToolType tool, ILaunchConfiguration configuration, String allargs)
			throws CoreException {
		String ops = EMPTY;
		// String tmp;
		if (tool.getCcCompiler() != null) {
			ops += getStandardMakeOp(CComp, getToolCommand(tool.getCcCompiler(), configuration), allargs, tool.isReplaceCompiler());
		}
		if (tool.getCxxCompiler() != null) {
			ops += getStandardMakeOp(CxxComp, getToolCommand(tool.getCxxCompiler(), configuration), allargs,
					tool.isReplaceCompiler());
		}
		if (tool.getF90Compiler() != null) {
			ops += getStandardMakeOp(FComp, getToolCommand(tool.getF90Compiler(), configuration), allargs, tool.isReplaceCompiler());
		}
		if (tool.getUpcCompiler() != null) {
			ops += getStandardMakeOp(UPCComp, getToolCommand(tool.getUpcCompiler(), configuration), allargs,
					tool.isReplaceCompiler());
		}
		return ops;
	}

	private String getStandardMakeOp(String var, String command, String args, boolean replace) {
		String op = EMPTY;
		if (command != null) {
			op = var + EQ + command + SPACE + args;
			if (!replace) {
				op += SPACE + "$(" + var + ")";
			}
			op += NEWLINE;
		}

		return op;
	}

	private void initBuild(ILaunchConfiguration conf) throws CoreException {
		buildConf = configuration.getAttribute(ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME, (String) null);

		if (tool == null) {
			return;
		}

		buildInfo = ManagedBuildManager.getBuildInfo(thisProject);
		olddefbuildconf = buildInfo.getDefaultConfiguration();// TODO: Make sure
																// default
																// configuration
																// always works.
																// Prompt user?
		isManaged = olddefbuildconf.isManagedBuildOn();

		if (isManaged) {
			runbuilt = initMMBuild();
		} else {
			runbuilt = initSMBuild();
		}
	}

	private boolean initMMBuild() throws CoreException {
		if (buildInfo == null || !buildInfo.isValid()) {
			System.out.println(Messages.BuilderTool_NoInfo);
			return false;
		}

		final IManagedProject managedBuildProj = buildInfo.getManagedProject();
		if (managedBuildProj == null) {
			System.out.println(Messages.BuilderTool_NoManagedProject);
			return false;
		}
		binary = buildInfo.getBuildArtifactName();

		if (binary.equals("${ProjName}")) {
			binary = thisProject.getName();
		}

		final String bextension = buildInfo.getBuildArtifactExtension();
		if (bextension.length() > 0) {
			binary = binary + "." + bextension; //$NON-NLS-1$
		}

		// Make a list of the configurations already within the project
		final IConfiguration[] buildconfigs = buildInfo.getManagedProject().getConfigurations();
		// IConfiguration selectedconf = null;
		for (final IConfiguration buildconfig : buildconfigs) {
			if ((buildconfig.getName()).equals(buildConf)) {
				selectedconf = buildconfig;
				break;
			}
		}

		if (selectedconf == null) {
			System.out.println(Messages.BuilderTool_NoConfSelected);
			return false;
		}
		if (selectedconf.getName() == null) {
			System.out.println(Messages.BuilderTool_SelConfHasNoName);
			return false;
		}

		// Make the new configuration name, and if there is already a
		// configuration with that name, remove it.
		final String basename = selectedconf.getName();
		newname = null;// =basename+"_PerformanceAnalysis"; //TODO: FIX RECOVERY
						// OF TOOLID!!!

		final String addname = configuration.getAttribute(TOOLCONFNAME + tool.getToolId(), DEFAULT_TOOLCONFNAME);
		// if(basename.indexOf(addname)<0)
		newname = basename + "_" + addname; //$NON-NLS-1$

		if (addname.equals(DEFAULT_TOOLCONFNAME)) {
			String nameMod = tool.getToolName();// tool.toolName;
			if (nameMod == null) {
				nameMod = tool.getToolId();
			}
			newname += "_" + nameMod; //$NON-NLS-1$
		}

		progPath = newname + File.separator + binary;
		// TODO: Need to get rid of this file

		// TODO: We have to do this because PTP puts its output in the build
		// directory

		if (configuration.getAttribute(EXTOOL_EXECUTABLE_PATH_TAG, (String) null) != null) {
			outputLocation = thisProject.getFile(newname).getLocationURI().getPath();// .toOSString();
		}

		boolean confExists = false;
		final IConfiguration[] confs = managedBuildProj.getConfigurations();
		for (final IConfiguration conf : confs) {
			if (conf.getName().equals(newname) || conf.getName().indexOf(newname) >= 0) {
				confExists = true;
				newBuildConfig = conf;
				break;
				// managedBuildProj.removeConfiguration(confs[i].getId());
			}
		}
		// Make a copy of the selected configuration(Clone works, basic create
		// does not) and rename it.
		if (!confExists) {
			newBuildConfig = managedBuildProj.createConfigurationClone(selectedconf, selectedconf.getId()
					+ "." + ManagedBuildManager.getRandomNumber()); //$NON-NLS-1$
		}
		if (newBuildConfig == null) {
			System.out.println(Messages.BuilderTool_NoConfig);
			return false;
		}

		return true;
	}

	/**
	 * Runs the managed make build system using the performance tool's compilers
	 * and compiler options. This is accomplished by creating a new build
	 * configuration and replacing the compiler with the relevant tool commands
	 * 
	 * @param monitor
	 * @throws CoreException
	 * @throws FileNotFoundException
	 */
	public boolean initMMBuildConf() throws CoreException, FileNotFoundException {

		// boolean preconf=false;
		if (newBuildConfig.getName().equals(newname)) {
			// preconf=true;
		} else {
			newBuildConfig.setName(newname);
		}

		final IToolChain chain = newBuildConfig.getToolChain();
		final ITool[] tools = chain.getTools();

		for (final ITool it : tools) {
			for (final IOption op : it.getOptions()) {
				if (op == null) {
					continue;
				}
				if (op.getName() == null) {
					continue;
				}
				// if(op.getName().equals("Optimization Level")){
				// System.out.println(op.getName()+" ID: "+op.getBaseId());
				// for(String vals:op.getApplicableValues())
				// {
				// System.out.println(vals);
				// }
				// }
			}
		}

		// TODO: Make sure this never has side-effects.
		String allargs = ""; //$NON-NLS-1$
		if (tool.getAllCompilers() != null && !tool.getAllCompilers().equals(tool.getCcCompiler())) {
			allargs = getToolArguments(tool.getAllCompilers(), configuration);
		}
		int numChanges = 0;
		for (final ITool tool2 : tools) {

			if (buildMods != null) {
				for (final String opName : buildMods.keySet()) {
					// System.out.println(op.getName()+" ID: "+op.getBaseId());
					for (final IOption op : tool2.getOptions()) {
						// IOption op=tools[i].getOptionById(opId);
						if (op.getName().equals(opName))// op.getName().equals("Optimization Level"))
						{
							try {
								op.setValue(buildMods.get(opName));
							} catch (final BuildException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			final String toolid = tool2.getId();
			if (toolid.indexOf(".c.") >= 0) //$NON-NLS-1$
			{
				numChanges += modifyCommand(tool2, getToolCommand(tool.getCcCompiler(), configuration), allargs,
						tool.isReplaceCompiler());
			}
			if (toolid.indexOf(".cpp.") >= 0) //$NON-NLS-1$
			{
				numChanges += modifyCommand(tool2, getToolCommand(tool.getCxxCompiler(), configuration), allargs,
						tool.isReplaceCompiler());
			}
			if (toolid.indexOf(".fortran.") >= 0) //$NON-NLS-1$
			{
				numChanges += modifyCommand(tool2, getToolCommand(tool.getF90Compiler(), configuration), allargs,
						tool.isReplaceCompiler());
			}
			if ((toolid.indexOf(".upc.") >= 0) || (toolid.indexOf(".bupc.") >= 0) || (toolid.indexOf(".xlupc.") >= 0)) //$NON-NLS-1$
			{
				numChanges += modifyCommand(tool2, getToolCommand(tool.getUpcCompiler(), configuration), allargs,
						tool.isReplaceCompiler());
			}
		}
		// System.out.println(tbpath+File.separator+"tau_xxx.sh"+tauCompilerArgs);
		if (numChanges > 0) {
			ManagedBuildManager.saveBuildInfo(thisProject, true);
		}

		return true;
	}

	private boolean initSMBuild() throws CoreException {
		if (buildInfo == null || !buildInfo.isValid()) {
			System.out.println(Messages.BuilderTool_NoInfo);
			return false;
		}

		// Make a list of the configurations already within the project
		final IConfiguration[] buildconfigs = buildInfo.getManagedProject().getConfigurations();
		// IConfiguration selectedconf = null;
		for (final IConfiguration buildconfig : buildconfigs) {
			if ((buildconfig.getName()).equals(buildConf)) {
				selectedconf = buildconfig;
				break;
			}
		}

		progPath = olddefbuildconf.getEditableBuilder().getBuildLocation().toOSString();// + "?";
		newname = progPath;
		// progPath = newname + File.separator + binary;
		// System.out.println(progPath);

		// TODO: We have to do this because PTP puts its output in the build
		// directory
		if (configuration.getAttribute(EXTOOL_EXECUTABLE_PATH_TAG, (String) null) != null) {
			if (newname == null) {
				outputLocation = "";
			} else {
				final IFileStore newFile = utilBlob.getFile(newname);// thisProject.getFile(newname);
				if (newFile.fetchInfo().exists()) {
					outputLocation = newFile.toURI().getPath(); // buildco.getLocationURI().getPath();
				} else {
					outputLocation = "";
				}
			}
		}
		return true;
	}

	private boolean managedMakeBuild(IProgressMonitor monitor) {
		// Build set the new configuration to default so we can build it.

		IFile programPath = null;

		IFileStore pathStore = null;
		if (isSyncProject) {
			pathStore = utilBlob.getFile(outputLocation);
			pathStore = pathStore.getChild(progPath);
		} else {
			programPath = thisProject.getFile(progPath);
			pathStore = utilBlob.getFile(programPath.getLocationURI().getPath());

		}

		// long lastBuilt=-1;
		// if(pathStore.fetchInfo().exists()){
		// lastBuilt=pathStore.fetchInfo().getLastModified();
		// }

		ManagedBuildManager.setDefaultConfiguration(thisProject, newBuildConfig);
		try {
			thisProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);// .FULL_BUILD
																				// //.INCREMENTAL_BUILD

		} catch (final Exception e) {
			return false;
		}

		while (waitForBuild(-1, programPath, pathStore.fetchInfo())) {// !programPath.exists() || !pathStore.fetchInfo().exists()) {
			if (monitor != null && monitor.isCanceled()) {
				// ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
				restoreBuild();
				runbuilt = false;
				throw new OperationCanceledException();
			}
			final long numMillisecondsToSleep = 1000;
			try {
				Thread.sleep(numMillisecondsToSleep);
			} catch (final InterruptedException e) {
			}
			if (!isSyncProject) {
				programPath = thisProject.getFile(progPath);
			}
		}

		restoreBuild();
		return true;
	}

	public void restoreBuild() {
		if (isManaged) {
			ManagedBuildManager.setDefaultConfiguration(thisProject, olddefbuildconf);
			// if(!configuration.getAttribute(NOCLEAN,
			// false)&&managedBuildProj!=null&&newBuildConfig!=null)
			// managedBuildProj.removeConfiguration(newBuildConfig.getId());
		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			buildIndstrumented(monitor);
		} catch (final Exception e) {
			return new Status(IStatus.ERROR, "com.ibm.jdg2e.concurrency", IStatus.ERROR, Messages.BuilderTool_BuildIncomplete, e); //$NON-NLS-1$
		}
		return new Status(IStatus.OK, "com.ibm.jdg2e.concurrency", IStatus.OK, Messages.BuilderTool_BuildSuccessful, null); //$NON-NLS-1$
	}

	@Override
	public void setSuccessAttribute(String value) {
		if (tool != null && tool.getSetSuccessAttribute() != null) {
			try {
				final ILaunchConfigurationWorkingCopy configuration = this.configuration.getWorkingCopy();
				configuration.setAttribute(tool.getSetSuccessAttribute(), value);
				configuration.doSave();
			} catch (final CoreException e) {
				// Ignore
			}
		}
	}

	/**
	 * Runs the standard make build system using the tool-supplied compiler and
	 * compiler options. This is accomplished by temporarily replacing the
	 * default compiler names in a pre-defined makefile inclusion with the names
	 * and arguments of the compilers
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	public void standardMakeBuild(IProgressMonitor monitor) throws CoreException {

		final IFileStore projectFileStore = utilBlob.getFile(projectLocation);
		final IFileStore compilerInclude = projectFileStore.getChild("eclipse.inc");//new File(projectLocation + File.separator + "eclipse.inc"); //$NON-NLS-1$
		//IFileStore compilerDef = projectFileStore.getChild("eclipse.inc.default");// new File(projectLocation + File.separator + "eclipse.inc.default"); //$NON-NLS-1$
		try {
			// if (compilerInclude.fetchInfo().exists()) {

			// compilerInclude.copy(compilerDef, EFS.OVERWRITE, null);

			// InputStream in = compilerInclude.openInputStream(EFS.NONE, null);// new FileInputStream(compilerInclude);
			// OutputStream out = compilerDef.openOutputStream(EFS.NONE, null);//new FileOutputStream(compilerDef);
			//
			// byte[] buf = new byte[1024];
			// int len;
			// while ((len = in.read(buf)) > 0) {
			// out.write(buf, 0, len);
			// }
			// in.close();
			// out.close();
			// }
			final BufferedOutputStream makeOut = new BufferedOutputStream(compilerInclude.openOutputStream(EFS.NONE, null));
			final String allargs = getToolArguments(tool.getAllCompilers(), configuration);
			final String ops = getStandardMakeBuildOps(tool, configuration, allargs);

			makeOut.write(ops.getBytes());
			makeOut.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final MakeTargetManager targetMan = new MakeTargetManager();
		targetMan.startup();

		final IMakeTarget[] targs = targetMan.getTargets(thisProject);
		IMakeTarget select = null;
		for (final IMakeTarget targ : targs) {
			if (targ.getName().equals(Messages.BuilderTool_all)) {
				select = targ;
				break;
			}
			// System.out.println(targs[i].getName()+" "+targs[i].getTargetBuilderID());
		}
		if (select == null) {
			if (!isSyncProject) {
				final IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(thisProject,
						RemoteBuildLaunchUtils.REMOTE_MAKE_BUILDER_ID);

				if (info == null || !info.isFullBuildEnabled()) {
					System.out.println(Messages.BuilderTool_NoMakeTargetAll);
					runbuilt = false;
					return;
				}
			}

			thisProject.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
			thisProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);

		} else {
			// System.out.println(select.getBuildLocation());

			select.build(monitor);
		}

		targetMan.shutdown();

		// if (compilerDef.fetchInfo().exists()) {
		// InputStream in;
		try {
			// in = compilerDef.openInputStream(EFS.NONE, null);//new FileInputStream(compilerDef);

			final OutputStream out = new BufferedOutputStream(compilerInclude.openOutputStream(EFS.NONE, null));// new
			// FileOutputStream(compilerInclude);

			// Transfer bytes from in to out
			final byte[] buf = new byte[1024];
			// int len;
			// while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, 0);
			// }
			// in.close();
			out.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		// }
		runbuilt = true;
		return;
	}

	private boolean waitForBuild(long lastBuilt, IFile programPath, IFileInfo progInfo) {
		if ((programPath != null && !programPath.exists()) && !progInfo.exists()) {
			return true;
		}

		// if(progInfo.getLastModified()==lastBuilt)
		// return true;

		return false;
	}
}
