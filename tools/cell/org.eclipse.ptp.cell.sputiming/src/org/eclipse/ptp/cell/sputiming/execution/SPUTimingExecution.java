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
package org.eclipse.ptp.cell.sputiming.execution;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.sputiming.Activator;
import org.eclipse.ptp.cell.sputiming.debug.Debug;
import org.eclipse.ptp.cell.utils.process.ProcessController;
import org.eclipse.ptp.cell.utils.stream.StreamBridge;
import org.eclipse.ptp.utils.core.linux.ArgumentParser;
import org.eclipse.ui.console.IOConsole;


/**
 * This class is responsible for executing the compiler and the sputiming
 * program. For that, it must receive parameters before the execution.
 * 
 * @author Richard Maciel
 * 
 */
public class SPUTimingExecution {
	// TODO remove this hard coded constant
	private static final long EXEC_TIMEOUT = 360;

	CompilerParameters compilerParameters;

	SPUTimingParameters spuTimingParameters;

	/**
	 * @param compilerParameters
	 * @param spuTimingParameters
	 */
	protected SPUTimingExecution(CompilerParameters compilerParameters,
			SPUTimingParameters spuTimingParameters) {
		this.compilerParameters = compilerParameters;
		this.spuTimingParameters = spuTimingParameters;
	}

	/**
	 * Generates and sets a new execution.
	 * 
	 * @param cp
	 * @param spup
	 * @return
	 */
	public static SPUTimingExecution createExecution(CompilerParameters cp,
			SPUTimingParameters spup) {
		return new SPUTimingExecution(cp, spup);
	}

	/**
	 * Start the execution and blocks until the end. Also connects console to
	 * process if available.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void startCompiler() throws CoreException {
		Debug.read();
		Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Start compiling."); //$NON-NLS-1$
		Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Parameters: {0}", compilerParameters.toString()); //$NON-NLS-1$

		/* 
		 * Create array with command line.
		 * Compiler, parameters, source file.
		 */ 
		List<String> l = new ArrayList<String>();
		l.add(compilerParameters.getCompilerPath());
		if (compilerParameters.getCompilerFlags() != null) {
			ArgumentParser argumentParser = new ArgumentParser(compilerParameters.getCompilerFlags());
			l.addAll(argumentParser.getTokenList());
		}
		l.add(compilerParameters.getSourceFile());
		String[] parameterArray = l.toArray(new String[l.size()]);
		Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Command line: {0}", (new ArgumentParser(parameterArray)).getCommandLine(true)); //$NON-NLS-1$
		
