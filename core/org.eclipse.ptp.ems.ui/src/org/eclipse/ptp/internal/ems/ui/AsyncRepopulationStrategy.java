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
package org.eclipse.ptp.internal.ems.ui;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Strategy object used for asynchronous population of a {@link SearchableChecklist}.
 * 
 * @author Jeff Overbey
 * 
 * @see SearchableChecklist#asyncRepopulate(AsyncRepopulationStrategy)
 */
public interface AsyncRepopulationStrategy {

	/** @return a message to be displayed to the user while the list is being repopulated (non-<code>null</code>) */
	String getMessage();

	/**
	 * @param monitor
	 *            progress monitor
	 * @return the items to be displayed in the checklist (non-<code>null</code>)
	 * @throws Exception
	 *             if an error occurs
	 */
	List<String> computeItems(IProgressMonitor monitor) throws Exception;

	/**
	 * @param monitor
	 *            progress monitor
	 * @return the list of items in the checklist which should be checked (non-<code>null</code>). Note that the ordering of items
	 *         in the list is important.
	 * @throws Exception
	 *             if an error occurs
	 */
	List<String> computeSelectedItems(IProgressMonitor monitor) throws Exception;

	/** Code to be executed in the UI thread after the list is repopulated */
	void afterRepopulation();
}