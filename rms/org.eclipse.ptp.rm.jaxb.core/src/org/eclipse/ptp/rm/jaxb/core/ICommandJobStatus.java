/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.rmsystem.IJobStatus;

public interface ICommandJobStatus extends IJobStatus {
	void cancel();

	void cancelWait();

	boolean isInteractive();

	void setProcess(IRemoteProcess process);

	void setState(String state);

	void startProxy();

	void waitForJobId(String uuid);
}
