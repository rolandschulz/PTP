/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.core.pdi.manager;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegister;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterGroup;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * 
 */
public class RegisterManager extends AbstractPDIManager implements IPDIRegisterManager {
	final int MAX_ENTRIES = 150;

	public RegisterManager(IPDISession session) {
		super(session, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager#createRegister(org.eclipse.ptp.debug.core.pdi.model.
	 * IPDIRegisterDescriptor)
	 */
	public IPDIRegister createRegister(IPDIRegisterDescriptor regDesc) throws PDIException {
		throw new PDIException(regDesc.getTasks(), Messages.RegisterManager_0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager#createShadowRegister(org.eclipse.ptp.debug.core.pdi.model.IPDIRegister
	 * , org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String)
	 */
	public IPDIVariable createShadowRegister(IPDIRegister register, IPDIStackFrame frame, String regName) throws PDIException {
		throw new PDIException(register.getTasks(), Messages.RegisterManager_1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager#destroyRegister(org.eclipse.ptp.debug.core.pdi.model.IPDIRegister)
	 */
	public void destroyRegister(IPDIRegister reg) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager#getRegisterGroups(org.eclipse.ptp.core.util.TaskSet)
	 */
	public IPDIRegisterGroup[] getRegisterGroups(TaskSet qTasks) throws PDIException {
		// TODO Not implement RegisterManager - getRegisterGroups() yet
		return new IPDIRegisterGroup[0];
		// throw new PDIException(qTasks, "Not implement RegisterManager - getRegisterGroups() yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#shutdown()
	 */
	@Override
	public void shutdown() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.TaskSet)
	 */
	@Override
	public void update(TaskSet tasks) throws PDIException {
	}
}
