package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.proxy.FastBitSet;

public class ProxyOKEvent extends AbstractProxyEvent implements IProxyEvent {
	
	public ProxyOKEvent(FastBitSet set) {
		super(EVENT_OK, set);
	}
	
	public String toString() {
		return "EVENT_OK " + this.getBitSet().toString();
	}
}
