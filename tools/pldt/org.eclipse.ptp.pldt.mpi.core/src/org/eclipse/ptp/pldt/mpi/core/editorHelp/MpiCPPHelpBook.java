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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionSummaryImpl;
import org.eclipse.ptp.pldt.mpi.core.Messages;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.osgi.framework.Bundle;

/**
 * Help book for C++ MPI functions
 * 
 * @author tibbitts
 * 
 */
public class MpiCPPHelpBook extends CHelpBookImpl
{
	private static final String TITLE = Messages.getString("MpiCPPHelpBook_MPI_CPP_HELP_BOOK_TITLE"); //$NON-NLS-1$
	private static final boolean traceOn = false;

	public MpiCPPHelpBook()
	{
		super(MpiPlugin.getPluginId());
		if (traceOn)
			System.out.println("MPI CPP help book ctor()..."); //$NON-NLS-1$
		// populate func map
		Bundle bundle = Platform.getBundle(MpiPlugin.getPluginId());
		Path path = new Path("mpiref.xml"); //$NON-NLS-1$

		URL fileURL = FileLocator.find(bundle, path, null);
		InputStream xmlIn = null;
		try {
			xmlIn = fileURL.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<FunctionSummaryImpl> mpiFuncList = MPIDocXMLParser.parseDOM(xmlIn, "cppname"); //$NON-NLS-1$
		int temp = 0;
		for (Iterator<FunctionSummaryImpl> it = mpiFuncList.iterator(); it.hasNext();) {
			FunctionSummaryImpl functionSummary = it.next();
			if (traceOn)
				if (2 > temp++)
					System.out.println("  " + functionSummary.getName() + "-" + functionSummary.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
			funcName2FuncInfo.put(functionSummary.getName(), functionSummary);
		}
		;
		// set title
		setTitle(TITLE);

	}

	public int getCHelpType()
	{
		return ICHelpBook.HELP_TYPE_CPP;
	}

}
