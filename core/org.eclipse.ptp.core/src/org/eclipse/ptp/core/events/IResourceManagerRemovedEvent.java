package org.eclipse.ptp.core.events;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * @since 5.0
 */
public interface IResourceManagerRemovedEvent {
	/**
	 * @return
	 */
	public IResourceManagerControl getResourceManager();

	/**
	 * @return
	 */
	public IModelManager getSource();
}
