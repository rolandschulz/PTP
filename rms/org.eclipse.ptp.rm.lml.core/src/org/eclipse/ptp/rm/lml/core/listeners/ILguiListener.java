package org.eclipse.ptp.rm.lml.core.listeners;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;

public interface ILguiListener extends IListener {
	
	public void handleEvent(ILguiUpdatedEvent e);

}
