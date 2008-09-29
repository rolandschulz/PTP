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
package org.eclipse.ptp.cell.environment.cellsimulator.core.common;

import org.eclipse.ptp.cell.simulator.core.ISimulatorControl;
import org.eclipse.ptp.cell.simulator.core.ISimulatorListener;
import org.eclipse.ptp.cell.simulator.core.SimulatorException;
import org.eclipse.ptp.remotetools.environment.control.ITargetControlJobListener;
import org.eclipse.ptp.remotetools.environment.control.ITargetJob;
import org.eclipse.ptp.remotetools.environment.control.SSHTargetControl;


public class StopResumeLogic implements ITargetControlJobListener,
		ISimulatorListener {

	private int jobCounter = 0;
	private boolean alreadyResumedOnFirstJob = false;

	private ISimulatorControl simulatorControl;

	private SSHTargetControl targetControl;

	public StopResumeLogic(ISimulatorControl simulatorControl,
			SSHTargetControl targetControl) {
		super();
		this.simulatorControl = simulatorControl;
		this.targetControl = targetControl;
	}
	
	public void activate() {
		/* Make sure the logic is not already registered. */
		simulatorControl.removeListener(this);
		targetControl.removeJobListener(this);
		jobCounter = 0;
		simulatorControl.addListener(this);
		targetControl.addJobListener(this);
	}
	
	public void deactivate() {
		simulatorControl.removeListener(this);
		targetControl.removeJobListener(this);
	}

	public void afterJobFinish(ITargetJob job) {
		/*
		 * If not more jobs are running, then the simulator will be paused.
		 */
		jobCounter--;
		if (jobCounter == 0) {
			if (simulatorControl.getStatus().isOperational()) {
				try {
					/* Only pause the simulator if it was not running
					 * when the first job started.
					 */
					if (! alreadyResumedOnFirstJob) {
						simulatorControl.pause();
					}
				} catch (SimulatorException e) {
					// Ignore;
				}
			}
		}

	}

	public void beforeJobStart(ITargetJob job) {
		/*
		 * The ssh target environmet automatically guarantees that the garget
		 * will be ready to run the job. If necessary, if the target is paused,
		 * it will first resume. Therefore, we need only to keep track that a
		 * new job has started.
		 */
		if (jobCounter == 0) {
			alreadyResumedOnFirstJob = ! simulatorControl.getStatus().isPaused();
		}
		jobCounter++;
	}

	public void lifecycleStateChanged(int state) {
		/* This notification is ignored. */
	}

	public void progressChanged(int progress) {
		/* This notification is ignored. */
	}

	public void simulationStatus(int status) {
		/* This notification is ignored. */
	}

}
