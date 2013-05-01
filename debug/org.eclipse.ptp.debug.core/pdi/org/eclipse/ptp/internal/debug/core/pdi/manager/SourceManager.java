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

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDISourceManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMixedInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class SourceManager extends AbstractPDIManager implements IPDISourceManager {
	public SourceManager(IPDISession session) {
		super(session, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getDetailTypeName(org.eclipse.ptp.internal.debug.core.pdi.model
	 * .Target,
	 * java.lang.String)
	 */
	public String getDetailTypeName(IPDITarget target, String typename) throws PDIException {
		throw new PDIException(target.getTasks(), Messages.SourceManager_0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getDetailTypeNameFromVariable(org.eclipse.ptp.internal.debug.core
	 * .pdi.model.StackFrame,
	 * java.lang.String)
	 */
	public String getDetailTypeNameFromVariable(IPDIStackFrame frame, String variable) throws PDIException {
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
	 * @see org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getInstructions(org.eclipse.ptp.core.util.TaskSet,
	 * java.math.BigInteger, java.math.BigInteger)
	 */
	public IPDIInstruction[] getInstructions(TaskSet qTasks, BigInteger start, BigInteger end) throws PDIException {
		throw new PDIException(qTasks, Messages.SourceManager_1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getInstructions(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String, int)
	 */
	public IPDIInstruction[] getInstructions(TaskSet qTasks, String filename, int linenum) throws PDIException {
		return getInstructions(qTasks, filename, linenum, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getInstructions(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String, int, int)
	 */
	public IPDIInstruction[] getInstructions(TaskSet qTasks, String filename, int linenum, int lines) throws PDIException {
		throw new PDIException(qTasks, Messages.SourceManager_1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getMixedInstructions(org.eclipse.ptp.core.util.TaskSet,
	 * java.math.BigInteger, java.math.BigInteger)
	 */
	public IPDIMixedInstruction[] getMixedInstructions(TaskSet qTasks, BigInteger start, BigInteger end) throws PDIException {
		throw new PDIException(qTasks, Messages.SourceManager_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getMixedInstructions(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String, int)
	 */
	public IPDIMixedInstruction[] getMixedInstructions(TaskSet qTasks, String filename, int linenum) throws PDIException {
		return getMixedInstructions(qTasks, filename, linenum, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getMixedInstructions(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String, int, int)
	 */
	public IPDIMixedInstruction[] getMixedInstructions(TaskSet qTasks, String filename, int linenum, int lines) throws PDIException {
		throw new PDIException(qTasks, Messages.SourceManager_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getSourcePaths(org.eclipse.ptp.core.util.TaskSet)
	 */
	public String[] getSourcePaths(TaskSet qTasks) throws PDIException {
		throw new PDIException(qTasks, Messages.SourceManager_3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getTypeName(org.eclipse.ptp.internal.debug.core.pdi.model.Target,
	 * java.lang.String)
	 */
	public String getTypeName(IPDITarget target, String variable) throws PDIException {
		throw new PDIException(target.getTasks(), Messages.SourceManager_4);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#getTypeNameFromVariable(org.eclipse.ptp.internal.debug.core.pdi
	 * .model.StackFrame,
	 * java.lang.String)
	 */
	public String getTypeNameFromVariable(IPDIStackFrame frame, String variable) throws PDIException {
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
	 * @see org.eclipse.ptp.internal.debug.core.pdi.IPDISourceManager#setSourcePaths(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String[])
	 */
	public void setSourcePaths(TaskSet qTasks, String[] dirs) throws PDIException {
		throw new PDIException(qTasks, Messages.SourceManager_5);
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
	public void update(TaskSet qTasks) throws PDIException {
		// Do dothing here
	}
}
