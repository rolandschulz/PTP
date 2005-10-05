package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugStepEvent extends AbstractProxyEvent implements IProxyEvent {
	private int		threadID;
	
	public ProxyDebugStepEvent(BitList set, int tid) {
		super(EVENT_DBG_STEP, set);
		this.threadID = tid;
	}
	
	public int getThreadID() {
		return this.threadID;
	}
	
	public String toString() {
		return "EVENT_DBG_STEP " + this.getBitSet().toString() + " " + this.threadID;
	}
}
