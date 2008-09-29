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
 * On object that can handle cancel events from the {@link ProgressQueue}..
 * This event is fired when the uses clicks on the cancel button of the
 * {@link IProgressMonitor} that is associated with the {@link ProgressQueue} or
 * when the method {@link ProgressQueue#cancel()} is called.
 * The event is not called a second time if the queue was already canceled.
 * 
 * @author Daniel Felix ferber
 * 
 */
public interface ICancelCallback {
	/**
	 * The cancel button from the {@link IProgressMonitor} that is associated
	 * with the {@link ProgressQueue} or when the method
	 * {@link ProgressQueue#cancel()} is called.
	 * 
	 * @param byUser
	 *            True if the cancel button was pressed and false if
	 *            {@link ProgressQueue#cancel()} is called
	 */
	void cancel(boolean byUser);
}
