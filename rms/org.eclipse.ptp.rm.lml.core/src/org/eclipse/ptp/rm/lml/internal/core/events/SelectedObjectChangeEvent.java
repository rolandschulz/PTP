package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;

public class SelectedObjectChangeEvent implements ISelectedObjectChangeEvent {

	private final String oid;

	public SelectedObjectChangeEvent(String oid) {
		this.oid = oid;
	}

	public String getOid() {
		return oid;
	}

}
