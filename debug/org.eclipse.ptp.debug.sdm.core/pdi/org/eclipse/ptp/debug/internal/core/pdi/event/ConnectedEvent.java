package org.eclipse.ptp.debug.internal.core.pdi.event;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.event.IPDIConnectedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.Session;

/**
 * @author clement
 *
 */
public class ConnectedEvent extends AbstractEvent implements IPDIConnectedEvent {
	public ConnectedEvent(Session session, BitList tasks) {
		super(session, tasks);
	}
}
