/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.core;

import org.eclipse.cldt.internal.core.InternalCExtension;
import org.eclipse.core.resources.IProject;

public abstract class AbstractCExtension extends InternalCExtension implements ICExtension {

	/**
	 * Returns the project for which this extrension is defined.
	 *	
	 * @return the project
	 */
	public final IProject getProject() {
		return super.getProject();
	}
	
	public final ICExtensionReference getExtensionReference() {
		return super.getExtensionReference();
	}
}
