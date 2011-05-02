/**********************************************************************
 * Copyright (c) 2005,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.editorHelp;

import org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary;

public class FunctionPrototypeSummaryImpl implements IFunctionPrototypeSummary
{
	private String name;
	private String returnType; // "void";
	private String arguments; // "int argc, char** argv";

	public FunctionPrototypeSummaryImpl()
	{

	}

	public FunctionPrototypeSummaryImpl(String name, String returnType, String arguments)
	{
		this.name = name;
		this.returnType = returnType;
		this.arguments = arguments;
	}

	public String getName()
	{
		return this.name;
	}

	public String getReturnType()
	{
		return returnType;
	}

	public String getArguments()
	{
		return arguments;
	}

	public String getPrototypeString(boolean namefirst)
	{
		return namefirst ? name + "(" + arguments + ") " + returnType : returnType + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ name + "(" + arguments + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
}