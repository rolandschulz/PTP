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

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.utils.BitList;

/**
 */
public abstract class AbstractEvent implements IPCDIEvent {
	IPCDISession session;	
	BitList sources;
	ICDIObject[] iCDIObjects;

	public AbstractEvent(IPCDISession s, BitList srcs) {
		session = s;
		sources = srcs;
		
		ArrayList sourceList = new ArrayList();
		
		int[] registeredTargets = session.getRegisteredTargetIds();
		
		for (int j = 0; j < registeredTargets.length; j++) {
			if (sources.get(registeredTargets[j])) {
				ICDIObject src = ((Session) session).getTarget(registeredTargets[j]);
				sourceList.add(src);
			}
			
		}
		
	    iCDIObjects = (ICDIObject[]) sourceList.toArray(new ICDIObject[0]);
	}
	
	public BitList getBitList() {
		return sources;
	}
	
	public int[] getProcesses() {
		int[] retValue = new int[sources.cardinality()];
		for(int i = sources.nextSetBit(0), j = 0; i >= 0; i = sources.nextSetBit(i+1), j++) {
			retValue[j] = i;
		}
		return retValue;
	}
	
	public ICDIObject[] getSources() {
		return iCDIObjects;
	}
	
	public ICDIObject getSource() {
		if (iCDIObjects == null || iCDIObjects.length == 0)
			return null;
		else
			return iCDIObjects[0];
	}
	
	public IPJob getDebugJob() {
		return PTPDebugCorePlugin.getDefault().getDebugJob(session);
	}
}
