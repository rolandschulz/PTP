/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.rm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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
public class JAXBRMConfigurationManager implements IJAXBNonNLSConstants {

	private static JAXBRMConfigurationManager instance;

	private final Map<String, String> external;
	private String[] types;
	private Properties rmXmlNames;
	private Properties rmXmlValues;

	private JAXBRMConfigurationManager() {
		setInternal();
		this.external = new TreeMap<String, String>();
	}

	public void addExternalPath(String path) {
		if (new File(path).exists()) {
			external.put(path, null);
		}
	}

	public void addExternalPaths(String[] path) {
		for (String p : path) {
			addExternalPath(p);
		}
	}

	public String[] getExternal() {
		pruneExternal();
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

	private void pruneExternal() {
		for (Iterator<String> k = external.keySet().iterator(); k.hasNext();) {
			String key = k.next();
			if (!new File(key).exists()) {
				k.remove();
			}
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

	public synchronized static JAXBRMConfigurationManager getInstance() {
		if (instance == null) {
			instance = new JAXBRMConfigurationManager();
		}
		return instance;
	}
}
