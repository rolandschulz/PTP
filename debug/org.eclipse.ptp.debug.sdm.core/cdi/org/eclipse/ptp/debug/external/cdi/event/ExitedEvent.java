/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi.event;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.event.DebugEvent;
import org.eclipse.ptp.debug.external.event.EExit;

/**
 */
public class ExitedEvent implements ICDIExitedEvent {
	Session session;
	ICDIObject[] sources;
	DebugEvent event;

	public ExitedEvent(Session s, EExit ev) {
		session = s;
		event = ev;
		
		Hashtable table = ev.getSources();
		ArrayList sourceList = new ArrayList();
		
	    Iterator it = table.keySet().iterator();
	    while (it.hasNext()) {
	       Integer targetId =  (Integer) it.next();
	       int[] threads = (int[]) table.get(targetId);
	       
	       if (threads.length == 0) {
    		   ICDIObject src = session.getTarget(targetId.intValue());
    		   sourceList.add(src);
	       }
       
	       for (int i = 0; i < threads.length; i++) {
	    	   try {
	    		   ICDIObject src = ((Target) session.getTarget(targetId.intValue())).getThread(threads[i]);
	    		   sourceList.add(src);
	    	   } catch (CDIException e) {
	    	   }
	       }
	    }
	    sources = (ICDIObject[]) sourceList.toArray(new ICDIObject[0]);
	}
	
	public ICDISessionObject getReason() {
		// Auto-generated method stub
		System.out.println("ExitedEvent.getReason()");
		return null;
	}

	public ICDIObject getSource() {
		// Auto-generated method stub
		System.out.println("CreatedEvent.getSource()");
		return sources[0];
	}
	
	public ICDIObject[] getSources() {
		return sources;
	}
}
