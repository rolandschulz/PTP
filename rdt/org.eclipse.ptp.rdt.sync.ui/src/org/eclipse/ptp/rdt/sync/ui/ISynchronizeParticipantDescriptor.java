/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
 * Must be implemented by extensions to the syncProvider extension point.
 * 
 */
public interface ISynchronizeParticipantDescriptor {
	/**
	 * Returns the unique id that identifies this participant type.
	 * 
	 * @return the unique id that identifies this participant type.
	 */
	public String getId();

	/**
	 * Returns the name of this participant. This can be shown to the user.
	 * 
	 * @return the name of this participant. This can be shown to the user.
	 */
	public String getName();

	/**
	 * @return
	 */
	public ISynchronizeParticipant getParticipant();

	/**
	 * Get the service ID that implements the sync service
	 * 
	 * @return service ID
	 */
	public String getServiceId();
}
