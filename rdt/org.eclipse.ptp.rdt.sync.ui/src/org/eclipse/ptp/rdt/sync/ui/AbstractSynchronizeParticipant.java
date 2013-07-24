/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

/**
 * Must be extended by extensions to the syncProvider extension point.
 * 
 */
public abstract class AbstractSynchronizeParticipant implements ISynchronizeParticipant {
	public ISynchronizeParticipantDescriptor fDescriptor;

	public AbstractSynchronizeParticipant(ISynchronizeParticipantDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor#getId()
	 */
	@Override
	public String getId() {
		return fDescriptor.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor#getName()
	 */
	@Override
	public String getName() {
		return fDescriptor.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor#getParticipant()
	 */
	@Override
	public ISynchronizeParticipant getParticipant() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor#getServiceId()
	 */
	@Override
	public String getServiceId() {
		return fDescriptor.getServiceId();
	}
}
