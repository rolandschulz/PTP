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

import org.eclipse.ptp.cell.utils.terminal.ITerminalProvider;

/**
 * Controls simulator life cycle and status.
 * This interface shall be used to launch and shut down the simulator.
 * @author Daniel Felix Ferber
 */
public interface ISimulatorControl {

	/**
	 * Get the parameters used to launch the simulator.
	 */
	public abstract ISimulatorParameters getParameters();
	
	/**
	 * Access to simulator process terminal (stdin, stdout, stderr).
	 */
	public abstract ITerminalProvider getProcessTerminal();

	/**
	 * Access to Linux console terminal (stdin, stdout).
	 */
	public abstract ITerminalProvider getLinuxTerminal();

	/**
	 * Access to simulator status information.
	 */
	public abstract ISimulatorStatus getStatus();

	/**
	 * Add a listener for simulator state events.
	 * 
	 * @param listener
	 */
	public abstract void addListener(ISimulatorListener listener);

	/**
	 * Remove a listener for simulator state events.
	 * 
	 * @param listener
	 */
	public abstract void removeListener(ISimulatorListener listener);

	/**
	 * Launches the simulator.
	 * The method blocks until the simulator has finished launching.
	 * The launch will keep a copy of the configuration.
	 * If the launch fails, all operations will be undone and ththe control will be reseted. 
	 * @param config The configuration to be used for the launch.
	 * @throws SimulatorException The failure during the launch.
	 */
	public abstract void launch(ISimulatorParameters config)
			throws SimulatorException;

	/**
	 * Shut down the simulator gracefully. The method blocks until the simulator has finished shutting down.
	 * Simulator triggers will detect linux shutdown and will terminate the simulator
	 * process automatically, clean up bogusnet, remove lock and reset the controller object.
	 * If the shut down fails, the simulator will be forced to terminate and the control will be reseted.
	 * 
	 * @throws SimulatorException The failure during the shut down.
	 */
	public abstract void shutdown() throws SimulatorException;

	/**
	 * Force the simulator to terminate, whatever is the current state.
	 * The control will be reseted, bogusnet will be cleaned up, any ongoing launch will be interrupted
	 * and any ongoing shutdown will be forced to terminate immediately.
	 * The ongoing operation will fail with a {@link SimulatorKilledException} exception.
	 * @throws SimulatorException 
	 */
	public abstract void kill();

	/**
	 * Pause the simulator. If already paused, nothing happens.
	 * @throws SimulatorException If pause fails.
	 */
	public abstract void pause() throws SimulatorException;

	/**
	 * Resume the simulator. If already simulating, nothing happens.
	 * @throws SimulatorException If resume fails.
	 */
	public abstract void resume() throws SimulatorException;
	
	/**
	 * Clear working directory.
	 */
	 public abstract void clear() throws SimulatorException;

}