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
package org.eclipse.ptp.cell.ui.progress;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Describes an operation in the {@link ProgressQueue}.
 * 
 * @author Daniel Felix Ferber
 */
class ProgressInfo {
	/**
	 * Integer number that identifies the operation. In future, will be replaced
	 * by a string to avoid collision of multiple independent components that contribute
	 * with operations to the queue..
	 */
	int id;

	/**
	 * A text that will be presented in the {@link IProgressMonitor} for this
	 * operation.
	 */
	String description;

	/**
	 * The number of steps that the {@link IProgressMonitor} will advance after
	 * this operation.
	 */
	int steps;

	/** Default constructor. */
	public ProgressInfo(int id, String description, int steps) {
		super();
		this.id = id;
		this.description = description;
		this.steps = steps;
	}
}
