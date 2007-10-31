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
package org.eclipse.ptp.debug.internal.core.pdi.model;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegister;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.internal.core.pdi.Session;

/**
 * @author clement
 *
 */
public class Register extends Variable implements IPDIRegister {
	public Register(Session session, BitList tasks, Thread thread, StackFrame frame, String n, String q, int pos, int depth, String varid) {
		super(session, tasks, thread, frame, n, q, pos, depth, varid);
	}
	public Register(Session session, RegisterDescriptor obj, String varid) {
		super(session, obj, varid);
	}
/*	
	protected void addToTypeCache(String nameType, IPDIType type) throws PDIException {
		Session session = (Session)getTarget().getSession();
		RegisterManager mgr = session.getRegisterManager();
		mgr.addToTypeCache(nameType, type);
	}

	protected IPDIType getFromTypeCache(String nameType) throws PDIException {
		Session session = (Session)getTarget().getSession();
		RegisterManager mgr = session.getRegisterManager();
		return mgr.getFromTypeCache(nameType);
	}
*/
	public String getFullName() {
		if (fFullName == null) {
			String n = getName();
			if (!n.startsWith("$")) {
				fFullName = "$" + n;
			} else {
				fFullName = n;
			}
		}
		return fFullName;
	}

	protected Variable createVariable(Session session, BitList tasks, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, String varid) {
		return new Register(session, tasks, thread, frame, name, fullName, pos, depth, varid);
	}
	public void dispose() throws PDIException {
		session.getRegisterManager().destroyRegister(this);
	}
	public IAIF getAIF(IPDIStackFrame context) throws PDIException {
		Variable var = session.getRegisterManager().createShadowRegister(this, (StackFrame)context, getQualifiedName());
		return var.getAIF();
	}
	public boolean equals(IPDIRegister register) {
		if (register instanceof Register) {
			Register reg = (Register) register;
			return super.equals(reg);
		}
		return super.equals(register);
	}

}
