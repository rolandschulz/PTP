package org.eclipse.ptp.rm.jaxb.core;

import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.rm.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * Allows the JAXB Launch (Resource) Tab access to the Resource Manager's
 * internal data.
 * 
 * @author arossi
 * 
 */
public interface IJAXBResourceManagerControl extends IResourceManagerControl {

	ResourceManagerData getData();

	IJAXBResourceManagerConfiguration getJAXBRMConfiguration();

}
