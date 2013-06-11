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

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

/**
 * 
 * This class implements ICHelpProvider and provides help information
 */
public abstract class CHelpProviderImpl implements ICHelpProvider
{
	protected CHelpBookImpl helpBook;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getCHelpBooks()
	 */
	public ICHelpBook[] getCHelpBooks()
	{
		return new ICHelpBook[] { helpBook };
	}

	/**
	 * Called on hover
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getFunctionInfo(org.eclipse.cdt.ui.text.ICHelpInvocationContext, org.eclipse.cdt.ui.ICHelpBook[], java.lang.String)
	 */
	public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name)
	{
		return (helpBooks.length == 0) ? null : helpBook.getFunctionInfo(context, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getMatchingFunctions(org.eclipse.cdt.ui.text.ICHelpInvocationContext, org.eclipse.cdt.ui.ICHelpBook[], java.lang.String)
	 */
	public IFunctionSummary[] getMatchingFunctions(ICHelpInvocationContext context, ICHelpBook[] helpBooks,
			String prefix)
	{
		return (helpBooks.length == 0) ? null : helpBook.getMatchingFunctions(context, prefix);
	}

	/**
	 * Called on F1
	 * @see org.eclipse.cdt.ui.ICHelpProvider#getHelpResources(org.eclipse.cdt.ui.text.ICHelpInvocationContext, org.eclipse.cdt.ui.ICHelpBook[], java.lang.String)
	 */
	public ICHelpResourceDescriptor[] getHelpResources(ICHelpInvocationContext context, ICHelpBook[] helpBooks,
			String name)
	{
		return (helpBooks.length == 0) ? null : helpBook.getHelpResources(context, name);
	}
}
