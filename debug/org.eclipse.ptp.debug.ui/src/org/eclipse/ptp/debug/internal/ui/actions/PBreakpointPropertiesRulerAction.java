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

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * @author clement chu
 *
 */
public class PBreakpointPropertiesRulerAction extends AbstractBreakpointRulerAction {
	public PBreakpointPropertiesRulerAction(IWorkbenchPart part, IVerticalRulerInfo info) {
		setInfo(info);
		setTargetPart(part);
		setText("Breakpoint Properties Ruler Action");
		setId(IPTPDebugUIConstants.ACTION_BREAKPOINT_PROPERTIES);
	}
	
	public void run() {
		if (getBreakpoint() != null) {
			PropertyDialogAction action = new PropertyDialogAction(getTargetPart().getSite(), new ISelectionProvider() {
				public void addSelectionChangedListener(ISelectionChangedListener listener) {
				}
				public ISelection getSelection() {
					return new StructuredSelection(getBreakpoint());
				}
				public void removeSelectionChangedListener(ISelectionChangedListener listener) {					
				}
				public void setSelection(ISelection selection) {
				}
				
			});
			action.run();
		}
	}
	
	public void update() {
		setBreakpoint(determineBreakpoint());
		//TODO replace IBreakpoint with IPBreakpoint
		if (getBreakpoint() == null || (getBreakpoint() instanceof IBreakpoint)) {
			setBreakpoint(null);
			setEnabled(false);
			return;
		}
		setEnabled(true);
	}
}
