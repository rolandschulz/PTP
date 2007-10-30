/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.internal.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;

/**
 * A process that behaves as it were a real running process.
 * <p>
 * Used to test and understand Eclipse Debugger and Launch facilities.
 * @author Daniel Felix Ferber
 */
public abstract class AbstractProcess extends PlatformObject implements IProcess {
	
	protected String label = Messages.AbstractProcess_DefaultLabel;
	static int counter;
	
	/**
	 * The launch this process is contained in
	 */
	protected ILaunch launch;
	
	/**
	 * Table of client defined attributes
	 */
	protected Map attributes;
		
	/**
	 * Whether output from the process should be captured or swallowed
	 */
	protected boolean captureOutput = true;
	
	public AbstractProcess(ILaunch launch, String label) {
		super();
		this.launch = launch;
		
		if (label == null) {
			counter++;
			this.label = "bogus "+Integer.toString(counter); //$NON-NLS-1$
		} else {
			this.label = label;
		}
		
		String captureOutputValue = launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT);
		captureOutput = !("false".equals(captureOutputValue)); //$NON-NLS-1$
	}
	
	public void start() {
		launch.addProcess(this);
		fireCreationEvent();
		fireChangeEvent();
	}
	
	public String getLabel() {
		return label;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	public void setAttribute(String key, String value) {
		if (attributes == null) {
			attributes = new HashMap(5);
		}
		Object origVal = attributes.get(key);
		if (origVal != null && origVal.equals(value)) {
			return; //nothing changed.
		}
		
		attributes.put(key, value);
		fireChangeEvent();
	}

	public String getAttribute(String key) {
		if (attributes == null) {
			return null;
		}
		return (String)attributes.get(key);
	}
	
	/**
	 * Copy the attributes of this process to those in the given map.
	 * 
	 * @param attributes attribute map or <code>null</code> if none
	 */
	public void copyAttributes(Map attributes) {
		if (attributes != null) {
			Iterator keys = attributes.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String)keys.next();
				setAttribute(key, (String)attributes.get(key));
			}	
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IProcess.class)) {
			return this;
		}
		if (adapter.equals(IDebugTarget.class)) {
			ILaunch launch = getLaunch();
			IDebugTarget[] targets = launch.getDebugTargets();
			for (int i = 0; i < targets.length; i++) {
				if (this.equals(targets[i].getProcess())) {
					return targets[i];
				}
			}
			return null;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Fires a creation event.
	 */
	protected void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/**
	 * Fires the given debug event.
	 * 
	 * @param event debug event to fire
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[]{event});
		}
	}

	/**
	 * Fires a terminate event.
	 */
	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/**
	 * Fires a change event.
	 */	
	protected void fireChangeEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
	}
}
