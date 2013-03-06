/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.jaxb.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.etfw.jaxb.ETFWCoreConstants;
import org.eclipse.ptp.etfw.jaxb.JAXBInitializationUtil;
import org.eclipse.ptp.etfw.jaxb.data.EtfwToolProcessType;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.xml.sax.SAXException;

/**
 * Utilities methods for reading an ETFw workflow and the available workflows.
 * 
 * @author "Chris Navarro"
 * 
 */
public class JAXBExtensionUtils {

	public static Map<String, IFileStore> fPluginWorkflows = new TreeMap<String, IFileStore>();

	public static String[] getToolNames() {
		loadExtensions();

		Set<String> names = fPluginWorkflows.keySet();
		return names.toArray(new String[0]);
	}

	public static EtfwToolProcessType getTool(String toolName) {
		loadExtensions();
		IFileStore toolFile = fPluginWorkflows.get(toolName);
		EtfwToolProcessType tool = null;
		try {
			String toolXML = JAXBInitializationUtil.getETFWConfigurationXML(toolFile.toURI().toURL());
			tool = JAXBInitializationUtil.initializeEtfwToolProcessType(toolXML);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return tool;
	}

	private static void loadExtensions() {
		if (fPluginWorkflows.isEmpty()) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(ETFWCoreConstants.WORKFLOW_EXT_PT);
			final IExtension[] extensions = extensionPoint.getExtensions();

			for (int iext = 0; iext < extensions.length; ++iext) {
				final IExtension ext = extensions[iext];

				final IConfigurationElement[] elements = ext.getConfigurationElements();

				IFileStore ifs = null;
				for (int i = 0; i < elements.length; i++)
				{
					IConfigurationElement ce = elements[i];
					try {
						String plugspace = ext.getNamespaceIdentifier();
						String toolFile = ce.getAttribute("file"); //$NON-NLS-1$
						String name = ce.getAttribute("name"); //$NON-NLS-1$

						URI toolFileUri = new URI(FileLocator.toFileURL((Platform.getBundle(plugspace).getEntry(toolFile)))
								.toString()
								.replaceAll(JAXBCoreConstants.SP, "%20")); //$NON-NLS-1$
						ifs = EFS.getLocalFileSystem().getStore(toolFileUri);
						fPluginWorkflows.put(name, ifs);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static Map<String, IFileStore> getWorkflows() {
		loadExtensions();
		return fPluginWorkflows;
	}
}
