package org.eclipse.ptp.rm.jaxb.core.rm;

import org.eclipse.ptp.rm.jaxb.core.rmsystem.IControlMonitorRMConfiguration;

public interface IJAXBResourceManagerConfiguration extends IControlMonitorRMConfiguration {

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
