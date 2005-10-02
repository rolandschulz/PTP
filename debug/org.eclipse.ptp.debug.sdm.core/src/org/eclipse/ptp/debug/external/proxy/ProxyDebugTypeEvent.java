package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.ptp.core.proxy.FastBitSet;
import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;

public class ProxyDebugTypeEvent extends AbstractProxyEvent implements IProxyEvent {
	private String type;
	
	public ProxyDebugTypeEvent(FastBitSet set, String type) {
		super(EVENT_DBG_TYPE, set);
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String toString() {
		return "EVENT_DBG_TYPE " + this.getBitSet().toString() + " " + this.type;
	}
}
