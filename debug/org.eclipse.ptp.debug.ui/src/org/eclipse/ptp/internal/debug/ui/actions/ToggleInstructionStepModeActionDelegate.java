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

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * @author Clement chu
 */
public class ToggleInstructionStepModeActionDelegate extends ActionDelegate implements IViewActionDelegate,
		IPreferenceChangeListener {
	private IPDebugTarget fTarget = null;
	private IAction fAction = null;
	private IViewPart fView;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.
	 * IPreferenceChangeListener
	 * #preferenceChange(org.eclipse.core.runtime.preferences
	 * .IEclipsePreferences.PreferenceChangeEvent)
	 */
	public void preferenceChange(PreferenceChangeEvent event) {
		IAction action = getAction();
		if (action != null) {
			if (event.getNewValue() instanceof Boolean) {
				boolean value = ((Boolean) event.getNewValue()).booleanValue();
				if (value != action.isChecked())
					action.setChecked(value);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fView = view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	@Override
	public void dispose() {
		IPDebugTarget target = getTarget();
		if (target != null) {
			target.removePreferenceChangeListener(this);
		}
		setTarget(null);
		setAction(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
		setAction(action);
		action.setChecked(false);
		action.setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		/*
		 * boolean enabled = getAction().isChecked(); IPDebugTarget target =
		 * getTarget(); if (target != null) {
		 * target.enableInstructionStepping(enabled); if (enabled) { try {
		 * getView
		 * ().getSite().getPage().showView(IPTPDebugUIConstants.ID_DISASSEMBLY_VIEW
		 * ); } catch(PartInitException e) {
		 * PTPDebugUIPlugin.log(e.getStatus()); } } }
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action
	 * .IAction, org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		IPDebugTarget newTarget = null;
		if (selection instanceof IStructuredSelection) {
			newTarget = getTargetFromSelection(((IStructuredSelection) selection).getFirstElement());
		}
		IPDebugTarget oldTarget = getTarget();
		if (oldTarget != null && !oldTarget.equals(newTarget)) {
			oldTarget.removePreferenceChangeListener(this);
			setTarget(null);
			action.setChecked(false);
		}
		if (newTarget != null && !newTarget.isTerminated() && !newTarget.isDisconnected()) {
			setTarget(newTarget);
			newTarget.addPreferenceChangeListener(this);
			action.setChecked(newTarget.isInstructionSteppingEnabled());
		}
		action.setEnabled(newTarget != null && newTarget.supportsInstructionStepping() && !newTarget.isTerminated()
				&& !newTarget.isDisconnected());
	}

	private IPDebugTarget getTarget() {
		return this.fTarget;
	}

	private void setTarget(IPDebugTarget target) {
		this.fTarget = target;
	}

	private IAction getAction() {
		return this.fAction;
	}

	private void setAction(IAction action) {
		this.fAction = action;
	}

	private IPDebugTarget getTargetFromSelection(Object element) {
		if (element instanceof IDebugElement) {
			IDebugTarget target = ((IDebugElement) element).getDebugTarget();
			return (target instanceof IPDebugTarget) ? (IPDebugTarget) target : null;
		}
		return null;
	}

	public IViewPart getView() {
		return fView;
	}
}
