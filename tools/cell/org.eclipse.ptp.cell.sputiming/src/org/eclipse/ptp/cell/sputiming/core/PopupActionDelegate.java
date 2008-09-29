/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.sputiming.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.cell.preferences.ui.PreferenceConstants;
import org.eclipse.ptp.cell.sputiming.Activator;
import org.eclipse.ptp.cell.sputiming.debug.Debug;
import org.eclipse.ptp.cell.sputiming.execution.CompilerParameters;
import org.eclipse.ptp.cell.sputiming.execution.SPUTimingExecution;
import org.eclipse.ptp.cell.sputiming.execution.SPUTimingParameters;
import org.eclipse.ui.console.IOConsole;


/**
 * Class responsible for executing sputiming tool on a Managed Build project
 * 
 * @author Richard Maciel
 *
 */
public class PopupActionDelegate {
	IFile selectedFile;

	private IProject project;
	private CommonOperations commonOperations;

	public PopupActionDelegate(IFile selectedFile) {
		this.selectedFile = selectedFile;
		project = selectedFile.getProject();
		commonOperations = new CommonOperations(project, selectedFile.getLocation());
	}

	/**
	 * Execute the sputiming tool
	 * 
	 * @param
	 * @return
	 */
	public void execute(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("SPU Timing", 3); //$NON-NLS-1$
		Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Source file: ''{0}''; Project: ''{1}''", selectedFile.toString(), project.getName()); //$NON-NLS-1$		
		
		try {
			// Discover if the project has managed build information associated
			checkBuildInfo();
			// Then get build info
			IManagedBuildInfo managedBuildInfo = ManagedBuildManager.getBuildInfo(project);
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Got managed build info for project: name {0}", managedBuildInfo.getConfigurationName()); //$NON-NLS-1$
			
			// Find if the selected project is a SPE project.
			checkSPEProjectType();
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Valid SPE project."); //$NON-NLS-1$
	
			// Prefer the selected configuration over the default.
			IConfiguration activeConfiguration;
			if(managedBuildInfo.getSelectedConfiguration() != null) {
				activeConfiguration = managedBuildInfo.getSelectedConfiguration();
			} else {
				activeConfiguration = managedBuildInfo.getDefaultConfiguration();
			}
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Got active configuration: id {0}; base id: {1}; name: {2}", activeConfiguration.getId(), activeConfiguration.getBaseId(), activeConfiguration.getName()); //$NON-NLS-1$
			
			// Fetch necessary tools and commands associated to tools
			ITool compilerTool = getCompilerTool(activeConfiguration, managedBuildInfo);
			String compilerCmd = getCompilerCommand(activeConfiguration, compilerTool);
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Got compiler tool: id {0}; base id: {1}; name: {2}", compilerTool.getId(), compilerTool.getBaseId(), compilerTool.getName()); //$NON-NLS-1$
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Compiler command: {0}", compilerCmd); //$NON-NLS-1$
			
			// Fetch the environment information
			String [] compilerEnvironmentPath = getEnvironmentPath(activeConfiguration);
			
	//		 Create console for displaying information
			IOConsole console = commonOperations.createConsole(Messages.LaunchConfigurationDelegate_Console_Name); 
			
			/*
			 * Generates parameters for compiler execution
			 */
			CompilerParameters compParam = new CompilerParameters();
			compParam.setCompilerName(compilerCmd);
			compParam.setCompilerEnvironmentPath(compilerEnvironmentPath);
			compParam.setSourceFile(selectedFile.getLocation().toOSString());
			compParam.setCompilerFlags(getCompilerFlags(managedBuildInfo, compilerTool));
			compParam.setWorkingDirectory(commonOperations.getWorkingDirectory().toFile());
			compParam.setConsole(console);
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Discovered compiler parameters: {0}", compParam.toString()); //$NON-NLS-1$
			
			/*
			 * Generates parameters for sputiming execution
			 */
			// Get sputiming command path
			PreferenceConstants preferences = PreferenceConstants.getInstance();
			String sputimingcommand = preferences.getTIMING_SPUBIN().toOSString();
			
			// Check the architecture
			String spu_timing_param = getArchitectureParameter(compilerTool);
			
			SPUTimingParameters spuTimingParam = new SPUTimingParameters();
			spuTimingParam.setSputimingPath(sputimingcommand);
			spuTimingParam.setInputFile(commonOperations.getAssemblyFilePath().toOSString());
			spuTimingParam.setWorkingDirectory(commonOperations.getWorkingDirectory().toFile());
			spuTimingParam.setConsole(console);
			spuTimingParam.setParameters(spu_timing_param);
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Discovered sputiming parameters: {0}", spuTimingParam.toString()); //$NON-NLS-1$
			
			// Assure the directory where the files will be created exists
			commonOperations.createWorkingDirectory(monitor);
	
			// Let the user cancel before the execution of the compiler
			if(monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			
	//		Create execution object.
			SPUTimingExecution spuexec = SPUTimingExecution.createExecution(compParam, spuTimingParam);
	
			monitor.worked(1);
	//		Execute compiler.		
			
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Request to compiler"); //$NON-NLS-1$
			spuexec.startCompiler();
	
	//		 Let the user cancel after the execution of the compiler
			if(monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			
			monitor.worked(1);
			
			// Execute sputiming tool
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Request to sputiming"); //$NON-NLS-1$
			spuexec.startSPUTimingTool();
	
	//		 Let the user cancel after the execution of the sputiming tool
			if(monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			
			monitor.worked(1);
	//		Copy file to console
	
			IPath sputiminggenfile = new Path(spuTimingParam.getInputFile() + ".timing"); //$NON-NLS-1$
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Read output file: ''{0}''", sputiminggenfile.toString()); //$NON-NLS-1$
			commonOperations.displayOutputFile(sputiminggenfile, console);
	
			// Warn all plugins that are registered in the
			// extension point that a new file has been generated.
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Request to extension points"); //$NON-NLS-1$
			ExternalTools.callExtensions(sputiminggenfile);
//		} catch (CoreException e) {
//			throw e;
//		} catch (Exception e) {
//			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Unexpected exception while running sputiming", e));
		} finally {
//			monitor.done();
		}
	}

	/**
	 * Retrieve environment variables from a given configuration
	 * 
	 * @param activeConfiguration
	 * @return A vector of strings containing the variables and their values in the <var-name>=<var-value> format
	 */
	private String[] getEnvironmentPath(IConfiguration activeConfiguration) { 
		Debug.POLICY.enter(Debug.DEBUG_COMPILER);
		
		// Fetch environment variable
		ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(activeConfiguration);
		IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable path = mngr.getVariable("PATH", cfgDes, true); //$NON-NLS-1$
		//IEnvironmentVariable[] vars = mngr.getVariables(cfgDes, true);
		
		/*String [] envStrArr = new String[vars.length];
		
		// Runtime's exec method requires a vector of Strings where each String is on the
		// <env-var-name>=<env-var-value> format.
		for(int i = 0; i < vars.length; i++) {
			envStrArr[i] = vars[i].getName() + "=" + vars[i].getValue(); //$NON-NLS-1$
		}*/
		
		// Separate variable values
		String [] pathValues = path.getValue().split(path.getDelimiter());
		
		Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Environment path: {0}", (Object [])pathValues); //$NON-NLS-1$
		Debug.POLICY.exit(Debug.DEBUG_COMPILER);
		
		return pathValues;
	}

	private String getArchitectureParameter(ITool compilerTool) throws CoreException {
		IOption option = null;
		String spu_timing_param = null;
		
		// Try to get options of GNU project
		option = compilerTool.getOptionBySuperClassId("cell.spu.gnu.c.compiler.option.arch"); //$NON-NLS-1$
		if (option == null) {
			// Try to get options of XL project
			option = compilerTool.getOptionBySuperClassId("cell.spu.xl.c.compiler.option.arch"); //$NON-NLS-1$
		} 
		if (option == null) {
			throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					0, Messages.PopupActionDelegate_CheckBuildInfo_ProjectTypeNotSupported, null));
		}
	
		String value = null;
		try {
			value = option.getStringValue();
		} catch (BuildException e) {
			throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					0, Messages.PopupActionDelegate_CheckBuildInfo_CannotExtractArchitectureInfo, e));
		}
		if (value.equals("cell.spu.gnu.option.arch.cell")) { //$NON-NLS-1$
			spu_timing_param = "-march=cell"; //$NON-NLS-1$
		} else if (value.equals("cell.spu.gnu.option.arch.celledp")) { //$NON-NLS-1$
			spu_timing_param = "-march=celledp"; //$NON-NLS-1$
		} else if (value.equals("cell.spu.xl.option.arch.cell")) { //$NON-NLS-1$ 
			spu_timing_param = "-march=cell"; //$NON-NLS-1$
		} else if (value.equals("cell.spu.xl.option.arch.celledp")) { //$NON-NLS-1$ 
			spu_timing_param = "-march=celledp"; //$NON-NLS-1$
		}
		return spu_timing_param;
	}
	
	/**
	 * Return a string containing the output flag and the output filename with the assembler extension
	 * @param compilerTool
	 * @return
	 * @throws CoreException 
	 */
	private String getOutputFlagBlock(ITool compilerTool) throws CoreException {
		return compilerTool.getOutputFlag() + commonOperations.getAssemblyFilePath().toOSString(); 
	}
	
	private String getCompilerFlags(IManagedBuildInfo managedBuildInfo, ITool compilerTool) throws CoreException {
//		 Get and alter the command flags.
		String flaglist = null;
		try
		{
			flaglist = compilerTool.getToolCommandFlagsString(
					selectedFile.getLocation(),
					getCompilerObjectFilePath(managedBuildInfo)) + " " + //$NON-NLS-1$
					getOutputFlagBlock(compilerTool) + " -S";  //$NON-NLS-1$ // Add hardcoded assembly flag
		} catch (BuildException e1)
		{
			throw new CoreException(new Status(Status.ERROR, 
					Activator.getDefault().getBundle().getSymbolicName(),
					0, Messages.PopupActionDelegate_GetCompilerFlags_CannotExtractInformation, e1));
		}
		return flaglist;
	}

