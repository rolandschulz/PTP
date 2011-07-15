/**
 * Based on the class IResourceManagerListener 
 * 		 in package org.eclipse.ptp.core.listeners
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 */

package org.eclipse.ptp.rm.lml.core.listeners;

import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;

/**
 * This interface manages the handling of different events.
 */
public interface ILMLListener {

	public void handleEvent(ILguiAddedEvent event);

	public void handleEvent(ILguiRemovedEvent event);

	public void handleEvent(IMarkObjectEvent event);

	public void handleEvent(ISelectedObjectChangeEvent event);

	public void handleEvent(ITableColumnChangeEvent e);

	/**
	 * Handles an IJobListSortedEevnt.
	 * 
	 * @param e
	 *            an IJobListSortedEvent
	 */
	public void handleEvent(ITableSortedEvent e);

	public void handleEvent(IUnmarkObjectEvent event);

	public void handleEvent(IUnselectedObjectEvent event);

	public void handleEvent(IViewUpdateEvent event);
}
