/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.services.core.messages.Messages;
import org.eclipse.ptp.services.internal.core.Service;
import org.eclipse.ptp.services.internal.core.ServiceCategory;
import org.eclipse.ptp.services.internal.core.ServiceConfiguration;
import org.eclipse.ptp.services.internal.core.ServiceModelEvent;
import org.eclipse.ptp.services.internal.core.ServiceModelEventManager;
import org.eclipse.ptp.services.internal.core.ServiceProviderDescriptor;
import org.eclipse.ptp.services.internal.core.ServicesCorePlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * A singleton class which is the entry point to a service model which
 * represents: - the set of contributed services - the set of providers which
 * provide those services - the service fProjectConfigurations for each project
 * which specify which services are mapped to which providers.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 */
public class ServiceModelManager extends PlatformObject implements IServiceModelManager {
	private final static String SERVICE_EXTENSION_ID = "services"; //$NON-NLS-1$
	private final static String PROVIDER_EXTENSION_ID = "providers"; //$NON-NLS-1$
	private final static String CATEGORY_EXTENSION_ID = "serviceCategories"; //$NON-NLS-1$
	private final static String SERVICE_ELEMENT_NAME = "service"; //$NON-NLS-1$
	private final static String NATURE_ELEMENT_NAME = "nature"; //$NON-NLS-1$
	private final static String PROVIDER_ELEMENT_NAME = "provider"; //$NON-NLS-1$
	private final static String CATEGORY_ELEMENT_NAME = "category"; //$NON-NLS-1$
	private final static String DISABLED_PROVIDERS_ELEMENT_NAME = "disabledProviders"; //$NON-NLS-1$
	private final static String ATTR_ID = "id"; //$NON-NLS-1$
	private final static String ATTR_NAME = "name"; //$NON-NLS-1$
	private final static String ATTR_PRIORITY = "priority"; //$NON-NLS-1$
	private final static String ATTR_SERVICE_ID = "serviceId"; //$NON-NLS-1$
	private final static String ATTR_CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	private final static String ATTR_NULL_PROVIDER_CLASS = "nullProviderClass"; //$NON-NLS-1$
	private final static String ATTR_CLASS = "class"; //$NON-NLS-1$
	private final static String ATTR_ACTIVE = "active"; //$NON-NLS-1$
	private final static String SERVICE_MODEL_ELEMENT_NAME = "service-model"; //$NON-NLS-1$
	private final static String PROJECT_ELEMENT_NAME = "project"; //$NON-NLS-1$
	private final static String SERVICE_CONFIGURATION_ELEMENT_NAME = "service-configuration"; //$NON-NLS-1$
	private final static String PROVIDER_CONFIGURATION_ELEMENT_NAME = "provider-configuration"; //$NON-NLS-1$
	private final static String ATTR_PROVIDER_ID = "provider-id"; //$NON-NLS-1$
	private final static String DEFAULT_SAVE_FILE_NAME = "service_model.xml"; //$NON-NLS-1$

	private static <T> T getConf(Map<IProject, T> map, IProject project) {
		if (project == null) {
			throw new NullPointerException();
		}
		T value = map.get(project);
		if (value == null) {
			throw new ProjectNotConfiguredException(project.getName());
		}
		return value;
	}

