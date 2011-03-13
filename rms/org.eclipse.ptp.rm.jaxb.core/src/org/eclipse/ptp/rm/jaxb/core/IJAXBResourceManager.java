package org.eclipse.ptp.rm.jaxb.core;

import java.util.Map;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.rmsystem.IResourceManager;

public interface IJAXBResourceManager extends IResourceManager {

	boolean getAppendSysEnv();

	IJAXBResourceManagerControl getControl();

	Map<String, String> getDynSystemEnv();

	IRemoteConnection getRemoteConnection();

	IRemoteServices getRemoteServices();

}
