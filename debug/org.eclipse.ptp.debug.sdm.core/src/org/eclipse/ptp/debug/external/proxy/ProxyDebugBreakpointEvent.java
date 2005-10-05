package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugBreakpointEvent extends AbstractProxyEvent implements IProxyEvent {
	private ICDIBreakpoint	bpt;
	private int				bpId;
	
	public ProxyDebugBreakpointEvent(BitList set, int type, int id, ICDIBreakpoint bpt) {
		super(type, set);
		this.bpId = id;
		this.bpt = bpt;
	}
	
	public ICDIBreakpoint getBreakpoint() {
		return this.bpt;
	}
	
	public int getBreakpointId() {
		return this.bpId;
	}
	
	public String toString() {
		if (this.getEventID() == EVENT_DBG_BPHIT)
			return "EVENT_DBG_BPHIT " + this.getBitSet().toString();
		else
			return "EVENT_DBG_BPSET " + this.getBitSet().toString();
	}
}
