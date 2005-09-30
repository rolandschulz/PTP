/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.mpi.core.help;

import java.util.ArrayList;
import java.util.List;

public class AllFunctions {
	private List /* of FunctionSummary */ allFunctionList = new ArrayList();

	public List getAllFunctionList() {
		return allFunctionList;
	}

	public void setAllFunctionList(List allFunctionList) {
		this.allFunctionList = allFunctionList;
	}
	
	public void addFunction(FunctionSummaryImpl functionSummary)
	{
		allFunctionList.add(functionSummary);
	}
}
