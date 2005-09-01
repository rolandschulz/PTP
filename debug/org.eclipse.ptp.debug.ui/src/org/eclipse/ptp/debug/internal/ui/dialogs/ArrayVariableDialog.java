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
package org.eclipse.ptp.debug.internal.ui.dialogs;

import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Clement chu
 *
 */
public class ArrayVariableDialog extends VariableDialog {
	
	public ArrayVariableDialog(Shell parent, IStackFrame frame) {
		super(parent, frame);
	}
	
	protected ViewerFilter getViewFilter() {
		return new ArrayFilter();
	}
	
	private class ArrayFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IVariable) {
				try {
					return isArray((IVariable)element);
				} catch (DebugException e) {
					return false;
				}
			}
			return false;
		}
		
		private boolean isArray(IVariable variable) throws DebugException {
			if (variable instanceof ICVariable) {
				return ((ICVariable)variable).getType().isArray();
			}
			return false;
		}
	}
}
