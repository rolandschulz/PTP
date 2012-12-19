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
package org.eclipse.ptp.core.jobs;

import java.util.BitSet;

/**
 * Extends IJobStatus for parallel jobs.
 * 
 * @since 7.0
 */
public interface IPJobStatus extends IJobStatus {
	public static String COMPLETED = "COMPLETED"; //$NON-NLS-1$
	public static String RUNNING = "RUNNING"; //$NON-NLS-1$
	public static String SUSPENDED = "SUSPENDED"; //$NON-NLS-1$
	public static String UNDETERMINED = "UNDETERMINED"; //$NON-NLS-1$

	/**
	 * @return
	 */
	public int getNumberOfProcesses();

	/**
	 * @param proc
	 * @return
	 */
	public String getProcessState(int proc);

	/**
	 * @param procs
	 * @param output
	 */
	public void setProcessOutput(BitSet procs, String output);

	/**
	 * @param procs
	 * @param state
	 */
	public void setProcessState(BitSet procs, String state);
}
