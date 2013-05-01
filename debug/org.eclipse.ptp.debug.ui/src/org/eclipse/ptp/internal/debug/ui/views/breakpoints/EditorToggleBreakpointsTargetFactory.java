/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui.views.breakpoints;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ui.IWorkbenchPart;

public class EditorToggleBreakpointsTargetFactory implements IToggleBreakpointsTargetFactory {
	public static final String PARALLEL_BREAKPOINT_TOGGLE_TARGET = "org.eclipse.ptp.debug.parallel.breakpoint"; //$NON-NLS-1$
	protected ToggleBreakpointsTarget fToggleBreakpointsTarget = new ToggleBreakpointsTarget();
	protected HashSet<String> fToggleTargets = new HashSet<String>();

	public EditorToggleBreakpointsTargetFactory() {
		fToggleTargets.add(PARALLEL_BREAKPOINT_TOGGLE_TARGET);
	}

	public IToggleBreakpointsTarget createToggleTarget(String targetID) {
		if (PARALLEL_BREAKPOINT_TOGGLE_TARGET.equals(targetID)) {
			return fToggleBreakpointsTarget;
		}
		return null;
	}

	public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
		return PARALLEL_BREAKPOINT_TOGGLE_TARGET;
	}

	public String getToggleTargetDescription(String targetID) {
		if (PARALLEL_BREAKPOINT_TOGGLE_TARGET.equals(targetID)) {
			return Messages.EditorToggleBreakpointsTargetFactory_0;
		}
		return null;
	}

	public String getToggleTargetName(String targetID) {
		if (PARALLEL_BREAKPOINT_TOGGLE_TARGET.equals(targetID)) {
			return Messages.EditorToggleBreakpointsTargetFactory_1;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Set getToggleTargets(IWorkbenchPart part, ISelection selection) {
		return fToggleTargets;
	}

}
