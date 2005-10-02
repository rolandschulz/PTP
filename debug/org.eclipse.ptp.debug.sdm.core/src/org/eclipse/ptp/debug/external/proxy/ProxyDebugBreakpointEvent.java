package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.ptp.core.proxy.FastBitSet;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;

public class ProxyDebugBreakpointEvent extends AbstractProxyEvent implements IProxyEvent {
	private ICDIBreakpoint	bpt;
	
	public ProxyDebugBreakpointEvent(FastBitSet set, int type, ICDIBreakpoint bpt) {
		super(type, set);
		this.bpt = bpt;
	}
	
	public ICDIBreakpoint getBreakpoint() {
		return this.bpt;
	}
	
	public String toString() {
		if (this.getEventID() == EVENT_DBG_BPHIT)
			return "EVENT_DBG_BPHIT " + this.getBitSet().toString();
		else
			return "EVENT_DBG_BPSET " + this.getBitSet().toString();
	}
}
