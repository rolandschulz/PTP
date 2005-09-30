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

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpProvider;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

/**
 * 
 * This class implements ICHelpProvider and provides MPI information
 */
public class CHelpProviderImpl implements ICHelpProvider
{
    protected CHelpBookImpl helpBook;
    
    public void initialize()
    {
        helpBook = new MpiCHelpBook();
    }

    public ICHelpBook[] getCHelpBooks()
    {
        return new ICHelpBook[] {helpBook};
    }

    public IFunctionSummary getFunctionInfo(ICHelpInvocationContext context, ICHelpBook[] helpBooks, String name)
    {
        return (helpBooks.length == 0) ? null : helpBook.getFunctionInfo(context, name);
    }

    public IFunctionSummary[] getMatchingFunctions(ICHelpInvocationContext context, ICHelpBook[] helpBooks,
            String prefix)
    {
        return (helpBooks.length == 0) ? null : helpBook.getMatchingFunctions(context, prefix);
    }

    public ICHelpResourceDescriptor[] getHelpResources(ICHelpInvocationContext context, ICHelpBook[] helpBooks,
            String name)
    {
        return (helpBooks.length == 0) ? null : helpBook.getHelResources(context, name);
    }
}
