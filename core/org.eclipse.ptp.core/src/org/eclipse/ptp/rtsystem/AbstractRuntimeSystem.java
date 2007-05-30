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
package org.eclipse.ptp.rtsystem;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMessageEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeShutdownStateEvent;

public abstract class AbstractRuntimeSystem implements IRuntimeSystem {
	final private ListenerList	listeners = new ListenerList();

	public void addRuntimeEventListener(IRuntimeEventListener listener) {
		listeners.add(listener);
	}

	public void removeRuntimeEventListener(IRuntimeEventListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getLaunchAttributes(java.lang.String,
	 *  java.lang.String, org.eclipse.ptp.core.attributes.IAttribute[])
	 */
	public IAttribute<?,?,?>[] getLaunchAttributes(String machineName, String queueName,
			IAttribute<?,?,?>[] currentAttrs) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeJob(IPJob job) {
		// TODO Auto-generated method stub

	}
	
	protected void fireRuntimeMessageEvent(IRuntimeMessageEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeMessageEvent(event);
		}
	}
	
	protected void fireRuntimeAttributeDefinitionEvent(IRuntimeAttributeDefinitionEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeAttributeDefinitionEvent(event);
		}
	}
	
	protected void fireRuntimeConnectedStateEvent(IRuntimeConnectedStateEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeConnectedStateEvent(event);
		}
	}

	protected void fireRuntimeJobChangeEvent(IRuntimeJobChangeEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeJobChangeEvent(event);
		}
	}

	protected void fireRuntimeMachineChangeEvent(IRuntimeMachineChangeEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeMachineChangeEvent(event);
		}
	}

	protected void fireRuntimeNodeChangeEvent(IRuntimeNodeChangeEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeNodeChangeEvent(event);
		}
	}

	protected void fireRuntimeProcessChangeEvent(IRuntimeProcessChangeEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeProcessChangeEvent(event);
		}
	}
	
	protected void fireRuntimeQueueChangeEvent(IRuntimeQueueChangeEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeQueueChangeEvent(event);
		}
	}

	protected void fireRuntimeNewJobEvent(IRuntimeNewJobEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeNewJobEvent(event);
		}
	}

	protected void fireRuntimeNewMachineEvent(IRuntimeNewMachineEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeNewMachineEvent(event);
		}
	}

	protected void fireRuntimeNewNodeEvent(IRuntimeNewNodeEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeNewNodeEvent(event);
		}
	}

	protected void fireRuntimeNewProcessEvent(IRuntimeNewProcessEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeNewProcessEvent(event);
		}
	}
	
	protected void fireRuntimeNewQueueEvent(IRuntimeNewQueueEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeNewQueueEvent(event);
		}
	}
	
	protected void fireRuntimeRunningStateEvent(IRuntimeRunningStateEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeRunningStateEvent(event);
		}
	}

	protected void fireRuntimeShutdownStateEvent(IRuntimeShutdownStateEvent event) {
		for (Object listener : listeners.getListeners()) {
			((IRuntimeEventListener)listener).handleRuntimeShutdownStateEvent(event);
		}
	}
}
