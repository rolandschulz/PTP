/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.parameters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.Parameters.Parameter;

/**
 * 
 * @author Greg Watson
 *
 */
public class OmpiInfo implements Cloneable {

	private Map<String, String> kvs = new HashMap<String, String>();
	private Parameters params = new Parameters();

	/**
	 * @param name
	 */
	public void add(String key, String value) {
		kvs.put(key, value);
	}

	public Parameter addParameter(String name) {
		return params.addParameter(name);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public OmpiInfo clone() throws CloneNotSupportedException {
		OmpiInfo newInfo = new OmpiInfo();
		newInfo.kvs = new HashMap<String, String>(kvs);
		newInfo.params = params.clone();
		return newInfo;
	}

	/**
	 * @param name
	 * @return
	 */
	public String get(String key) {
		return kvs.get(key);
	}

	public Parameters.Parameter getParameter(String name) {
		return params.getParameter(name);
	}
	
	public Parameter[] getParameters() {
		return params.getParameters();
	}

}
