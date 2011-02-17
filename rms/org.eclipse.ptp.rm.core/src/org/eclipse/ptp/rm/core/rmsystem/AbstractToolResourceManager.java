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
package org.eclipse.ptp.rm.core.rmsystem;

import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager;

/**
 * @author Daniel Felix Ferber
 */
public abstract class AbstractToolResourceManager extends AbstractRuntimeResourceManager {

	/**
	 * @since 3.0
	 */
	public AbstractToolResourceManager(IPUniverse universe, IResourceManagerConfiguration config) {
		super(universe, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#
	 * doBeforeCloseConnection()
	 */
	@Override
	protected void doBeforeCloseConnection() {
		/*
		 * Defaults to empty implementation
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#
	 * doAfterCloseConnection()
	 */
	@Override
	protected void doAfterCloseConnection() {
		/*
		 * Defaults to empty implementation
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doAfterOpenConnection
	 * ()
	 */
	@Override
	protected void doAfterOpenConnection() {
		/*
		 * Defaults to empty implementation
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#
	 * doBeforeOpenConnection()
	 */
	@Override
	protected void doBeforeOpenConnection() {
		/*
		 * Defaults to empty implementation
		 */
	}
}
