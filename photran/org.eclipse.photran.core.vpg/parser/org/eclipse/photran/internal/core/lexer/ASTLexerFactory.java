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

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.internal.core.lexer.sourceform.ISourceForm;

/**
 * A factory for creating Fortran lexers that feed the Fortran parser.
 * 
 * @author Jeff Overbey
 */
public class ASTLexerFactory extends LexerFactory
{
    public ASTLexerFactory()
    {
        super(ASTTokenFactory.getInstance(), true);
    }
    
    public IAccumulatingLexer createLexer(IFile file) throws CoreException, IOException
    {
        return new LexerPhase3(super.createLexer(file));
    }
    
    public IAccumulatingLexer createLexer(IFile file, ISourceForm sourceForm) throws CoreException, IOException
    {
        return new LexerPhase3(super.createLexer(file, sourceForm));
    }

    public IAccumulatingLexer createLexer(File file) throws IOException
    {
        return new LexerPhase3(super.createLexer(file));
    }

    public IAccumulatingLexer createLexer(File file, ISourceForm sourceForm) throws IOException
    {
        return new LexerPhase3(super.createLexer(file, sourceForm));
    }

    public IAccumulatingLexer createLexer(Reader in, IFile file, String filename) throws IOException
    {
        return new LexerPhase3(super.createLexer(in, file, filename));
    }
    
    public IAccumulatingLexer createLexer(Reader in, IFile file, String filename, ISourceForm sourceForm) throws IOException
    {
        return new LexerPhase3(super.createLexer(in, file, filename, sourceForm));
    }
}