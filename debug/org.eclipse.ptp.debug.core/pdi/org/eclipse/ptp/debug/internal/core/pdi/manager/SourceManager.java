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
package org.eclipse.ptp.debug.internal.core.pdi.manager;

import java.math.BigInteger;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDISourceManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMixedInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;

/**
 * @author Clement chu
 * 
 */
public class SourceManager extends AbstractPDIManager implements
		IPDISourceManager {
	public SourceManager(IPDISession session) {
		super(session, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getDetailTypeName(org.eclipse.ptp.debug.internal.core.pdi.model.Target,
	 *      java.lang.String)
	 */
	public String getDetailTypeName(IPDITarget target, String typename)
			throws PDIException {
		throw new PDIException(target.getTasks(),
				"Not implements SourceManager - getDetailTypeName() yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getDetailTypeNameFromVariable(org.eclipse.ptp.debug.internal.core.pdi.model.StackFrame,
	 *      java.lang.String)
	 */
	public String getDetailTypeNameFromVariable(IPDIStackFrame frame,
			String variable) throws PDIException {
		IPDITarget target = frame.getTarget();
		IPDIThread currentThread = target.getCurrentThread();
		IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			frame.getThread().setCurrentStackFrame(frame, false);
			return getDetailTypeName(target, variable);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getInstructions(org.eclipse.ptp.core.util.BitList,
	 *      java.math.BigInteger, java.math.BigInteger)
	 */
	public IPDIInstruction[] getInstructions(BitList qTasks, BigInteger start,
			BigInteger end) throws PDIException {
		throw new PDIException(qTasks,
				"Not implemented SourceManager - getInstructions() yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getInstructions(org.eclipse.ptp.core.util.BitList,
	 *      java.lang.String, int)
	 */
	public IPDIInstruction[] getInstructions(BitList qTasks, String filename,
			int linenum) throws PDIException {
		return getInstructions(qTasks, filename, linenum, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getInstructions(org.eclipse.ptp.core.util.BitList,
	 *      java.lang.String, int, int)
	 */
	public IPDIInstruction[] getInstructions(BitList qTasks, String filename,
			int linenum, int lines) throws PDIException {
		throw new PDIException(qTasks,
				"Not implemented SourceManager - getInstructions() yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getMixedInstructions(org.eclipse.ptp.core.util.BitList,
	 *      java.math.BigInteger, java.math.BigInteger)
	 */
	public IPDIMixedInstruction[] getMixedInstructions(BitList qTasks,
			BigInteger start, BigInteger end) throws PDIException {
		throw new PDIException(qTasks,
				"Not implemented SourceManager - getMixedInstructions() yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getMixedInstructions(org.eclipse.ptp.core.util.BitList,
	 *      java.lang.String, int)
	 */
	public IPDIMixedInstruction[] getMixedInstructions(BitList qTasks,
			String filename, int linenum) throws PDIException {
		return getMixedInstructions(qTasks, filename, linenum, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getMixedInstructions(org.eclipse.ptp.core.util.BitList,
	 *      java.lang.String, int, int)
	 */
	public IPDIMixedInstruction[] getMixedInstructions(BitList qTasks,
			String filename, int linenum, int lines) throws PDIException {
		throw new PDIException(qTasks,
				"Not implemented SourceManager - getMixedInstructions() yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getSourcePaths(org.eclipse.ptp.core.util.BitList)
	 */
	public String[] getSourcePaths(BitList qTasks) throws PDIException {
		throw new PDIException(qTasks,
				"Not implemented SourceManager - getSourcePaths() yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getTypeName(org.eclipse.ptp.debug.internal.core.pdi.model.Target,
	 *      java.lang.String)
	 */
	public String getTypeName(IPDITarget target, String variable)
			throws PDIException {
		throw new PDIException(target.getTasks(),
				"Not implements SourceManager - getTypeName() yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#getTypeNameFromVariable(org.eclipse.ptp.debug.internal.core.pdi.model.StackFrame,
	 *      java.lang.String)
	 */
	public String getTypeNameFromVariable(IPDIStackFrame frame, String variable)
			throws PDIException {
		IPDITarget target = frame.getTarget();
		IPDIThread currentThread = target.getCurrentThread();
		IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			frame.getThread().setCurrentStackFrame(frame, false);
			return getTypeName(target, variable);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.internal.core.pdi.IPDISourceManager#setSourcePaths(org.eclipse.ptp.core.util.BitList,
	 *      java.lang.String[])
	 */
	public void setSourcePaths(BitList qTasks, String[] dirs)
			throws PDIException {
		throw new PDIException(qTasks,
				"Not implemented SourceManager - setSourcePaths() yet");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#shutdown()
	 */
	public void shutdown() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.BitList)
	 */
	public void update(BitList qTasks) throws PDIException {
		// Do dothing here
	}
}
