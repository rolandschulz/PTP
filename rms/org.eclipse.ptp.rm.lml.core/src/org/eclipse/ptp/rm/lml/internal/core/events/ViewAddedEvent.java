package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.IViewAddedEvent;

public class ViewAddedEvent implements IViewAddedEvent {

	private final String gid;
	private final String type;

	public ViewAddedEvent(String gid, String type) {
		this.gid = gid;
		this.type = type;
	}

	public String getGid() {
		return gid;
	}

	public String getType() {
		return type;
	}

}
