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
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgument;
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgumentDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocalVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorage;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorageDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;

/**
 * Represent expression manager to manage processes
 * 
 * @author clement
 * 
 */
public interface IPDIVariableManager extends IPDIManager {
	/**
	 * Create an argument
	 * 
	 * @param argDesc
	 * @return
	 * @throws PDIException
	 */
	public IPDIArgument createArgument(IPDIArgumentDescriptor argDesc) throws PDIException;

	/**
	 * Create a global variable
	 * 
	 * @param varDesc
	 * @return
	 * @throws PDIException
	 */
	public IPDIGlobalVariable createGlobalVariable(IPDIGlobalVariableDescriptor varDesc) throws PDIException;

	/**
	 * Create a local variable
	 * 
	 * @param varDesc
	 * @return
	 * @throws PDIException
	 */
	public IPDILocalVariable createLocalVariable(IPDILocalVariableDescriptor varDesc) throws PDIException;

	/**
	 * Get argument descriptors
	 * 
	 * @param frame
	 * @return
	 * @throws PDIException
	 */
	public IPDIArgumentDescriptor[] getArgumentDescriptors(IPDIStackFrame frame) throws PDIException;

	/**
	 * Get local variable descriptors
	 * 
	 * @param frame
	 * @return
	 * @throws PDIException
	 */
	public IPDILocalVariableDescriptor[] getLocalVariableDescriptors(IPDIStackFrame frame) throws PDIException;

	/**
	 * Get variable by name
	 * 
	 * @since 4.0
	 */
	public IPDIVariable getVariableByName(TaskSet tasks, String varname);

	/**
	 * Get global variable descriptor
	 * 
	 * @since 4.0
	 */
	public IPDIGlobalVariableDescriptor getGlobalVariableDescriptor(TaskSet tasks, String filename, String function, String name)
			throws PDIException;

	/**
	 * Create thread storage
	 * 
	 * @param desc
	 * @return
	 * @throws PDIException
	 */
	public IPDIThreadStorage createThreadStorage(IPDIThreadStorageDescriptor desc) throws PDIException;

	/**
	 * Get thread storage descriptors
	 * 
	 * @param thread
	 * @return
	 * @throws PDIException
	 */
	public IPDIThreadStorageDescriptor[] getThreadStorageDescriptors(IPDIThread thread) throws PDIException;

	/**
	 * Destroy variable
	 * 
	 * @param variable
	 * @throws PDIException
	 */
	public void destroyVariable(IPDIVariable variable) throws PDIException;

	/**
	 * Create variable
	 * 
	 * @param varDesc
	 * @return
	 * @throws PDIException
	 */
	public IPDIVariable createVariable(IPDIVariableDescriptor varDesc) throws PDIException;

	/**
	 * Get variable descriptor as array
	 * 
	 * @param varDesc
	 * @param start
	 * @param length
	 * @return
	 * @throws PDIException
	 */
	public IPDIVariableDescriptor getVariableDescriptorAsArray(IPDIVariableDescriptor varDesc, int start, int length)
			throws PDIException;

	/**
	 * Get variable descriptor as type
	 * 
	 * @param varDesc
	 * @param type
	 * @return
	 * @throws PDIException
	 */
	public IPDIVariableDescriptor getVariableDescriptorAsType(IPDIVariableDescriptor varDesc, String type) throws PDIException;

	/**
	 * Update variables
	 * 
	 * @param qTasks
	 * @param vars
	 * @throws PDIException
	 * @since 4.0
	 */
	public void update(TaskSet qTasks, String[] vars) throws PDIException;
}
