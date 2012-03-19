package org.eclipse.ptp.rm.launch.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ptp.core.IServiceConstants;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory;
import org.eclipse.ptp.ui.wizards.RMProviderContributor;

public class ProviderInfo implements Comparable<ProviderInfo> {
	public static ProviderInfo getProvider(String name) {
		getProviders();
		return fProviderMap.get(name);
	}

	public static List<ProviderInfo> getProviders() {
		if (fProviders == null) {
			fProviders = new ArrayList<ProviderInfo>();
			fProviderMap = new HashMap<String, ProviderInfo>();
			Set<IServiceProviderDescriptor> providers = ServiceModelManager.getInstance()
					.getService(IServiceConstants.LAUNCH_SERVICE).getProvidersByPriority();
			for (IServiceProviderDescriptor desc : providers) {
				/*
				 * Check if this provider has an extension
				 */
				RMConfigurationSelectionFactory factory = RMProviderContributor.getRMConfigurationSelectionFactory(desc.getId());
				if (factory != null) {
					for (String name : factory.getConfigurationNames()) {
						ProviderInfo info = new ProviderInfo(name, desc, factory);
						fProviders.add(info);
						fProviderMap.put(name, info);
					}
				}
			}
			Collections.sort(fProviders);
		}
		return fProviders;
	}

	private final String fName;
	private final IServiceProviderDescriptor fDescriptor;
	private final RMConfigurationSelectionFactory fFactory;

	private static List<ProviderInfo> fProviders = null;
	private static Map<String, ProviderInfo> fProviderMap = null;

	public ProviderInfo(String name, IServiceProviderDescriptor descriptor, RMConfigurationSelectionFactory factory) {
		this.fName = name;
		this.fDescriptor = descriptor;
		this.fFactory = factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ProviderInfo o) {
		return fName.compareTo(o.fName);
	}

	/**
	 * @return the descriptor
	 */
	public IServiceProviderDescriptor getDescriptor() {
		return fDescriptor;
	}

	/**
	 * @return the factory
	 */
	public RMConfigurationSelectionFactory getFactory() {
		return fFactory;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return fName;
	}

}
