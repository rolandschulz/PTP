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
package org.eclipse.ptp.debug.core.cdi.model;

import java.math.BigInteger;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibraryManagement;
import org.eclipse.cdt.debug.core.cdi.model.ICDISourceManagement;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.cdi.IPCDIAddressLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDICondition;
import org.eclipse.ptp.debug.core.cdi.IPCDIFunctionLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDISessionObject;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

public interface IPCDITarget extends IPCDIThreadGroup, IPCDIExpressionManagement, ICDISourceManagement, ICDISharedLibraryManagement, IPCDIMemoryBlockManagement, IPCDISessionObject {
	public int getTargetID();
	public IPProcess getPProcess();
	
	Process getProcess();
	IPCDITargetConfiguration getConfiguration();
	String evaluateExpressionToString(IPCDIStackFrame context, String expressionText) throws PCDIException;
	IPCDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws PCDIException;
	IPCDIGlobalVariable createGlobalVariable(IPCDIGlobalVariableDescriptor varDesc) throws PCDIException;
	ICDIRegisterGroup[] getRegisterGroups() throws PCDIException;
	ICDIRegister createRegister(ICDIRegisterDescriptor varDesc) throws PCDIException;
	boolean isTerminated();
	void terminate() throws PCDIException;
	boolean isDisconnected();
	void disconnect() throws PCDIException;
	void restart() throws PCDIException;
	
	ICDIRuntimeOptions getRuntimeOptions();
	IPCDICondition createCondition(int ignoreCount, String expression);
	IPCDICondition createCondition(int ignoreCount, String expression, String[] threadIds);
	IPCDILineLocation createLineLocation(String file, int line);
	IPCDIFunctionLocation createFunctionLocation(String file, String function);
	IPCDIAddressLocation createAddressLocation(BigInteger address);	
}
