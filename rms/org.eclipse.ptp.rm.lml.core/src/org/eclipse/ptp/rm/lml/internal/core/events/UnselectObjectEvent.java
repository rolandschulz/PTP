package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;

public class UnselectObjectEvent implements IUnselectedObjectEvent {

	private final String oid;

	public UnselectObjectEvent(String oid) {
		this.oid = oid;
	}

	public String getOid() {
		return oid;
	}

}
