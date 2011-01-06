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

import java.util.Map;

/**
 * @since 5.0
 */
public class JobInfo {
	private String fJobId = null;
	private Map<String, String> fResourceUsage = null;
	private boolean fHasExited = false;
	private int fExitStatus = 0;
	private boolean fHasSignaled = false;
	private String fTerminateSignal = null;
	private boolean fHasCoreDump = false;
	private boolean fWasAborted = false;

	public int getExitStatus() {
		return fExitStatus;
	}

	public String getJobId() {
		return fJobId;
	}

	public Map<String, String> getResourceUsage() {
		return fResourceUsage;
	}

	public String getTerminatingSignal() {
		return fTerminateSignal;
	}

	public boolean hasCoreDump() {
		return fHasCoreDump;
	}

	public boolean hasExited() {
		return fHasExited;
	}

	public boolean hasSignaled() {
		return fHasSignaled;
	}

	public void setExitStatus(int exitStatus) {
		fExitStatus = exitStatus;
	}

	public void setHasCoreDump(boolean coreDump) {
		fHasCoreDump = coreDump;
	}

	public void setHasExited(boolean exited) {
		fHasExited = exited;
	}

	public void setHasSignaled(boolean signaled) {
		fHasSignaled = signaled;
	}

	public void setJobId(String jobId) {
		fJobId = jobId;
	}

	public void setResourceUsage(Map<String, String> usage) {
		fResourceUsage = usage;
	}

	public void setTerminatingSignal(String signal) {
		fTerminateSignal = signal;
	}

	public void setWasAborted(boolean aborted) {
		fWasAborted = aborted;
	}

	public boolean wasAborted() {
		return fWasAborted;
	}
}
