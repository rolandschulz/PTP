package org.eclipse.ptp.rdt.sync.core;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;

/**
 * A generic host machine configuration.
 * Currently, this class simply wraps the CDT configurations, providing a generic host configuration object that can be passed to
 * clients. Eventually, this class will be expanded to take over the role of the CDT configurations to support non-CDT projects.
 */
public class HostConfig {
	private final IConfiguration config;

	/**
	 * Create a new host configuration that wraps the given CDT configuration.
	 *
	 * @param c 
	 *          host configuration
	 */
	public HostConfig(IConfiguration c) {
		config = c;
	}

	/**
	 * Get CDT configuration
	 * @return 
	 * @return CDT configuration
	 */
	public IConfiguration getCDTConfig() {
		return config;
	}

	/**
	 * Get configuration name
	 * @return name
	 */
	public String getName() {
		return config.getName();
	}
}