package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;

public class MarkObjectEvent implements IMarkObjectEvent {

	private String oid;
	
	public MarkObjectEvent(String oid) {
		this.oid = oid;
	}

	@Override
	public String getOid() {
		return oid;
	}

}
