package org.eclipse.ptp.rm.jaxb.core;

import org.eclipse.ptp.rm.jaxb.core.rm.JAXBResourceManagerControl;
import org.eclipse.ptp.rmsystem.IResourceManager;

public interface IJAXBResourceManager extends IResourceManager {
	JAXBResourceManagerControl getControl();
}
