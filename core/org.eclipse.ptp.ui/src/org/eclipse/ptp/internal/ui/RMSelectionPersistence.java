/**
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.internal.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public class RMSelectionPersistence {
	private final static String TAG_DEFAULT_RESOURCEMANAGER = "DefaultResourceManager"; //$NON-NLS-1$
	private final static String TAG_RESOURCEMANAGER = "ResourceMananger"; //$NON-NLS-1$
	private final static String TAG_RESOURCEMANAGER_ID = "ResourceManangerID"; //$NON-NLS-1$
	private final static String MEMENTO_FILE = "defaultResourceManager.xml"; //$NON-NLS-1$
	private final static String NULL_ID = "null"; //$NON-NLS-1$

	/**
	 * Create a resource mananger selection persistence object
	 */
	public RMSelectionPersistence() {
	}

	/**
	 * Get the default resource mananger ID from persistent storage
	 * 
	 * @return - the default resource mananger ID or null if no default resource
	 *         mananger set
	 */
	public String getDefaultRMID() {
		File file;
		FileReader reader = null;
		String rmId = null;

		file = getPersistenceFile();
		try {
			IMemento rmSelectionInfo;
			IMemento child;

			reader = new FileReader(file);
			rmSelectionInfo = XMLMemento.createReadRoot(reader);
			child = rmSelectionInfo.getChild(TAG_RESOURCEMANAGER);
			if (child != null) {
				rmId = child.getString(TAG_RESOURCEMANAGER_ID);
			}
		} catch (FileNotFoundException e) {
			// ignored... Default resource manager not set yet
		} catch (WorkbenchException e) {
			// ignored... Default resource manager not set yet
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				PTPCorePlugin.log(e);
			}
		}
		return rmId;
	}

	/*
	 * Record the identity of the current default resource manager in persistent
	 * storage
	 * 
	 * @param rm - The default resource manager
	 */
	public void saveDefaultRMID(String rmId) {
		XMLMemento memento;
		FileWriter writer;
		File file;

		file = getPersistenceFile();
		memento = createMemento(rmId);
		writer = null;
		try {
			writer = new FileWriter(file);
			memento.save(writer);
		} catch (IOException e) {
			PTPCorePlugin.log(e);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				PTPCorePlugin.log(e);
			}
		}
	}

	/**
	 * Create the persistent data object which stores the identity of the
	 * default resource mananger
	 * 
	 * @param rmId
	 *            - the default resource manager or null if there is no default
	 *            resource manager
	 * @return - the persistent data object used to store the resource manager
	 *         id
	 */
	private XMLMemento createMemento(String rmId) {
		XMLMemento memento;
		IMemento child;

		memento = XMLMemento.createWriteRoot(TAG_DEFAULT_RESOURCEMANAGER);
		child = memento.createChild(TAG_RESOURCEMANAGER);
		if (rmId == null) {
			child.putString(TAG_RESOURCEMANAGER_ID, NULL_ID);
		} else {
			child.putString(TAG_RESOURCEMANAGER_ID, rmId);
		}
		return memento;
	}

	/**
	 * Get the location of the file holding the resource manager data
	 * 
	 * @return - file object for resource manager data
	 */
	private File getPersistenceFile() {
		final PTPUIPlugin plugin;

		plugin = PTPUIPlugin.getDefault();
		return plugin.getStateLocation().append(MEMENTO_FILE).toFile();
	}
}
