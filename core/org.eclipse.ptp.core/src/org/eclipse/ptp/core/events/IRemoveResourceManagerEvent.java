package org.eclipse.ptp.core.events;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.elements.IResourceManager;

public interface IRemoveResourceManagerEvent {
	/**
	 * @return
	 */
	public IResourceManager getResourceManager();
	
	/**
	 * @return
	 */
	public IModelManager getSource();
}
