package org.eclipse.ptp.etfw.internal;

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
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.messages.Messages;
import org.eclipse.ptp.etfw.toolopts.BuildTool;

@SuppressWarnings("restriction")
public class BuilderTool extends ToolStep implements IToolLaunchConfigurationConstants {

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
	// private String apppathattrib=null;
	// private IManagedProject managedBuildProj=null;
	private IConfiguration newBuildConfig = null;
	private String buildConf = null;
	// private String projectLocation=null;
	private IConfiguration olddefbuildconf = null;

	// private IConfiguration standardbuildconf=null;
	// private ICProject thisCProject=null;
	// private IProject thisProject = null;

	private IManagedBuildInfo buildInfo = null;

	// private String outputLocation=null;
	private Map<String, String> buildMods = null;

	private String newname = null;
	private String binary = null;
	private BuildTool tool = null;

	private boolean isManaged;

	public BuilderTool(ILaunchConfiguration conf, BuildTool btool, Map<String, String> buildMods) throws CoreException {
		super(conf, Messages.BuilderTool_InstrumentingBuilding);
		this.buildMods = buildMods;
		tool = btool;
		initBuild(conf);
	}

	public BuilderTool(ILaunchConfiguration conf, BuildTool btool) throws CoreException {
		super(conf, Messages.BuilderTool_InstrumentingBuilding);
		tool = btool;
		initBuild(conf);
	}

