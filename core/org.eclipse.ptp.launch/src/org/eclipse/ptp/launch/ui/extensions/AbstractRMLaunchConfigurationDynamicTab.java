/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.launch.ui.extensions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.PTPLaunchPlugin;


/**
 * Abstract class that is the extension point for contributing
 * ConfigurationWizardPages to this plug-in.
 * 
 * @author rsqrd
 *
 */
public abstract class AbstractRMLaunchConfigurationDynamicTab implements IRMLaunchConfigurationDynamicTab {
	public static final String EMPTY_STRING = "";
	
	private final Map<Integer, IPQueue> queues = new HashMap<Integer, IPQueue>();
	private final Map<IPQueue, Integer> queueIndices = new HashMap<IPQueue, Integer>();

	/**
	 * @param string
	 * @return
	 */
	protected static CoreException makeCoreException(String string) {
		IStatus status = new Status(Status.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
				Status.ERROR, string, null);
		return new CoreException(status);
	}
	
	private final ListenerList listenerList = new ListenerList();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#addContentsChangedListener(org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener)
	 */
	public void addContentsChangedListener(IRMLaunchConfigurationContentsChangedListener listener) {
		listenerList.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#removeContentsChangedListener(org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener)
	 */
	public void removeContentsChangedListener(IRMLaunchConfigurationContentsChangedListener listener) {
		listenerList.remove(listener);
	}

	/**
	 * This should be called when GUI elements are modified by the user,
	 * e.g. a Text widget should have its ModifyListener's
	 * modifyText method set up to notify all of the contents
	 * changed listeners.
	 */
	protected void fireContentsChanged() {
		Object[] listeners = listenerList.getListeners();
		for (Object listener : listeners) {
			((IRMLaunchConfigurationContentsChangedListener) listener).handleContentsChanged(this);
		}
	}

	/**
	 * Get the queue with the corresponding name
	 * 
	 * @param rm resource manager
	 * @param queueName queue name
	 * @return queue
	 */
	public IPQueue getQueueFromName(IResourceManager rm, String queueName) {
		if (rm == null) {
			return null;
		}
		
		IPQueue[] queues = rm.getQueues();
		
		for (IPQueue queue : queues) {
			if (queue.getName().equals(queueName))
				return queue;
		}
		return null;
	}
	
	/**
	 * 
	 */
	public void clearQueues() {
		queues.clear();
	}
	
	/**
	 * @param queue
	 * @param index
	 */
	public void addQueue(IPQueue queue, int index) {
		queues.put(index, queue);
		queueIndices.put(queue, index);
	}
	
	/**
	 * @param index
	 * @return
	 */
	public IPQueue getQueue(int index) {
		return queues.get(index);
	}
	
	/**
	 * @param queue
	 * @return
	 */
	public Integer getQueueIndex(IPQueue queue) {
		return queueIndices.get(queue);
	}
}
