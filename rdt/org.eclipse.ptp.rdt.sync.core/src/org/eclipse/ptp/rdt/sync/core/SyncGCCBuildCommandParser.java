/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import org.eclipse.ptp.remote.core.IRemoteConnection;

/**
 * Language settings provider to detect compiler settings from the build output of the GCC compiler, modified to work with
 * synchronized projects. The only modification currently is to convert include paths to UNC notation with the connection name
 * prepended:
 * syntax: //<connection name>/<discovered path> 
 */
public class SyncGCCBuildCommandParser extends GCCBuildCommandParser implements ILanguageSettingsEditableProvider {
	/**
	 * This method intercepts and modifies the output from the superclass call. It changes include paths to UNC notation with the
	 * correct connection name prepended.
	 * 
	 * This function is very similar to {@link org.eclipse.ptp.rdt.sync.core.SyncGCCBuiltinSpecsDetector#parseOptions(String)}
	 *
	 * @return list of options
	 */
	@Override
	protected List<String> parseOptions(String line) {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		BuildScenario bs = bcm.getBuildScenarioForProject(currentProject);
		// For local configurations, we can fall back to the original implementation.
		if (bs.getSyncProvider() == null) {
			return super.parseOptions(line);
		}
	
		IRemoteConnection conn = null;
		try {
			conn = bs.getRemoteConnection();
		} catch (MissingConnectionException e1) {
			// Impossible to build includes properly without connection name
			return new ArrayList<String>();
		} 
	
		List<String> originalOptions = super.parseOptions(line);
		if (originalOptions == null) {
			return null;
		}
		List<String> newOptions = new ArrayList<String>();
		for (String o : originalOptions) {
			if (o.startsWith("#include <") && o.endsWith(">")) { //$NON-NLS-1$ //$NON-NLS-2$
				o = o.replaceFirst("^#include <", "#include <" + "//" + conn.getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (o.startsWith("#include \"") && o.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
				o = o.replaceFirst("^#include \\\"", "#include \"" + "//" + conn.getName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			newOptions.add(o);
		}
		return newOptions;
	}
}