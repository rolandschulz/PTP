package org.eclipse.ptp.debug.internal.core.pdi.event;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIErrorInfo;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public class ErrorInfo extends SessionObject implements IPDIErrorInfo {
	private int code;
	private String detailMsg;
	private String msg;

	public ErrorInfo(Session session, BitList tasks, int code, String msg, String detailMsg) {
		super(session, tasks);
		this.code = code;
		this.msg = msg;
		this.detailMsg = detailMsg;
	}
	public int getCode() {
		return code;
	}
	public String getDetailMessage() {
		return detailMsg;
	}
	public String getMessage() {
		return msg;
	}
}

