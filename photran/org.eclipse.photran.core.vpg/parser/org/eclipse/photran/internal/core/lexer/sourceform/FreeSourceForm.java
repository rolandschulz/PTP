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
import org.eclipse.photran.internal.core.lexer.FreeFormLexerPhase2;
import org.eclipse.photran.internal.core.lexer.ILexer;
import org.eclipse.photran.internal.core.lexer.TokenFactory;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.PreprocessingFreeFormLexerPhase1;

/**
 * An {@link ISourceForm} for free form Fortran source code that may
 * contain INCLUDE lines (but not any other kind of preprocessor directives).
 * 
 * @author Jeff Overbey
 */
public class FreeSourceForm implements ISourceForm
{
    public static final String DESCRIPTION = "Free Form";

    private IncludeLoaderCallback callback = null;
    
    public ILexer createLexer(
        Reader in, IFile file, String filename,
        TokenFactory tokenFactory, boolean accumulateWhitetext) throws IOException
    {
        return new FreeFormLexerPhase2(
            new PreprocessingFreeFormLexerPhase1(
                in,
                file,
                filename,
                callback,
                accumulateWhitetext));
    }

    public FreeSourceForm configuredWith(Object data)
    {
        if (data instanceof IncludeLoaderCallback)
            this.callback = (IncludeLoaderCallback)data;

        return this;
    }
}
