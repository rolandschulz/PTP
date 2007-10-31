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
package org.eclipse.ptp.debug.internal.core.pdi;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIRegisterManager;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterGroup;
import org.eclipse.ptp.debug.internal.core.pdi.model.Register;
import org.eclipse.ptp.debug.internal.core.pdi.model.RegisterDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.StackFrame;
import org.eclipse.ptp.debug.internal.core.pdi.model.Variable;

/**
 * @author clement
 *
 */
public class RegisterManager extends Manager implements IPDIRegisterManager {
	final int MAX_ENTRIES = 150;
	
	public RegisterManager(Session session) {
		super(session, true);
	}
	public void shutdown() {
	}
	public void update(BitList tasks) throws PDIException {
	}
	public Variable createShadowRegister(Register register, StackFrame frame, String regName) throws PDIException {
		throw new PDIException(register.getTasks(), "Not implement RegisterManager - createShadowRegister() yet");
	}
	public void destroyRegister(Register reg) {
		
	}
	public Register createRegister(RegisterDescriptor regDesc) throws PDIException {
		throw new PDIException(regDesc.getTasks(), "Not implement RegisterManager - createRegister() yet");
	}
	public IPDIRegisterGroup[] getRegisterGroups(BitList qTasks) throws PDIException {
		//TODO Not implement RegisterManager - getRegisterGroups() yet
		return new IPDIRegisterGroup[0];
		//throw new PDIException(qTasks, "Not implement RegisterManager - getRegisterGroups() yet");
	}
}
