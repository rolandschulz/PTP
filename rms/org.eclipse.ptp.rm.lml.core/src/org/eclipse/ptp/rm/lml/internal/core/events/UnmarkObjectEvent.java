package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;

public class UnmarkObjectEvent implements IUnmarkObjectEvent {

	private String oid;
	
	public UnmarkObjectEvent(String oid) {
		this.oid = oid;
	}

	@Override
	public String getOid() {
		return oid;
	}

}
