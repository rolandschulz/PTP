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

package org.eclipse.ptp.pldt.mpi.core.editorHelp;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;

/**
 * Help book for C++ MPI functions
 * @author tibbitts
 *
 */
public class MpiCPPHelpBook extends CHelpBookImpl
{
    private static final String TITLE = "MPI C++ Help Book";

    public MpiCPPHelpBook()
    {
		super(MpiPlugin.getPluginId());
    	// TODO populate func map for C++
    	
    	// set title
    	setTitle(TITLE);
    }
    
    public int getCHelpType()
    {
        return ICHelpBook.HELP_TYPE_CPP;
    }
}
