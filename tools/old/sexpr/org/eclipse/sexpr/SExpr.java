package org.eclipse.sexpr;

import java.text.ParseException;
import java.util.Vector;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

public class SExpr {
	private String strSexpr = null;
	private SExprElement sexprVal = null;
	private StreamTokenizer stToken;
	private boolean completeParse;
	
	private static final int DOUBLE_QUOTE = 34;
	private static final int SINGLE_QUOTE = 39;
	private static final int LEFT_PAREN = 40;
	private static final int RIGHT_PAREN = 41;
	private static final int SEMICOLON = 59;
	
	public SExpr(String sexpr) {
		StringReader sin = new StringReader(sexpr);
		init(sin, true);
	}

	public SExpr(Reader rdr) {
		init(rdr, false);
	}

	private void init(Reader rdr, boolean complete) {
		stToken = new StreamTokenizer(rdr);
		stToken.ordinaryChar(SINGLE_QUOTE);
		stToken.quoteChar(DOUBLE_QUOTE);
		stToken.eolIsSignificant(false);
		stToken.commentChar(SEMICOLON);
		stToken.wordChars(33, 33); // !
		stToken.wordChars(35, 38); // # - &
		stToken.wordChars(42, 47); // * - /
		stToken.wordChars(58, 58); // :
		stToken.wordChars(60, 64); // < - @
		stToken.wordChars(91, 96); // [ - `
		stToken.wordChars(123, 126); // { - ~
		completeParse = complete;
	}
	
	public boolean parse() throws ParseException, IOException {
		if (stToken.nextToken() == StreamTokenizer.TT_EOF)
			return false;
		
		strSexpr = null;

		if (stToken.ttype != LEFT_PAREN) {
			sexprVal = parseAtom();
			return !completeParse;
		}
		
		sexprVal = new SExprList();
		
		parseSX((SExprList)sexprVal);
		
		if (stToken.ttype != RIGHT_PAREN)
			throw new ParseException("expected ')'", stToken.lineno());
		
		if (completeParse) {
			if (stToken.nextToken() != StreamTokenizer.TT_EOF)
				throw new ParseException("trailing garbage", stToken.lineno());
			
			return false;
		}
		
		return true;
	}
	
	private SExprAtom parseAtom() throws ParseException, IOException {
		SExprAtom atom;
		
		switch (stToken.ttype) {
		case StreamTokenizer.TT_NUMBER:
			atom = new SExprAtom(new Double(stToken.nval));
			break;
			
		case StreamTokenizer.TT_WORD:
			atom = new SExprAtom(SExprAtom.SEXPR_ATOM_SYMBOL, stToken.sval);
			break;
			
		case DOUBLE_QUOTE:
			atom = new SExprAtom(SExprAtom.SEXPR_ATOM_STRING, stToken.sval);
			break;
			
		case SINGLE_QUOTE:
			String str = null;
			
			stToken.nextToken();
			
			switch (stToken.ttype) {
			case LEFT_PAREN:
				SExprList l = new SExprList();
				parseSX(l);
				str = l.toString();
				break;
				
			case StreamTokenizer.TT_NUMBER:
			case StreamTokenizer.TT_WORD:
			case DOUBLE_QUOTE:
				atom = parseAtom();
				str = atom.toString();
				break;

			default:
				throw new ParseException("parse error after \"'\"", stToken.lineno());
			}
			
			atom = new SExprAtom(SExprAtom.SEXPR_ATOM_QUOTED, str);
			break;
		
		default:
			throw new ParseException("error parsing atom", stToken.lineno());
		}
		
		return atom;
	}

	private void parseSX(SExprList list) throws ParseException, IOException {
		while (true) {
			stToken.nextToken();
			
			switch (stToken.ttype) {
			case LEFT_PAREN:
				SExprList nlist = new SExprList();
				parseSX(nlist);
				if (stToken.ttype != RIGHT_PAREN)
					throw new ParseException("expected ')'", stToken.lineno());
				list.add(nlist);
				break;
				
			case RIGHT_PAREN:
				return;
			
			case StreamTokenizer.TT_EOF:
				throw new ParseException("unexpected end of sexpr", stToken.lineno());

			default:
				list.add(parseAtom());
			}
				
		}
	}
	
	public String toString() {
		if (strSexpr == null) {
			strSexpr = sexprVal.toString();
		}
		
		return strSexpr;
	}
}
