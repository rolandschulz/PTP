package org.eclipse.fdt.core;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.fdt.core.model.IWorkingCopy;
import org.eclipse.fdt.internal.core.model.BufferManager;
import org.eclipse.fdt.internal.core.model.CModelManager;
import org.eclipse.fdt.internal.core.model.IBufferFactory;

public class CommonLanguageCore {

	private static CCorePlugin fgCLCorePlugin = null;
	private static ResourceBundle fgResourceBundle;
	
	// TODO Default use org.eclipse.cdt.core
	public static String PLUGIN_ID = "org.eclipse.cdt.core"; //$NON-NLS-1$


	// -------- static methods --------
	
	public static String getResourceString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}

	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}
	
	public static void setResoureBundle(ResourceBundle resourceBundle) {
		fgResourceBundle = resourceBundle;
	}

	public static Plugin getDefault() {
		return fgCLCorePlugin;
	}
	
	public static void log(Throwable e) {
		if ( e instanceof CoreException ) {
			log(((CoreException)e).getStatus());
		} else {
			log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
		}
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}



	public CommonLanguageCore() {
		super();
		fgCLCorePlugin = CCorePlugin.getDefault();	// Default value
		fgResourceBundle = CCorePlugin.getResourceBundle();	// Default value
	}

	public void setDefault(CCorePlugin corePlugin, String pluginID, ResourceBundle resourceBundle) {
		fgCLCorePlugin = corePlugin;
		PLUGIN_ID = pluginID;
		fgResourceBundle = resourceBundle;
	}
	

}
