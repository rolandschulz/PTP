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
import org.eclipse.photran.internal.core.sourceform.ISourceForm;
import org.eclipse.photran.internal.core.sourceform.SourceForm;

/**
 * A factory for creating Fortran lexers that feed the Fortran parser.
 * <p>
 * <i>Historical Note:</i>
 * There used to be a separate <code>LexerFactory</code> for lexers that were intended for
 * uses other than feeding the parser (e.g., a lexer-based model builder or MBS dependency
 * analyzer); its methods returned an {@link ILexer} rather than an {@link IAccumulatingLexer}.
 * However, those uses have since disappeared, and so the two classes were consolidated into
 * this single class.
 * 
 * @author Jeff Overbey
 */
public class ASTLexerFactory
{
    public IAccumulatingLexer createLexer(IFile file) throws CoreException, IOException
    {
        return createLexer(
            new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset())),
            file,
            SourceForm.determineFilename(file));
    }
    
    public IAccumulatingLexer createLexer(IFile file, ISourceForm sourceForm) throws CoreException, IOException
    {
        return createLexer(
            new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset())),
            file,
            SourceForm.determineFilename(file),
            sourceForm);
    }

    public IAccumulatingLexer createLexer(File file) throws IOException
    {
        return createLexer(
            new BufferedReader(new FileReader(file)),
            null,
            file.getAbsolutePath());
    }

    public IAccumulatingLexer createLexer(File file, ISourceForm sourceForm) throws IOException
    {
        return createLexer(
            new BufferedReader(new FileReader(file)),
            null,
            file.getAbsolutePath(),
            sourceForm);
    }
    
    public IAccumulatingLexer createLexer(Reader in, IFile file, String filename) throws IOException
    {
        return createLexer(
            in,
            file,
            filename,
            SourceForm.of(file, filename));
    }
    
    public IAccumulatingLexer createLexer(Reader in, IFile file, String filename, ISourceForm sourceForm) throws IOException
    {
        return new LexerPhase3(sourceForm.<ILexer>createLexer(in, file, filename, true));
    }
}