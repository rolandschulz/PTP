/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.services.core;


@SuppressWarnings("serial")
public class ProjectNotConfiguredException extends RuntimeException {

	public ProjectNotConfiguredException() {
		super();
	}

	public ProjectNotConfiguredException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ProjectNotConfiguredException(String arg0) {
		super(arg0);
	}

	public ProjectNotConfiguredException(Throwable arg0) {
		super(arg0);
	}

}
