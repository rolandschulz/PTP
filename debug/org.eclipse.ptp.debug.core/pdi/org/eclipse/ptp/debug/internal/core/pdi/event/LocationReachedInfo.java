package org.eclipse.ptp.debug.internal.core.pdi.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.SessionObject;
import org.eclipse.ptp.debug.core.pdi.event.IPDILocationReachedInfo;

/**
 * @author clement
 *
 */
public class LocationReachedInfo extends SessionObject implements IPDILocationReachedInfo {
	private IPDILocator locator;

	public LocationReachedInfo(IPDISession session, TaskSet tasks, IPDILocator locator) {
		super(session, tasks);
		this.locator = locator;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDILocationReachedInfo#getLocator()
	 */
	public IPDILocator getLocator() {
		return locator;
	}
}
