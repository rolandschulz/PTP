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
package org.eclipse.ptp.debug.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.core.model.ITargetProperties;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.ptp.core.util.BitList;

/**
 * @author Clement chu
 * 
 */
public interface IPDebugTarget extends IDebugTarget, IExecFileInfo, IRestart, IResumeWithoutSignal, IPDebugElement, ISteppingModeTarget, ITargetProperties {
	public void cleanup();
	public void terminated();
	public int getTargetID();
	public BitList getTask();
	public boolean isLittleEndian();
	public boolean hasSignals() throws DebugException;
	public IPSignal[] getSignals() throws DebugException;
	public IDisassembly getDisassembly() throws DebugException;
	public boolean isPostMortem();
	public boolean hasModules() throws DebugException;
	public ICModule[] getModules() throws DebugException;
	public void loadSymbolsForAllModules() throws DebugException;
	public IRegisterDescriptor[] getRegisterDescriptors() throws DebugException;
	public void addRegisterGroup(String name, IRegisterDescriptor[] descriptors);
	public void removeRegisterGroups(IRegisterGroup[] groups);
	public void modifyRegisterGroup(IPersistableRegisterGroup group, IRegisterDescriptor[] descriptors);
	public void restoreDefaultRegisterGroups();
	public IAddress getBreakpointAddress(IPLineBreakpoint breakpoint) throws DebugException;
}
