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
package org.eclipse.cldt.debug.core.cdi;

/**
 * Represents information provided by the session when the program exited.
 * 
 */
public interface ICDISignalExitInfo extends ICDISessionObject {

	/**
	 * Method getName.
	 * @return String
	 */
	String getName();
	
	/**
	 * Method getDescription.
	 * @return String
	 */
	String getDescription();

}
