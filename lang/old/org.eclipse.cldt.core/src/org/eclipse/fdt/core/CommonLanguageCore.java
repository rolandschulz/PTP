package org.eclipse.fdt.core;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.fdt.core.model.IWorkingCopy;
import org.eclipse.fdt.internal.core.model.BufferManager;
import org.eclipse.fdt.internal.core.model.CModelManager;
import org.eclipse.fdt.internal.core.model.IBufferFactory;

public class CommonLanguageCore {

	private static ResourceBundle fgResourceBundle;

	// -------- static methods --------

	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle("org.eclipse.fdt.internal.core.FortranCorePluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}

	
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
	
}