		/*
		 * Execute compiler using Runtime class, passing the compiler parameters
		 */
		Process compilerProcess = null;
		try {
			OutputStream os = compilerParameters.getConsole().newOutputStream();
			OutputStreamWriter w = new OutputStreamWriter(os);
			w.write((new ArgumentParser(parameterArray)).getCommandLine(true)+"\n"); //$NON-NLS-1$
			w.close();
			
			// Since the compiler path is on environment, loop over the PATH values, using them as prefix to
			// the compiler name and tries to execute the compiler.
			String [] path = compilerParameters.getCompilerEnvironment();
			String compilerCommand = parameterArray[0];
			//boolean executedCompiler = false;
			for(int i=0; i < path.length; i++) {
				Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Path entry: {0}", path[i]); //$NON-NLS-1$
				
				// Add path to first entry of the parameter array before executing.
				File pathEntryFile = new File(path[i]);
				File compilerFile = new File(pathEntryFile, compilerCommand);
//						pathEntryFile.getAbsolutePath() + File.separator + compilerCommand);
				parameterArray[0] = compilerFile.getAbsolutePath();
				
				try {
					compilerProcess = Runtime.getRuntime().exec(parameterArray, null, 
						compilerParameters.getWorkingDirectory());
					//executedCompiler = true;
					break;
				} catch (IOException e) {
					// This path is invalid. Ignore it.
					Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Compiler not found on this path entry"); //$NON-NLS-1$
				}
			}
			
			// In case of all pathEntries fail, generate an error.
			if(compilerProcess == null) {
				throw new IOException(Messages.SPUTimingExecution_ExecuteCompiler_CompilerNotFoundInPath + path);
			}
			
			/*compilerProcess = Runtime.getRuntime().exec(parameterArray, compilerParameters.getCompilerEnvironment(), 
					compilerParameters.getWorkingDirectory());*/
			/*Map<String, String> envMap;// = new Map<String, String>();
			//envMap.put("PATH", "/opt/cell/toolchain/bin");
			
			ProcessBuilder pb = new ProcessBuilder("spu-gcc");
			pb.directory(new File("/tmp"));
			envMap = pb.environment();
			
			envMap.put("PATH", "/opt/cell/toolchain/bin");
			compilerProcess = pb.start();*/
			
			
			/*compilerProcess = Runtime.getRuntime().exec("env", compilerParameters.getCompilerEnvironment(), 
					compilerParameters.getWorkingDirectory());
			compilerProcess = Runtime.getRuntime().exec("spu-gcc", new String [] {"PATH=/opt/cell/toolchain/bin"}, 
					new File("/tmp"));*/
			/*compilerProcess = Runtime.getRuntime().exec("spu-gcc", compilerParameters.getCompilerEnvironment(), 
					compilerParameters.getWorkingDirectory());*/
			Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Compiler is running"); //$NON-NLS-1$
		} catch (Exception e) {
			Debug.POLICY.error(Debug.DEBUG_COMPILER, "Failed to execute compiler: {0}", e.getMessage()); //$NON-NLS-1$			
			// TODO check this exception
			throw new CoreException(
					new Status(
							Status.ERROR,
							Activator.getDefault().getBundle().getSymbolicName(),
							0,
							Messages.StartCompiler_CompilerExecError, 
							e
			));
		}

		// Only connects process output if console is
		// available.
		if (compilerParameters.getConsole() != null) {
			connectConsoleToProcess(compilerParameters.getConsole(), compilerProcess);
			Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Connected to console"); //$NON-NLS-1$
		}

		// Uses ProcessController to manage timeout
		ProcessController pControl = new ProcessController(
				Messages.StartCompiler_CompilerProcessLabel, compilerProcess, EXEC_TIMEOUT);
		pControl.run();
		Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Created process controller"); //$NON-NLS-1$

