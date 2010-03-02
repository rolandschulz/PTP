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
 * -Redirected the original Token class reference to an edited version
 *  of Token in my package.
 * -Made a new optional constructor with an extra parameter for passing
 *  in preceding white spaces.
 */
package org.eclipse.photran.internal.core.preprocessor.c;

import org.eclipse.photran.internal.core.preprocessor.c.TokenUtil;

/**
 * Tokens for digraphs simply have a different image.
 * @since 5.0
 */
public class TokenForDigraph extends Token {
    public TokenForDigraph(int kind, Object source, int offset, int endOffset) {
		super(kind, source, offset, endOffset);
	}
	
    public TokenForDigraph(int kind, Object source, int offset, int endOffset, char[] preWhiteSpace) {
        super(kind, source, offset, endOffset, preWhiteSpace);
    }

	@Override
	public char[] getCharImage() {
		return TokenUtil.getDigraphImage(getType());
	}
}
