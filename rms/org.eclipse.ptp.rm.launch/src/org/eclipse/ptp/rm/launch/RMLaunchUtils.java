package org.eclipse.ptp.rm.launch;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.control.IJAXBLaunchControl;
import org.eclipse.ptp.rm.jaxb.control.JAXBLaunchControl;
import org.eclipse.ptp.rm.jaxb.ui.util.JAXBExtensionUtils;
import org.eclipse.ptp.rm.launch.internal.ProviderInfo;

/**
 * @since 6.0
 * 
 */
public class RMLaunchUtils {

	private static Map<String, URL> fJAXBConfigurations = null;

	public static String getConnectionName(ILaunchConfiguration configuration) {
		try {
			return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONNECTION_NAME, (String) null);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Looks up the XML configuration and returns its location
	 * 
	 * @param name
	 * @return URL of the configuration
	 */
	public static URL getJAXBConfigurationURL(String name) {
		loadJAXBResourceManagers(false);
		if (fJAXBConfigurations != null) {
			return fJAXBConfigurations.get(name);
		}
		return null;
	}

	public static IJAXBLaunchControl getLaunchControl(ILaunchConfiguration configuration) throws CoreException {
		String type = getResourceManagerType(configuration);
		if (type != null) {
			ProviderInfo provider = ProviderInfo.getProvider(type);
			if (provider != null) {
				return getLaunchControl(provider, configuration);
			}
		}
		return null;
	}

	public static IJAXBLaunchControl getLaunchControl(ProviderInfo provider, ILaunchConfiguration configuration) {
		IJAXBLaunchControl control = new JAXBLaunchControl();
		control.setRMConfigurationURL(getJAXBConfigurationURL(provider.getName()));
		String name = getConnectionName(configuration);
		String id = getRemoteServicesId(configuration);
		if (name != null && id != null) {
			control.setConnectionName(name);
			control.setRemoteServicesId(id);
			return control;
		}
		return null;
	}

	/**
	 * Get the remote connection that was selected in the resources tab
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return remote connection or null if it is invalid or not specified
	 * @throws CoreException
	 */
	public static IRemoteConnection getRemoteConnection(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		String remId = getRemoteServicesId(configuration);
		if (remId != null) {
			IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(remId, monitor);
			if (services != null) {
				String name = getConnectionName(configuration);
				if (name != null) {
					return services.getConnectionManager().getConnection(name);
				}
			}
		}
		return null;
	}

	public static String getRemoteServicesId(ILaunchConfiguration configuration) {
		try {
			return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_REMOTE_SERVICES_ID, (String) null);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getResourceManagerType(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_TYPE, (String) null);
	}

	/**
	 * Wrapper method. Calls {@link JAXBExtensionUtils#loadJAXBResourceManagers(Map, boolean)}
	 */
	private static void loadJAXBResourceManagers(boolean showError) {
		if (fJAXBConfigurations == null) {
			fJAXBConfigurations = new HashMap<String, URL>();
		} else {
			fJAXBConfigurations.clear();
		}

		JAXBExtensionUtils.loadJAXBResourceManagers(fJAXBConfigurations, showError);
	}

	/**
	 * Constructor
	 */
	public RMLaunchUtils() {
	}
}
