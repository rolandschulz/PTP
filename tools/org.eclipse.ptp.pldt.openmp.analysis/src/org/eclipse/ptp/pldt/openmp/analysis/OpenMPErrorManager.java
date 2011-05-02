/**********************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis;

import java.util.LinkedList;

/**
 * Holds all the errors encountered during analysis, ref. OpenMPAnalysisManager
 * 
 * @author pazel
 * 
 */
public class OpenMPErrorManager
{
	protected LinkedList errors_ = new LinkedList(); // holds the errors

	protected static OpenMPErrorManager currentManager_ = null;

	/**
	 * OpenMPErrorManager - constructor
	 * 
	 */
	public OpenMPErrorManager()
	{
		currentManager_ = this;
	}

	/**
	 * Get the current error manager
	 * 
	 * @return OpenMPErrorManager
	 */
	public static OpenMPErrorManager getCurrentErrorManager()
	{
		return currentManager_;
	}

	/**
	 * Add an error
	 * 
	 * @param error
	 *            - OpenMPError
	 */
	public void addError(OpenMPError error)
	{
		errors_.add(error);
	}

	/**
	 * Return an list of all errors
	 * 
	 * @return LinkedList
	 */
	public LinkedList getErrors()
	{
		return errors_;
	}

}
