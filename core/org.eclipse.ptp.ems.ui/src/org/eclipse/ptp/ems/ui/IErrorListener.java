/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ems.ui;

/**
 * Callback used to display an error messages to the user as well as to clear previously-displayed messages.
 * 
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public interface IErrorListener {

	/**
	 * Invoked when an error occurs.
	 * <p>
	 * Implementors should display the given message to the user.
	 * 
	 * @param message
	 */
	void errorRaised(String message);

	/**
	 * Invoked when it is acceptable to clear any previously-raised error messages from the user's display.
	 * <p>
	 * Implementors should clear any visible error messages.
	 * 
	 * @param message
	 */
	void errorCleared();
}
