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
package org.eclipse.ptp.debug.core.pdi.manager;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegister;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterGroup;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;

/**
 * Represent register manager to manage register
 * 
 * @author clement
 * 
 */
public interface IPDIRegisterManager extends IPDIManager {
	/**
	 * Creat register
	 * 
	 * @param regDesc
	 * @return
	 * @throws PDIException
	 */
	public IPDIRegister createRegister(IPDIRegisterDescriptor regDesc) throws PDIException;

	/**
	 * Create shadow register
	 * 
	 * @param register
	 * @param frame
	 * @param regName
	 * @return
	 * @throws PDIException
	 */
	public IPDIVariable createShadowRegister(IPDIRegister register, IPDIStackFrame frame, String regName) throws PDIException;

	/**
	 * Destroy register
	 * 
	 * @param reg
	 */
	public void destroyRegister(IPDIRegister reg);

	/**
	 * Create register groups
	 * 
	 * @param qTasks
	 * @return
	 * @throws PDIException
	 * @since 4.0
	 */
	public IPDIRegisterGroup[] getRegisterGroups(TaskSet qTasks) throws PDIException;
}
