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
package org.eclipse.ptp.internal.debug.ui.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.ptp.debug.core.model.IRestart;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;

/**
 * @author Clement chu
 */
public class RestartActionDelegate extends AbstractListenerActionDelegate {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction(Object element) throws DebugException {
		IRestart restartTarget = getRestartTarget(element);
		if (restartTarget != null) {
			restartTarget.restart();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#isEnabledFor(java.lang.Object)
	 */
	protected boolean isEnabledFor(Object element) {
		IRestart restartTarget = getRestartTarget(element);
		if (restartTarget != null) {
			return checkCapability(restartTarget);
		}
		return false;
	}

	protected boolean checkCapability(IRestart element) {
		return element.canRestart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return Messages.RestartActionDelegate_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return Messages.RestartActionDelegate_1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return Messages.RestartActionDelegate_2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#isRunInBackground()
	 */
	protected boolean isRunInBackground() {
		return true;
	}

	protected IRestart getRestartTarget(Object element) {
		if (element instanceof IAdaptable)
			return (IRestart)((IAdaptable)element).getAdapter(IRestart.class);
		return getDefaultRestartTarget(element);
	}

	private IRestart getDefaultRestartTarget(Object element) {
		if (element instanceof IDebugElement) {
			IDebugTarget target = ((IDebugElement)element).getDebugTarget();
			if (target instanceof IRestart)
				return (IRestart)target;
		}
		return null;
	}
}
