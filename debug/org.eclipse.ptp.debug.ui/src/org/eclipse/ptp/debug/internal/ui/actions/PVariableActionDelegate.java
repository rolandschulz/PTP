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
package org.eclipse.ptp.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.debug.core.model.IPVariable;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author Clement chu
 * 
 */
public abstract class PVariableActionDelegate implements IObjectActionDelegate {
	private IPVariable[] fVariables = null;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

	public void run(IAction action) {
		IPVariable[] vars = getVariables();
		if (vars != null && vars.length > 0) {
			final MultiStatus ms = new MultiStatus(PTPDebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, "", null);
			BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
				public void run() {
					try {
						doAction(getVariables());
					}
					catch(DebugException e) {
						ms.merge(e.getStatus());
					}
				}
			} );
			if (!ms.isOK()) {
				IWorkbenchWindow window = PTPDebugUIPlugin.getActiveWorkbenchWindow();
				if ( window != null ) {
					PTPDebugUIPlugin.errorDialog(ActionMessages.getString("VariableFormatActionDelegate.0" ), ms);
				}
				else {
					PTPDebugUIPlugin.log(ms);
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			List list = new ArrayList();
			IStructuredSelection ssel = (IStructuredSelection)selection;
			Iterator i = ssel.iterator();
			while(i.hasNext()) {
				Object o = i.next();
				if (o instanceof IPVariable) {
					list.add(o);
				}
			}
			setVariables((IPVariable[])list.toArray(new IPVariable[list.size()]));
		}
		else {
			action.setChecked(false);
			action.setEnabled(false);
		}
	}

	protected IPVariable[] getVariables() {
		return fVariables;
	}

	private void setVariables(IPVariable[] variables) {
		fVariables = variables;
	}
	protected abstract void doAction(IPVariable[] vars) throws DebugException;
}
