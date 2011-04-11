/**
 * Based on the class IResourceManagerListener 
 * 		 in package org.eclipse.ptp.core.listeners
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 */

package org.eclipse.ptp.rm.lml.core.listeners;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;

/**
 * This interface manages the handling of different events.
 * @author Claudia Knobloch
 */
public interface ILguiListener extends IListener {
	
	public void handleEvent(ILguiUpdatedEvent e);

}

