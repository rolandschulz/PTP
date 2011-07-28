/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.core;


/**
 * Runtime exception indicating that for some reason the given project's service
 * model has not been configured.
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 *
 */
@SuppressWarnings("serial")
public class ProjectNotConfiguredException extends RuntimeException {

	public ProjectNotConfiguredException() {
		super();
	}

	public ProjectNotConfiguredException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProjectNotConfiguredException(String message) {
		super(message);
	}

	public ProjectNotConfiguredException(Throwable cause) {
		super(cause);
	}

}
