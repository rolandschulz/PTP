package org.eclipse.ptp.lang.fortran.core.parser;

import org.eclipse.ptp.lang.fortran.internal.core.dom.parser.FortranParserAction;

public class FortranParserActionFactory {
	
	static IFortranParserAction newAction(FortranParser parser, String kind) {
		return new FortranParserAction(parser);
	}

}
