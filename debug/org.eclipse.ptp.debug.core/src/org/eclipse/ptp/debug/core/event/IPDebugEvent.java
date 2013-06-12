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
package org.eclipse.ptp.debug.core.event;

/**
 * Base interface for debugger events
 * 
 * @author Clement
 */
public interface IPDebugEvent {
	/**
	 * Resume event kind.
	 */
	public static final int RESUME = 0x0001;

	/**
	 * Suspend event kind.
	 */
	public static final int SUSPEND = 0x0002;

	/**
	 * Create event kind.
	 */
	public static final int CREATE = 0x0004;

	/**
	 * Terminate event kind.
	 */
	public static final int TERMINATE = 0x0008;

	/**
	 * Change event kind.
	 */
	public static final int CHANGE = 0x0010;

	/**
	 * Error event kind or detail
	 */
	public static final int ERROR = 0x0040;

	/**
	 * Process specific event detail.
	 * 
	 * @since 2.1.2
	 */
	// public static final int MODEL_SPECIFIC= 0x0020;
	public static final int PROCESS_SPECIFIC = 0x0020;

	/**
	 * Step start detail. Indicates a thread was resumed by a step into action.
	 * 
	 * @since 2.0
	 */
	public static final int STEP_INTO = 0x0001;

	/**
	 * Step start detail. Indicates a thread was resumed by a step over action.
	 * 
	 * @since 2.0
	 */
	public static final int STEP_OVER = 0x0002;

	/**
	 * Step start detail. Indicates a thread was resumed by a step return
	 * action.
	 * 
	 * @since 2.0
	 */
	public static final int STEP_RETURN = 0x0004;

	/**
	 * Step end detail. Indicates a thread was suspended due to the completion
	 * of a step action.
	 */
	public static final int STEP_END = 0x0008;

	/**
	 * Breakpoint detail. Indicates a thread was suspended by a breakpoint.
	 */
	public static final int BREAKPOINT = 0x0010;

	/**
	 * Client request detail. Indicates a thread was suspended due to a client
	 * request.
	 */
	public static final int CLIENT_REQUEST = 0x0020;

	/**
	 * Evaluation detail. Indicates that a thread was resumed or suspended to
	 * perform an expression evaluation.
	 * 
	 * @since 2.0
	 */
	public static final int EVALUATION = 0x0040;

	/**
	 * Evaluation detail. Indicates that a thread was resumed or suspended to
	 * perform an implicit expression evaluation. An implicit evaluation is an
	 * evaluation that is performed as an indirect result of a user action.
	 * Clients may use this detail event to decide whether or not to alert the
	 * user that an evaluation is taking place..
	 * 
	 * @since 2.0
	 */
	public static final int EVALUATION_IMPLICIT = 0x0080;

	/**
	 * State change detail. Indicates the state of a single debug element has
	 * changed. Only valid for <code>CHANGE</code> events.
	 * 
	 * @since 2.0
	 */
	public static final int STATE = 0x0100;

	/**
	 * Content change detail. Indicates the content of a debug element (and
	 * potentially its children) has changed. Only valid for <code>CHANGE</code> events.
	 * 
	 * @since 2.0
	 */
	public static final int CONTENT = 0x0200;

	/**
	 * Debugger detail. Indicates the debugger is terminated or created
	 * 
	 * @since 2.0
	 */
	public static final int DEBUGGER = 0x0400;

	/**
	 * Register detail. Indicates the registered process is created or
	 * terminated
	 * 
	 * @since 2.0
	 */
	public static final int REGISTER = 0x0800;

	/**
	 * Constant indicating that the kind or detail of a debug event is
	 * unspecified.
	 */
	public static final int UNSPECIFIED = 0;

	public static final int ERR_NORMAL = 0x0001;
	public static final int ERR_WARNING = 0x0002;
	public static final int ERR_FATAL = 0x0004;

	/**
	 * Get the event detail
	 * 
	 * @return
	 */
	public int getDetail();

	/**
	 * Get the event info
	 * 
	 * @return
	 */
	public IPDebugInfo getInfo();

	/**
	 * Get the event kind
	 * 
	 * @return
	 */
	public int getKind();

	/**
	 * Get the event source
	 * 
	 * @return
	 */
	public Object getSource();

	/**
	 * Check if this is an evaluation event
	 * 
	 * @return
	 */
	public boolean isEvaluation();

	/**
	 * Check if this is a step start event
	 * 
	 * @return
	 */
	public boolean isStepStart();
}