	/**
	 * Save a collection of service configurations to the memento NOTE: does not
	 * actually save the memento
	 * 
	 * @param memento
	 *            memento used to save configurations
	 * @param configs
	 *            collection of serice configurations to save
	 */
	private static void saveConfigurations(IMemento memento, IServiceConfiguration[] configs) {
		for (IServiceConfiguration config : configs) {
			String configurationId = config.getId();
			String configurationName = config.getName();

			IMemento configMemento = memento.createChild(SERVICE_CONFIGURATION_ELEMENT_NAME);
			configMemento.putString(ATTR_ID, configurationId);
			configMemento.putString(ATTR_NAME, configurationName);

			Set<IService> services = config.getServices();
			for (IService service : services) {
				if (!config.isDisabled(service)) {
					IServiceProvider provider = config.getServiceProvider(service);
					IMemento serviceMemento = configMemento.createChild(SERVICE_ELEMENT_NAME);
					serviceMemento.putString(ATTR_ID, service.getId());
					serviceMemento.putString(ATTR_PROVIDER_ID, provider.getId());
					saveProviderState(provider, serviceMemento);
				}

				if (config instanceof ServiceConfiguration) {
					Set<IServiceProvider> disabledProviders = ((ServiceConfiguration) config).getFormerServiceProviders(service);
					if (!disabledProviders.isEmpty()) {
						IMemento disabledMemento = configMemento.createChild(DISABLED_PROVIDERS_ELEMENT_NAME);
						disabledMemento.putString(ATTR_ID, service.getId());
						for (IServiceProvider disabledProvider : disabledProviders) {
							IMemento providerMemento = disabledMemento.createChild(PROVIDER_ELEMENT_NAME);
							providerMemento.putString(ATTR_PROVIDER_ID, disabledProvider.getId());
							saveProviderState(disabledProvider, providerMemento);
						}
					}
				}
			}
		}
	}

	/**
	 * Save the model configuration to persistent storage
	 * 
	 * @param configs
	 * @param projectConfigs
	 * @param activeConfigs
	 * @param writer
	 * @throws IOException
	 */
	private static void saveModelConfiguration(Map<String, IServiceConfiguration> configs,
			Map<IProject, Map<String, IServiceConfiguration>> projectConfigs, Map<IProject, IServiceConfiguration> activeConfigs,
			Writer writer) throws IOException {

		XMLMemento rootMemento = XMLMemento.createWriteRoot(SERVICE_MODEL_ELEMENT_NAME);

		saveConfigurations(rootMemento, configs.values().toArray(new IServiceConfiguration[0]));

		for (Entry<IProject, Map<String, IServiceConfiguration>> entry : projectConfigs.entrySet()) {
			IProject project = entry.getKey();
			if (!project.exists()) {// Skip over deleted projects
				continue;
			}

			String projectName = project.getName();
			IMemento projectMemento = rootMemento.createChild(PROJECT_ELEMENT_NAME);
			projectMemento.putString(ATTR_NAME, projectName);

			Map<String, IServiceConfiguration> configurations = entry.getValue();
			for (IServiceConfiguration config : configurations.values()) {
				String configurationId = config.getId();

				IMemento configMemento = projectMemento.createChild(SERVICE_CONFIGURATION_ELEMENT_NAME);
				configMemento.putString(ATTR_ID, configurationId);
				IServiceConfiguration active = activeConfigs.get(project);
				configMemento.putBoolean(ATTR_ACTIVE, active != null);
			}
		}

		rootMemento.save(writer);
	}

	/**
	 * Save the state of a service provider
	 * 
	 * @param provider
	 * @param parentMemento
	 */
	private static void saveProviderState(IServiceProvider provider, IMemento parentMemento) {
		if (provider instanceof ServiceProvider) {
			IMemento providerConfigMemento = parentMemento.createChild(PROVIDER_CONFIGURATION_ELEMENT_NAME);
			((ServiceProvider) provider).saveState(providerConfigMemento);
		}
	}

	/** Default location to save service model configuration */
	private final IPath defaultSaveFile;
	private final Map<String, IServiceConfiguration> fConfigurations = new HashMap<String, IServiceConfiguration>();
	private final Map<IProject, Map<String, IServiceConfiguration>> fProjectConfigurations = new HashMap<IProject, Map<String, IServiceConfiguration>>();
	private final Map<IProject, IServiceConfiguration> fActiveConfigurations = new HashMap<IProject, IServiceConfiguration>();
	private final Map<IProject, Set<IService>> fProjectServices = new HashMap<IProject, Set<IService>>();
	private Map<String, Service> fServices = null;
	private Map<String, ServiceCategory> fCategories;
	private Set<IService> fServiceSet = null;
	private Map<String, Set<IService>> fNatureServices = null;
	private IServiceConfiguration fDefaultServiceConfiguration = null;
	private final ServiceModelEventManager fEventManager = new ServiceModelEventManager();
	private boolean fModelLoaded = false;
	private boolean fEventsEnabled = true;

	private static ServiceModelManager fInstance;

