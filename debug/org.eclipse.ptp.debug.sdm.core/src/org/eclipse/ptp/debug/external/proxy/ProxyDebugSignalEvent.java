package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.ptp.core.proxy.FastBitSet;
import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;

public class ProxyDebugSignalEvent extends AbstractProxyEvent implements IProxyEvent {
	private String	signalName;
	private String	signalMeaning;
	private int		threadID;
	
	public ProxyDebugSignalEvent(FastBitSet set, String name, String meaning, int tid) {
		super(EVENT_DBG_SIGNAL, set);
		this.signalName = name;
		this.signalMeaning = meaning;
		this.threadID = tid;
	}
	
	public String getSignalName() {
		return this.signalName;
	}
	
	public String getSignalMeaning() {
		return this.signalMeaning;
	}
	
	public int getThreadID() {
		return this.threadID;
	}
	
	public String toString() {
		return "EVENT_DBG_SIGNAL " + this.getBitSet().toString() + " " + this.signalName;
	}
}
