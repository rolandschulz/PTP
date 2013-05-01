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

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.debug.core.model.IEnableDisableTarget;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author Clement chu
 */
public class EnableVariablesActionDelegate implements IViewActionDelegate {
	private IViewPart fView;

	private IAction fAction;

	public EnableVariablesActionDelegate() {
	}

	protected IViewPart getView() {
		return fView;
	}

	protected void setView(IViewPart view) {
		fView = view;
	}

	protected IAction getAction() {
		return fAction;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}

	/**
	 * This action enables variables.
	 */
	protected boolean isEnableAction() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		setView(view);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection = getSelection();
		final int size = selection.size();
		if (size == 0) {
			return;
		}
		final Iterator<?> it = selection.iterator();
		final MultiStatus ms = new MultiStatus(PTPDebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED,
				Messages.EnableVariablesActionDelegate_0, null);
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				while (it.hasNext()) {
					IEnableDisableTarget target = getEnableDisableTarget(it.next());
					if (target != null) {
						try {
							if (size > 1) {
								target.setEnabled(isEnableAction());
							} else {
								target.setEnabled(!target.isEnabled());
							}
						} catch (DebugException e) {
							ms.merge(e.getStatus());
						}
					}
				}
				update();
			}
		});
		if (!ms.isOK()) {
			PTPDebugUIPlugin.errorDialog(Messages.EnableVariablesActionDelegate_1, ms);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection sel = (IStructuredSelection) selection;
		Object o = sel.getFirstElement();
		if (getEnableDisableTarget(o) == null) {
			return;
		}
		Iterator<?> it = sel.iterator();
		boolean allEnabled = true;
		boolean allDisabled = true;
		while (it.hasNext()) {
			IEnableDisableTarget target = getEnableDisableTarget(it.next());
			if (target != null && !target.canEnableDisable()) {
				continue;
			}
			if (target.isEnabled()) {
				allDisabled = false;
			} else {
				allEnabled = false;
			}
		}
		if (isEnableAction()) {
			action.setEnabled(!allEnabled);
		} else {
			action.setEnabled(!allDisabled);
		}
	}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection) getView().getViewSite().getSelectionProvider().getSelection();
	}

	protected void update() {
		getView().getViewSite().getSelectionProvider().setSelection(getView().getViewSite().getSelectionProvider().getSelection());
	}

	protected IEnableDisableTarget getEnableDisableTarget(Object obj) {
		IEnableDisableTarget target = null;
		if (obj instanceof IAdaptable) {
			target = (IEnableDisableTarget) ((IAdaptable) obj).getAdapter(IEnableDisableTarget.class);
		}
		return target;
	}
}
