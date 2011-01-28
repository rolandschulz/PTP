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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes.State;
import org.eclipse.ptp.core.elements.events.IQueueChangeEvent;
import org.eclipse.ptp.core.elements.listeners.IQueueListener;
import org.eclipse.ptp.internal.core.elements.events.QueueChangeEvent;

public class PQueue extends Parent implements IPQueue {
	private final ListenerList fElementListeners = new ListenerList();
	private final Map<String, IPJob> fJobs = new HashMap<String, IPJob>();
	private final IResourceManagerControl fResourceManager;

	public PQueue(String id, IResourceManagerControl rm, IPElement parent, IAttribute<?, ?, ?>[] attrs) {
		super(id, parent, attrs);
		fResourceManager = rm;
		/*
		 * Create required attributes.
		 */
		EnumeratedAttribute<QueueAttributes.State> state = getAttribute(QueueAttributes.getStateAttributeDefinition());
		if (state == null) {
			state = QueueAttributes.getStateAttributeDefinition().create();
			addAttribute(state);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPQueue#addElementListener(org.eclipse.
	 * ptp.core.elements.listeners.IQueueListener)
	 */
	public void addElementListener(IQueueListener listener) {
		fElementListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPQueueControl#addJobs(java.util
	 * .Collection)
	 */
	public void addJobs(Collection<IPJob> jobs) {
		synchronized (fJobs) {
			for (IPJob job : jobs) {
				fJobs.put(job.getID(), job);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java
	 * .util.Map)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
		fireChangedQueue(attrs);
	}

	/**
	 * Send IQueueChangeEvent to registered listeners
	 * 
	 * @param attrs
	 *            attributes that have changed
	 */
	private void fireChangedQueue(AttributeManager attrs) {
		IQueueChangeEvent e = new QueueChangeEvent(this, attrs);

		for (Object listener : fElementListeners.getListeners()) {
			((IQueueListener) listener).handleEvent(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPQueue#getJobs()
	 */
	public IPJob[] getJobs() {
		return fJobs.values().toArray(new IPJob[fJobs.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IPMachine#getResourceManager()
	 */
	public IResourceManagerControl getResourceManager() {
		return fResourceManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPQueue#getState()
	 */
	public State getState() {
		return getAttribute(QueueAttributes.getStateAttributeDefinition()).getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPQueue#removeElementListener(org.eclipse
	 * .ptp.core.elements.listeners.IQueueListener)
	 */
	public void removeElementListener(IQueueListener listener) {
		fElementListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPQueueControl#removeJobs(java.util
	 * .Collection)
	 */
	public void removeJobs(Collection<IPJob> jobs) {
		synchronized (fJobs) {
			for (IPJob job : jobs) {
				fJobs.remove(job);
			}
		}
	}
}
