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
package org.eclipse.ptp.internal.debug.ui;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.ptp.debug.core.model.IPStackFrame;

/**
 * @author Clement chu
 */
public class PWatchExpressionDelegate implements IWatchExpressionDelegate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IWatchExpressionDelegate#evaluateExpression(java.lang.String,
	 * org.eclipse.debug.core.model.IDebugElement, org.eclipse.debug.core.model.IWatchExpressionListener)
	 */
	public void evaluateExpression(final String expression, IDebugElement context, final IWatchExpressionListener listener) {
		if (!(context instanceof IPStackFrame)) {
			listener.watchEvaluationFinished(null);
			return;
		}
		final IPStackFrame frame = (IPStackFrame) context;
		Runnable runnable = new Runnable() {
			public void run() {
				IValue value = null;
				DebugException de = null;
				try {
					value = frame.evaluateExpression(expression);
				} catch (DebugException e) {
					de = e;
				}
				IWatchExpressionResult result = evaluationComplete(expression, value, de);
				listener.watchEvaluationFinished(result);
			}
		};
		DebugPlugin.getDefault().asyncExec(runnable);
	}

	/**
	 * Get watch expression result
	 * 
	 * @param expression
	 * @param value
	 * @param de
	 * @return
	 */
	protected IWatchExpressionResult evaluationComplete(final String expression, final IValue value, final DebugException de) {
		return new IWatchExpressionResult() {
			public IValue getValue() {
				return value;
			}

			public boolean hasErrors() {
				return (de != null);
			}

			public String getExpressionText() {
				return expression;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.debug.core.model.IWatchExpressionResult#getException()
			 */
			public DebugException getException() {
				return de;
			}

			public String[] getErrorMessages() {
				return (de != null) ? new String[] { de.getMessage() } : new String[0];
			}
		};
	}
}
