package org.eclipse.ptp.core.events;

import java.util.Collection;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IResourceManager;

public interface IModelManagerChangedResourceManagerEvent {
	/**
	 * @return
	 */
	public IModelManager getSource();
	
	/**
	 * @return
	 */
	public IResourceManager getResourceManager();
	
	/**
	 * @return
	 */
	public Collection<IAttribute> getAttributes();

}
