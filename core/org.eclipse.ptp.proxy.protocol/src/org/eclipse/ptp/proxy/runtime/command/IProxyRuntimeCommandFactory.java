/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.IProxyCommandFactory;

public interface IProxyRuntimeCommandFactory extends IProxyCommandFactory {
	/**
	 * @param baseID
	 * @return
	 */
	public IProxyRuntimeInitCommand newProxyRuntimeInitCommand(int baseId);
	
	/**
	 * @return
	 */
	public IProxyRuntimeModelDefCommand newProxyRuntimeModelDefCommand();
	
	/**
	 * @return
	 */
	public IProxyRuntimeStartEventsCommand newProxyRuntimeStartEventsCommand();
	
	/**
	 * @return
	 */
	public IProxyRuntimeStopEventsCommand newProxyRuntimeStopEventsCommand();
	
	/**
	 * @param args
	 * @return
	 */
	public IProxyRuntimeSubmitJobCommand newProxyRuntimeSubmitJobCommand(String[] args);
	
	/**
	 * @param jobID
	 * @return
	 */
	public IProxyRuntimeTerminateJobCommand newProxyRuntimeTerminateJobCommand(String jobId);
}
