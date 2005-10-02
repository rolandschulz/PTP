package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.ptp.core.proxy.FastBitSet;
import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;

public class ProxyDebugVarsEvent extends AbstractProxyEvent implements IProxyEvent {
	private String[] vars;
	
	public ProxyDebugVarsEvent(FastBitSet set, String[] vars) {
		super(EVENT_DBG_INIT, set);
		this.vars = vars;
	}
	
	public String[] getVariables() {
		return this.vars;
	}
	
	public String toString() {
		return "EVENT_DBG_VARS " + this.getBitSet().toString() + " <" + this.vars.length + " vars>";
	}
}
