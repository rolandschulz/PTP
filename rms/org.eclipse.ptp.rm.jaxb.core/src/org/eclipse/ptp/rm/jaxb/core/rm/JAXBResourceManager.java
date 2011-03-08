package org.eclipse.ptp.rm.jaxb.core.rm;

import java.util.Map;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.IResourceManagerMonitor;

public final class JAXBResourceManager extends AbstractResourceManager implements IJAXBNonNLSConstants {

	private final JAXBResourceManagerControl fControl;

	public JAXBResourceManager(IResourceManagerConfiguration jaxbServiceProvider, IResourceManagerControl control,
			IResourceManagerMonitor monitor) {
		super(jaxbServiceProvider, control, monitor);
		fControl = (JAXBResourceManagerControl) control;
	}

	public boolean getAppendSysEnv() {
		return fControl.getAppendSysEnv();
	}

	public Map<String, String> getDynSystemEnv() {
		return fControl.getDynSystemEnv();
	}

	public IRemoteConnection getRemoteConnection() {
		return fControl.getRemoteConnection();
	}

	public IRemoteServices getRemoteServices() {
		return fControl.getRemoteServices();
	}
}
