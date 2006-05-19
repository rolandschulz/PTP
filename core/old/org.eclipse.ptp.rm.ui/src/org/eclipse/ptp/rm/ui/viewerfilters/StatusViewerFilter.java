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
package org.eclipse.ptp.rm.ui.viewerfilters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.internal.rm.ui.views.IRMElementsProvider;
import org.eclipse.ptp.internal.rm.ui.views.IStatusDisplayProvider;
import org.eclipse.ptp.rm.core.IRMElement;

public class StatusViewerFilter extends ViewerFilter {

	private static final IStatusDisplayProvider[] NO_STATUS_DISPLAY_PROVIDERS = new IStatusDisplayProvider[0];

	// The factored out provider of IRMElement's
	private final IRMElementsProvider elementsProvider;

	private IStatusDisplayProvider[] filteredStatuses = NO_STATUS_DISPLAY_PROVIDERS;
	

	public StatusViewerFilter(IRMElementsProvider elementsProvider) {
		super();
		this.elementsProvider = elementsProvider;
	}

	public IStatusDisplayProvider[] getFilteredStatuses() {
		return filteredStatuses;
	}

	public boolean isFilterProperty(Object element, String property) {
		return IStatusDisplayProvider.STATUS_CHANGED_PROPERTY.equals(property);
	}

	public void reset() {
		setFilteredStatus(null);
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (filteredStatuses == null) {
			return true;
		}
		final IStatusDisplayProvider status = elementsProvider.getStatus((IRMElement) element);
		for (int i=0; i<filteredStatuses.length; ++i) {
			final int compareTo = filteredStatuses[i].compareTo(status);
			if (compareTo == 0)
				return true;
		}
		return false;
	}

	public void setFilteredStatus(IStatusDisplayProvider newFilteredStatus) {
		if (newFilteredStatus == null) {
			filteredStatuses = NO_STATUS_DISPLAY_PROVIDERS;
		} else {
			filteredStatuses = new IStatusDisplayProvider[]{newFilteredStatus};
		}
	}

	public void setFilteredStatuses(IStatusDisplayProvider[] newFilteredStatuses) {
		if (newFilteredStatuses == null) {
			filteredStatuses = NO_STATUS_DISPLAY_PROVIDERS;
		} else {
			filteredStatuses = (IStatusDisplayProvider[]) newFilteredStatuses.clone();
		}
	}

}
