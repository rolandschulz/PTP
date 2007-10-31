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
package org.eclipse.ptp.debug.core.pdi;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;

/**
 * Provides the ability to resume a thread.
 * Provides the ability to step into, over, and until from the current execution location.  Implementations must be non-blocking.
 * Provides the ability to step  return from the frame. Implementations must be non-blocking.
 * @author clement
 *
 */
public interface IPDIExecuteManagement {
	/**
	 * Causes this target to resume its execution. 
	 * if passSignal is <code>false</code> and the target was suspended by a signal when resuming the signal will be discarded
	 * Has no effect on a target that is not suspended.
	 * @param tasks target process
	 * @param passSignal whether to discard the signal
	 * @throws PDIException on failure
	 */
	void resume(BitList tasks, boolean passSignal) throws PDIException;

	/**
	 * Resume execution at location. Note the method does not change stackframe.
	 * The result is undefined if it jumps outside of the stackframe.
	 * Can  only be called when the associated target is suspended.
	 * @param tasks target process
	 * @param location
	 * @throws PDIException on failure
	 */
	void resume(BitList tasks, IPDILocation location) throws PDIException;

	/**
	 * Resume execution where the program stopped but immediately give the signal.
	 * @param tasks target process
	 * @param signal
	 * @throws PDIException on failure
	 */
	void resume(BitList tasks, IPDISignal signal) throws PDIException;

	/**
	 * Steps over the current source line. if count <= 0 it is a loop. Can only be called when the associated target/thread is suspended. 
	 * @param tasks target process
	 * @param count as in `step', but do so count times.
	 * @throws PDIException on failure
	 */
	void stepOver(BitList tasks, int count) throws PDIException;

	/**
	 * Steps over the current machine instruction. Can only be called when the associated target/thread is suspended. if count <= 0 it is a loop.
	 * @param tasks target process
	 * @param count as in `stepOverInstruction', but do so count times.
	 * @throws PDIException on failure
	 */
	void stepOverInstruction(BitList tasks, int count) throws PDIException;

	/**
	 * Steps into the current source line. Can only be called when the associated target/thread is suspended. if count <= 0 it is a loop.
	 * @param tasks target process
	 * @param count as in `step', but do so count times.
	 * @throws PDIException on failure
	 */
	void stepInto(BitList tasks, int count) throws PDIException;

	/**
	 * Steps into the current machine instruction. Can only be called when the associated target/thread is suspended. if count <= 0 it is a loop.
	 * @param tasks target process
	 * @throws PDIException on failure
	 */
	void stepIntoInstruction(BitList tasks, int count) throws PDIException;

	/**
	 * Continues running until location is reached.
	 * If the program will be suspended if attempt to exit the current frame.
	 * Can only be called when the associated target is suspended.
	 * @param tasks target process
	 * @param location location
	 * @throws PDIException on failure
	 */
	void stepUntil(BitList tasks, IPDILocation location) throws PDIException;
	
	/**
	 * Continue execution until the frame return.
	 * @param count as in `step', but do so count times.
	 * @param tasks target process
	 * @throws PDIException on failure
	 */
	void stepReturn(BitList tasks, int count) throws PDIException;

	/**
	 * Cancel execution of the frame and return with value.
	 * value can be <code>null</code>, if no return value is needed.
	 * Can only be called when the associated target/thread is suspended.
	 * @param tasks target process
	 * @param aif value use as the returning value.
	 * @throws PDIException on failure
	 */
	void stepReturn(BitList tasks, IAIF aif) throws PDIException;
	
	/**
	 * Causes this target/thread to suspend its execution. 
	 * Has no effect on an already suspended thread.
	 * @param tasks target process
	 * @throws PDIException on failure
	 */
	void suspend(BitList tasks) throws PDIException;

	/**
	 * Requests to terminate of specify process
	 * @param tasks target process
	 * @throws PDIException on failure
	 */
	void terminate(BitList tasks) throws PDIException;	

	/**
	 * TODO not implemented yet in 2.0
	 * Requests to restart of debugging
	 * @param tasks
	 * @throws PDIException on failure
	 */
	void restart(BitList tasks) throws PDIException;
	
	/**
	 * Starts specify process
	 * @param tasks target process
	 * @throws PDIException on failure
	 */
	void start(BitList tasks) throws PDIException;
}
