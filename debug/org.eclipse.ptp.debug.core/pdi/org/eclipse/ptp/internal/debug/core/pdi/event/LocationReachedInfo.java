package org.eclipse.ptp.internal.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.event.IPDILocationReachedInfo;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;

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
