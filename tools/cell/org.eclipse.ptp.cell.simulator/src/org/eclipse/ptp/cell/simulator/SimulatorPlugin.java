/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.simulator;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.cell.simulator.core.ISimulatorControl;
import org.eclipse.ptp.cell.simulator.extensions.ArchitectureManager;
import org.eclipse.ptp.cell.simulator.extensions.LaunchProfileManager;
import org.eclipse.ptp.cell.simulator.internal.SimulatorControl;


public class SimulatorPlugin extends Plugin {
	private static SimulatorPlugin instance = null;
	
	public SimulatorPlugin() {
		// TODO Auto-generated constructor stub
	}

	public static ArchitectureManager getArchitectureManager() {
		return ArchitectureManager.getInstance();
	}
	
	public static LaunchProfileManager getLaunchProfileManager() {
		return LaunchProfileManager.getInstance();
	}

	public static SimulatorPlugin getDefault() {
		if (instance == null) {
			instance = new SimulatorPlugin();
		}
		return instance;
	}

	public ISimulatorControl createSimulatorControl() {
		return new SimulatorControl();
	}
}
