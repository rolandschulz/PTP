/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - original API
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.attributes;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;

/**
 * Defines data structure wrapping attribute definition with additional
 * information.
 * 
 * @since 5.0
 */
public interface IPBSJobAttributeData {
	void deserialize(InputStream inputStream) throws Throwable;

	Map<String, IAttributeDefinition<?, ?, ?>> getAttributeDefinitionMap() throws Throwable;

	Map<String, String[]> getConstrained() throws Throwable;

	Map<String, String> getMinSet() throws Throwable;

	Properties getPBSQsubFlags() throws Throwable;

	Properties getToolTips() throws Throwable;

	void serialize(OutputStream outputStream) throws Throwable;
}