	public static synchronized ServiceModelManager getInstance() {
		if (fInstance == null) {
			fInstance = new ServiceModelManager();
		}
		return fInstance;
	}

	private ServiceModelManager() {
		defaultSaveFile = ServicesCorePlugin.getDefault().getStateLocation().append(DEFAULT_SAVE_FILE_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#addConfiguration(org
	 * .eclipse.core.resources.IProject,
	 * org.eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void addConfiguration(IProject project, IServiceConfiguration conf) {
		checkAndLoadModel();

		if (project == null || conf == null) {
			throw new NullPointerException();
		}

		Map<String, IServiceConfiguration> confs = fProjectConfigurations.get(project);
		if (confs == null) {
			confs = new HashMap<String, IServiceConfiguration>();
			fProjectConfigurations.put(project, confs);
			fActiveConfigurations.put(project, conf);
		}

		confs.put(conf.getId(), conf);

		Set<IService> services = fProjectServices.get(project);
		if (services == null) {
			services = new HashSet<IService>();
			fProjectServices.put(project, services);
		}

		for (IServiceConfiguration config : confs.values()) {
			for (IService service : config.getServices()) {
				services.add(service);
			}
		}

		addConfiguration(conf);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#addConfiguration(org
	 * .eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void addConfiguration(IServiceConfiguration conf) {
		checkAndLoadModel();
		if (fConfigurations.put(conf.getId(), conf) == null) {
			notifyListeners(new ServiceModelEvent(conf, IServiceModelEvent.SERVICE_CONFIGURATION_ADDED));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#addEventListener(org
	 * .eclipse.ptp.services.core.IServiceModelEventListener, int)
	 */
	public void addEventListener(IServiceModelEventListener listener, int type) {
		fEventManager.addEventListener(listener, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#exportConfigurations
	 * (java.lang.String, java.util.Set)
	 */
	public boolean exportConfigurations(String filename, IServiceConfiguration[] configs) throws InvocationTargetException {
		final File file = new File(filename);
		if (!file.exists()) {
			final Writer writer;
			try {
				writer = new BufferedWriter(new FileWriter(file));
			} catch (IOException e) {
				throw new InvocationTargetException(e);
			}
			try {
				final XMLMemento rootMemento = XMLMemento.createWriteRoot(SERVICE_MODEL_ELEMENT_NAME);
				saveConfigurations(rootMemento, configs);
				rootMemento.save(writer);
			} catch (IOException e) {
				throw new InvocationTargetException(e);
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getDefaultConfiguration
	 * ()
	 */
	public IServiceConfiguration getActiveConfiguration() {
		checkAndLoadModel();
		return fDefaultServiceConfiguration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getActiveConfiguration
	 * (org.eclipse.core.resources.IProject)
	 */
	public IServiceConfiguration getActiveConfiguration(IProject project) {
		checkAndLoadModel();
		return getConf(fActiveConfigurations, project);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getCategories()
	 */
	public Set<IServiceCategory> getCategories() {
		checkAndLoadModel();
		return new HashSet<IServiceCategory>(fCategories.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getConfiguration(org
	 * .eclipse.core.resources.IProject, java.lang.String)
	 */
	public IServiceConfiguration getConfiguration(IProject project, String name) {
		checkAndLoadModel();
		Map<String, IServiceConfiguration> confMap = getConf(fProjectConfigurations, project);
		for (IServiceConfiguration conf : confMap.values()) {
			if (conf.getName().equals(name)) {
				return conf;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getConfiguration(java
	 * .lang.String)
	 */
	public IServiceConfiguration getConfiguration(String id) {
		checkAndLoadModel();
		return fConfigurations.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getConfigurations()
	 */
	public Set<IServiceConfiguration> getConfigurations() {
		checkAndLoadModel();
		return new HashSet<IServiceConfiguration>(fConfigurations.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getConfigurations(
	 * org.eclipse.core.resources.IProject)
	 */
	public Set<IServiceConfiguration> getConfigurations(IProject project) {
		checkAndLoadModel();
		return new HashSet<IServiceConfiguration>(getConf(fProjectConfigurations, project).values());
	}

	/**
	 * Get the set of projects which use the specified service configuration
	 * 
	 * @param serviceConfiguration
	 *            The service configuration
	 * @return Set of projects which use the service configuration
	 */
	public Set<IProject> getProjectsForConfiguration(IServiceConfiguration serviceConfiguration) {
		checkAndLoadModel();

		Set<IProject> projects;
		Set<IProject> projectsForConfig;

		// Get the set of projects known to the service model manager
		projects = fProjectConfigurations.keySet();
		projectsForConfig = new HashSet<IProject>();
		// For each project, check if it uses the specified service
		// configuration
		// If so, add the project to the projectsForConfig set.
		for (IProject project : projects) {
			Set<IServiceConfiguration> configs;

			configs = getConfigurations(project);
			for (IServiceConfiguration config : configs) {
				if (config == serviceConfiguration) {
					projectsForConfig.add(project);
				}
			}
		}
		return projectsForConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getService(java.lang
	 * .String)
	 */
	public IService getService(String id) {
		loadServicesFromExtensionRegistry();
		return fServices.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getServiceProvider
	 * (org.eclipse.ptp.services.core.IServiceProviderDescriptor)
	 */
	public IServiceProvider getServiceProvider(IServiceProviderDescriptor desc) {
		/*
		 * Avoid generating SERVICE_PROVIDER_CHANGED events when service
		 * providers are created. This may happen if the constructor contains
		 * put method calls.
		 */
		fEventsEnabled = false;
		try {
			if (desc != null) {
				IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ServicesCorePlugin.PLUGIN_ID,
						PROVIDER_EXTENSION_ID);
				if (extensionPoint != null) {
					for (IExtension extension : extensionPoint.getExtensions()) {
						for (IConfigurationElement element : extension.getConfigurationElements()) {
							if (element.getName().equals(PROVIDER_ELEMENT_NAME)) {
								String attr = element.getAttribute(ATTR_ID);
								if (attr != null && attr.equals(desc.getId())) {
									try {
										IServiceProvider provider = (IServiceProvider) element
												.createExecutableExtension(ATTR_CLASS);
										provider.setDescriptor(desc);
										return provider;
									} catch (Exception e) {
										ServicesCorePlugin.getDefault().log(e);
										return null;
									}
								}
							}
						}
					}
				}
			}
			return null;
		} finally {
			fEventsEnabled = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceModelManager#getServices()
	 */
	public Set<IService> getServices() {
		loadServicesFromExtensionRegistry();
		return fServiceSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getServices(org.eclipse
	 * .core.resources.IProject)
	 */
	public Set<IService> getServices(IProject project) {
		checkAndLoadModel();
		return getConf(fProjectServices, project);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#getServices(java.lang
	 * .String)
	 */
	public Set<IService> getServices(String natureId) {
		loadServicesFromExtensionRegistry();
		return fNatureServices.get(natureId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#importConfigurations
	 * (java.lang.String)
	 */
	public IServiceConfiguration[] importConfigurations(String filename) throws InvocationTargetException {
		final File file = new File(filename);
		if (file.exists()) {
			final Reader reader;
			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				throw new InvocationTargetException(e);
			}
			try {
				final XMLMemento rootMemento = XMLMemento.createReadRoot(reader);
				setEnableEvents(false);
				return doLoadConfigurations(rootMemento, true);
			} catch (WorkbenchException e) {
				throw new InvocationTargetException(e);
			} finally {
				setEnableEvents(true);
				try {
					reader.close();
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#isConfigured(org.eclipse
	 * .core.resources.IProject)
	 */
	public boolean isConfigured(IProject project) {
		checkAndLoadModel();
		return fProjectConfigurations.containsKey(project);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#isValidConfigurationFile
	 * (java.lang.String)
	 */
	public boolean isValidConfigurationFile(String filename) {
		File file = new File(filename);
		if (file.exists()) {
			final Reader reader;
			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				return false;
			}
			try {
				final XMLMemento rootMemento = XMLMemento.createReadRoot(reader);
				final IMemento[] children = rootMemento.getChildren(SERVICE_CONFIGURATION_ELEMENT_NAME);
				if (children == null || children.length == 0) {
					return false;
				}
				for (IMemento configMemento : children) {
					String configId = configMemento.getString(ATTR_ID);
					if (configId == null) {
						return false;
					}
					String configName = configMemento.getString(ATTR_NAME);
					if (configName == null) {
						return false;
					}
					for (IMemento serviceMemento : configMemento.getChildren(SERVICE_ELEMENT_NAME)) {
						String serviceId = serviceMemento.getString(ATTR_ID);
						if (serviceId == null) {
							return false;
						}
						IService service = getService(serviceId);
						if (service == null) {
							return false;
						}
						if (!validateServiceProvider(serviceMemento, service)) {
							return false;
						}
					}
					for (IMemento disabledMemento : configMemento.getChildren(DISABLED_PROVIDERS_ELEMENT_NAME)) {
						String serviceId = disabledMemento.getString(ATTR_ID);
						if (serviceId == null) {
							return false;
						}
						IService service = getService(serviceId);
						if (service == null) {
							return false;
						}
						for (IMemento providerMemento : disabledMemento.getChildren(PROVIDER_ELEMENT_NAME)) {
							if (!validateServiceProvider(providerMemento, service)) {
								return false;
							}
						}
					}

				}
			} catch (WorkbenchException e) {
				return false;
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					// Too late now
				}
			}
		}
		return true;
	}

	/**
	 * Replaces the current service model configuration with what is specified
	 * in the default save file. If the file does not exist then this method
	 * does nothing.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.services.core<code> plugin.
	 * 
	 * @throws IOException
	 * @throws CoreException
	 */
	public void loadModelConfiguration() throws IOException, CoreException {
		loadServicesFromExtensionRegistry();
		File file = defaultSaveFile.toFile();
		if (file.exists()) {
			Reader reader = new BufferedReader(new FileReader(file));
			try {
				loadModelConfiguration(reader);
			} finally {
				reader.close();
			}
		}
		notifyListeners(new ServiceModelEvent(this, IServiceModelEvent.SERVICE_MODEL_LOADED));
	}

	/**
	 * Replaces the current service model configuration with what is specified
	 * in the given <code>file</code>.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.services.core<code> plugin.
	 * 
	 * @throws IOException
	 */
	public void loadModelConfiguration(Reader reader) throws IOException, CoreException {
		fModelLoaded = true; // avoid re-entry

		initialize(); // Clear out the existing model

		XMLMemento rootMemento = XMLMemento.createReadRoot(reader);

		for (IServiceConfiguration config : doLoadConfigurations(rootMemento, false)) {
			addConfiguration(config);
		}

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		for (IMemento projectMemento : rootMemento.getChildren(PROJECT_ELEMENT_NAME)) {
			String projectName = projectMemento.getString(ATTR_NAME);
			IProject project = root.getProject(projectName);

			if (!project.exists()) {
				continue;
			}

			for (IMemento configMemento : projectMemento.getChildren(SERVICE_CONFIGURATION_ELEMENT_NAME)) {
				String configId = configMemento.getString(ATTR_ID);
				IServiceConfiguration config = fConfigurations.get(configId);
				if (config != null) {
					addConfiguration(project, config);
					Boolean active = configMemento.getBoolean(ATTR_ACTIVE);
					if (active != null && active.booleanValue()) {
						setActiveConfiguration(project, config);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#newServiceConfiguration
	 * (java.lang.String)
	 */
	public IServiceConfiguration newServiceConfiguration(String name) {
		return newServiceConfiguration(UUID.randomUUID().toString(), name);
	}

	/**
	 * Notify listeners of an event occurrence.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.services.core<code> plugin.
	 * 
	 * @param event
	 *            event to notify
	 */
	public void notifyListeners(IServiceModelEvent event) {
		if (fEventsEnabled) {
			fEventManager.notifyListeners(event);
		}
	}

	/**
	 * Prints the current service model to the console, for debugging purposes.
	 */
	public void printServiceModel() {
		System.out.println("Service Model: "); //$NON-NLS-1$
		if (fProjectConfigurations.isEmpty()) {
			System.out.println("  Service Model is empty"); //$NON-NLS-1$
		}

		for (Entry<IProject, Map<String, IServiceConfiguration>> entry : fProjectConfigurations.entrySet()) {
			IProject project = entry.getKey();
			System.out.println("  Project: " + project.getName()); //$NON-NLS-1$
			for (IServiceConfiguration conf : entry.getValue().values()) {
				System.out.println("      " + conf); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @since 2.0
	 */
	public void remap(IProject removedProject, IProject addedProject) {
		if (removedProject == null || addedProject == null) {
			throw new NullPointerException();
		}

		if (isConfigured(removedProject)) {
			fProjectConfigurations.put(addedProject, fProjectConfigurations.remove(removedProject));
			fActiveConfigurations.put(addedProject, fActiveConfigurations.remove(removedProject));
			fProjectServices.put(addedProject, fProjectServices.remove(removedProject));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#remove(org.eclipse
	 * .core.resources.IProject)
	 */
	public void remove(IProject project) {
		checkAndLoadModel();
		if (project == null) {
			throw new NullPointerException();
		}
		fProjectConfigurations.remove(project);
		fActiveConfigurations.remove(project);
		fProjectServices.remove(project);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#remove(org.eclipse
	 * .ptp.services.core.IServiceConfiguration)
	 */
	public void remove(IServiceConfiguration conf) {
		checkAndLoadModel();
		for (IProject project : fProjectConfigurations.keySet()) {
			removeConfiguration(project, conf);
			if (conf.equals(getActiveConfiguration(project))) {
				fActiveConfigurations.remove(project);
			}
		}
		fConfigurations.remove(conf.getId());
		notifyListeners(new ServiceModelEvent(conf, IServiceModelEvent.SERVICE_CONFIGURATION_REMOVED));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#removeConfiguration
	 * (org.eclipse.core.resources.IProject,
	 * org.eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void removeConfiguration(IProject project, IServiceConfiguration conf) {
		checkAndLoadModel();
		Map<String, IServiceConfiguration> confs = getConf(fProjectConfigurations, project);
		if (confs != null) {
			confs.remove(conf.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#removeEventListener
	 * (org.eclipse.ptp.services.core.IServiceModelEventListener)
	 */
	public void removeEventListener(IServiceModelEventListener listener) {
		fEventManager.removeEventListener(listener);
	}

	/**
	 * Saves the model configuration into the plugin's metadata area using the
	 * default file name. Will not save data for projects that do not exist.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.services.core<code> plugin.
	 * 
	 * @throws IOException
	 */
	public void saveModelConfiguration() throws IOException {
		checkAndLoadModel();
		File file = defaultSaveFile.toFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		try {
			saveModelConfiguration(writer);
		} finally {
			writer.close();
		}
	}

	/**
	 * Saves the service model configuration to the given <code>file</code>.
	 * Will not save data for projects that do not exist.
	 * 
	 * This method is not meant to be called outside of the
	 * <code>org.eclipse.ptp.services.core<code> plugin.
	 * 
	 * @param file
	 * @throws IOException
	 * @throws NullPointerException
	 *             if file is null
	 */
	public void saveModelConfiguration(Writer writer) throws IOException {
		if (writer == null) {
			throw new NullPointerException();
		}
		saveModelConfiguration(fConfigurations, fProjectConfigurations, fActiveConfigurations, writer);
		notifyListeners(new ServiceModelEvent(this, IServiceModelEvent.SERVICE_MODEL_SAVED));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#setActiveConfiguration
	 * (org.eclipse.core.resources.IProject,
	 * org.eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void setActiveConfiguration(IProject project, IServiceConfiguration configuration) {
		checkAndLoadModel();

		Map<String, IServiceConfiguration> confs = getConf(fProjectConfigurations, project);

		if (!confs.containsKey(configuration.getId())) {
			throw new IllegalArgumentException();
		}

		fActiveConfigurations.put(project, configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.core.IServiceModelManager#setDefaultConfiguration
	 * (org.eclipse.ptp.services.core.IServiceConfiguration)
	 */
	public void setActiveConfiguration(IServiceConfiguration config) {
		fDefaultServiceConfiguration = config;
		notifyListeners(new ServiceModelEvent(config, IServiceModelEvent.SERVICE_CONFIGURATION_SELECTED));
	}

	/**
	 * Check if the model is already loaded. If not, load it.
	 * 
	 * This is used to ensure that the model is loaded prior to accessing it.
	 */
	private void checkAndLoadModel() {
		if (!fModelLoaded) {
			try {
				loadModelConfiguration();
			} catch (Exception e) {
				ServicesCorePlugin.getDefault().log(e);
			}
		}
	}

	/**
	 * Do the actual job of loading configurations.
	 * 
	 * If the configurations are being imported then a new ID is generated for
	 * each configuration. This is to avoid the import causing duplicate
	 * configuration IDs.
	 * 
	 * @param rootMemento
	 * @return
	 */
	private IServiceConfiguration[] doLoadConfigurations(IMemento rootMemento, boolean importing) {
		Set<IServiceConfiguration> configs = new HashSet<IServiceConfiguration>();

		for (IMemento configMemento : rootMemento.getChildren(SERVICE_CONFIGURATION_ELEMENT_NAME)) {
			String configId = configMemento.getString(ATTR_ID);
			if (importing) {
				configId = UUID.randomUUID().toString();
			}
			String configName = configMemento.getString(ATTR_NAME);
			ServiceConfiguration config = newServiceConfiguration(configId, configName);

			for (IMemento serviceMemento : configMemento.getChildren(SERVICE_ELEMENT_NAME)) {
				String serviceId = serviceMemento.getString(ATTR_ID);
				IService service = getService(serviceId);
				IServiceProvider provider = loadServiceProvider(serviceMemento, service);
				config.setServiceProvider(service, provider);
			}

			for (IMemento disabledMemento : configMemento.getChildren(DISABLED_PROVIDERS_ELEMENT_NAME)) {
				String serviceId = disabledMemento.getString(ATTR_ID);
				IService service = getService(serviceId);
				for (IMemento providerMemento : disabledMemento.getChildren(PROVIDER_ELEMENT_NAME)) {
					IServiceProvider provider = loadServiceProvider(providerMemento, service);
					config.addFormerServiceProvider(service, provider);
				}
			}

			configs.add(config);
		}

		return configs.toArray(new IServiceConfiguration[0]);
	}

	/**
	 * Initialize model
	 */
	private void initialize() {
		fActiveConfigurations.clear();
		fProjectConfigurations.clear();
		fProjectServices.clear();
		fConfigurations.clear();
	}

	/**
	 * Load a service provider from persistent state
	 * 
	 * @param providerMemento
	 * @param service
	 * @return
	 */
	private IServiceProvider loadServiceProvider(IMemento providerMemento, IService service) {
		if (service == null) {
			return null;
		}

		String providerId = providerMemento.getString(ATTR_PROVIDER_ID);
		IServiceProviderDescriptor descriptor = service.getProviderDescriptor(providerId);
		if (descriptor != null) {
			IServiceProvider provider = getServiceProvider(descriptor);
			if (provider != null) {
				if (provider instanceof ServiceProvider) {
					IMemento providerConfigMemento = providerMemento.getChild(PROVIDER_CONFIGURATION_ELEMENT_NAME);
					((ServiceProvider) provider).restoreState(providerConfigMemento);
				}
				return provider;
			} else {
				ServicesCorePlugin.getDefault().logErrorMessage(Messages.ServiceModelManager_2);
			}
		} else {
			ServicesCorePlugin.getDefault().logErrorMessage(Messages.ServiceModelManager_0 + providerId);
		}
		return null;
	}

	/**
	 * Locate and initialize service extensions.
	 */
	private void loadServicesFromExtensionRegistry() {
		if (fServices != null) {
			return;
		}
		fServices = new HashMap<String, Service>();
		fCategories = new HashMap<String, ServiceCategory>();
		fServiceSet = new HashSet<IService>();
		fNatureServices = new HashMap<String, Set<IService>>();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ServicesCorePlugin.PLUGIN_ID,
				CATEGORY_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(CATEGORY_ELEMENT_NAME)) {
						String id = element.getAttribute(ATTR_ID);
						String name = element.getAttribute(ATTR_NAME);
						ServiceCategory category = new ServiceCategory(id, name);
						fCategories.put(id, category);
					}
				}
			}
		}

		extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ServicesCorePlugin.PLUGIN_ID, SERVICE_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(SERVICE_ELEMENT_NAME)) {
						String id = element.getAttribute(ATTR_ID);
						String name = element.getAttribute(ATTR_NAME);
						String priority = element.getAttribute(ATTR_PRIORITY);
						String categoryId = element.getAttribute(ATTR_CATEGORY_ID);
						IConfigurationElement[] natureConf = element.getChildren(NATURE_ELEMENT_NAME);
						Set<String> natures = new HashSet<String>();
						if (natureConf != null) {
							for (IConfigurationElement nature : natureConf) {
								String natureId = nature.getAttribute(ATTR_ID);
								if (workspace.getNatureDescriptor(natureId) != null) {
									natures.add(natureId);
								}
							}
						}
						Service service = new Service(id, name, priority, natures);
						fServiceSet.add(service);
						fServices.put(id, service);
						for (String nature : natures) {
							Set<IService> svcs = fNatureServices.get(nature);
							if (svcs == null) {
								svcs = new HashSet<IService>();
								fNatureServices.put(nature, svcs);
							}
							svcs.add(service);
						}

						if (element.getAttribute(ATTR_NULL_PROVIDER_CLASS) != null) {
							try {
								IServiceProvider nullProvider = (IServiceProvider) element
										.createExecutableExtension(ATTR_NULL_PROVIDER_CLASS);
								if (nullProvider instanceof ServiceProvider) {
									String providerId = service.getId() + ".nullProvider"; //$NON-NLS-1$
									ServiceProviderDescriptor descriptor = new ServiceProviderDescriptor(providerId,
											Messages.ServiceModelManager_3, service.getId(), "0"); //$NON-NLS-1$
									((ServiceProvider) nullProvider).setDescriptor(descriptor);
								}
								service.setNullServiceProvider(nullProvider);
							} catch (CoreException e) {
								ServicesCorePlugin.getDefault().log(e);
							}
						}

						ServiceCategory category = fCategories.get(categoryId);
						if (category != null) {
							category.addService(service);
							service.setCategory(category);
						}
					}
				}
			}
		}
		extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ServicesCorePlugin.PLUGIN_ID, PROVIDER_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getName().equals(PROVIDER_ELEMENT_NAME)) {
						String id = element.getAttribute(ATTR_ID);
						String name = element.getAttribute(ATTR_NAME);
						String priority = element.getAttribute(ATTR_PRIORITY);
						String serviceId = element.getAttribute(ATTR_SERVICE_ID);
						IServiceProviderDescriptor desc = new ServiceProviderDescriptor(id, name, serviceId, priority);
						Service service = fServices.get(serviceId);
						if (service != null) {
							service.addServiceProvider(desc);
						} else {
							ServicesCorePlugin.getDefault()
									.logErrorMessage(NLS.bind(Messages.Services_invalidServiceId, serviceId));
						}
					}
				}
			}
		}
	}

	/**
	 * Create a service configuration with the specified id and name. Used when
	 * restoring saved state.
	 * 
	 * @param id
	 *            id of service configuration
	 * @param name
	 *            name of service configuration
	 * @return service configuration
	 */
	private ServiceConfiguration newServiceConfiguration(String id, String name) {
		return new ServiceConfiguration(id, name);
	}

	/**
	 * Enable/disable model events.
	 * 
	 * @param enable
	 */
	private void setEnableEvents(boolean enable) {
		fEventsEnabled = enable;
	}

	/**
	 * Validate a service provider from persistent state
	 * 
	 * @param providerMemento
	 * @param service
	 * @return
	 */
	private boolean validateServiceProvider(IMemento providerMemento, IService service) {
		String providerId = providerMemento.getString(ATTR_PROVIDER_ID);
		if (providerId == null) {
			return false;
		}
		IServiceProviderDescriptor descriptor = service.getProviderDescriptor(providerId);
		if (descriptor == null) {
			return false;
		}
		IMemento providerConfigMemento = providerMemento.getChild(PROVIDER_CONFIGURATION_ELEMENT_NAME);
		if (providerConfigMemento == null) {
			return false;
		}
		return true;
	}
}
