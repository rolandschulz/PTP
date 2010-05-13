/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer.sourceform;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.CPreprocessingFreeFormLexerPhase1;
import org.eclipse.photran.internal.core.lexer.FreeFormLexerPhase2;
import org.eclipse.photran.internal.core.lexer.ILexer;
import org.eclipse.photran.internal.core.lexer.TokenFactory;

/**
 * An {@link ISourceForm} for free form Fortran source code that may
 * contain C preprocessor directives.
 * 
 * @author Jeff Overbey
 */
public class CPreprocessedFreeSourceForm implements ISourceForm
{
    public ILexer createLexer(
        Reader in, IFile file, String filename,
        TokenFactory tokenFactory, boolean accumulateWhitetext) throws IOException
    {
        return new FreeFormLexerPhase2(
            new CPreprocessingFreeFormLexerPhase1(
                in,
                file,
                filename,
                true));
    }

    public CPreprocessedFreeSourceForm configuredWith(Object data)
    {
        return this;
    }
}
