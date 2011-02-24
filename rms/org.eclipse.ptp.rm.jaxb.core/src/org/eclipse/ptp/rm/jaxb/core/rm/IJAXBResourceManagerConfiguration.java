package org.eclipse.ptp.rm.jaxb.core.rm;

import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.rmsystem.IControlMonitorRMConfiguration;

public interface IJAXBResourceManagerConfiguration extends IControlMonitorRMConfiguration {
	/**
	 * @since 5.0
	 */
	void addExternalRMInstanceXMLLocation(String location);

	/**
	 * @since 5.0
	 */
	String getDefaultControlHost();

	/**
	 * @since 5.0
	 */
	String getDefaultControlPath();

	/**
	 * @since 5.0
	 */
	String getDefaultControlPort();

	/**
	 * @since 5.0
	 */
	String getDefaultMonitorHost();

	/**
	 * @since 5.0
	 */
	String getDefaultMonitorPath();

	/**
	 * @since 5.0
	 */
	String getDefaultMonitorPort();

	/**
	 * @since 5.0
	 */

	String[] getExternalRMInstanceXMLLocations();

	/**
	 * @since 5.0
	 */
	String getRMInstanceXMLLocation();

	/**
	 * @since 5.0
	 */
	String getSelectedAttributeSet();

	/**
	 * @since 5.0
	 */
	IRemoteServices getService();

	/**
	 * @since 5.0
	 */
	String getValidAttributeSet();

	/**
	 * @since 5.0
	 */
	void realizeRMDataFromXML() throws Throwable;

	/**
	 * @since 5.0
	 */
	void removeSelectedAttributeSet();

	/**
	 * @since 5.0
	 */
	void removeValidAttributeSet();

	/**
	 * @since 5.0
	 */
	ResourceManagerData resourceManagerData();

	/**
	 * @since 5.0
	 */
	void setRMInstanceXMLLocation(String location);

	/**
	 * @since 5.0
	 */
	void setSelectedAttributeSet(String serialized);

	/**
	 * @since 5.0
	 */
	void setService(IRemoteServices service);

	/**
	 * @since 5.0
	 */
	void setValidAttributeSet(String serialized);
}
