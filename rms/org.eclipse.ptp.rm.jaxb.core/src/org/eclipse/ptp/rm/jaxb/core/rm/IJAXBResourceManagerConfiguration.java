package org.eclipse.ptp.rm.jaxb.core.rm;

import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.rmsystem.IControlMonitorRMConfiguration;

public interface IJAXBResourceManagerConfiguration extends IControlMonitorRMConfiguration {

	/**
	 * @since 5.0
	 */
	public String getDefaultControlHost();

	/**
	 * @since 5.0
	 */
	public String getDefaultControlPath();

	/**
	 * @since 5.0
	 */
	public String getDefaultControlPort();

	/**
	 * @since 5.0
	 */
	public String getDefaultMonitorHost();

	/**
	 * @since 5.0
	 */
	public String getDefaultMonitorPath();

	/**
	 * @since 5.0
	 */
	public String getDefaultMonitorPort();

	/**
	 * @since 5.0
	 */
	public ResourceManagerData getResourceManagerData();

	/**
	 * @since 5.0
	 */
	public String getRMInstanceXMLLocation();

	/**
	 * @since 5.0
	 */
	public String getSelectedAttributeSet();

	/**
	 * @since 5.0
	 */
	public String getValidAttributeSet();

	/**
	 * @since 5.0
	 */
	public void realizeRMDataFromXML() throws Throwable;

	/**
	 * @since 5.0
	 */
	public void removeSelectedAttributeSet();

	/**
	 * @since 5.0
	 */
	public void removeValidAttributeSet();

	/**
	 * @since 5.0
	 */
	public void setRMInstanceXMLLocation(String location);

	/**
	 * @since 5.0
	 */
	public void setSelectedAttributeSet(String serialized);

	/**
	 * @since 5.0
	 */
	public void setValidAttributeSet(String serialized);
}
