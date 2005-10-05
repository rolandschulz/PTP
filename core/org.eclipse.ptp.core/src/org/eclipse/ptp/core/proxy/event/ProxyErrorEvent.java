package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.util.BitList;

public class ProxyErrorEvent extends AbstractProxyEvent implements IProxyEvent {
	public static final int EVENT_ERR_EVENT = 11;
	
	private int		err_code;
	private String	err_msg;
	
	public ProxyErrorEvent(BitList set, int err_code, String err_msg) {
		super(EVENT_ERROR, set);
		this.err_code = err_code;
		this.err_msg = err_msg;
	}
	
	public int getErrorCode() {
		return err_code;
	}
	
	public String getErrorMessage() {
		return err_msg;
	}
	
	public String toString() {
		return "EVENT_ERROR " + this.getBitSet().toString() + " " + this.err_code + " " + this.err_msg;
	}
}
