/**
 * Copyright (c) 2007 ORNL and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the term of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 
 * @author - Feiyi Wang
 * initial API and implementation
 * 
 */

package org.eclipse.ptp.ui.consoles;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.events.IResourceManagerAddedEvent;
import org.eclipse.ptp.core.events.IResourceManagerChangedEvent;
import org.eclipse.ptp.core.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.events.IResourceManagerRemovedEvent;
import org.eclipse.ptp.core.listeners.IResourceManagerListener;

public class ConsoleManager implements IResourceManagerListener, IResourceManagerChildListener {

	private IModelManager imm = null;
	private final Map<IPJob, JobConsole> consoles = new HashMap<IPJob, JobConsole>();

	public ConsoleManager() {
		imm = PTPCorePlugin.getDefault().getModelManager();
		imm.addListener(this);
		for (IPResourceManager rm : imm.getUniverse().getResourceManagers()) {
			rm.addChildListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
	 * #handleEvent(org.eclipse.ptp.core.elements.events.IChangedMachineEvent)
	 */
	public void handleEvent(IChangedMachineEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
	 * #handleEvent(org.eclipse.ptp.core.elements.events.IChangedQueueEvent)
	 */
	public void handleEvent(IChangedQueueEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.INewJobEvent)
	 */
	public void handleEvent(INewJobEvent e) {
		for (IPJob job : e.getJobs()) {
			ILaunchConfiguration configuration = job.getLaunchConfiguration();
			try {
				if (configuration != null && configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONSOLE, false)) {
					JobConsole jc = new JobConsole(job);
					job.addChildListener(jc);
					synchronized (consoles) {
						consoles.put(job, jc);
					}
				}
			} catch (CoreException e1) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
	 * #handleEvent(org.eclipse.ptp.core.elements.events.INewMachineEvent)
	 */
	public void handleEvent(INewMachineEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
	 * #handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
	 */
	public void handleEvent(INewQueueEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
	 */
	public void handleEvent(IRemoveJobEvent e) {
		for (IPJob job : e.getJobs()) {
			removeConsole(job);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
	 * #handleEvent(org.eclipse.ptp.core.elements.events.IRemoveMachineEvent)
	 */
	public void handleEvent(IRemoveMachineEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
	 * #handleEvent(org.eclipse.ptp.core.elements.events.IRemoveQueueEvent)
	 */
	public void handleEvent(IRemoveQueueEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent(org
	 * .eclipse.ptp.core.events.IResourceManagerAddedEvent)
	 */
	public void handleEvent(IResourceManagerAddedEvent e) {
		IPResourceManager rm = (IPResourceManager) e.getResourceManager().getAdapter(IPResourceManager.class);
		rm.addChildListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent(org
	 * .eclipse.ptp.core.events.IResourceManagerChangedEvent)
	 */
	public void handleEvent(IResourceManagerChangedEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent(org
	 * .eclipse.ptp.core.events.IResourceManagerErrorEvent)
	 */
	public void handleEvent(IResourceManagerErrorEvent e) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent(org
	 * .eclipse.ptp.core.events.IResourceManagerRemovedEvent)
	 */
	public void handleEvent(IResourceManagerRemovedEvent e) {
		IPResourceManager rm = (IPResourceManager) e.getResourceManager().getAdapter(IPResourceManager.class);
		rm.removeChildListener(this);
	}

	/**
	 * Shut down the console manager. This removes any job consoles that have
	 * been created and all listeners.
	 */
	public void shutdown() {
		imm.removeListener(this);
		for (IPResourceManager rm : imm.getUniverse().getResourceManagers()) {
			rm.removeChildListener(this);
			for (IPJob job : rm.getJobs()) {
				removeConsole(job);
			}
		}
	}

	/**
	 * Convenience method to remove the console associated with a job.
	 * 
	 * @param job
	 *            job that has an associated console
	 */
	private void removeConsole(IPJob job) {
		synchronized (consoles) {
			JobConsole jc = consoles.get(job);
			if (jc != null) {
				jc.removeConsole();
				job.removeChildListener(jc);
				consoles.remove(jc);
			}
		}
	}
}
