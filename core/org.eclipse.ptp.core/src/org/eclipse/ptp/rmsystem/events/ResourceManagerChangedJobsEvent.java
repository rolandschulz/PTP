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
/**
 * 
 */
package org.eclipse.ptp.rmsystem.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IResourceManager;

/**
 * @author rsqrd
 *
 */
public class ResourceManagerChangedJobsEvent implements
		IResourceManagerChangedJobsEvent {

	private final List<IPJob> changedJobs;
	private final IResourceManager rm;
	private final Set<IAttribute> changedAttributes;

	public ResourceManagerChangedJobsEvent(IResourceManager manager,
			Collection<? extends IPJob> jobs,
			Collection<? extends IAttribute> attrs) {
		this.rm = manager;
		this.changedJobs = new ArrayList<IPJob>(jobs);
		this.changedAttributes = new HashSet<IAttribute>(attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.events.IResourceManagerChangedJobsEvent#getChangedJobs()
	 */
	public Collection<IPJob> getChangedJobs() {
		return Collections.unmodifiableList(changedJobs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.events.IResourceManagerChangedJobsEvent#getSource()
	 */
	public IResourceManager getSource() {
		return rm;
	}

	public Collection<IAttribute> getChangedAttributes() {
		return Collections.unmodifiableSet(changedAttributes);
	}

}
