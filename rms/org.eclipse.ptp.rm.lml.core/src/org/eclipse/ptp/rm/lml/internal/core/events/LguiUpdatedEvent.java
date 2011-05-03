package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;

public class LguiUpdatedEvent implements ILguiUpdatedEvent{

	private final LguiItem lguiItem;
	
	public LguiUpdatedEvent(LguiItem lguiItem) {
		this.lguiItem = lguiItem;
	}
	
	public LguiItem getLguiItem() {
		return lguiItem;
	}
	
}
