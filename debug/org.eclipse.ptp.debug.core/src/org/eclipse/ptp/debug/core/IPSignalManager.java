/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;

/**
 * @author greg
 *
 */
public interface IPSignalManager {

	/**
	 * @param qTasks
	 */
	public void dispose(BitList qTasks);

	/**
	 * @param qTasks
	 * @return
	 * @throws DebugException
	 */
	public IPSignal[] getSignals(BitList qTasks) throws DebugException;

	/**
	 * @param qTasks
	 * @param pdiSignal
	 */
	public void signalChanged(BitList qTasks, IPDISignal pdiSignal);

}
