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
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
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
	 * This method intercepts and modifies the scanner discovery entries. It changes include paths to UNC notation with the correct
	 * connection name prepended.
	 * 
	 * This is an exact copy of {@link org.eclipse.ptp.rdt.sync.core.SyncGCCBuiltinSpecsDetector#setSettingEntries(List)}
	 */
	@Override
	protected void setSettingEntries(List<ICLanguageSettingEntry> entries) {
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		BuildScenario bs = bcm.getBuildScenarioForProject(currentProject);
		if (bs.getSyncProvider() == null) {
			// For local configurations, no special processing is needed.
			super.setSettingEntries(entries);
			return;
		}

		IRemoteConnection conn = null;
		try {
			conn = bs.getRemoteConnection();
		} catch (MissingConnectionException e1) {
			// Impossible to build includes properly without connection name
			super.setSettingEntries(entries);
			return;
		} 

		List<ICLanguageSettingEntry> newEntries = new ArrayList<ICLanguageSettingEntry>();
		for (ICLanguageSettingEntry entry : entries) {
			if (entry instanceof CIncludePathEntry) {
				String oldPath = ((CIncludePathEntry) entry).getValue();
				String newPath = "//" +  conn.getName() + oldPath; //$NON-NLS-1$
				ICLanguageSettingEntry newEntry = new CIncludePathEntry(newPath, entry.getFlags());
				newEntries.add(newEntry);
			} else {
				newEntries.add(entry);
			}
		}
		super.setSettingEntries(newEntries);
	}
}