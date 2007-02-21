package org.eclipse.ptp.lang.fortran.core.parser;

import org.antlr.runtime.Token;

public class FortranParserActionNull implements IFortranParserAction {
	
	FortranParserActionNull(FortranParser parser) {
		super();
	}

	public void buildExpressionBinaryOperator(int op) {
		// TODO Auto-generated method stub
	}

	public void buildExpressionConstant(LiteralConstant kind, Token cToken,
			KindParam kindType, Token ktToken) {
		// TODO Auto-generated method stub
	}

	public void buildExpressionID() {
		// TODO Auto-generated method stub
	}

}
