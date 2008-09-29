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
package org.eclipse.ptp.cell.simulator.core;

import org.eclipse.core.runtime.CoreException;

/**
 * A dummy implementation of the {@link ISimulatorListener}.
 * @author Daniel Felix ferber
 *
 */
class SimulatorListenerAdapter implements ISimulatorListener {

	public void lifecycleStateChanged(int state) {
	}

	public void progressChanged(int progress) {
	}

	public void simulationStatus(int status) {
	}

	public void exception(CoreException exception) {
	}

}
