package org.eclipse.ptp.rmsystem;

import org.eclipse.core.runtime.Assert;

public class ResourceManagerConfiguration implements
		IResourceManagerConfiguration {

	/** The name of resource manager */
	private final String name;
	/** A description of resource manager */
	private final String description;
	/** The id of the resource manager*/
	private final String resourceManagerId;
	/** The host of resource manager, if any */
	private final String host;
	/** The port of resource manager, if any */
	private final int port;

	/**
	 * Creates a copy of a resource manager configuration
	 *
	 * @param config the configuration to copy
	 */
	public ResourceManagerConfiguration(IResourceManagerConfiguration config) {
		this(config.getName(), config.getDescription(), config.getResourceManagerId(), config.getHost(), config.getPort());
	}

	/**
	 * Creates a resource manager configuration
	 *
	 * @param name the name of the resource manager
	 * @param description the description of the resource manager
	 * @param resourceManagerId the id of the resource manager which resides on the resource manager 
	 * @param host the host of the resource manger
	 * @param port the port of the resource manger
	 */
	public ResourceManagerConfiguration(String name, String description, String resourceManagerId, String host, int port) {
		Assert.isNotNull(description);
		this.name = name;
		this.description= description;
		Assert.isNotNull(resourceManagerId);
		this.resourceManagerId= resourceManagerId;
		this.host = host;
		this.port = port;
	}

	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return name.hashCode() ^ host.hashCode() ^ new Integer(port).hashCode();
	}

	/**
	 * Returns the name of the resource manger.
	 *
	 * @return the name of the resource manger
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the description of the resource manger
	 *
	 *
	 * @return the description of the resource manger
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Returns the id of the resource manager
	 *
	 * @return the id of the resource manager
	 */
	public String getResourceManagerId() {
		return this.resourceManagerId;
	}

	/**
	 * Returns the resource manger host.
	 *
	 * @return the resource manger host
	 */
	public String getHost() {
		return this.host;
	}

	/**
	 * Returns the resource manger port.
	 *
	 * @return the resource manger port
	 */
	public int getPort() {
		return this.port;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof IResourceManagerConfiguration))
			return false;

		IResourceManagerConfiguration t= (IResourceManagerConfiguration) o;
		if (t == this)
			return true;

		return t.getName().equals(name)
				&& t.getResourceManagerId().equals(resourceManagerId)
				&& t.getHost().equals(host)
				&& t.getPort() == port;
	}

}
