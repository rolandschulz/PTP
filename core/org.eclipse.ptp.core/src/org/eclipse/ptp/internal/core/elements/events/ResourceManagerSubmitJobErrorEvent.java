/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.core.elements.events;

import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.events.IResourceManagerSubmitJobErrorEvent;

/**
 * @author grw
 * 
 */
public class ResourceManagerSubmitJobErrorEvent extends ResourceManagerErrorEvent
		implements IResourceManagerSubmitJobErrorEvent {

	private final String id;

	public ResourceManagerSubmitJobErrorEvent(IPResourceManager rm, String id, String message) {
		super(rm, message);
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.events.IResourceManagerSubmitJobErrorEvent
	 * #getJobSubmissionId()
	 */
	public String getJobSubmissionId() {
		return id;
	}
}
