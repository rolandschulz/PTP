/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw.jaxb.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.internal.etfw.jaxb.ETFWCoreConstants;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;

/**
 * Utility class to parse arguments and environment variables for ToolPanes
 * 
 * @author Chris Navarro
 * 
 */
public class ToolPaneTypeUtil {

	public static String getArgument(ILaunchConfiguration configuration, String configID) {
		try {
			String controlId = configuration.getAttribute(ETFWCoreConstants.RM_NAME,
					JAXBCoreConstants.ZEROSTR);
			String attributeKey = controlId + JAXBCoreConstants.DOT + configID;
			String args = new String(JAXBCoreConstants.ZEROSTR);
			Iterator<?> iterator = configuration.getAttributes().keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next().toString();
				if (key.startsWith(attributeKey) && !key.endsWith(ETFWCoreConstants.PAIRED_ATTRIBUTE_SAVED)) {
					String value = JAXBCoreConstants.ZEROSTR;
					try {
						// Try string attribute
						value = configuration.getAttribute(key, JAXBCoreConstants.ZEROSTR);
					} catch (DebugException e) {
						try {
							value = Integer.toString(configuration.getAttribute(key, 0));
						} catch (DebugException e1) {
							// e1.printStackTrace();
							// just ignore the value
						}
					}

					if (!value.trim().isEmpty()) {
						if (value.endsWith(JAXBCoreConstants.EQ)) {
							// Locate paired attribute
							String pairedKey = key + ETFWCoreConstants.PAIRED_ATTRIBUTE_SAVED;
							if (configuration.hasAttribute(pairedKey)) {
								value += configuration.getAttribute(pairedKey, JAXBCoreConstants.ZEROSTR);
							}
						}
						args += value + JAXBCoreConstants.SP;
					}
				}
			}
			args = args.trim();
			return args;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}

	public static Map<String, String> getEnvVars(ILaunchConfiguration configuration, String configVarID) {

		// Doing try/catch on the variable type doesn't seem right, is there a better way?
		// TODO Handle other potential variable types (e.g. double)
		Map<String, String> envMap = null;
		try {
			// TODO remove appending controlId to variable name, there is really no need for this
			String controlId = configuration.getAttribute(ETFWCoreConstants.RM_NAME, JAXBCoreConstants.ZEROSTR);
			envMap = new HashMap<String, String>();
			Iterator<?> iterator = configuration.getAttributes().keySet().iterator();
			String keyStartsWith = controlId + JAXBCoreConstants.DOT + configVarID;
			while (iterator.hasNext()) {
				String key = iterator.next().toString();
				if (key.startsWith(keyStartsWith)) {
					
					String value = ""; //$NON-NLS-1$
					if (key.endsWith("MAP")) {
						Map map = configuration.getAttribute(key, new HashMap<String, String>());
						Iterator mapIterator = map.keySet().iterator();
						while(mapIterator.hasNext()) {
							Object envKey = mapIterator.next();
							Object envValue = map.get(envKey);
							envMap.put(envKey.toString().trim(), envValue.toString().trim());
							
						}
					}
					else{
						// split the key with the variable name (after the last underscore)
						String mapKey = key.replace(keyStartsWith, JAXBCoreConstants.ZEROSTR);
					try {
						// Try string attribute
						value = configuration.getAttribute(key, JAXBCoreConstants.ZEROSTR);
					} catch (DebugException e) {
						try {
							// Try integer attribute
							value = Integer.toString(configuration.getAttribute(key, 0));
						} catch (DebugException e1) {
							// Ignore other types
						}
					}
					if (value != null && value.trim().length() > 0) {
						envMap.put(mapKey, value);
					}
				}
				}
			}
			return envMap;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
