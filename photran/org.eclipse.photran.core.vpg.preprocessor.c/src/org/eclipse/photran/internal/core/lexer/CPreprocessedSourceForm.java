/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;

public final class CPreprocessedSourceForm extends SourceForm
{
	protected IncludeLoaderCallback callback;

    public CPreprocessedSourceForm()
    {
    	this.callback = null;
    }

    public CPreprocessedSourceForm(IncludeLoaderCallback callback)
    {
    	this.callback = callback;
    }

    @Override public IAccumulatingLexer createLexer(InputStream in, IFile file, String filename, boolean accumulateWhitetext) throws IOException
    {
        return new LexerPhase3(new FreeFormLexerPhase2(new CPreprocessingFreeFormLexerPhase1(in, file, filename, callback, accumulateWhitetext)));
    }
    
    @Override public String getDescription(String filename)
    {
        return "Free Form (C Preprocessed)";
    }
}
