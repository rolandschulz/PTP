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
package org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;

/**
 * Provides callbacks to handle errors encountered while loading files in order to process INCLUDE lines.
 * 
 * @author Jeff Overbey
 */
public class IncludeLoaderCallback
{
    protected IProject project;
    
    public IncludeLoaderCallback(IProject project)
    {
        this.project = project;
    }
    
    /**
     * Called back when an INCLUDE line is found.  The parameter is the (verbatim) text of the file
     * to include.
     * 
     * @param fileToInclude
     * @return <code>InputStream</code>, not null
     * @throws FileNotFoundException if the file cannot be found
     */
    public Reader getIncludedFileAsStream(String fileToInclude) throws FileNotFoundException
    {
        try
        {
        	IFile file = getIncludedFile(fileToInclude);
            try
            {
                return new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset()));
            }
            catch (UnsupportedEncodingException e)
            {
                return new BufferedReader(new InputStreamReader(file.getContents(true)));
            }
        }
        catch (CoreException e)
        {
        	throw new FileNotFoundException(fileToInclude + " - " + e.getMessage());
        }
    }
    
    protected IFile getIncludedFile(String fileToInclude) throws FileNotFoundException
    {
        String[] paths = SearchPathProperties.parseString(SearchPathProperties.getProperty(project, SearchPathProperties.INCLUDE_PATHS_PROPERTY_NAME));
    	for (int i = 0; i < paths.length; i++)
        {
    	    IResource folder = ResourcesPlugin.getWorkspace().getRoot().findMember(paths[i]);
            if (folder != null && folder.isAccessible())
            {
                IFile result = getIncludedFile(fileToInclude, folder);
                if (result != null)
                    return (IFile)result;
            }
        }
        throw new FileNotFoundException(fileToInclude);
    }

    private IFile getIncludedFile(final String fileToInclude, IResource folder)
    {
        class Visitor implements IResourceVisitor
        {
            private IFile result = null;
            
            public boolean visit(IResource resource) throws CoreException
            {
                if (resource instanceof IFile
                                && resource.isAccessible()
                                && resource.getName().equals(fileToInclude))
                {
                    result = (IFile)resource;
                    return false;
                }
                else
                {
                    return true;
                }
            }
        };
        
        Visitor visitor = new Visitor();
        try { ((IContainer)folder).accept(visitor); } catch (CoreException e) { throw new Error(e); }
        return visitor.result;
    }

    /**
     * Called to log an error message when an INCLUDE file cannot be found.
     * 
     * @param message the error message to display to the user
     * @param topLevelFile the file in which the INCLUDE line occurred
     * @param offset the offset in topLevelFile at which the INCLUDE line was found
     */
    public void logError(String message, IFile topLevelFile, int offset)
    {
        // By default, ignore errors
    }
}
