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
/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.ptp.debug.external.cdi.event;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.core.utils.BitList;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcessSet;

/**
 */
public abstract class AbstractEvent implements IPCDIEvent {
	IPCDISession session;	
	IPCDIDebugProcessSet sources;
	ICDIObject src; /* For compatibility with CDI/CDT */

	public AbstractEvent(IPCDISession s, IPCDIDebugProcessSet srcs) {
		session = s;
		sources = srcs;
	}
	
	public IPJob getDebugJob() {
		return PTPDebugCorePlugin.getDefault().getDebugJob(session);
	}

	public IPCDIDebugProcessSet getAllProcesses() {
		return sources;
	}

	public IPCDIDebugProcessSet getAllUnregisteredProcesses() {
		IPCDIDebugProcessSet retVal = new DebugProcessSet((DebugProcessSet) sources);
		
		int[] registeredTargets = session.getRegisteredTargetIds();
		BitList bitList = retVal.toBitList();
		
		for (int i = 0; i < registeredTargets.length; i++) {
			if (bitList.get(registeredTargets[i])) {
				retVal.removeProcess(registeredTargets[i]);
			}
		}
		
		return retVal;
	}

	public IPCDIDebugProcessSet getAllRegisteredProcesses() {
		IPCDIDebugProcessSet retVal = new DebugProcessSet((Session) session);
		
		int[] registeredTargets = session.getRegisteredTargetIds();
		BitList bitList = sources.toBitList();
		
		for (int i = 0; i < registeredTargets.length; i++) {
			if (bitList.get(registeredTargets[i])) {
				retVal.addProcess(registeredTargets[i]);
			}
		}
		
		return retVal;
	}
	
	public ICDIObject getSource() {
		// Auto-generated method stub
		System.out.println("AbstractEvent.getSource()");
		if (src == null) {
			int[] registeredTargets = session.getRegisteredTargetIds();
			BitList bitList = sources.toBitList();
			
			for (int i = 0; i < registeredTargets.length; i++) {
				if (bitList.get(registeredTargets[i])) {
					src = session.getTarget(registeredTargets[i]);
					break;
				}
			}
		}
		return src;
	}
	
	public boolean isForProcess(int procNumber) {
		return sources.toBitList().get(procNumber);
	}
}
