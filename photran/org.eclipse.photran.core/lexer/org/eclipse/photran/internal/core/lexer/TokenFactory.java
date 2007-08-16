/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

/**
 * A <code>TokenFactory</code> is used by the Fortran lexer to create IToken objects.
 * <p>
 * This is necessary since the parser uses a Token object that is dependent on
 * the VPG, which has purposely been separated from the Photran core.
 * 
 * @author Jeff Overbey
 */
public interface TokenFactory
{
    public IToken createToken(Terminal terminal, String whiteBefore, String tokenText, String whiteAfter);
    public IToken createToken(Terminal terminal, String tokenText);
}