		// Wait until the end of the process (until timeout)
		try {
			Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Waiting for compiler to finish"); //$NON-NLS-1$
			int retCode = compilerProcess.waitFor();
			if (retCode == 0) {
				Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Compiler finished successfully"); //$NON-NLS-1$
			} else {
				Debug.POLICY.error(Debug.DEBUG_COMPILER, "Compiler finished with exit code {0}", retCode); //$NON-NLS-1$
				// TODO check this exception
				throw new CoreException(
						new Status(
								Status.ERROR,
								Activator.getDefault().getBundle().getSymbolicName(),
								0,
								NLS.bind(Messages.StartCompiler_OSCompilerError, retCode), 
								null
				));
			}
		} catch (InterruptedException e) {
			Debug.POLICY.error(Debug.DEBUG_COMPILER, "Compiler was interrupted: {0}", e.getMessage()); //$NON-NLS-1$
			// TODO check this exception
			throw new CoreException(
					new Status(
							Status.ERROR, 
							Activator.getDefault().getBundle().getSymbolicName(), 
							0, 
							Messages.StartCompiler_Timeout,
							e
			));
		}
		Debug.POLICY.trace(Debug.DEBUG_COMPILER, "Finished compiling."); //$NON-NLS-1$
	}

	/**
	 * Start the execution and blocks until the end. Also connects console to
	 * process if available.
	 * 
	 * @throws CoreException
	 * 
	 * @see SPUTimingExecution#startCompiler() for more info
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void startSPUTimingTool() throws CoreException {	
		Debug.POLICY.trace(Debug.DEBUG_SPUTIMING, "Start sputiming."); //$NON-NLS-1$
		Debug.POLICY.trace(Debug.DEBUG_SPUTIMING, "Parameters: {0}", compilerParameters.toString()); //$NON-NLS-1$
		
		/* 
		 * Create array with command line.
		 */
		List<String> l = new ArrayList<String>();
		l.add(spuTimingParameters.getSputimingPath());
		if (spuTimingParameters.getParameters() != null) {
			ArgumentParser argumentParser = new ArgumentParser(spuTimingParameters.getParameters());
			l.addAll(argumentParser.getTokenList());
		}
		l.add(spuTimingParameters.getInputFile());
		String[] commandLine = l.toArray(new String[l.size()]);
		Debug.POLICY.trace(Debug.DEBUG_SPUTIMING, "Command line: {0}", (new ArgumentParser(commandLine)).getCommandLine(true)); //$NON-NLS-1$
		
		/*
		 * Execute sputiming using Runtime class, passing the sputiming parameters
		 */
		Process procsputiming = null;
		try {
			OutputStream os = compilerParameters.getConsole().newOutputStream();
			OutputStreamWriter w = new OutputStreamWriter(os);
			w.write((new ArgumentParser(commandLine)).getCommandLine(true)+"\n"); //$NON-NLS-1$
			w.close();

			procsputiming = Runtime.getRuntime().exec(commandLine, null, spuTimingParameters.getWorkingDirectory());
			Debug.POLICY.trace(Debug.DEBUG_SPUTIMING, "sputiming is running"); //$NON-NLS-1$
		} catch (Exception e) {
			Debug.POLICY.error(Debug.DEBUG_SPUTIMING, "Failed to execute sputiming: {0}", e.getMessage()); //$NON-NLS-1$			
			// TODO check this exception
			throw new CoreException(
					new Status(
							Status.ERROR,
							Activator.getDefault().getBundle()
									.getSymbolicName(),
							0,
							Messages.StartSPUTiming_SPUTimingExecError, e
			));
		}

		if (spuTimingParameters.getConsole() != null) {
			connectConsoleToProcess(spuTimingParameters.getConsole(), procsputiming);
			Debug.POLICY.trace(Debug.DEBUG_SPUTIMING, "Connected to console"); //$NON-NLS-1$
		}
		
		// Uses ProcessController to manage timeout
		ProcessController pControl = new ProcessController(
				Messages.StartSPUTiming_ProcessLabel, procsputiming,
				EXEC_TIMEOUT);
		pControl.run();
		Debug.POLICY.trace(Debug.DEBUG_SPUTIMING, "Created process controller"); //$NON-NLS-1$
		
		try {
			int retCode = procsputiming.waitFor();
			if (retCode == 0) {
				Debug.POLICY.trace(Debug.DEBUG_SPUTIMING, "sputiming finished successfully"); //$NON-NLS-1$
			} else {
				Debug.POLICY.error(Debug.DEBUG_SPUTIMING, "sputiming finished with exit code {0}", retCode); //$NON-NLS-1$
				// TODO check this exception
				throw new CoreException(
						new Status(
								Status.ERROR,
								Activator.getDefault().getBundle().getSymbolicName(),
								0,
								NLS.bind(Messages.StartSPUTiming_OSSPUTimingError, retCode), null
				));
			}
		} catch (InterruptedException e) {
			Debug.POLICY.error(Debug.DEBUG_SPUTIMING, "sputiming was interrupted: {0}", e.getMessage()); //$NON-NLS-1$
			throw new CoreException(
					// TODO check this exception
					new Status(
							Status.ERROR, 
							Activator.getDefault().getBundle().getSymbolicName(), 
							0, 
							Messages.StartSPUTiming_Timeout, 
							e
			));
		}
	}

	/**
	 * Attaches the {@link Process} output to the given {@link IOConsole}.
	 * 
	 * @param console
	 * @param process
	 * @author Richard Maciel
	 * @since 1.0
	 */
	public void connectConsoleToProcess(IOConsole console, Process process) {
		OutputStream consoleoutputstream = console.newOutputStream();

		// Use Text stream bridge to manage the application output.
		StreamBridge bridgeStdOutput = new StreamBridge(process
				.getInputStream(), consoleoutputstream, "Output"); //$NON-NLS-1$
		StreamBridge bridgeStdError = new StreamBridge(
				process.getErrorStream(), consoleoutputstream, "Error"); //$NON-NLS-1$
		
		// Never forget to start the bridges.
		bridgeStdOutput.run();
		bridgeStdError.run();
	}
}
