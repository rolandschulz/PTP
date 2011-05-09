package org.eclipse.ptp.rm.lml.core.events;

import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;

/**
 * Interface to manage the event that a joblist has been sorted.  
 * @author Claudia Knobloch
 */
public interface IJobListSortedEvent {
	
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
