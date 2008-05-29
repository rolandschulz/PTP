/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.util;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Various utility methods for project
 * 
 * @author Beth Tibbitts
 * 
 * 
 */
public class AnalysisUtil {
 
	private AnalysisUtil() {
		super();
	}

	/**
	 * Is the given filename a valid file for analysis processing? <br>
	 * Currently this means, is it a .c or .cpp etc file, or .h or .hpp? <br>
	 * Don't pollute this with fortran info - currently fortran processing is in
	 * a completely different action.
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean validForAnalysis(String filename, boolean isCPP) {
		int loc = filename.lastIndexOf(".");
		if (loc <= 0) // if no dot, or filename is ".foo", not valid for
						// analysis.
			return false;
		String ext = filename.substring(loc + 1);
		ext = ext.toLowerCase();
		boolean result = false;
		if (ext.startsWith("c")/* ||ext.startsWith("f") */) // c or fortran
			result = true;
		else
			if(isCPP && ext.startsWith("h")) // C++ can have code in header files
			  result=true;
		return result;
	}

	/**
	 * Given a C/C++ source, decide if it is C or C++.
	 * 
	 * @param file
	 *            must be either C or CPP file
	 * @return
	 */
	public static ParserLanguage getLanguageFromFile(IFile file) {
		if (file == null) { // assume CPP
			return ParserLanguage.CPP;
		}

		IProject project = file.getProject();
		String lid = null;
		IContentType type = CCorePlugin.getContentType(project, file.getFullPath().lastSegment());
		if (type != null) {
			lid = type.getId();
		}
		if (lid != null
				&& (lid.equals(CCorePlugin.CONTENT_TYPE_CXXSOURCE) || lid.equals(CCorePlugin.CONTENT_TYPE_CXXHEADER))) {
			return ParserLanguage.CPP;
		}

		return ParserLanguage.C;
	}
}
