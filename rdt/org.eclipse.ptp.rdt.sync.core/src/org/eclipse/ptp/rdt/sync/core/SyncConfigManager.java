/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.listeners.ISyncConfigListener;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

public class SyncConfigManager {
	private static final String projectLocationPathVariable = "${project_loc}"; //$NON-NLS-1$

	private static final String PREF_SYNCCONFIG = "SYNC_CONFIG"; //$NON-NLS-1$
	private static final String CONFIGS_ELEMENT = "sync-configs"; //$NON-NLS-1$
	private static final String CONFIG_ELEMENT = "sync-config"; //$NON-NLS-1$
	private static final String CONFIG_NAME_ELEMENT = "config-name"; //$NON-NLS-1$
	private static final String SYNC_PROVIDER_ID_ELEMENT = "sync-provider-id"; //$NON-NLS-1$
	private static final String CONNECTION_NAME_ELEMENT = "connection-name"; //$NON-NLS-1$
	private static final String LOCATION_ELEMENT = "location"; //$NON-NLS-1$
	private static final String REMOTE_SERVICES_ID_ELEMENT = "remote-services-id"; //$NON-NLS-1$
	private static final String DATA_ELEMENT = "data"; //$NON-NLS-1$
	private static final String ACTIVE_ELEMENT = "active"; //$NON-NLS-1$
	private static final String SYNC_ON_PREBUILD_ELEMENT = "sync-on-prebuild"; //$NON-NLS-1$
	private static final String SYNC_ON_POSTBUILD_ELEMENT = "sync-on-postbuild"; //$NON-NLS-1$
	private static final String SYNC_ON_SAVE_ELEMENT = "sync-on-save"; //$NON-NLS-1$

	private static final Map<IProject, ListenerList> fSyncConfigListenerMap = Collections
			.synchronizedMap(new HashMap<IProject, ListenerList>());
	private static final Map<IProject, SyncConfig> fActiveSyncConfigMap = Collections
			.synchronizedMap(new HashMap<IProject, SyncConfig>());
	private static final Map<IProject, List<SyncConfig>> fSyncConfigMap = Collections
			.synchronizedMap(new HashMap<IProject, List<SyncConfig>>());

	/**
	 * Add a new sync configuration to the project
	 * 
	 * @param project
	 *            project
	 * @param config
	 *            sync configuration to add to the project
	 */
	public static void addConfig(IProject project, SyncConfig config) {
		try {
			loadConfigs(project);
			doAddConfig(project, config);
			saveConfigs(project);
			fireSyncConfigAdded(project, config);
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(e);
		}
	}

	public static void updateConfigs(IProject project, SyncConfig[] addedConfigs, SyncConfig[] removedConfigs) {
		for (SyncConfig config : addedConfigs) {
			doAddConfig(project, config);
		}
		for (SyncConfig config : removedConfigs) {
			doRemoveConfig(project, config);
		}
		try {
			saveConfigs(project);
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(e);
		}
	}

	/**
	 * Register to receive sync configuration events
	 * 
	 * @param project
	 *            project on which to monitor changes
	 * @param listener
	 *            listener to receive events
	 */
	public static void addSyncConfigListener(IProject project, ISyncConfigListener listener) {
		ListenerList list = fSyncConfigListenerMap.get(project);
		if (list == null) {
			list = new ListenerList();
			fSyncConfigListenerMap.put(project, list);
		}
		list.add(listener);
	}

	private static void doAddConfig(IProject project, SyncConfig config) {
		List<SyncConfig> projConfigs = fSyncConfigMap.get(project);
		if (projConfigs == null) {
			projConfigs = new ArrayList<SyncConfig>();
			fSyncConfigMap.put(project, projConfigs);
		}
		projConfigs.add(config);
	}

	private static void fireSyncConfigAdded(IProject project, SyncConfig config) {
		ListenerList list = fSyncConfigListenerMap.get(project);
		if (list != null) {
			for (Object obj : list.getListeners()) {
				ISyncConfigListener listener = (ISyncConfigListener) obj;
				listener.configAdded(project, config);
			}
		}
	}

	private static void fireSyncConfigRemoved(IProject project, SyncConfig config) {
		ListenerList list = fSyncConfigListenerMap.get(project);
		if (list != null) {
			for (Object obj : list.getListeners()) {
				ISyncConfigListener listener = (ISyncConfigListener) obj;
				listener.configRemoved(project, config);
			}
		}
	}

	private static void fireSyncConfigSelected(IProject project, SyncConfig newConfig, SyncConfig oldConfig) {
		ListenerList list = fSyncConfigListenerMap.get(project);
		if (list != null) {
			for (Object obj : list.getListeners()) {
				ISyncConfigListener listener = (ISyncConfigListener) obj;
				listener.configSelected(project, newConfig, oldConfig);
			}
		}
	}

