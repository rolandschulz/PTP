package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.ptp.core.proxy.FastBitSet;
import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;

public class ProxyDebugStackframeEvent extends AbstractProxyEvent implements IProxyEvent {
	private ICDIStackFrame[]	frames;
	
	public ProxyDebugStackframeEvent(FastBitSet set, ICDIStackFrame[] frames) {
		super(EVENT_DBG_FRAMES, set);
		this.frames = frames;
	}
	
	public ICDIStackFrame[] getStackframes() {
		return this.frames;
	}
	
	public String toString() {
		return "EVENT_DBG_FRAMES " + this.getBitSet().toString() + " <" + frames.length + " frames>";
	}
}
