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
package org.eclipse.ptp.rm.ui.views;

import org.eclipse.ptp.internal.rm.ui.views.AbstractElementsView;
import org.eclipse.ptp.internal.rm.ui.views.AbstractResourceManagerListener;
import org.eclipse.ptp.internal.rm.ui.views.IRMElementsProvider;
import org.eclipse.ptp.internal.rm.ui.views.IStatusDisplayProvider;
import org.eclipse.ptp.internal.rm.ui.views.StatusDisplayProviderFactory;
import org.eclipse.ptp.rm.core.IRMElement;
import org.eclipse.ptp.rm.core.IRMJob;
import org.eclipse.ptp.rm.core.IRMResourceManager;
import org.eclipse.ptp.rm.core.RMJobStatus;
import org.eclipse.ptp.rm.core.attributes.IAttrDesc;
import org.eclipse.ptp.rm.core.events.IRMResourceManagerListener;
import org.eclipse.ptp.rm.core.events.RMJobsChangedEvent;
import org.eclipse.ptp.rm.core.events.RMResourceManagerEvent;

public class JobsView extends AbstractElementsView {

	/**
	 * Factored out class to provide the IRMJob's needed for this view.
	 * 
	 * @author rsqrd
	 * 
	 */
	private static class ElementsProvider implements IRMElementsProvider {

		public IStatusDisplayProvider[] getAllStatuses() {
			return StatusDisplayProviderFactory.getAll((RMJobStatus)null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.internal.rm.ui.views.IRMElementsProvider#getElementAttrDescs(org.eclipse.ptp.rm.core.IRMResourceManager)
		 */
		public IAttrDesc[] getElementAttrDescs(IRMResourceManager manager) {
			return manager.getJobAttrDescs();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.internal.rm.ui.views.IRMElementsProvider#getElements(org.eclipse.ptp.rm.core.IRMResourceManager)
		 */
		public IRMElement[] getElements(IRMResourceManager manager) {
			return manager.getAllJobs();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.internal.rm.ui.views.IRMElementsProvider#getNameFieldName()
		 */
		public String getNameFieldName() {
			return "Job";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.internal.rm.ui.views.IRMElementsProvider#getStatus(org.eclipse.ptp.rm.core.IRMElement)
		 */
		public IStatusDisplayProvider getStatus(IRMElement element) {
			return StatusDisplayProviderFactory.make(((IRMJob) element)
					.getStatus());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.internal.rm.ui.views.IRMElementsProvider#hasStatus()
		 */
		public boolean hasStatus() {
			return true;
		}

	}

	/**
	 * Respond to changes in the Jobs from the resource manager.
	 * 
	 * @author rsqrd
	 * 
	 */
	private final class ResourceManagerListener extends
			AbstractResourceManagerListener {
		public void jobsChanged(RMJobsChangedEvent event) {
			switch (event.getType()) {
			case RMResourceManagerEvent.MODIFIED:
				elementsModified(event.getJobs(), event
						.getModifiedAttributeDescriptions(), event
						.isStatusChanged());
				break;
			case RMResourceManagerEvent.ADDED:
				elementsAdded(event.getJobs());
				break;
			case RMResourceManagerEvent.REMOVED:
				elementsRemoved(event.getJobs());
				break;
			}
		}
	}

	private final AbstractResourceManagerListener listener = new ResourceManagerListener();

	public JobsView() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.ui.views.AbstractElementsView#createElementsProvider()
	 */
	protected IRMElementsProvider createElementsProvider() {
		return new ElementsProvider();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.ui.views.AbstractElementsView#getListener()
	 */
	protected IRMResourceManagerListener getListener() {
		return listener;
	}

}
