package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;

public class LguiUpdatedEvent implements ILguiUpdatedEvent {

	private final LguiItem lguiItem;
	private final LguiType lgui;

	public LguiUpdatedEvent(LguiItem lguiItem, LguiType lgui) {
		this.lguiItem = lguiItem;
		this.lgui = lgui;
	}

	public LguiType getLgui() {
		return lgui;
	}

	public LguiItem getLguiItem() {
		return lguiItem;
	}

}
