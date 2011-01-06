/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.core.rm;

import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @since 5.0
 */
public interface IJobTemplate {
	public enum SubmissionState {
		/**
		 * Job submission state allowing job to be queued but not run
		 */
		HOLD_STATE,

		/**
		 * Job submission state allowing job to be run
		 */
		ACTIVE_STATE
	};

	/**
	 * Get the arguments to be passed to the command
	 * 
	 * @return list of arguments
	 */
	public List<String> getArguments();

	/**
	 * Get the command to be launched. This can be an absolute or relative path.
	 * If path is relative, it is evaluated in the context of the working
	 * directory.
	 * 
	 * @return command to be launched
	 */
	public String getCommand();

	/**
	 * Get the arguments to be passed to the debugger command. Only valid if
	 * {@link isDebug()} is true.
	 * 
	 * @return list of debugger arguments
	 */
	public List<String> getDebuggerArguments();

	/**
	 * Get the debugger command. This must be an absolute path to the debugger
	 * command executable.Only valid if {@link isDebug()} is true.
	 * 
	 * @return
	 */
	public String getDebuggerCommand();

	/**
	 * Get any environment variables to be used for the launch. If
	 * {@link isAppendEnvironment} is true, the variables will be appended to
	 * the native environment, otherwise the native environment will be
	 * replaced.
	 * 
	 * @return map containing environment variables
	 */
	public Map<String, String> getEnvironment();

	/**
	 * Get the name of the job
	 * 
	 * @return name of the job
	 */
	public String getJobName();

	/**
	 * Get the job submission state
	 * 
	 * @return job submission state
	 */
	public SubmissionState getJobSubmissionState();

	/**
	 * Get the launch configuration associated with this job launch. This launch
	 * configuration may contain additional attributes required for the launch.
	 * 
	 * @return launch configuration
	 */
	public ILaunchConfiguration getLaunchConfiguration();

	/**
	 * Get the working directory for the job launch. Must be an absolute path.
	 * 
	 * @return working directory path
	 */
	public String getWorkingDirectory();

	/**
	 * Test if the environment should be appended or not.
	 * 
	 * @return true if the environment should be appended
	 */
	public boolean isAppendEnvironment();

	/**
	 * Check if this is a debug launch.
	 * 
	 * @return true if this is a debug launch
	 */
	public boolean isDebug();
}
