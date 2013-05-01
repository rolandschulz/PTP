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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPValue;

/**
 * @author Clement chu
 * 
 */
public class PValueDetailProvider {
	private static PValueDetailProvider fInstance = null;

	/**
	 * Get instance
	 * 
	 * @return
	 */
	public static PValueDetailProvider getDefault() {
		if (fInstance == null) {
			fInstance = new PValueDetailProvider();
		}
		return fInstance;
	}

	/**
	 * Compute detail
	 * 
	 * @param value
	 * @param listener
	 */
	public void computeDetail(final IValue value, final IValueDetailListener listener) {
		if (value instanceof IPValue) {
			final IPStackFrame frame = PDebugUIUtils.getCurrentStackFrame();
			if (frame != null) {
				DebugPlugin.getDefault().asyncExec(new Runnable() {
					public void run() {
						listener.detailComputed(value, ((IPValue) value).evaluateAsExpression(frame));
					}
				});
			}
		}
	}
}
