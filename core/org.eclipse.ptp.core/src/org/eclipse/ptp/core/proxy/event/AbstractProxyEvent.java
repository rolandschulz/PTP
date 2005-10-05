package org.eclipse.ptp.core.proxy.event;

import org.eclipse.ptp.core.util.BitList;

public abstract class AbstractProxyEvent implements IProxyEvent {
	private int			eventID;
	private BitList	bitSet;
	
	public AbstractProxyEvent(int id) {
		eventID = id;
		bitSet = null;
	}
	
	public AbstractProxyEvent(int id, BitList set) {
		eventID = id;
		bitSet = set;
	}

	public int getEventID() {
		return eventID;
	}
	
	public BitList getBitSet() {
		return bitSet;
	}
}
