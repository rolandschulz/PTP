package org.eclipse.ptp.internal.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorInfo;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public class ErrorInfo extends SessionObject implements IPDIErrorInfo {
	private int code;
	private String detailMsg;
	private String msg;

	public ErrorInfo(IPDISession session, TaskSet tasks, int code, String msg, String detailMsg) {
		super(session, tasks);
		this.code = code;
		this.msg = msg;
		this.detailMsg = detailMsg;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIErrorInfo#getCode()
	 */
	public int getCode() {
		return code;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIErrorInfo#getDetailMessage()
	 */
	public String getDetailMessage() {
		return detailMsg;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIErrorInfo#getMessage()
	 */
	public String getMessage() {
		return msg;
	}
}

