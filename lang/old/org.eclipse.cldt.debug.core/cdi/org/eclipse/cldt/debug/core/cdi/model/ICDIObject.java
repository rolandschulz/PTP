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

package org.eclipse.cldt.debug.core.cdi.model;

/**
 * 
 * Represents an object in the CDI model.
 * 
 * @since Jul 8, 2002
 */
public interface ICDIObject {
	/**
	 * Returns the target this object is contained in.
	 * 
	 * @return the target this object is contained in
	 */
	ICDITarget getTarget();
}
