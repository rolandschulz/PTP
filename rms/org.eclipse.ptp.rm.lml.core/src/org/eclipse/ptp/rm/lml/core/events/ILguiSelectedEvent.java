package org.eclipse.ptp.rm.lml.core.events;

import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;

public interface ILguiSelectedEvent {

	/**
	 * Getting the involved IlguiItem.
	 * @return the involved ILguiItem
	 */
	public ILguiItem getLguiItem();

	/**
	 * Getting the involved LMLManager.
	 * @return the involved LMLManager
	 */
	public ILMLManager getLMLManager();
}
