package org.eclipse.ptp.debug.internal.core.pdi.event;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.SessionObject;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEndSteppingRangeInfo;

/**
 * @author clement
 *
 */
public class EndSteppingRangeInfo extends SessionObject implements IPDIEndSteppingRangeInfo {
	private IPDILocator locator;
	
	public EndSteppingRangeInfo(IPDISession session, BitList tasks, IPDILocator locator) {
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
