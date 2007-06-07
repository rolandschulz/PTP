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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

public class ResourceManagerPersistence {

	private static final String TAG_RESOURCEMANAGER_INDEX = "ResourceManagerIndex";

	private static final String TAG_RESOURCEMANAGERS = "ResourceManagers";

	private static final String TAG_RESOURCEMANGER = "ResourceManager";

	private static final String TAG_RESOURCEMANGER_CONFIGURATION = "Configuration";

	private static final String TAG_RESOURCEMANGER_ID = "ResourceManagerId";

	private static final String TAG_RESOURCEMANGER_RUNNING = "IsRunning";

	public static void saveResourceManagers(File file,
			IResourceManagerControl[] resourceManagers) {
		System.out.println("In saveResourceManagers to file, " + file.getAbsolutePath());
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_RESOURCEMANAGERS);
		saveResourceManagers(memento, resourceManagers);
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
			IResourceManagerControl[] resourceManagers) {
		for (int i = 0; i < resourceManagers.length; ++i) {
			IMemento child = memento.createChild(TAG_RESOURCEMANGER);
			child.putString(
					TAG_RESOURCEMANGER_ID,
					resourceManagers[i].getConfiguration().getResourceManagerId());
			child.putInteger(TAG_RESOURCEMANAGER_INDEX, i);
			boolean isRunning = resourceManagers[i].getState().equals(ResourceManagerAttributes.State.STARTED);
			child.putString(TAG_RESOURCEMANGER_RUNNING, isRunning ? "true" : "false");
			IMemento grandchild = child.createChild(TAG_RESOURCEMANGER_CONFIGURATION);
			resourceManagers[i].getConfiguration().save(grandchild);
		}
	}

	private final List<IResourceManagerControl> resourceManagers =
	    new ArrayList<IResourceManagerControl>();

	private IResourceManagerControl savedCurrentResourceManager;

    private final List<IResourceManagerControl> rmsNeedStarting =
        new ArrayList<IResourceManagerControl>();

	public IResourceManagerControl[] getResourceManagerControls() {
		return resourceManagers.toArray(new IResourceManagerControl[0]);
	}

	public IResourceManagerControl[] getResourceManagerControlsNeedStarting() {
        return rmsNeedStarting.toArray(new IResourceManagerControl[0]);
    }

	public IResourceManager getSavedCurrentResourceManager() {
		return savedCurrentResourceManager;
	}

	/**
	 * Loads and, if necessary, starts saved resource managers.
	 * @param file
	 * @param factories
	 * @param monitor
	 * @throws CoreException 
	 */
	public void loadResourceManagers(File file,
			IResourceManagerFactory[] factories, IProgressMonitor monitor) throws CoreException {
	    if (monitor == null) {
	        monitor = new NullProgressMonitor();
	    }
	    monitor.beginTask("loading resource manager from " + file, 100);
	    try {
	        rmsNeedStarting.clear();
	        resourceManagers.clear();

	        FileReader reader = null;
	        try {
	            reader = new FileReader(file);
	            // Loads and, if necessary, starts saved resource managers.
	            loadResourceManagers(XMLMemento.createReadRoot(reader), factories,
	                    new SubProgressMonitor(monitor, 100));
	        } catch (FileNotFoundException e) {
	            // ignored... no ResourceManager items exist yet.
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
		finally {
		    monitor.done();
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

    /**
	 * Loads and, if necessary, starts saved resource managers.
	 * @param memento
	 * @param factories
	 * @param monitor
	 * @throws CoreException 
	 */
	private void loadResourceManagers(XMLMemento memento,
			IResourceManagerFactory[] factories,
			final IProgressMonitor monitor) throws CoreException {
		IMemento[] children = memento.getChildren(TAG_RESOURCEMANGER);
		ArrayList<IStatus> statuses = new ArrayList<IStatus>();
		monitor.beginTask("Loading the Resource Managers", children.length);
		try {
			final IResourceManagerControl[] tmpRMs = new IResourceManagerControl[children.length];
			ArrayList<IResourceManagerControl> rms = new ArrayList<IResourceManagerControl>(tmpRMs.length);

			for (int i = 0; i < children.length; ++i) {
				String resourceManagerId = children[i].getString(TAG_RESOURCEMANGER_ID);
				final int index = children[i].getInteger(TAG_RESOURCEMANAGER_INDEX).intValue();
				String isRunningRep = children[i].getString(TAG_RESOURCEMANGER_RUNNING);
				boolean isRunning = "true".equalsIgnoreCase(isRunningRep);
				IResourceManagerFactory factory = getResourceManagerFactory(
						factories, resourceManagerId);
				if (factory == null) {
					monitor.worked(1);
				}
				else {
					final IMemento grandchild = children[i].getChild(TAG_RESOURCEMANGER_CONFIGURATION);
					IResourceManagerConfiguration configuration = factory.loadConfiguration(grandchild);
					if (configuration != null) {
						tmpRMs[index] = factory.create(configuration);
						if (tmpRMs[index] != null) {
							// start the resource manager if it was running when saved.
							if (isRunning) {
							    rmsNeedStarting .add(tmpRMs[index]);
							}
							else {
								monitor.worked(1);
							}
							rms.add(tmpRMs[index]);
						}
					}
				}
			}

			resourceManagers.addAll(rms);
			if (statuses.size() > 0) {
				throw new CoreException(new MultiStatus(PTPCorePlugin.PLUGIN_ID,
						MultiStatus.ERROR, statuses.toArray(new IStatus[0]),
						"loading resource managers", null));
			}
		}
		finally {
			monitor.done();
		}
	}

}
