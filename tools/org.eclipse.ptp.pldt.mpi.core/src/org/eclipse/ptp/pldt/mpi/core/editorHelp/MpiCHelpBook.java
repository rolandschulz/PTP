/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation.
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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionSummaryImpl;
import org.eclipse.ptp.pldt.mpi.core.Messages;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.osgi.framework.Bundle;

/**
 * Help book for C MPI functions
 * 
 * @author tibbitts
 * 
 */
public class MpiCHelpBook extends CHelpBookImpl {
	private static final String TITLE = Messages.getString("MpiCHelpBook_MPI_C_HELP_BOOK_TITLE"); //$NON-NLS-1$

	/**
	 * builds the list of function summaries by parsing an XML file
	 */
	public MpiCHelpBook() {
		super(MpiPlugin.getPluginId());
		// populate func map
		Bundle bundle = Platform.getBundle(MpiPlugin.getPluginId());
		Path path = new Path("mpiref.xml"); //$NON-NLS-1$
		// //
		// URL fileURL = Platform.find(bundle, path); // old
		URL fileURL = FileLocator.find(bundle, path, null);
		InputStream xmlIn = null;
		try {
			xmlIn = fileURL.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<FunctionSummaryImpl> mpiFuncList = MPIDocXMLParser.parseDOM(xmlIn, "cname"); //$NON-NLS-1$
		for (Iterator<FunctionSummaryImpl> it = mpiFuncList.iterator(); it.hasNext();) {
			FunctionSummaryImpl functionSummary = it.next();
			funcName2FuncInfo.put(functionSummary.getName(), functionSummary);
		}

		// set title
		setTitle(TITLE);
	}
}
