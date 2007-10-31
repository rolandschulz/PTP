package org.eclipse.ptp.debug.internal.core.pdi.event;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIEndSteppingRangeInfo;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public class EndSteppingRangeInfo extends SessionObject implements IPDIEndSteppingRangeInfo {
	private IPDILocator locator;
	
	public EndSteppingRangeInfo(Session session, BitList tasks, IPDILocator locator) {
		super(session, tasks);
		this.locator = locator;
	}
	public IPDILocator getLocator() {
		return locator;
	}
}
