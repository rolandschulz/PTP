/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.sputiming.core;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.ptp.cell.sputiming.debug.Debug;
import org.eclipse.ptp.cell.sputiming.execution.CompilerParameters;
import org.eclipse.ptp.cell.sputiming.execution.SPUTimingExecution;
import org.eclipse.ptp.cell.sputiming.execution.SPUTimingParameters;
import org.eclipse.ui.console.IOConsole;



public class LaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate, ILaunchConfigurationDelegate2
{
	//protected 
	
	/**
	 * Compiles selected file (in configuration) with assembly flag
	 * and executes SPUTiming using the generated file.
	 * This method will choose which SPUTiming it will execute
	 * based on the compiler name.
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 * @author Richard Maciel
	 * @since 1.0
	 */
	public void launch(ILaunchConfiguration configuration,
						String mode,
						ILaunch launch,
						IProgressMonitor monitor) throws CoreException
	{
		monitor.beginTask(Messages.LaunchConfigurationDelegate_Task_Name, 4);
		Debug.read();
		Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Start sputiming launcher delegate."); //$NON-NLS-1$
		Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Launch configuration map: {0}", configuration.getAttributes().toString()); //$NON-NLS-1$
		
		try {
			// Fetch parameters from the configuration attributes
			String projectName = configuration.getAttribute(
					LaunchConfigurationConstants.PROJECT_NAME, ""); //$NON-NLS-1$
			IPath projectDirPath = new Path(configuration.getAttribute(
				    LaunchConfigurationConstants.PROJECT_DIR, "")); //$NON-NLS-1$
			IPath srcFilePath = new Path(configuration.getAttribute(
					LaunchConfigurationConstants.SOURCE_FILE_NAME, "")); //$NON-NLS-1$
			if(!srcFilePath.isAbsolute()) {
				srcFilePath = projectDirPath.append(srcFilePath); 
			}
			String compbin = configuration.getAttribute(
					LaunchConfigurationConstants.COMPILER_NAME, ""); //$NON-NLS-1$
			String compflags = configuration.getAttribute(
					LaunchConfigurationConstants.COMPILER_FLAGS,"");   //$NON-NLS-1$
			String spubin = configuration.getAttribute(
					LaunchConfigurationConstants.SPU_EXECUTABLE_NAME, ""); //$NON-NLS-1$
			
			// Create the common operations object
			ICProject cproj = CoreModel.getDefault().getCModel().getCProject(projectName);
			CommonOperations commonOperations = new CommonOperations(cproj.getProject(), 
					srcFilePath);
			IOConsole console = commonOperations.createConsole(Messages.LaunchConfigurationDelegate_Console_Name);
			
			// Retrieve the working dir path
			IPath workDirPath = commonOperations.getWorkingDirectory();
			
			// Assure that the working directory exists
			commonOperations.createWorkingDirectory(monitor);
			
			// Fetch assembly path
			IPath asmPath = new Path(configuration.getAttribute(
					LaunchConfigurationConstants.ASSEMBLY_FILE_NAME, "")); //$NON-NLS-1$
			if(!asmPath.isAbsolute()) {
				asmPath = workDirPath.append(asmPath);
			}
			
			// Fetch architecture switch
			String architecture = configuration.getAttribute(
					LaunchConfigurationConstants.SPU_ARCH_TYPE, LaunchConfigurationConstants.SPU_ARCH_SPU);
			String spu_parameter = null;
			if (architecture.equals(LaunchConfigurationConstants.SPU_ARCH_SPU)) {
				spu_parameter = "-march=cell"; //$NON-NLS-1$
			} else if (architecture.equals(LaunchConfigurationConstants.SPU_ARCH_SPUEFP)) {
				spu_parameter = "-march=celledp"; //$NON-NLS-1$		
			}
			
			// Generates parameters for executions
			CompilerParameters cp = new CompilerParameters(compbin, //FIXME environment cannot be null
					compflags, null, srcFilePath.toOSString(), workDirPath.toFile(), console);
			SPUTimingParameters spup = new SPUTimingParameters(spubin, spu_parameter,
					asmPath.toOSString(), workDirPath.toFile(), console);
	
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Given compiler parameters: {0}", cp.toString()); //$NON-NLS-1$
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Given sputiming parameters: {0}", spup.toString()); //$NON-NLS-1$
	
			monitor.worked(1);
			
			// Create execution object.
			SPUTimingExecution spuexec = SPUTimingExecution.createExecution(cp, spup);
			
			// Execute compiler tool
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Request to compiler"); //$NON-NLS-1$
			spuexec.startCompiler();
			
			monitor.worked(1);
			
			// Execution sputiming tool
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Request to sputiming"); //$NON-NLS-1$
			spuexec.startSPUTimingTool();
			
			monitor.worked(1);
			
	//		 Retrieve path of the generated file
			IPath sputGenFilePath = workDirPath.append(
					asmPath.addFileExtension("timing").lastSegment()); //$NON-NLS-1$
			
	//		 Copy file to console
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Read output file: ''{0}''", sputGenFilePath.toString()); //$NON-NLS-1$
			commonOperations.displayOutputFile(sputGenFilePath, console);
	
			monitor.worked(1);
			
			// Warn all plugins that are registered in the
			// extension point that a new file has been generated.
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Request to extension points"); //$NON-NLS-1$
			ExternalTools.callExtensions(sputGenFilePath);
//		} catch (CoreException e) {
//			throw e;
//		} catch (Exception e) {
//			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Unexpected exception while running sputiming", e));
		} finally {
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Finished sputiming launcher delegate."); //$NON-NLS-1$
//			monitor.done();
		}
	}

	public boolean buildForLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		// Prevent incremental build from the managed build
		return false;
	}

	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}

	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return null;
	}

	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}
	
	
}
