package org.eclipse.ptp.core.events;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;

public interface IRemoveResourceManagerEvent {
	/**
	 * @return
	 * @since 5.0
	 */
	public IResourceManagerControl getResourceManager();

	/**
	 * @return
	 */
	public IModelManager getSource();
}
