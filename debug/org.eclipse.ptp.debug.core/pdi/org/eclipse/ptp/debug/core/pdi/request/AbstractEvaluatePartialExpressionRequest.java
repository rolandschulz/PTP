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
package org.eclipse.ptp.debug.core.pdi.request;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;


/**
 * @author clement
 *
 */
public abstract class AbstractEvaluatePartialExpressionRequest extends AbstractEventResultRequest implements IPDIEvaluatePartialExpressionRequest {
	private String expr;
	private String exprId;
	private boolean listChildren = false;
	private boolean express = false;
	
	public AbstractEvaluatePartialExpressionRequest(TaskSet tasks, String expr, String exprId) {
		this(tasks, expr, exprId, false, (exprId != null));
	}
	
	public AbstractEvaluatePartialExpressionRequest(TaskSet tasks, String expr, String exprId, boolean listChildren) {
		this(tasks, expr, exprId, listChildren, false);
	}
	
	public AbstractEvaluatePartialExpressionRequest(TaskSet tasks, String expr, String exprId, boolean listChildren, boolean express) {
		super(tasks);
		this.expr = expr;
		this.exprId = exprId;
		this.listChildren = listChildren;
		this.express = express;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractEventRequest#doExecute(org.eclipse.ptp.debug.core.pdi.IPDIDebugger)
	 */
	public void doExecute(IPDIDebugger debugger) throws PDIException {
		debugger.evaluatePartialExpression(tasks, expr, exprId, listChildren, express);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#getName()
	 */
	public String getName() {
		return Messages.AbstractEvaluatePartialExpressionRequest_0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluatePartialExpressionRequest#getPartialAIF(org.eclipse.ptp.core.util.TaskSet)
	 */
	public IAIF getPartialAIF(TaskSet qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof Object[]) {
			Object[] returnValues = (Object[])obj;
			return (IAIF)returnValues[1];
		}
		throw new PDIException(qTasks, NLS.bind(Messages.AbstractEvaluatePartialExpressionRequest_1, expr));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluatePartialExpressionRequest#getId(org.eclipse.ptp.core.util.TaskSet)
	 */
	public String getId(TaskSet qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof Object[]) {
			Object[] returnValues = (Object[])obj;
			return (String)returnValues[0];
		}
		throw new PDIException(qTasks, NLS.bind(Messages.AbstractEvaluatePartialExpressionRequest_2, exprId));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractEventRequest#toString()
	 */
	public String toString() {
		return NLS.bind(Messages.AbstractEvaluatePartialExpressionRequest_3, 
				new Object[] {getName(), getTasks(), expr, exprId, listChildren, express}); 
	}
}
