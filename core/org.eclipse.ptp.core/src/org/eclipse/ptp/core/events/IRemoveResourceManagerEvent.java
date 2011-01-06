package org.eclipse.ptp.core.events;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.elements.IPResourceManager;

public interface IRemoveResourceManagerEvent {
	/**
	 * @return
	 * @since 5.0
	 */
	public IPResourceManager getResourceManager();

	/**
	 * @return
	 */
	public IModelManager getSource();
}
