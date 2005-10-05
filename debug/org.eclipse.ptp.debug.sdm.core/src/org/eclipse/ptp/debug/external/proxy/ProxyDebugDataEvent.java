package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugDataEvent extends AbstractProxyEvent implements IProxyEvent {
	private String format;
	private String data;
	
	public ProxyDebugDataEvent(BitList set, String fmt, String data) {
		super(EVENT_DBG_DATA, set);
		this.format = fmt;
		this.data = data;
	}
	
	public int getData() {
		return 0;
	}
	
	public String toString() {
		return "EVENT_DBG_DATA " + this.getBitSet().toString() + " " + this.format + " " + this.data;
	}
}
