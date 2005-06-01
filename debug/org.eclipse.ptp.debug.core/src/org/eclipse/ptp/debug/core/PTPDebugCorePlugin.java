package org.eclipse.ptp.debug.core;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.debug.internal.core.PDebugConfiguration;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PTPDebugCorePlugin extends Plugin {
	//The shared instance.
	private static PTPDebugCorePlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The plug-in identifier (value <code>"org.eclipse.ptp.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.ptp.debug.core" ; //$NON-NLS-1$
	
	private HashMap fDebugConfigurations;
	
	/**
	 * The constructor.
	 */
	public PTPDebugCorePlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PTPDebugCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PTPDebugCorePlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.eclipse.ptp.debug.core.PTPDebugCorePluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 * 
	 * @return the unique identifier of this plugin
	 */
	public static String getUniqueIdentifier() {
		if ( getDefault() == null ) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}
	
	private void initializeDebugConfiguration() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint( getUniqueIdentifier(), "PTPDebugger" ); //$NON-NLS-1$
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		fDebugConfigurations = new HashMap( infos.length );
		for( int i = 0; i < infos.length; i++ ) {
			IConfigurationElement configurationElement = infos[i];
			IPDebugConfiguration configType = new PDebugConfiguration( configurationElement );
			fDebugConfigurations.put( configType.getID(), configType );
		}
	}

	public IPDebugConfiguration[] getDebugConfigurations() {
		if ( fDebugConfigurations == null ) {
			initializeDebugConfiguration();
		}
		return (IPDebugConfiguration[])fDebugConfigurations.values().toArray( new PDebugConfiguration[0] );
	}

	public IPDebugConfiguration getDebugConfiguration( String id ) throws CoreException {
		if ( fDebugConfigurations == null ) {
			initializeDebugConfiguration();
		}
		IPDebugConfiguration dbgCfg = (IPDebugConfiguration)fDebugConfigurations.get( id );
		if ( dbgCfg == null ) {
			IStatus status = new Status( IStatus.ERROR, getUniqueIdentifier(), 100, "Error", null ); //$NON-NLS-1$
			throw new CoreException( status );
		}
		return dbgCfg;
	}
}
