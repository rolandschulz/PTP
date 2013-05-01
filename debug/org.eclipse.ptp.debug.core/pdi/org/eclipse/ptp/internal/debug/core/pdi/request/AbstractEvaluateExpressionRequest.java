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
package org.eclipse.ptp.internal.debug.core.pdi.request;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluateExpressionRequest;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * 
 */
public abstract class AbstractEvaluateExpressionRequest extends AbstractEventResultRequest implements IPDIEvaluateExpressionRequest {
	private final String expr;

	/**
	 * @since 4.0
	 */
	public AbstractEvaluateExpressionRequest(TaskSet tasks, String expr) {
		super(tasks);
		this.expr = expr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventRequest#
	 * doExecute(org.eclipse.ptp.debug.core.pdi.IPDIDebugger)
	 */
	@Override
	public void doExecute(IPDIDebugger debugger) throws PDIException {
		debugger.evaluateExpression(tasks, expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIDataEvaluateExpressionRequest
	 * #getAIF(org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IAIF getAIF(TaskSet qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof IAIF) {
			return (IAIF) obj;
		}
		throw new PDIException(qTasks, NLS.bind(Messages.AbstractEvaluateExpressionRequest_0, expr));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#getName()
	 */
	public String getName() {
		return Messages.AbstractEvaluateExpressionRequest_1;
	}

}
