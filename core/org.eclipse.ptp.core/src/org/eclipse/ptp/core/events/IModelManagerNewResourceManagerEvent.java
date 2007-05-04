package org.eclipse.ptp.core.events;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.elements.IResourceManager;

public interface IModelManagerNewResourceManagerEvent {
	/**
	 * @return
	 */
	public IModelManager getSource();
	
	/**
	 * @return
	 */
	public IResourceManager getResourceManager();
}
