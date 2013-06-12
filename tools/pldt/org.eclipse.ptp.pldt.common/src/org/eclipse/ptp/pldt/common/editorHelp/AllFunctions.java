/**********************************************************************
 * Copyright (c) 2005,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.editorHelp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.IFunctionSummary;

/**
 * @deprecated this is assumed to be unused
 * 
 */

public class AllFunctions {
	private List<IFunctionSummary> allFunctionList = new ArrayList<IFunctionSummary>();

	public List<IFunctionSummary> getAllFunctionList() {
		return allFunctionList;
	}

	public void setAllFunctionList(List<IFunctionSummary> allFunctionList) {
		this.allFunctionList = allFunctionList;
	}

	public void addFunction(IFunctionSummary functionSummary)
	{
		allFunctionList.add(functionSummary);
	}
}
