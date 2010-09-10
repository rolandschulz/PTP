/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;

public interface ITargetElement {

	/**
	 * @since 2.0
	 */
	public abstract ControlAttributes getAttributes();

	public abstract ITargetControl getControl() throws CoreException;

	public abstract String getId();

	public abstract String getName();

	public abstract int getStatus();

	public abstract TargetTypeElement getType();

	/**
	 * @since 2.0
	 */
	public abstract void setAttributes(ControlAttributes attributes);

	public abstract void setName(String name);

	public abstract String toString();

}