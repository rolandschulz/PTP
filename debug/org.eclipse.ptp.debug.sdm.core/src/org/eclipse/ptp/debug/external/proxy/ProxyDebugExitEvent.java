package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.ptp.core.proxy.FastBitSet;
import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;

public class ProxyDebugExitEvent extends AbstractProxyEvent implements IProxyEvent {
	private int		exitStatus;
	
	public ProxyDebugExitEvent(FastBitSet set, int code) {
		super(EVENT_DBG_EXIT, set);
		this.exitStatus = code;
	}
	
	public int getExitStatus() {
		return this.exitStatus;
	}
	
	public String toString() {
		return "EVENT_DBG_EXIT " + this.getBitSet().toString() + " " + this.exitStatus;
	}
}
