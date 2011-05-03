package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;

public class UnselectObjectEvent implements IUnselectedObjectEvent {

	private String oid;
	
	public UnselectObjectEvent(String oid) {
		this.oid = oid;
	}

	@Override
	public String getOid() {
		return oid;
	}

}
