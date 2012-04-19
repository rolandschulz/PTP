/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rm.jaxb.control.internal.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.internal.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.core.AbstractJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBInitializationUtils;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * LML_JAXB implementation.
 * 
 * @author arossi
 * 
 */
public class JAXBResourceManagerConfiguration extends AbstractJAXBResourceManagerConfiguration {
	protected IVariableMap map;

	/**
	 * @param namespace
	 *            base, control or monitor
	 * @param provider
	 *            base provider configuration
	 */
	public JAXBResourceManagerConfiguration(String namespace, IServiceProvider provider) {
		super(namespace, provider);
		setDescription(Messages.JAXBServiceProvider_defaultDescription);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration# getRMVariableMap()
	 */
	public IVariableMap getRMVariableMap() {
		if (map == null) {
			map = new RMVariableMap();
		}
		if (!((RMVariableMap) map).isInitialized()) {
			JAXBInitializationUtils.initializeMap(getResourceManagerData(), map);
		}
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.AbstractJAXBResourceManagerConfiguration#initialize()
	 */
	@Override
	public void initialize() throws CoreException {
		map = null;
		super.initialize();
	}

	/*
	 * (non-Javadoc) Sets JAXB@connection as the default service provider description.
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc ()
	 */
	public void setDefaultNameAndDesc() {
		String name = getName();
		if (name == null) {
			name = JAXBControlConstants.JAXB;
		}
		String conn = getConnectionName();
		if (conn != null && !conn.equals(JAXBControlConstants.ZEROSTR)) {
			name += JAXBControlConstants.AT + conn;
		}
		setName(name);
		setDescription(Messages.JAXBServiceProvider_defaultDescription);
	}

}
