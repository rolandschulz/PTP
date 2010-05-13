/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.internal.core.lexer.sourceform.ISourceForm;
import org.eclipse.photran.internal.core.lexer.sourceform.SourceForm;

/**
 * A collection of (static) factory methods for creating Fortran lexers.
 * 
 * @author Jeff Overbey
 */
public class LexerFactory
{
    protected final TokenFactory tokenFactory;
    protected final boolean accumulateWhitetext;
    
    public LexerFactory(TokenFactory tokenFactory)
    {
        this(tokenFactory, true);
    }
    
    public LexerFactory(TokenFactory tokenFactory, boolean accumulateWhitetext)
    {
        this.tokenFactory = tokenFactory;
        this.accumulateWhitetext = accumulateWhitetext;
    }
    
    public ILexer createLexer(IFile file) throws CoreException, IOException
    {
        return createLexer(
            new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset())),
            file,
            SourceForm.determineFilename(file));
    }
    
    public ILexer createLexer(IFile file, ISourceForm sourceForm) throws CoreException, IOException
    {
        return createLexer(
            new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset())),
            file,
            SourceForm.determineFilename(file),
            sourceForm);
    }

    public ILexer createLexer(File file) throws IOException
    {
        return createLexer(
            new BufferedReader(new FileReader(file)),
            null,
            file.getAbsolutePath());
    }

    public ILexer createLexer(File file, ISourceForm sourceForm) throws IOException
    {
        return createLexer(
            new BufferedReader(new FileReader(file)),
            null,
            file.getAbsolutePath(),
            sourceForm);
    }
    
    public ILexer createLexer(Reader in, IFile file, String filename) throws IOException
    {
        return createLexer(
            in,
            file,
            filename,
            SourceForm.of(file, filename));
    }
    
    public ILexer createLexer(Reader in, IFile file, String filename, ISourceForm sourceForm) throws IOException
    {
        return sourceForm.createLexer(in, file, filename, tokenFactory, accumulateWhitetext);
    }
}
