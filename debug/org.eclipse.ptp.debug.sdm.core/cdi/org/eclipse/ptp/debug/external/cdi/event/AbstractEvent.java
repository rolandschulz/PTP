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
import java.util.BitSet;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.event.DebugEvent;

/**
 */
public abstract class AbstractEvent implements IPCDIEvent {
	Session session;	
	BitSet sources;
	DebugEvent event;
	ICDIObject[] iCDIObjects;

	public AbstractEvent(Session s, DebugEvent ev) {
		session = s;
		event = ev;
		sources = ev.getSources();
		
		ArrayList sourceList = new ArrayList();
		
		int[] registeredTargets = session.getRegisteredTargetIds();
		
		for (int j = 0; j < registeredTargets.length; j++) {
			if (sources.get(registeredTargets[j])) {
				ICDIObject src = session.getTarget(registeredTargets[j]);
				sourceList.add(src);
			}
			
		}
		
	    iCDIObjects = (ICDIObject[]) sourceList.toArray(new ICDIObject[0]);
	}
	
	public BitSet getBitSet() {
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
}
