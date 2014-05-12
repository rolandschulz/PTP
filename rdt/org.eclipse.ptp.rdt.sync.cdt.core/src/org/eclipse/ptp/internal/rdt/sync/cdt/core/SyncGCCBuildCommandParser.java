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
package org.eclipse.ptp.internal.rdt.sync.cdt.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.remote.core.IRemoteConnection;

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
	 * This is an exact copy of
	 * {@link org.eclipse.ptp.internal.rdt.sync.cdt.core.SyncGCCBuiltinSpecsDetector#setSettingEntries(List)}
	 */
	@Override
	protected void setSettingEntries(List<? extends ICLanguageSettingEntry> entries) {
		if (entries == null) {
			super.setSettingEntries(entries);
			return;
		}
		SyncConfig config = SyncConfigManager.getActive(currentProject);
		if (config.getSyncProviderId() == null) {
			// For local configurations, no special processing is needed.
			super.setSettingEntries(entries);
			return;
		}

		IRemoteConnection conn = null;
		try {
			conn = config.getRemoteConnection();
		} catch (MissingConnectionException e1) {
			// Impossible to build includes properly without connection name
			super.setSettingEntries(entries);
			return;
		}

		List<ICLanguageSettingEntry> newEntries = new ArrayList<ICLanguageSettingEntry>();
		for (ICLanguageSettingEntry entry : entries) {
			ICLanguageSettingEntry newEntry;
			if (!(entry instanceof CIncludePathEntry) || ((entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) == 1)) {
				newEntry = entry;
			} else {
				String remotePath = ((CIncludePathEntry) entry).getValue();
				String workspacePath = this.getWorkspacePath(remotePath, config.getLocation(currentProject));
				if (workspacePath == null) {
					// Bug 402350: Sync scanner discovery has corrupt remote paths when using Windows
					if (remotePath.startsWith("C:")) { //$NON-NLS-1$
						remotePath = remotePath.substring(2);
					}
					newEntry = new CIncludePathEntry("//" + conn.getName() + remotePath, entry.getFlags()); //$NON-NLS-1$
				} else {
					newEntry = new CIncludePathEntry(workspacePath, entry.getFlags());
				}
			}
			newEntries.add(newEntry);
		}
		super.setSettingEntries(newEntries);
	}

	/**
	 * This method intercepts and modifies resources found in build output. Remote paths inside the remote sync location are
	 * altered to point to the corresponding local (workspace) path, which is what CDT expects.
	 */
	@Override
	public String parseResourceName(String line) {
		// Extracts path from compiler output line
		String compilerPath = super.parseResourceName(line);
		if (compilerPath == null) {
			return compilerPath;
		}
		SyncConfig config = SyncConfigManager.getActive(currentProject);
		if (config.getSyncProviderId() == null) {
			// For local configurations, no special processing is needed.
			return compilerPath;
		}

		String workspacePath = this.getWorkspacePath(compilerPath, config.getLocation(currentProject));
		if (workspacePath == null) {
			return compilerPath;
		} else {
			return workspacePath;
		}
	}

	// Get the local workspace path for the given remote path and root directory. Neither parameter may be null.
	// Returns the new path or null if the remote root is not a prefix of the remote path.
	private String getWorkspacePath(String remotePathString, String remoteRootString) {
		Path remotePath = new Path(remotePathString);
		Path remoteRoot = new Path(remoteRootString);
		if (!remoteRoot.isPrefixOf(remotePath)) {
			return null;
		}
		IPath localRoot = currentProject.getLocation();
		return remotePath.toOSString().replaceFirst(remoteRoot.toOSString(), localRoot.toOSString());
	}
}