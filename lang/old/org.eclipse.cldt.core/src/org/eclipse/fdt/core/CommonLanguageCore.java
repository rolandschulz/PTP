package org.eclipse.fdt.core;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

public class CommonLanguageCore {

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
}
