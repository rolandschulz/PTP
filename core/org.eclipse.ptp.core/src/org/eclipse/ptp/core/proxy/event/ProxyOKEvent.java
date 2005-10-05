package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.util.BitList;

public class ProxyOKEvent extends AbstractProxyEvent implements IProxyEvent {
	
	public ProxyOKEvent(BitList set) {
		super(EVENT_OK, set);
	}
	
	public String toString() {
		return "EVENT_OK " + this.getBitSet().toString();
	}
}
