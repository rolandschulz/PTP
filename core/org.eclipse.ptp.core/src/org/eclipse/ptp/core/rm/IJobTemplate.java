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
import java.util.Set;

public interface IJobTemplate {
	/*
	 * Job submission state allowing job to be queued but not run
	 */
	public static final int HOLD_STATE = 0;

	/*
	 * Job submission state allowing job to be run
	 */
	public static final int ACTIVE_STATE = 1;

	public List<String> getArgs();

	public String getAttribute(String name);

	public Set<String> getAttributeNames();

	public boolean getBlockEmail();

	public String getCommand();

	public String getDeadlineTime();

	public Set<String> getEmail();

	public String getErrorPath();

	public String getHardRunDurationLimit();

	public String getHardWallclockTimeLimit();

	public String getInputPath();

	public String getJobCategory();

	public Map<String, String> getJobEnvironment();

	public String getJobName();

	public int getJobSubmissionState();

	public boolean getJoinFiles();

	public String getOutputPath();

	public long getSoftRunDurationLimit();

	public long getSoftWallClockTimeLimit();

	public String getStartTime();

	public String getWorkingDirectory();

	public void setArgs(List<String> args);

	public void setAttribute(String name, String value);

	public void setBlockEmail(boolean blockEmail);

	public void setCommand(String command);

	public void setDeadlineTime(String deadline);

	public void setEmail(Set<String> email);

	public void setErrorPath(String errorPath);

	public void setHardRunDurationLimit(String limit);

	public void setHardWallclockTimeLimit(String limit);

	public void setInputPath(String inputPath);

	public void setJobCategory(String category);

	public void setJobEnvironment(Map<String, String> env);

	public void setJobName(String name);

	public void setJobSubmissionState(int state);

	public void setJoinFiles(boolean joinFiles);

	public void setOutputPath(String outputPath);

	public void setSoftRunDurationLimit(String limit);

	public void setSoftWallClockTimeLimit(String limit);

	public void setStartTime(String startTime);

	public void setWorkingDirectory(String wd);
}
