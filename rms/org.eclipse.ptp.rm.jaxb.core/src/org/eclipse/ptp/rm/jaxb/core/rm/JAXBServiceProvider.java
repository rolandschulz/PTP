package org.eclipse.ptp.rm.jaxb.core.rm;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.Site;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.rmsystem.AbstractControlMonitorRMServiceProvider;
import org.eclipse.ptp.rm.jaxb.core.xml.JAXBUtils;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;

public class JAXBServiceProvider extends AbstractControlMonitorRMServiceProvider implements IJAXBResourceManagerConfiguration,
		IJAXBNonNLSConstants {

	private ResourceManagerData rmdata;

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

	public void addExternalRMInstanceXMLLocation(String location) {
		StringBuffer list = new StringBuffer(getString(EXTERNAL_RM_XSD_PATHS, ZEROSTR));
		if (list.length() > 0) {
			list.append(CM);
		}
		list.append(location);
		putString(EXTERNAL_RM_XSD_PATHS, list.toString());
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
		if (adapter == IResourceManagerControl.class) {
			return new JAXBResourceManager(PTPCorePlugin.getDefault().getModelManager().getUniverse(), this);
		}
		return null;
	}

	public String getDefaultControlHost() {
		if (rmdata != null) {
			Site site = rmdata.getSite();
			URI defaultURI = null;
			if (site != null) {
				try {
					defaultURI = new URI(site.getControlConnection());
					if (defaultURI != null) {
						return defaultURI.getHost();
					}
				} catch (URISyntaxException t) {
					t.printStackTrace();
				}
			}
		}
		return ZEROSTR;
	}

	public String getDefaultControlPath() {
		if (rmdata != null) {
			Site site = rmdata.getSite();
			URI defaultURI = null;
			if (site != null) {
				try {
					defaultURI = new URI(site.getControlConnection());
					if (defaultURI != null) {
						return defaultURI.getPath();
					}
				} catch (URISyntaxException t) {
					t.printStackTrace();
				}
			}
		}
		return ZEROSTR;
	}

	public String getDefaultControlPort() {
		if (rmdata != null) {
			Site site = rmdata.getSite();
			URI defaultURI = null;
			if (site != null) {
				try {
					defaultURI = new URI(site.getControlConnection());
					if (defaultURI != null) {
						int p = defaultURI.getPort();
						if (p != -1) {
							return ZEROSTR + p;
						}
					}
				} catch (URISyntaxException t) {
					t.printStackTrace();
				}
			}
		}
		return ZEROSTR;
	}

	public String getDefaultMonitorHost() {
		if (rmdata != null) {
			Site site = rmdata.getSite();
			URI defaultURI = null;
			if (site != null) {
				try {
					defaultURI = new URI(site.getMonitorServerInstall());
					if (defaultURI != null) {
						return defaultURI.getHost();
					}
				} catch (URISyntaxException t) {
					t.printStackTrace();
				}
			}
		}
		return ZEROSTR;
	}

	public String getDefaultMonitorPath() {
		if (rmdata != null) {
			Site site = rmdata.getSite();
			URI defaultURI = null;
			if (site != null) {
				try {
					String uri = site.getMonitorServerInstall();
					if (uri != null && uri.length() > 0) {
						defaultURI = new URI(site.getMonitorServerInstall());
						return defaultURI.getPath();
					}
				} catch (URISyntaxException t) {
					t.printStackTrace();
				}
			}
		}
		return ZEROSTR;
	}

	public String getDefaultMonitorPort() {
		if (rmdata != null) {
			Site site = rmdata.getSite();
			URI defaultURI = null;
			if (site != null) {
				try {
					defaultURI = new URI(site.getMonitorServerInstall());
					int p = defaultURI.getPort();
					if (p != -1) {
						return ZEROSTR + p;
					}
				} catch (URISyntaxException t) {
					t.printStackTrace();
				}
			}
		}
		return ZEROSTR;
	}

	public String[] getExternalRMInstanceXMLLocations() {
		String list = getString(EXTERNAL_RM_XSD_PATHS, ZEROSTR);
		return list.split(CM);
	}

	public ResourceManagerData getResourceManagerData() {
		return rmdata;
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

	public void realizeRMDataFromXML() throws Throwable {
		String location = getRMInstanceXMLLocation();
		if (ZEROSTR.equals(location)) {
			rmdata = null;
		} else {
			rmdata = JAXBUtils.initializeRMData(location);
		}
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
		if (conn != null && !conn.equals(ZEROSTR)) {
			name += AMP + conn;
		}
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
