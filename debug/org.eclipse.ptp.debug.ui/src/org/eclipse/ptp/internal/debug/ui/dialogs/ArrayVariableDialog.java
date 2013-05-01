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
package org.eclipse.ptp.internal.debug.ui.dialogs;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Clement chu
 * 
 */
public class ArrayVariableDialog extends VariableDialog {
	public ArrayVariableDialog(Shell parent, IPStackFrame frame) {
		super(parent, frame);
	}

	@Override
	protected ViewerFilter getViewFilter() {
		return new ArrayFilter();
	}

	private class ArrayFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IPVariable) {
				try {
					IAIF aif = ((IPVariable) element).getAIF();
					if (aif == null) {
						return false;
					}
					if (aif.getType() instanceof IAIFTypeArray) {
						return true;
					}
				} catch (DebugException e) {
					return false;
				}
			}
			return false;
		}
	}
}
