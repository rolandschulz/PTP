package org.eclipse.ptp.debug.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;

/**
 * 
 * A base interface for all CDI events.
 * 
 * @since Jul 18, 2002
 */
public interface IPCDIEvent {
	ICDIObject[] getSources();
	int[] getProcesses();
}

