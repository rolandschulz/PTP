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
package org.eclipse.ptp.internal.core.elements;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.listeners.IQueueJobListener;
import org.eclipse.ptp.core.elements.listeners.IQueueListener;

public class PQueue extends Parent implements IPQueueControl {
	private final ListenerList elementListeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();

	public PQueue(String id, IResourceManagerControl rm, IAttribute[] attrs) {
		super(id, rm, P_QUEUE, attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#addChildListener(org.eclipse.ptp.core.elements.listeners.IQueueJobListener)
	 */
	public void addChildListener(IQueueJobListener listener) {
		childListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#addElementListener(org.eclipse.ptp.core.elements.listeners.IQueueListener)
	 */
	public void addElementListener(IQueueListener listener) {
		elementListeners.add(listener);
	}

	public void addJob(IPJobControl job) {
		if (job != null) {
			addChild(job);
		}
	}

	public IPJob getJobById(String job_id) {
		return getJobControl(job_id);
	}

	public IPJobControl getJobControl(String job_id) {
		IPElementControl element = findChild(job_id);
		if (element != null)
			return (IPJobControl) element;
		return null;
	}
	
	public IPJobControl[] getJobControls() {
		return (IPJobControl[]) getCollection().toArray(new IPJobControl[size()]);
	}

	public IPJob[] getJobs() {
		return getJobControls();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPMachine#getResourceManager()
	 */
	public IResourceManager getResourceManager() {
		return (IResourceManager) getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#removeChildListener(org.eclipse.ptp.core.elements.listeners.IQueueJobListener)
	 */
	public void removeChildListener(IQueueJobListener listener) {
		childListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#removeElementListener(org.eclipse.ptp.core.elements.listeners.IQueueListener)
	 */
	public void removeElementListener(IQueueListener listener) {
		elementListeners.remove(listener);
	}

	public void removeJob(IPJobControl job) {
		if (job != null) {
			job.removeAllProcesses();
			removeChild(job);
		}
	}
}
