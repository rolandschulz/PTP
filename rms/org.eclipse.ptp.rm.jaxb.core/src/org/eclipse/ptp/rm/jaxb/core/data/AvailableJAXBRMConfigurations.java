package org.eclipse.ptp.rm.jaxb.core.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.osgi.framework.Bundle;

/**
 * A singleton class.
 * 
 * @author arossi
 * 
 */
public class AvailableJAXBRMConfigurations implements IJAXBNonNLSConstants {

	private static AvailableJAXBRMConfigurations instance;
	private String[] types;
	private Properties rmXmlNames;
	private Properties rmXmlValues;
	private Map<String, String> external;

	private AvailableJAXBRMConfigurations() {
		setInternal();
		setExternal(null);
	}

	public void addExternalPath(String path) {
		external.put(path, null);
	}

	public void addExternalPaths(String[] path) {
		for (String p : path) {
			external.put(p, null);
		}
	}

	public String[] getExternal() {
		List<String> list = new ArrayList<String>(external.keySet());
		list.add(0, ZEROSTR);
		return list.toArray(new String[0]);
	}

	public String getPathForType(String type) {
		return rmXmlNames.getProperty(type);
	}

	public String getTypeForPath(String path) {
		return rmXmlValues.getProperty(path);
	}

	public String[] getTypes() {
		return types;
	}

	public void removeExternal(String[] path) {
		for (String p : path) {
			external.remove(p);
		}
	}

	public void removeExternalPath(String path) {
		external.remove(path);
	}

	private void getPluginResourceConfigurations() throws IOException {
		rmXmlNames = new Properties();
		rmXmlValues = new Properties();
		URL url = null;
		if (JAXBCorePlugin.getDefault() != null) {
			Bundle bundle = JAXBCorePlugin.getDefault().getBundle();
			url = FileLocator.find(bundle, new Path(DATA + RM_CONFIG_PROPS), null);
		} else {
			url = new File(RM_CONFIG_PROPS).toURL();
		}

		if (url == null) {
			return;
		}
		InputStream s = null;
		try {
			s = url.openStream();
			rmXmlNames.load(s);
		} finally {
			try {
				if (s != null) {
					s.close();
				}
			} catch (IOException e) {
			}
		}

		for (Object name : rmXmlNames.keySet()) {
			String value = (String) name;
			String key = rmXmlNames.getProperty(value);
			rmXmlValues.setProperty(key, value);
		}
	}

	private void setExternal(String[] external) {
		this.external = new TreeMap<String, String>();
		if (external == null) {
			return;
		}
		for (int i = 0; i < external.length; i++) {
			this.external.put(external[i], null);
		}
	}

	private void setInternal() {
		try {
			getPluginResourceConfigurations();
			List<Object> list = new ArrayList<Object>();
			list.add(ZEROSTR);
			list.addAll(rmXmlNames.keySet());
			types = list.toArray(new String[0]);
		} catch (IOException t) {
			t.printStackTrace();
			types = new String[0];
		}
	}

	public synchronized static AvailableJAXBRMConfigurations getInstance() {
		if (instance == null) {
			instance = new AvailableJAXBRMConfigurations();
		}
		return instance;
	}
}
