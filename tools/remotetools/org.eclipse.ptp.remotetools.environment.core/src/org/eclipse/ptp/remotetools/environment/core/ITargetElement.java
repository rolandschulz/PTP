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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;


public interface ITargetElement {

	public abstract Map getAttributes();

	public abstract void setAttributes(Map attributes);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract ITargetControl getControl() throws CoreException;

	public abstract TargetTypeElement getType();

	public abstract int getStatus();

	public abstract String toString();

	public abstract String getId();

}