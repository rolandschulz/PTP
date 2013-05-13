/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.sdm.core;

import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager;
import org.eclipse.ptp.internal.debug.core.pdi.manager.AbstractManagerFactory;


public class SDMManagerFactory extends AbstractManagerFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newEventManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDIEventManager newEventManager(IPDISession session) {
		return new SDMEventManager(session);
	}
}
