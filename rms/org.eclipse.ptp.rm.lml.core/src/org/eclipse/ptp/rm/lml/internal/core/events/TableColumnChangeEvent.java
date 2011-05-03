package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.events.ITableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;

public class TableColumnChangeEvent implements ITableColumnChangeEvent{
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
	public TableColumnChangeEvent(ILMLManager lmlManager, ILguiItem lguiItem) {
		this.lmlManager = lmlManager;
		this.lguiItem = lguiItem;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.IJobListSortedEvent#getLgui()
	 */
	public ILguiItem getLguiItem() {
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
