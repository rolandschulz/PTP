package org.eclipse.ptp.debug.internal.core.pdi.event;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIExitInfo;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public class ExitInfo extends SessionObject implements IPDIExitInfo {
	private int code;

	public ExitInfo(Session session, BitList tasks, int code) {
		super(session, tasks);
		this.code = code;
	}
	public int getCode() {
		return code;
	}
}

