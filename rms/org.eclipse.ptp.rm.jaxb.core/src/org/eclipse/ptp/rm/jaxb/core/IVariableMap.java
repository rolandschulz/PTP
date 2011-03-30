/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public interface IVariableMap {

	Map<String, ?> getDiscovered();

	String getString(String value);

	String getString(String jobId, String value);

	Map<String, ?> getVariables();

	void maybeOverwrite(String key1, String key2, ILaunchConfiguration configuration) throws CoreException;

}
