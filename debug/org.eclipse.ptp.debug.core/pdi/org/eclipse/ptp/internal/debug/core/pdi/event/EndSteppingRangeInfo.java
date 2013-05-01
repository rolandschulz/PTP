package org.eclipse.ptp.internal.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEndSteppingRangeInfo;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public class EndSteppingRangeInfo extends SessionObject implements IPDIEndSteppingRangeInfo {
	private IPDILocator locator;
	
	public EndSteppingRangeInfo(IPDISession session, TaskSet tasks, IPDILocator locator) {
		super(session, tasks);
		this.locator = locator;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIEndSteppingRangeInfo#getLocator()
	 */
	public IPDILocator getLocator() {
		return locator;
	}
}
