package org.eclipse.ptp.internal.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.event.IPDIConnectedEvent;

/**
 * @author clement
 *
 */
public class ConnectedEvent extends AbstractEvent implements IPDIConnectedEvent {
	public ConnectedEvent(IPDISessionObject reason, TaskSet tasks) {
		super(reason.getSession(), tasks);
	}
}
