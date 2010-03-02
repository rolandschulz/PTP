/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Photran modifications
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
/**
 * Class edited by Matthew Michelotti.
 * 
 * Overview of changes:
 * -Redirected the original IToken interface reference to an edited
 *  version of IToken in my package.
 * -Added a member variable fPreWhiteSpace to keep track of white
 *  spaces preceding this token.
 * -Made a new optional constructor with an extra parameter for passing
 *  in preceding white spaces.
 * -Added the methods getPrecedingWhiteSpace and getCharPrecedingWhiteSpace.
 * -Added the method setPrecedingWhiteSpace.
 * 
 * -Added a member variable fParent to keep track of the parent token
 * -initialized fParent to null in the constructors
 * -Added the methods getParent and setParent
 * 
 * -Added member variable fOrigOffset to keep track of the original value
 *  of offset, which will be the Lexer char-array offset for Lexer-constructed
 *  tokens
 * -initialized fOrigOffset in constructors
 * -Added the method getOrigOffset
 * 
 * -Changed the setPrecedingWhiteSpace function to set the preceding
 *  white space of this token and all of its ancestors.
 *  
 * -added getIncludeFile method
 */
package org.eclipse.photran.internal.core.preprocessor.c;

import org.eclipse.photran.internal.core.preprocessor.c.TokenUtil;

/**
 * Represents tokens found by the lexer. The preprocessor reuses the tokens and passes
 * them on to the parsers.
 * @since 5.0
 */
public class Token implements IToken, Cloneable {
	private int fKind;
	private int fOffset;
	private int fEndOffset;
	private IToken fNextToken;
	Object fSource;
	
	private char[] fPreWhiteSpace;
	private IToken fParent;
	
	private final int fOrigOffset;

	Token(int kind, Object source, int offset, int endOffset) {
		fKind= kind;
		fOffset= offset;
		fEndOffset= endOffset;
		fSource= source;
		fPreWhiteSpace = new char[] {};
		fParent = null;
		fOrigOffset = offset;
	}
	
	Token(int kind, Object source, int offset, int endOffset, char[] preWhiteSpace) {
        fKind= kind;
        fOffset= offset;
        fEndOffset= endOffset;
        fSource= source;
        fPreWhiteSpace = preWhiteSpace;
        fParent = null;
        fOrigOffset = offset;
    }

	final public int getType() {
		return fKind;
	}

	final public int getOffset() {
		return fOffset;
	}

	final public int getEndOffset() {
		return fEndOffset;
	}

	final public int getLength() {
		return fEndOffset-fOffset;
	}

	final public IToken getNext() {
		return fNextToken;
	}

	
	final public void setType(int kind) {
		fKind= kind;
	}

	final public void setNext(IToken t) {
		fNextToken= t;
	}

	public void setOffset(int offset, int endOffset) {
		fOffset= offset;
		fEndOffset= endOffset;
	}

	public void shiftOffset(int shift) {
		fOffset+= shift;
		fEndOffset+= shift;
	}

	public char[] getCharImage() {
		return TokenUtil.getImage(getType());
	}

	@Override
	public String toString() {
		return getImage();
	}
	
	final public boolean isOperator() {
		return TokenUtil.isOperator(fKind);
	}

	public String getImage() {
		return new String(getCharImage());
	}

	@Override
	final public Object clone() {
	    //throw new IllegalArgumentException();//for debugging to get stack trace
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	final public char[] getCharPrecedingWhiteSpace() {
	    return fPreWhiteSpace;
	}
	
	final public String getPrecedingWhiteSpace() {
	    return new String(getCharPrecedingWhiteSpace());
	}
	
	final public void setPrecedingWhiteSpace(char[] whiteSpace) {
	    fPreWhiteSpace = whiteSpace;
	    //Token t = this;
	    //do {
	    //    t.fPreWhiteSpace = whiteSpace;
	    //    t = (Token)t.fParent;
	    //}while(t != null);
	}
    
    final public IToken getParent() {
        return fParent;
    }
    
    final public void setParent(IToken parent) {
        fParent = parent;
    }
    
    final public int getOrigOffset() {
        return fOrigOffset;
    }

	public String getIncludeFile() {
		return null;
	}
}