	/**
	 * Get the sync configurations associated with the project
	 * 
	 * @param project
	 * @return sync configurations for the project
	 */
	public static SyncConfig[] getConfigs(IProject project) {
		try {
			loadConfigs(project);
			List<SyncConfig> configs = fSyncConfigMap.get(project);
			if (configs != null) {
				return configs.toArray(new SyncConfig[0]);
			}
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(e);
		}
		return new SyncConfig[0];
	}

	private static void loadConfigs(IProject project) throws CoreException {
		if (!fSyncConfigMap.containsKey(project)) {
			String prefs = project.getPersistentProperty(new QualifiedName(RDTSyncCorePlugin.PLUGIN_ID, PREF_SYNCCONFIG));
			if (prefs != null) {
				StringReader reader = new StringReader(prefs);
				XMLMemento rootMemento = XMLMemento.createReadRoot(reader);
				for (IMemento configMemento : rootMemento.getChildren(CONFIG_ELEMENT)) {
					String configName = configMemento.getString(CONFIG_NAME_ELEMENT);
					String location = configMemento.getString(LOCATION_ELEMENT);
					String data = configMemento.getString(DATA_ELEMENT);
					String connectionName = configMemento.getString(CONNECTION_NAME_ELEMENT);
					String remoteServicesId = configMemento.getString(REMOTE_SERVICES_ID_ELEMENT);
					String syncProviderId = configMemento.getString(SYNC_PROVIDER_ID_ELEMENT);
					Boolean syncOnPreBuild = configMemento.getBoolean(SYNC_ON_PREBUILD_ELEMENT);
					Boolean syncOnPostBuild = configMemento.getBoolean(SYNC_ON_POSTBUILD_ELEMENT);
					Boolean syncOnSave = configMemento.getBoolean(SYNC_ON_SAVE_ELEMENT);
					SyncConfig config = new SyncConfig(configName, syncProviderId, connectionName, remoteServicesId, location);
					config.setData(data);
					if (syncOnPreBuild != null) {
						config.setSyncOnPreBuild(syncOnPreBuild.booleanValue());
					}
					if (syncOnPostBuild != null) {
						config.setSyncOnPostBuild(syncOnPostBuild.booleanValue());
					}
					if (syncOnSave != null) {
						config.setSyncOnSave(syncOnSave.booleanValue());
					}
					doAddConfig(project, config);
				}
				String activeName = rootMemento.getString(ACTIVE_ELEMENT);
				if (activeName != null) {
					List<SyncConfig> configs = fSyncConfigMap.get(project);
					if (configs != null) {
						for (SyncConfig config : configs) {
							if (config.getName().equals(activeName)) {
								fActiveSyncConfigMap.put(project, config);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Remove the sync configuration from the project. Note that this methods allows an active configuration to be removed.
	 * 
	 * Clients should check the active status of the configuration and call {@link #setActive(IProject, SyncConfig)} if necessary.
	 * 
	 * Clients will not be allowed to remove all configurations from the project. There must always be at least one configuration
	 * for each project.
	 * 
	 * @param project
	 *            project
	 * @param config
	 *            configuration to remove
	 */
	public static void removeConfig(IProject project, SyncConfig config) {
		try {
			loadConfigs(project);
			boolean removed = doRemoveConfig(project, config);
			if (removed) {
				saveConfigs(project);
				fireSyncConfigRemoved(project, config);
			}
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(e);
		}
	}

	private static boolean doRemoveConfig(IProject project, SyncConfig config) {
		List<SyncConfig> projConfigs = fSyncConfigMap.get(project);
		if (projConfigs != null && projConfigs.size() > 1) {
			return projConfigs.remove(config);
		}
		return false;
	}

	/**
	 * Remove the listener for sync config events
	 * 
	 * @param project
	 * @param listener
	 */
	public static void removeSyncConfigListener(IProject project, ISyncConfigListener listener) {
		ListenerList list = fSyncConfigListenerMap.get(project);
		if (list != null) {
			list.remove(listener);
		}
	}

	/**
	 * Save the current configurations for the project. This method should be called prior to workbench shutdown if any
	 * modifications have been made to the configurations
	 * 
	 * @param project
	 * @throws CoreException
	 */
	private static void saveConfigs(IProject project) throws CoreException {
		List<SyncConfig> projConfigs = fSyncConfigMap.get(project);
		if (projConfigs != null) {
			XMLMemento rootMemento = XMLMemento.createWriteRoot(CONFIGS_ELEMENT);
			for (SyncConfig config : projConfigs) {
				IMemento configMemento = rootMemento.createChild(CONFIG_ELEMENT);
				configMemento.putString(CONFIG_NAME_ELEMENT, config.getName());
				configMemento.putString(LOCATION_ELEMENT, config.getLocation());
				configMemento.putString(DATA_ELEMENT, config.getData());
				configMemento.putString(CONNECTION_NAME_ELEMENT, config.getConnectionName());
				configMemento.putString(REMOTE_SERVICES_ID_ELEMENT, config.getRemoteServicesId());
				configMemento.putString(SYNC_PROVIDER_ID_ELEMENT, config.getSyncProviderId());
				configMemento.putBoolean(SYNC_ON_PREBUILD_ELEMENT, config.isSyncOnPreBuild());
				configMemento.putBoolean(SYNC_ON_POSTBUILD_ELEMENT, config.isSyncOnPostBuild());
				configMemento.putBoolean(SYNC_ON_SAVE_ELEMENT, config.isSyncOnSave());
			}
			SyncConfig active = fActiveSyncConfigMap.get(project);
			if (active != null) {
				rootMemento.putString(ACTIVE_ELEMENT, active.getName());
			}
			StringWriter writer = new StringWriter();
			try {
				rootMemento.save(writer);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, RDTSyncCorePlugin.PLUGIN_ID,
						Messages.SyncConfigManager_Unable_to_save, e));
			}
			project.setPersistentProperty(new QualifiedName(RDTSyncCorePlugin.PLUGIN_ID, PREF_SYNCCONFIG), writer.toString());
		}
	}

	/**
	 * Set the active sync configuration for the project. Automatically deselects the current active configuration.
	 * 
	 * @param project
	 * @param config
	 */
	public static void setActive(IProject project, SyncConfig config) {
		try {
			loadConfigs(project);
			SyncConfig oldConfig = fActiveSyncConfigMap.get(project);
			fActiveSyncConfigMap.put(project, config);
			saveConfigs(project);
			fireSyncConfigSelected(project, config, oldConfig);
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(e);
		}
	}

	/**
	 * Get the active configuration for the project. There is always at least one active configuration for every project. Returns
	 * null if the project is not a synchronized project.
	 * 
	 * @param project
	 * @return active configuration
	 */
	public static SyncConfig getActive(IProject project) {
		try {
			if (project.hasNature(RemoteSyncNature.NATURE_ID)) {
				try {
					loadConfigs(project);
					return fActiveSyncConfigMap.get(project);
				} catch (CoreException e) {
					RDTSyncCorePlugin.log(e);
				}
			}
		} catch (CoreException e) {
			// fail
		}
		return null;
	}

	/**
	 * Check if this config is active for the project.
	 * 
	 * @param project
	 * @param config
	 * @return true if this config is the active config for the project
	 */
	public static boolean isActive(IProject project, SyncConfig config) {
		SyncConfig active = fActiveSyncConfigMap.get(project);
		return (active != null && config != null && active.getName().equals(config.getName()));
	}

	/**
	 * Get the synchronize location URI of the resource associated with the active sync configuration. Returns null if the project
	 * containing the resource is not a synchronized project.
	 * 
	 * @param resource
	 *            target resource - cannot be null
	 * @return URI or null if not a sync project
	 * @throws CoreException
	 */
	public static URI getActiveSyncLocationURI(IResource resource) throws CoreException {
		SyncConfig config = getActive(resource.getProject());
		if (config != null) {
			return getSyncLocationURI(config, resource.getProject());
		}
		return null;
	}

	/**
	 * Get the synchronize location URI of the resource associated with the sync configuration. Returns null if the sync
	 * configuration has not been configured correctly.
	 * 
	 * @param config
	 *            sync configuration
	 * @param resource
	 *            target resource
	 * @return URI or null if not correctly configured
	 * @throws CoreException
	 */
	public static URI getSyncLocationURI(SyncConfig config, IResource resource) throws CoreException {
		if (config != null) {
			IPath path = new Path(config.getLocation()).append(resource.getProjectRelativePath());
			IRemoteConnection conn;
			try {
				conn = config.getRemoteConnection();
			} catch (MissingConnectionException e) {
				return null;
			}
			IRemoteFileManager fileMgr = conn.getRemoteServices().getFileManager(conn);
			return fileMgr.toURI(path);
		}
		return null;
	}

	/**
	 * Create a sync configuration in the local Eclipse workspace.
	 * This function makes no changes to the internal data structures and is of little value for most clients.
	 * 
	 * @param project
	 *            - cannot be null
	 * @return the sync configuration - never null
	 * @throws CoreException
	 *             on problems getting local resources, either the local connection or local services
	 */
	public static SyncConfig createLocal(IProject project) throws CoreException {
		IRemoteServices localService = RemoteServices.getLocalServices();

		if (localService != null) {
			IRemoteConnection localConnection = localService.getConnectionManager().getConnection(
					IRemoteConnectionManager.LOCAL_CONNECTION_NAME);
			if (localConnection != null) {
				return new SyncConfig(localConnection.getName(), null, localConnection, projectLocationPathVariable);
			} else {
				throw new CoreException(new Status(IStatus.ERROR, RDTSyncCorePlugin.PLUGIN_ID, Messages.BCM_LocalConnectionError));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, RDTSyncCorePlugin.PLUGIN_ID, Messages.BCM_LocalServiceError));
		}
	}
}
