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

import java.math.BigInteger;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMixedInstruction;
import org.eclipse.ptp.debug.internal.core.pdi.model.StackFrame;
import org.eclipse.ptp.debug.internal.core.pdi.model.Target;
import org.eclipse.ptp.debug.internal.core.pdi.model.Thread;

/**
 * @author Clement chu
 * 
 */
public class SourceManager extends Manager {
	public SourceManager(Session session) {
		super(session, false);
	}
	public void shutdown() {
	}
	public void setSourcePaths(BitList qTasks, String[] dirs) throws PDIException {
		throw new PDIException (qTasks, "Not implemented SourceManager - setSourcePaths() yet");
	}
	public String[] getSourcePaths(BitList qTasks) throws PDIException {
		throw new PDIException (qTasks, "Not implemented SourceManager - getSourcePaths() yet");
	}
	public IPDIInstruction[] getInstructions(BitList qTasks, String filename, int linenum) throws PDIException {
		return getInstructions(qTasks, filename, linenum, -1);
	}
	public IPDIInstruction[] getInstructions(BitList qTasks, String filename, int linenum, int lines) throws PDIException {
		throw new PDIException (qTasks, "Not implemented SourceManager - getInstructions() yet");
	}
	public IPDIInstruction[] getInstructions(BitList qTasks, BigInteger start, BigInteger end) throws PDIException {
		throw new PDIException (qTasks, "Not implemented SourceManager - getInstructions() yet");
	}
	public IPDIMixedInstruction[] getMixedInstructions(BitList qTasks, String filename, int linenum) throws PDIException {
		return getMixedInstructions(qTasks, filename, linenum, -1);
	}
	public IPDIMixedInstruction[] getMixedInstructions(BitList qTasks, String filename, int linenum, int lines) throws PDIException {
		throw new PDIException (qTasks, "Not implemented SourceManager - getMixedInstructions() yet");
	}
	public IPDIMixedInstruction[] getMixedInstructions(BitList qTasks, BigInteger start, BigInteger end) throws PDIException {
		throw new PDIException (qTasks, "Not implemented SourceManager - getMixedInstructions() yet");
	}	
	public void update(BitList qTasks) throws PDIException {
		//Do dothing here
	}
	public String getDetailTypeNameFromVariable(StackFrame frame, String variable) throws PDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
			return getDetailTypeName(target, variable);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
	}
	public String getDetailTypeName(Target target, String typename) throws PDIException {
		throw new PDIException(target.getTasks(), "Not implements SourceManager - getDetailTypeName() yet");
	}
	public String getTypeNameFromVariable(StackFrame frame, String variable) throws PDIException {
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
			return getTypeName(target, variable);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
	}
	public String getTypeName(Target target, String variable) throws PDIException {
		throw new PDIException(target.getTasks(), "Not implements SourceManager - getTypeName() yet");
	}
}
