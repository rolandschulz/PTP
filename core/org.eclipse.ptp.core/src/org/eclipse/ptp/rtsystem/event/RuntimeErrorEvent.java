package org.eclipse.ptp.rtsystem.event;

public class RuntimeErrorEvent implements IRuntimeErrorEvent {
	private String msg;
	private int code;
	public RuntimeErrorEvent(String msg, int code) {
		super();
		this.msg = msg;
		this.code = code;
	}
	public int getCode() {
		return code;
	}
	public String getMessage() {
		return msg;
	}	
}