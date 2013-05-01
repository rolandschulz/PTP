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
package org.eclipse.ptp.internal.debug.core.pdi.manager;

import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIEventRequestManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDISignalManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDISourceManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITargetManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIThreadManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager;


public abstract class AbstractManagerFactory implements IPDIManagerFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newBreakpointManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDIBreakpointManager newBreakpointManager(IPDISession session) {
		return new BreakpointManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newEventRequestManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDIEventRequestManager newEventRequestManager(IPDISession session) {
		return new EventRequestManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newExpressionManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDIExpressionManager newExpressionManager(IPDISession session) {
		return new ExpressionManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newMemoryManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDIMemoryManager newMemoryManager(IPDISession session) {
		return new MemoryManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newRegisterManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDIRegisterManager newRegisterManager(IPDISession session) {
		return new RegisterManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newSignalManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDISignalManager newSignalManager(IPDISession session) {
		return new SignalManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newSourceManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDISourceManager newSourceManager(IPDISession session) {
		return new SourceManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newTargetManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDITargetManager newTargetManager(IPDISession session) {
		return new TargetManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newTaskManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDITaskManager newTaskManager(IPDISession session) {
		return new TaskManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newThreadManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDIThreadManager newThreadManager(IPDISession session) {
		return new ThreadManager(session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory#newVariableManager(org.eclipse.ptp.debug.core.pdi.IPDISession)
	 */
	public IPDIVariableManager newVariableManager(IPDISession session) {
		return new VariableManager(session);
	}

}
