package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.proxy.FastBitSet;

public abstract class AbstractProxyEvent implements IProxyEvent {
	private int			eventID;
	private FastBitSet	bitSet;
	
	public AbstractProxyEvent(int id) {
		eventID = id;
		bitSet = null;
	}
	
	public AbstractProxyEvent(int id, FastBitSet set) {
		eventID = id;
		bitSet = set;
	}

	public int getEventID() {
		return eventID;
	}
	
	public FastBitSet getBitSet() {
		return bitSet;
	}
}
