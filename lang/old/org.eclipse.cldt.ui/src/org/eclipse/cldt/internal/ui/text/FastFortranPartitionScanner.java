package org.eclipse.cldt.internal.ui.text;


/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cldt.internal.ui.text.BufferedDocumentScanner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * This scanner recognizes the C multi line comments, C single line comments,
 * C strings and C characters.
 */
public class FastFortranPartitionScanner implements IPartitionTokenScanner, IFortranPartitions {

	// states
	private static final int FORTRANCODE= 0;	
	private static final int SINGLE_LINE_COMMENT= 1;
	private static final int STRING= 2;
	
	// beginning of prefixes and postfixes
	private static final int NONE= 0;
	private static final int BACKSLASH= 1; // postfix for STRING and CHARACTER
	private static final int BANG= 2; // prefix for SINGLE_LINE 
	private static final int CARRIAGE_RETURN= 3; // postfix for STRING, CHARACTER and SINGLE_LINE_COMMENT
	
	/** The scanner. */
//	private final BufferedRuleBasedScanner fScanner= new BufferedRuleBasedScanner(1000);
	private final BufferedDocumentScanner fScanner= new BufferedDocumentScanner(1000);	// faster implementation
	
	/** The offset of the last returned token. */
	private int fTokenOffset;
	/** The length of the last returned token. */
	private int fTokenLength;
	
	/** The state of the scanner. */	
	private int fState;
	/** The last significant characters read. */
	private int fLast;
	/** The amount of characters already read on first call to nextToken(). */
	private int fPrefixLength;
	
	private int fCOffset;
	private int fCLength;
	
	private final IToken[] fTokens= new IToken[] {
		new Token(null),
		new Token(FORTRAN_SINGLE_LINE_COMMENT),
		new Token(SKIP),
		new Token(FORTRAN_STRING)
	};

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		
		fTokenOffset += fTokenLength;
		fTokenLength= fPrefixLength;

		while (true) {
			final int ch= fScanner.read();
			
			// characters
	 		switch (ch) {
	 		case ICharacterScanner.EOF:
		 		if (fTokenLength > 0) {
		 			fLast= NONE; // ignore last
		 			return preFix(fState, FORTRANCODE, NONE, 0);

		 		}
		 		fLast= NONE;
		 		fPrefixLength= 0;
		 		return Token.EOF;

	 		case '\r':
	 			if (fLast != CARRIAGE_RETURN) {
						fLast= CARRIAGE_RETURN;
						fTokenLength++;
	 					continue;

	 			}
	 				
	 			switch (fState) {
	 			case SINGLE_LINE_COMMENT:
	 			case STRING:
	 				if (fTokenLength > 0) {
	 					IToken token= fTokens[fState];
	 					
	 					fLast= CARRIAGE_RETURN;	
	 					fPrefixLength= 1;
	 					
	 					fState= FORTRANCODE;
	 					return token;
	 					
	 				}
	 				consume();
	 				continue;	
	 				
	 			default:
	 				consume();
	 			continue;
	 		}
	
	 		case '\n': 		 		
				switch (fState) {
				case SINGLE_LINE_COMMENT:
				case STRING:				
					// assert(fTokenLength > 0);
					return postFix(fState);

				default:
					consume();
					continue;
				}

			default:
				if (fLast == CARRIAGE_RETURN) {			
					switch (fState) {
					case SINGLE_LINE_COMMENT:
					case STRING:

						int last;
						int newState;
						switch (ch) {
						case '!':
							last= BANG;
							newState= FORTRANCODE;
							break;

						case '\'':
						case '"':
							last= NONE;
							newState= STRING;
							break;

						case '\r':
							last= CARRIAGE_RETURN;
							newState= FORTRANCODE;
							break;

						case '\\':
							last= BACKSLASH;
							newState= FORTRANCODE;
							break;

						default:
							last= NONE;
							newState= FORTRANCODE;
							break;
						}
						
						fLast= NONE; // ignore fLast
						return preFix(fState, newState, last, 1);
	
					default:
						break;
					}
				}
			}

			// states	 
	 		switch (fState) {
	 		case FORTRANCODE:
				switch (ch) {
				case '!':
					if (fTokenLength  > 0) {
						return preFix(FORTRANCODE, SINGLE_LINE_COMMENT, NONE, 1);
					}							
					preFix(FORTRANCODE, SINGLE_LINE_COMMENT, NONE, 1);
					fTokenOffset += fTokenLength;
					fTokenLength= fPrefixLength;
					break;
	
				case '\'':
				case '"':
					fLast= NONE; // ignore fLast				
					if (fTokenLength > 0)
						return preFix(FORTRANCODE, STRING, NONE, 1);
					preFix(FORTRANCODE, STRING, NONE, 1);
					fTokenOffset += fTokenLength;
					fTokenLength= fPrefixLength;
					break;
	
				default:
					consume();
					break;
				}
				break;
	
	 		case SINGLE_LINE_COMMENT:
				consume();
				break;
	
	 		case STRING:
	 			switch (ch) {
	 			case '\\':
					fLast= (fLast == BACKSLASH) ? NONE : BACKSLASH;
					fTokenLength++;
					break;
					
	 			case '\'':
				case '\"':	 			 			
	 				if (fLast != BACKSLASH) {
	 					return postFix(STRING);

		 			}
	 				consume();
	 				break; 					
		 		
		 		default:
					consume();
	 				break;
	 			}
	 			break;
	
	 		}
		} 
 	}		

	private static final int getLastLength(int last) {
		switch (last) {
		default:
			return -1;

		case NONE:
			return 0;
			
		case CARRIAGE_RETURN:
		case BACKSLASH:
		case BANG:
			return 1;
		}	
	}

	private final void consume() {
		fTokenLength++;
		fLast= NONE;	
	}
	
	private final IToken postFix(int state) {
		fTokenLength++;
		fLast= NONE;
		fState= FORTRANCODE;
		fPrefixLength= 0;		
		return fTokens[state];
	}

	private final IToken preFix(int state, int newState, int last, int prefixLength) {
		fTokenLength -= getLastLength(fLast);
		fLast= last;
		fPrefixLength= prefixLength;
		IToken token= fTokens[state];		
		fState= newState;
		return token;
	}

	private static int getState(String contentType) {

		if (contentType == null)
			return FORTRANCODE;

		else if (contentType.equals(FORTRAN_SINGLE_LINE_COMMENT))
			return SINGLE_LINE_COMMENT;

		else if (contentType.equals(FORTRAN_STRING))
			return STRING;

		else
			return FORTRANCODE;
	}

	/*
	 * @see IPartitionTokenScanner#setPartialRange(IDocument, int, int, String, int)
	 */
	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {

		fScanner.setRange(document, offset, length);
		fTokenOffset= partitionOffset;
		fTokenLength= 0;
		fPrefixLength= offset - partitionOffset;
		fLast= NONE;
		
		if (offset == partitionOffset) {
			// restart at beginning of partition
			fState= FORTRANCODE;
		} else {
			fState= getState(contentType);			
		}
	}

	/*
	 * @see ITokenScanner#setRange(IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {

		fScanner.setRange(document, offset, length);
		fTokenOffset= offset;
		fTokenLength= 0;		
		fPrefixLength= 0;
		fLast= NONE;
		fState= FORTRANCODE;
	}

	/*
	 * @see ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		return fTokenLength;
	}

	/*
	 * @see ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		return fTokenOffset;
	}

}