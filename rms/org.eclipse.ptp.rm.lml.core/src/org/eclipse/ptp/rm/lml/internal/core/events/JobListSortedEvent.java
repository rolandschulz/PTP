package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.elements.ILguiItem;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;

/**
 * Class of the interface IJobListSortedEvent.
 * @author Claudia Knobloch
 */
public class JobListSortedEvent implements IJobListSortedEvent{
	
	/*
	 * The associated LMLManager
	 */
	private final ILMLManager lmlManager;
	
	/*
	 * The associated ILguiItem
	 */
	private final ILguiItem lguiItem;
	
	/**
	 * Constructor
	 * @param lmlManager the associated LMLManager
	 * @param lguiItem the associated ILguiItem
	 */
	public JobListSortedEvent(ILMLManager lmlManager, ILguiItem lguiItem) {
		this.lmlManager = lmlManager;
		this.lguiItem = lguiItem;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.IJobListSortedEvent#getLgui()
	 */
	public ILguiItem getLgui() {
		return lguiItem;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.IJobListSortedEvent#getLMLManager()
	 */
	public ILMLManager getLMLManager() {
		return lmlManager;
	}

}
