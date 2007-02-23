/*******************************************************************************
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

// remove the following two lines for use outside of Eclipse
package org.eclipse.ptp.lang.fortran.core.parser;
import org.eclipse.ptp.lang.fortran.internal.core.dom.parser.FortranParserActionDom;

public class FortranParserActionFactory {
	
	static IFortranParserAction newAction(FortranParser parser, String kind) {
		IFortranParserAction action = null;
		if (kind.compareToIgnoreCase("cdt") == 0) {
			// remove following line for use outside of Eclipse
			action = new FortranParserActionDom(parser);
		} else if (kind.compareToIgnoreCase("dump") == 0) {
			action = new FortranParserActionPrint(parser);
		}
		if (action == null) {
			action = new FortranParserActionNull(parser);
		}
		return action;
	}

}
