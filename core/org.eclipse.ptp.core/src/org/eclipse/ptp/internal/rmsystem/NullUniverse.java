/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.rmsystem;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPUniverse;

public class NullUniverse implements IPUniverse {

	private static final IPJob[] NO_JOBS = new IPJob[0];
	private static final IPMachine[] NO_MACHINES = new IPMachine[0];
	private static final IPJob NULL_JOB = new NullJob();
	private static final IPNode NULL_NODE = new NullNode();
	private static final IPProcess NULL_PROCESS = new NullProcess();
	private static final IPMachine NULL_MACHINE = new NullMachine();

	public void deleteJob(IPJob job) {
		// no-op
	}

	public IPJob findJobById(String job_id) {
		// no-op
		return NULL_JOB;
	}

	public IPJob findJobByName(String jname) {
		// no-op
		return NULL_JOB;
	}

	public IPMachine findMachineById(String machine_id) {
		// no-op
		return NULL_MACHINE;
	}

	public IPMachine findMachineByName(String mname) {
		// no-op
		return NULL_MACHINE;
	}

	public IPNode findNodeByHostname(String nname) {
		// no-op
		return NULL_NODE;
	}

	public IPNode findNodeByName(String nname) {
		// no-op
		return NULL_NODE;
	}

	public IPProcess findProcessByName(String pname) {
		// no-op
		return NULL_PROCESS;
	}

	public IPJob[] getJobs() {
		// no-op
		return NO_JOBS;
	}

	public IPMachine[] getMachines() {
		// no-op
		return NO_MACHINES;
	}

	public IPJob[] getSortedJobs() {
		// no-op
		return NO_JOBS;
	}

	public IPMachine[] getSortedMachines() {
		// no-op
		return NO_MACHINES;
	}

	public IPMachine findMachineByGlobalId(String machin_id) {
		return NULL_MACHINE;
	}

}