//	private String getX(IManagedBuildInfo managedBuildInfo, ITool compilerTool) throws CoreException {
//		compilerTool.
//		return null;
//	}

	/**
	 * Build the path of the output file of the compiler using as base selectedFile.
	 * This path is only useful to extract the compiler tool information, since the actual object path
	 * (where the object file will be generated) is extracted using the {@link #getActualObjectFilePath(IManagedBuildInfo)}
	 * @param managedBuildInfo
	 * @return
	 */
	private IPath getCompilerObjectFilePath(IManagedBuildInfo managedBuildInfo) {
		/*String outputextension = managedBuildInfo.getOutputExtension(selectedFile.getFileExtension());
		IPath outputfilenamewoext = selectedFile.getFullPath().removeFileExtension().makeRelative().removeFirstSegments(1);
		IPath outputfilename = selectedFile.getLocation().removeLastSegments(1).
		addTrailingSeparator().append(builddir).addTrailingSeparator().append(outputfilenamewoext.toString() + "." + outputextension);*/
		
		String compBuildDir = managedBuildInfo.getConfigurationName();
		String outputExt = managedBuildInfo.getOutputExtension(selectedFile.getFileExtension());
		IPath compilerOutputFilePath = selectedFile.getLocation().removeLastSegments(1).
		addTrailingSeparator().append(compBuildDir).addTrailingSeparator().
			append(commonOperations.getOutputFilenameWithoutExtension() + "." + outputExt); //$NON-NLS-1$
		
		return compilerOutputFilePath;
	}
	
	/**
	 * Return the path of the object file that will be generated by this plugin.
	 * @param managedBuildInfo
	 * @return
	 */
	private IPath getActualObjectFilePath(IManagedBuildInfo managedBuildInfo) {
		String outputExt = managedBuildInfo.getOutputExtension(selectedFile.getFileExtension());
		IPath outputFilePath = commonOperations.getWorkingDirectory().addTrailingSeparator().
			append(commonOperations.getOutputFilenameWithoutExtension() + "." + outputExt); //$NON-NLS-1$
			
		return outputFilePath;
	}

	/**
	 * Return the compiler command from the associated configuration and tool
	 * @return
	 * @throws CoreException
	 */
	private String getCompilerCommand(IConfiguration activeConfiguration, ITool compilerTool) throws CoreException {		

//		Get the compiler command
		String compilerCmd = compilerTool.getToolCommand();
//		Convert the compilerPath before storing it
		String resolvedCompilerCmd = null;
		try {
			resolvedCompilerCmd = ManagedBuildManager.getBuildMacroProvider().
			resolveValue(compilerCmd, "", "", IBuildMacroProvider.CONTEXT_CONFIGURATION,  //$NON-NLS-1$ //$NON-NLS-2$
					activeConfiguration);
		} catch (BuildMacroException e2) {
			throw new CoreException(new Status(Status.ERROR, 
					Activator.getDefault().getBundle().getSymbolicName(),
					0, Messages.PopupActionDelegate_GetCompilerCommand_CannotResolveName, e2));
		}

	/*	// Get path from preferences
		IPreferenceStore gnustore = org.eclipse.ptp.cell.managedbuilder.gnu.core.Activator.getDefault()
		.getPreferenceStore();
		IPreferenceStore xlcstore = org.eclipse.ptp.cell.managedbuilder.xl.core.Activator.getDefault()
		.getPreferenceStore();
		String gcc = gnustore.getString(GnuToolsProperties.gnuToolsPath);
		String xlc = xlcstore.getString(XlToolsProperties.xlToolsPath);*/
		
		
		
		if(resolvedCompilerCmd == null || resolvedCompilerCmd.equals("")) //$NON-NLS-1$
		{
			throw new CoreException(new Status(Status.ERROR, 
					Activator.getDefault().getBundle().getSymbolicName(),
					0, Messages.PopupActionDelegate_GetCompilerCommand_CannotExtractBuildCommand, null));
		}
		
		return resolvedCompilerCmd;
	}
	
	private ITool getCompilerTool(IConfiguration activeConfiguration, 
			IManagedBuildInfo managedBuildInfo) throws CoreException {
		IResourceConfiguration resconf = activeConfiguration.
		getResourceConfiguration(selectedFile.getFullPath().toOSString());

		// Prefer the tool connected to the resourceConfiguration
		// to the associated with the source file extension.
		ITool compilerTool;
		if (resconf == null)
			compilerTool = managedBuildInfo
					.getToolFromInputExtension(selectedFile.getFileExtension());
		else
			compilerTool = resconf.getTools()[0];

		if (compilerTool == null) {
			throw new CoreException(
					new Status(
							Status.ERROR,
							Activator.getDefault().getBundle()
									.getSymbolicName(),
							0,
							Messages.PopupActionDelegate_GetCompilerTool_ProblemFetchingCompilerTool, null));
		}
		
		return compilerTool;
	}

	/**
	 * Check if the project type is of the SPE type. Throw an exception if not. 
	 * @throws CoreException
	 */
	private void checkSPEProjectType() throws CoreException {
		IProjectType projecttype = ManagedBuildManager.
		getBuildInfo(project).getManagedProject().getProjectType();
		if(projecttype.getBaseId().indexOf("cell.managedbuild.target.cell.spu") == -1) { //$NON-NLS-1$
			throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					0, Messages.PopupActionDelegate_CheckSPEProjectType_NotSPEProject, null));
		}
		
	}

	/**
	 * Check if is there build info available. Throw an exception if not.
	 * 
	 * @throws CoreException
	 */
	private void checkBuildInfo() throws CoreException {
		if(!ManagedBuildManager.canGetBuildInfo(project)) {
			throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					0, Messages.PopupActionDelegate_CheckBuildInfo_CannotExtractBuildInfo, null));
		}
	}	
}
