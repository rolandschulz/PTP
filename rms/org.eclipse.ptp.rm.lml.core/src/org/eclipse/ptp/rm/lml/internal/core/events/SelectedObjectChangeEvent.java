package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;

public class SelectedObjectChangeEvent implements ISelectedObjectChangeEvent {
	
	private String oid;
	
	public SelectedObjectChangeEvent(String oid) {
		this.oid = oid;
	}

	@Override
	public String getOid() {
		return oid;
	}

}
