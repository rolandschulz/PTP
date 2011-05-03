package org.eclipse.ptp.rm.lml.internal.core.events;

import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.events.ILguiSelectedEvent;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;

public class LguiSelectedEvent implements ILguiSelectedEvent {
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
	public LguiSelectedEvent(ILMLManager lmlManager, ILguiItem lguiItem) {
		this.lmlManager = lmlManager;
		this.lguiItem = lguiItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiRemovedEvent#getLgui()
	 */
	public ILguiItem getLguiItem() {
		return lguiItem;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.lml.core.elements.ILguiRemovedEvent#getLMLManager()
	 */
	public ILMLManager getLMLManager() {
		return lmlManager;
	}
}
