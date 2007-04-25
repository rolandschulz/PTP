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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeErrorEvent;
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
	final private List<IRuntimeEventListener>	listeners = Collections.synchronizedList(new ArrayList<IRuntimeEventListener>());

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
	public IAttribute[] getLaunchAttributes(String machineName, String queueName,
			IAttribute[] currentAttrs) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeJob(IPJob job) {
		// TODO Auto-generated method stub

	}
	
	protected void fireRuntimeErrorEvent(IRuntimeErrorEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeErrorEvent(event);
		}
	}
	
	protected void fireRuntimeAttributeDefinitionEvent(IRuntimeAttributeDefinitionEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeAttributeDefinitionEvent(event);
		}
	}
	
	protected void fireRuntimeConnectedStateEvent(IRuntimeConnectedStateEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeConnectedStateEvent(event);
		}
	}

	protected void fireRuntimeJobChangeEvent(IRuntimeJobChangeEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeJobChangeEvent(event);
		}
	}

	protected void fireRuntimeMachineChangeEvent(IRuntimeMachineChangeEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeMachineChangeEvent(event);
		}
	}

	protected void fireRuntimeNodeChangeEvent(IRuntimeNodeChangeEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeNodeChangeEvent(event);
		}
	}

	protected void fireRuntimeProcessChangeEvent(IRuntimeProcessChangeEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeProcessChangeEvent(event);
		}
	}
	
	protected void fireRuntimeQueueChangeEvent(IRuntimeQueueChangeEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeQueueChangeEvent(event);
		}
	}

	protected void fireRuntimeNewJobEvent(IRuntimeNewJobEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeNewJobEvent(event);
		}
	}

	protected void fireRuntimeNewMachineEvent(IRuntimeNewMachineEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeNewMachineEvent(event);
		}
	}

	protected void fireRuntimeNewNodeEvent(IRuntimeNewNodeEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeNewNodeEvent(event);
		}
	}

	protected void fireRuntimeNewProcessEvent(IRuntimeNewProcessEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeNewProcessEvent(event);
		}
	}
	
	protected void fireRuntimeNewQueueEvent(IRuntimeNewQueueEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeNewQueueEvent(event);
		}
	}
	
	protected void fireRuntimeRunningStateEvent(IRuntimeRunningStateEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeRunningStateEvent(event);
		}
	}

	protected void fireRuntimeShutdownStateEvent(IRuntimeShutdownStateEvent event) {
		IRuntimeEventListener[] la = listeners.toArray(new IRuntimeEventListener[0]);
		for (IRuntimeEventListener listener : la) {
			listener.handleRuntimeShutdownStateEvent(event);
		}
	}
}
