/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.rmsystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.rmsystem.ResourceManagerStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

public class ResourceManagerPersistence {

	private static final String TAG_CURRENT_RESOURCEMANAGER = "CurrentResourceManager";

	private static final String TAG_RESOURCEMANAGER_INDEX = "ResourceManagerIndex";

	private static final String TAG_RESOURCEMANAGERS = "ResourceManagers";

	private static final String TAG_RESOURCEMANGER = "ResourceManager";

	private static final String TAG_RESOURCEMANGER_CONFIGURATION = "Configuration";

	private static final String TAG_RESOURCEMANGER_ID = "ResourceManagerId";

	private static final String TAG_RESOURCEMANGER_RUNNING = "IsRunning";

	public static void saveResourceManagers(File file,
			IResourceManager[] resourceManagers,
			IResourceManager currentResourceManager) {
		System.out.println("In saveResourceManagers");
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_RESOURCEMANAGERS);
		saveResourceManagers(memento, resourceManagers, currentResourceManager);
		FileWriter writer = null;
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
		System.out.println("Leaving saveResourceManagers");
	}

	private static void saveResourceManagers(XMLMemento memento,
			IResourceManager[] resourceManagers,
			IResourceManager currentResourceManager) {
		int currentResourceManagerIndex = -1;
		for (int i = 0; i < resourceManagers.length; ++i) {
			if (currentResourceManager == resourceManagers[i]) {
				currentResourceManagerIndex = i;
			}
			IMemento child = memento.createChild(TAG_RESOURCEMANGER);
			child.putString(
					TAG_RESOURCEMANGER_ID,
					resourceManagers[i].getConfiguration().getResourceManagerId());
			child.putInteger(TAG_RESOURCEMANAGER_INDEX, i);
			boolean isRunning = resourceManagers[i].getStatus().equals(ResourceManagerStatus.STARTED);
			child.putString(TAG_RESOURCEMANGER_RUNNING, isRunning ? "true" : "false");
			IMemento grandchild = child.createChild(TAG_RESOURCEMANGER_CONFIGURATION);
			resourceManagers[i].getConfiguration().save(grandchild);
		}
		memento.putInteger(TAG_CURRENT_RESOURCEMANAGER,
				currentResourceManagerIndex);
	}

	private IResourceManager[] resourceManagers = new IResourceManager[0];

	private IResourceManager savedCurrentResourceManager;

	public IResourceManager[] getResourceManagers() {
		return resourceManagers;
	}

	public IResourceManager getSavedCurrentResourceManager() {
		return savedCurrentResourceManager;
	}

	public void loadResourceManagers(File file,
			IResourceManagerFactory[] factories) {
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			loadResourceManagers(XMLMemento.createReadRoot(reader), factories);
		} catch (FileNotFoundException e) {
			// ignored... no ResourceManager items exist yet.
		} catch (Exception e) {
			PTPCorePlugin.log(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				PTPCorePlugin.log(e);
			}
		}
	}

	private IResourceManagerFactory getResourceManagerFactory(
			IResourceManagerFactory[] factories, String id) {
		for (int i = 0; i < factories.length; i++) {
			if (factories[i].getId().equals(id))
				return factories[i];
		}

		return null;
	}

	private void loadResourceManagers(XMLMemento memento,
			IResourceManagerFactory[] factories) {
		IMemento[] children = memento.getChildren(TAG_RESOURCEMANGER);

		IResourceManager[] tmpRMs = new IResourceManager[children.length];
		ArrayList rms = new ArrayList(tmpRMs.length);

		for (int i = 0; i < children.length; ++i) {
			String resourceManagerId = children[i].getString(TAG_RESOURCEMANGER_ID);
			int index = children[i].getInteger(TAG_RESOURCEMANAGER_INDEX).intValue();
			String isRunningRep = children[i].getString(TAG_RESOURCEMANGER_RUNNING);
			boolean isRunning = "true".equalsIgnoreCase(isRunningRep);
			IResourceManagerFactory factory = getResourceManagerFactory(
					factories, resourceManagerId);
			if (factory != null) {
				final IMemento grandchild = children[i].getChild(TAG_RESOURCEMANGER_CONFIGURATION);
				IResourceManagerConfiguration configuration = factory.loadConfiguration(grandchild);
				if (configuration != null) {
					tmpRMs[index] = factory.create(configuration);
					if (tmpRMs[index] != null) {
						if (isRunning) {
							try {
								tmpRMs[index].start();
							} catch (CoreException e) {
								PTPCorePlugin.log(e);
							}
						}
						rms.add(tmpRMs[index]);
					}
				}
			}
		}

		int savedCurrentRMIndex = memento.getInteger(TAG_CURRENT_RESOURCEMANAGER).intValue();
		if (savedCurrentRMIndex >= 0) {
			setSavedCurrentResourceManager(tmpRMs[savedCurrentRMIndex]);
		}
		resourceManagers = (IResourceManager[]) rms.toArray(new IResourceManager[rms.size()]);
	}

	private void setSavedCurrentResourceManager(IResourceManager manager) {
		savedCurrentResourceManager = manager;
	}

}
