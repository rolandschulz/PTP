/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.services;

/**
 * @since 4.0
 */
public abstract class AbstractSynchronizeService implements ISynchronizeService {
	private final ISynchronizeServiceDescriptor fDescriptor;

	public AbstractSynchronizeService(ISynchronizeServiceDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	@Override
	public String getId() {
		return fDescriptor.getId();
	}

	@Override
	public String getName() {
		return fDescriptor.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.core.services.ISynchronizeServiceDescriptor#getService()
	 */
	@Override
	public ISynchronizeService getService() {
		return this;
	}
}