	// private String rootLocation=null;
	private void initBuild(ILaunchConfiguration conf) throws CoreException {
		// apppathattrib=apa;
		// outputLocation=projectLocation;
		// rootLocation=projectLocation;
		buildConf = configuration.getAttribute(ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME, (String) null);

		if (tool == null)
			return;

		buildInfo = ManagedBuildManager.getBuildInfo(thisCProject.getResource());
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

	public boolean getBuildSuccessful() {
		return runbuilt;
	}

	public String getOutputLocation() {
		return outputLocation;
	}

	public String getProgramPath() {
		return progPath;
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

		File compilerInclude = new File(projectLocation + File.separator + "eclipse.inc"); //$NON-NLS-1$
		File compilerDef = new File(projectLocation + File.separator + "eclipse.inc.default"); //$NON-NLS-1$
		try {
			if (compilerInclude.exists()) {
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
			// TODO: Make this work again (i.e. distinguish between all-compiler
			// and discrete compiler systems)
			BufferedWriter makeOut = new BufferedWriter(new FileWriter(compilerInclude));
			String allargs = getToolArguments(tool.getGlobalCompiler(), configuration);
			makeOut.write(getToolCommand(tool.getCcCompiler(), configuration) + " " + allargs + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			makeOut.write(getToolCommand(tool.getCxxCompiler(), configuration) + " " + allargs + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			makeOut.write(getToolCommand(tool.getF90Compiler(), configuration) + " " + allargs + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			makeOut.write(getToolCommand(tool.getUPCCompiler(), configuration) + " " + allargs + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
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
		for (int i = 0; i < targs.length; i++) {
			if (targs[i].getName().equals(Messages.BuilderTool_all)) {
				select = targs[i];
				break;
			}
			// System.out.println(targs[i].getName()+" "+targs[i].getTargetBuilderID());
		}
		if (select == null) {
			System.out.println(Messages.BuilderTool_NoMakeTargetAll);
			runbuilt = false;
			return;
		}

		// System.out.println(select.getBuildLocation());

		select.build(monitor);

		targetMan.shutdown();

		if (compilerDef.exists()) {
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
		runbuilt = true;
		return;
	}

	private boolean initSMBuild() throws CoreException {
		if (buildInfo == null || !buildInfo.isValid()) {
			System.out.println(Messages.BuilderTool_NoInfo);
			return false;
		}

		// Make a list of the configurations already within the project
		IConfiguration[] buildconfigs = buildInfo.getManagedProject().getConfigurations();
		//IConfiguration selectedconf = null;
		for (int i = 0; i < buildconfigs.length; i++) {
			if ((buildconfigs[i].getName()).equals(buildConf)) {
				selectedconf = buildconfigs[i];
				break;
			}
		}

		progPath = olddefbuildconf.getEditableBuilder().getBuildLocation() + "?"; //$NON-NLS-1$

		progPath = newname + File.separator + binary;
		// System.out.println(progPath);

		// TODO: We have to do this because PTP puts its output in the build
		// directory
		if (configuration.getAttribute(EXTOOL_EXECUTABLE_PATH_TAG, (String) null) != null) {
			outputLocation = thisProject.getFile(newname).getLocationURI().getPath();//.toOSString();
		}
		return true;
	}

	public IConfiguration selectedconf = null;

	private boolean initMMBuild() throws CoreException {
		if (buildInfo == null || !buildInfo.isValid()) {
			System.out.println(Messages.BuilderTool_NoInfo);
			return false;
		}

		IManagedProject managedBuildProj = buildInfo.getManagedProject();
		if (managedBuildProj == null) {
			System.out.println(Messages.BuilderTool_NoManagedProject);
			return false;
		}
		binary = buildInfo.getBuildArtifactName();
		
		if(binary.equals("${ProjName}")){
			binary=thisProject.getName();
		}
		
		String bextension = buildInfo.getBuildArtifactExtension();
		if (bextension.length() > 0)
			binary = binary + "." + bextension; //$NON-NLS-1$

		// Make a list of the configurations already within the project
		IConfiguration[] buildconfigs = buildInfo.getManagedProject().getConfigurations();
		// IConfiguration selectedconf = null;
		for (int i = 0; i < buildconfigs.length; i++) {
			if ((buildconfigs[i].getName()).equals(buildConf)) {
				selectedconf = buildconfigs[i];
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
		String basename = selectedconf.getName();
		newname = null;// =basename+"_PerformanceAnalysis"; //TODO: FIX RECOVERY
						// OF TOOLID!!!

		String addname = configuration.getAttribute(TOOLCONFNAME + tool.toolID, DEFAULT_TOOLCONFNAME);
		// if(basename.indexOf(addname)<0)
		newname = basename + "_" + addname; //$NON-NLS-1$

		if (addname.equals(DEFAULT_TOOLCONFNAME)) {
			String nameMod = tool.toolName;
			if (nameMod == null) {
				nameMod = tool.toolID;
			}
			newname += "_" + nameMod; //$NON-NLS-1$
		}

		progPath = newname + File.separator + binary;
		// System.out.println(progPath);

		// TODO: We have to do this because PTP puts its output in the build
		// directory
		if (configuration.getAttribute(EXTOOL_EXECUTABLE_PATH_TAG, (String) null) != null) {
			outputLocation = thisProject.getFile(newname).getLocationURI().getPath();//.toOSString();
		}

		boolean confExists = false;
		IConfiguration[] confs = managedBuildProj.getConfigurations();
		for (int i = 0; i < confs.length; i++) {
			if (confs[i].getName().equals(newname) || confs[i].getName().indexOf(newname) >= 0) {
				confExists = true;
				newBuildConfig = confs[i];
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

		// TODO: Restore TAU build configuration adjustment
		// if(useTau)
		// {
		// TAULaunch.adjustBuild(newBuildConfig);
		// }
		// else
		{
			// TODO: Make adjustments based on configuration (map build
			// attribute names to values?)
		}

		IToolChain chain = newBuildConfig.getToolChain();
		ITool[] tools = chain.getTools();

		for (ITool it : tools) {
			for (IOption op : it.getOptions()) {
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
		if (tool.getGlobalCompiler() != null && !tool.getGlobalCompiler().equals(tool.getCcCompiler())) {
			allargs = getToolArguments(tool.getGlobalCompiler(), configuration);
		}
		int numChanges = 0;
		for (int i = 0; i < tools.length; i++) {

			if (buildMods != null) {
				for (String opName : buildMods.keySet()) {
					// System.out.println(op.getName()+" ID: "+op.getBaseId());
					for (IOption op : tools[i].getOptions()) {
						// IOption op=tools[i].getOptionById(opId);
						if (op.getName().equals(opName))// op.getName().equals("Optimization Level"))
						{
							// for(String vals:op.getApplicableValues())
							// {
							// System.out.println(vals);
							// }
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

			String toolid = tools[i].getId();
			if (toolid.indexOf(".c.") >= 0) //$NON-NLS-1$
			{
				numChanges += modifyCommand(tools[i], getToolCommand(tool.getCcCompiler(), configuration), allargs,
						tool.replaceCompiler);
			}
			if (toolid.indexOf(".cpp.") >= 0) //$NON-NLS-1$
			{
				numChanges += modifyCommand(tools[i], getToolCommand(tool.getCxxCompiler(), configuration), allargs,
						tool.replaceCompiler);
			}
			if (toolid.indexOf(".fortran.") >= 0) //$NON-NLS-1$
			{
				numChanges += modifyCommand(tools[i], getToolCommand(tool.getF90Compiler(), configuration), allargs,
						tool.replaceCompiler);
			}
			if ((toolid.indexOf(".upc.") >= 0)||(toolid.indexOf(".bupc.") >= 0)||(toolid.indexOf(".xlupc.") >= 0)) //$NON-NLS-1$
			{
				numChanges += modifyCommand(tools[i], getToolCommand(tool.getUPCCompiler(), configuration), allargs,
						tool.replaceCompiler);
			}
		}
		// System.out.println(tbpath+File.separator+"tau_xxx.sh"+tauCompilerArgs);
		if (numChanges > 0) {
			ManagedBuildManager.saveBuildInfo(thisCProject.getProject(), true);
		}

		return true;
	}

	private boolean managedMakeBuild(IProgressMonitor monitor) {
		// Build set the new configuration to default so we can build it.

		IFile programPath = thisProject.getFile(progPath);

		ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(), newBuildConfig);
		try {
			thisProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);// .FULL_BUILD
																				// //.INCREMENTAL_BUILD

		} catch (Exception e) {
			return false;
		}

		// TODO: Find out how to get build progress from within the managed
		// build system!
		IFileStore pathStore=null;
		try {
			pathStore = EFS.getStore(programPath.getLocationURI());
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (!programPath.exists() || !pathStore.fetchInfo().exists()) {
			if (monitor != null && monitor.isCanceled()) {
				// ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(),olddefbuildconf);
				restoreBuild();
				runbuilt = false;
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

	private static int modifyCommand(ITool tool, String command, String args, boolean replace) {
		int didChange = 0;
		String toolCommand = tool.getToolCommand();
		if (replace) {
			String newcom = command + " " + args; //$NON-NLS-1$
			if (!newcom.equals(toolCommand)) {
				tool.setToolCommand(command + " " + args); //$NON-NLS-1$
				didChange = 1;
			}
		} else {
			String oldcom = toolCommand.trim();
			int lastspc = oldcom.lastIndexOf(' ');
			if (lastspc >= 0) {
				oldcom = toolCommand.substring(lastspc).trim();
			}
			String newcom = command + " " + args + " " + oldcom; //$NON-NLS-1$ //$NON-NLS-2$
			if (!newcom.equals(toolCommand)) {
				tool.setToolCommand(newcom);
				didChange = 1;
			}
		}
		return didChange;
	}

	public void restoreBuild() {
		if (isManaged) {
			ManagedBuildManager.setDefaultConfiguration(thisCProject.getProject(), olddefbuildconf);
			// if(!configuration.getAttribute(NOCLEAN,
			// false)&&managedBuildProj!=null&&newBuildConfig!=null)
			// managedBuildProj.removeConfiguration(newBuildConfig.getId());
		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			buildIndstrumented(monitor);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, "com.ibm.jdg2e.concurrency", IStatus.ERROR, Messages.BuilderTool_BuildIncomplete, e); //$NON-NLS-1$
		}
		return new Status(IStatus.OK, "com.ibm.jdg2e.concurrency", IStatus.OK, Messages.BuilderTool_BuildSuccessful, null); //$NON-NLS-1$
	}
}
