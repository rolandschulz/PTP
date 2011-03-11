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

import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;

public interface ICommandJobStreamsProxy extends IStreamsProxy, IStreamsProxy2 {

	void close();

	void setErrMonitor(ICommandJobStreamMonitor err);

	void setOutMonitor(ICommandJobStreamMonitor out);

	void startMonitors();
}
