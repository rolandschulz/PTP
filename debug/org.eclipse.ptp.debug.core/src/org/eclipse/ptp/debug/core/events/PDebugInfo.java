/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.debug.core.events;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;

/**
 * @author Clement
 */
public class PDebugInfo implements IPDebugInfo {
	private BitList allProcesses = null;
	private BitList allRegProcesses = null;
	private BitList allUnregProcesses = null;
	private IPJob job = null;
	
	public PDebugInfo(IPDebugInfo info) {
		this.job = info.getJob();
		this.allProcesses = info.getAllProcesses();
		this.allRegProcesses = info.getAllRegisteredProcesses();
		this.allUnregProcesses = info.getAllUnregisteredProcesses();
	}
	public PDebugInfo(IPJob job, BitList allProcesses, BitList allRegProcesses, BitList allUnregProcesses) {
		this.job = job;
		this.allProcesses = allProcesses;
		this.allRegProcesses = allRegProcesses;
		this.allUnregProcesses = allUnregProcesses;
	}
	public BitList getAllProcesses() {
		return allProcesses;
	}
	public BitList getAllRegisteredProcesses() {
		return allRegProcesses;
	}
	public BitList getAllUnregisteredProcesses() {
		return allUnregProcesses;
	}
	public IPJob getJob() {
		return job;
	}
	
}

