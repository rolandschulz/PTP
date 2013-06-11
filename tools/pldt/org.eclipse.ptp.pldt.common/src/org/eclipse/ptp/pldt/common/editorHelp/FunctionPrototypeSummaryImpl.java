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

/**
 * Encapsulation of a function's name, return type, and arguments
 * @author beth
 *
 */
public class FunctionPrototypeSummaryImpl implements IFunctionPrototypeSummary
{
	private String name;
	private String returnType; // "void";
	private String arguments; // "int argc, char** argv";

	/**
	 * Default Constructor
	 */
	public FunctionPrototypeSummaryImpl()
	{
	}

	/**
	 * Constructor with information provided
	 * @param name the function name
	 * @param returnType return type of function
	 * @param arguments arguments to the function
	 */
	public FunctionPrototypeSummaryImpl(String name, String returnType, String arguments)
	{
		this.name = name;
		this.returnType = returnType;
		this.arguments = arguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary#getName()
	 */
	public String getName()
	{
		return this.name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary#getReturnType()
	 */
	public String getReturnType()
	{
		return returnType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary#getArguments()
	 */
	public String getArguments()
	{
		return arguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary#getPrototypeString(boolean)
	 */
	public String getPrototypeString(boolean namefirst)
	{
		return namefirst ? name + "(" + arguments + ") " + returnType : returnType + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ name + "(" + arguments + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Set the arguments
	 * @param arguments
	 */
	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	/**
	 * Set the name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the return type
	 * @param returnType
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
}