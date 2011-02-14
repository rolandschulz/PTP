package org.eclipse.ptp.rm.jaxb.core.rm;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.rmsystem.AbstractControlMonitorRMServiceProvider;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;

public class JAXBServiceProvider extends AbstractControlMonitorRMServiceProvider implements IJAXBResourceManagerConfiguration,
		IJAXBNonNLSConstants {

	public JAXBServiceProvider() {
		super();
		setDescription(Messages.JAXBServiceProvider_defaultDescription);
	}

	/**
	 * Constructor for creating a working copy of the service provider Don't
	 * register listeners as this copy will just be discarded at some point.
	 * 
	 * @param provider
	 *            provider we are making a copy from
	 */
	public JAXBServiceProvider(IServiceProvider provider) {
		super(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProvider#copy()
	 */
	@Override
	public IServiceProviderWorkingCopy copy() {
		return new JAXBServiceProvider(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IResourceManagerControl.class)
			return new JAXBResourceManager(PTPCorePlugin.getDefault().getModelManager().getUniverse(), this);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#
	 * getResourceManagerId()
	 */
	@Override
	public String getResourceManagerId() {
		return getId();
	}

	public String getRMInstanceXMLLocation() {

		return getString(RM_XSD_PATH, ZEROSTR);
	}

	public String getSelectedAttributeSet() {
		return getString(SELECTED_ATTRIBUTES, null);
	}

	public String getValidAttributeSet() {
		return getString(VALID_ATTRIBUTES, null);
	}

	public void removeSelectedAttributeSet() {
		keySet().remove(SELECTED_ATTRIBUTES);
	}

	public void removeValidAttributeSet() {
		keySet().remove(VALID_ATTRIBUTES);
	}

	public void setDefaultNameAndDesc() {
		String name = JAXB;
		String conn = getConnectionName();
		if (conn != null && !conn.equals(ZEROSTR))
			name += AMP + conn;
		setName(name);
		setDescription(Messages.JAXBServiceProvider_defaultDescription);
	}

	public void setRMInstanceXMLLocation(String location) {
		putString(RM_XSD_PATH, location);
	}

	public void setSelectedAttributeSet(String serialized) {
		putString(SELECTED_ATTRIBUTES, serialized);
	}

	public void setValidAttributeSet(String serialized) {
		putString(VALID_ATTRIBUTES, serialized);
	}
}
