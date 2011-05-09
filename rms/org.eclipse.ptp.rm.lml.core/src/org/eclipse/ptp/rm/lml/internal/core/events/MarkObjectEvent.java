package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;

public class MarkObjectEvent implements IMarkObjectEvent {

	private final String oid;

	public MarkObjectEvent(String oid) {
		this.oid = oid;
	}

	public String getOid() {
		return oid;
	}

}
