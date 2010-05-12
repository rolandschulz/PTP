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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * A collection of (static) factory methods for creating Fortran lexers.
 * 
 * @author Jeff Overbey
 */
public final class LexerFactory
{
    private LexerFactory() {;}
    
    public static IAccumulatingLexer createLexer(Reader in, IFile file, String filename, SourceForm sourceForm, boolean accumulateWhitetext) throws IOException
    {
        return sourceForm.createLexer(in, file, filename, accumulateWhitetext);
    }
    
    public static IAccumulatingLexer createLexer(File file, SourceForm sourceForm, boolean accumulateWhitetext) throws IOException
    {
        return createLexer(new BufferedReader(new FileReader(file)), null, file.getAbsolutePath(), sourceForm, accumulateWhitetext);
    }
    
    public static IAccumulatingLexer createLexer(IFile file, SourceForm sourceForm, boolean accumulateWhitetext) throws CoreException, IOException
    {
        return createLexer(new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset())),
            file,
            determineFilename(file),
            sourceForm,
            accumulateWhitetext);
    }

    private static String determineFilename(IFile file)
    {
        if (file == null) return null;
        
        IPath location = file.getLocation();
        if (location == null) return file.getFullPath().toOSString();
        
        return location.toOSString();
    }
}
