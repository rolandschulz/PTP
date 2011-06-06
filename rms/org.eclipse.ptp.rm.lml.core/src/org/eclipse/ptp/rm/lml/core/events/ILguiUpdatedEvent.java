package org.eclipse.ptp.rm.lml.core.events;

import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;

public interface ILguiUpdatedEvent {
	public LguiType getLgui();

	public LguiItem getLguiItem();
}