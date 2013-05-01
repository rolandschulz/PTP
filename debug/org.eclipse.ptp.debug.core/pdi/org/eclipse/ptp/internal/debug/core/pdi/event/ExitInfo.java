package org.eclipse.ptp.internal.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.event.IPDIExitInfo;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public class ExitInfo extends SessionObject implements IPDIExitInfo {
	private int code;

	public ExitInfo(IPDISession session, TaskSet tasks, int code) {
		super(session, tasks);
		this.code = code;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExitInfo#getCode()
	 */
	public int getCode() {
		return code;
	}
}

