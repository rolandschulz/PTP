// remove the following two lines for use outside of Eclipse
package org.eclipse.ptp.lang.fortran.core.parser;
import org.eclipse.ptp.lang.fortran.internal.core.dom.parser.FortranParserActionDom;

public class FortranParserActionFactory {
	
	static IFortranParserAction newAction(FortranParser parser, String kind) {
		IFortranParserAction action = null;
		if (kind.compareToIgnoreCase("cdt") == 0) {
			// remove following line for use outside of Eclipse
			action = new FortranParserActionDom(parser);
		}
		if (action == null) {
			action = new FortranParserActionNull(parser);
		}
		return action;
	}

}
