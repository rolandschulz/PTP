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
package org.eclipse.ptp.debug.internal.ui.views.variable;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.debug.ui.PVariableManager.PVariableInfo;
import org.eclipse.swt.graphics.Image;

/**
 * @author Clement chu
 */
public class PVariableLabelProvider extends LabelProvider implements ITableLabelProvider, ICheckProvider {
	public boolean isCheck(Object element) {
		if (element instanceof PVariableInfo) {
			return ((PVariableInfo) element).isEnabled();
		}
		return false;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof PVariableInfo) {
			PVariableInfo jVar = (PVariableInfo) element;
			switch (columnIndex) {
			case 1:
				return jVar.getName();
			case 2:
				return "XXX PVariableLabelProvider"; // jVar.getJob().getName();
			}
		}
		return null;
	}
}