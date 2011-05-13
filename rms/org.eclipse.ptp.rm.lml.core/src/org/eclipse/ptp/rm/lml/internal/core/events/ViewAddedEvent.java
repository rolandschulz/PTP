package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.IViewAddedEvent;

public class ViewAddedEvent implements IViewAddedEvent {
	
	private String gid;
	private String type;
	
	public ViewAddedEvent(String gid, String type) {
		this.gid = gid;
		this.type = type;
	}

	@Override
	public String getGid() {
		return gid;
	}

	@Override
	public String getType() {
		return type;
	}

}
