package org.eclipse.photran.internal.core.lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
    public InputStream getIncludedFileAsStream(String fileToInclude) throws FileNotFoundException
    {
        try
        {
        	return getIncludedFile(fileToInclude).getContents();
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
            IResource result = ResourcesPlugin.getWorkspace().getRoot().findMember(paths[i] + File.separatorChar + fileToInclude);
            if (result != null && result instanceof IFile)
                return (IFile)result;
        }
        throw new FileNotFoundException(fileToInclude);
    }
    
    /**
     * Called back when an INCLUDE file cannot be loaded.  Subclasses are expected to override this method and
     * display the given message to the user.
     * 
     * @param message
     * @param filename
     * @return <code>null</code> to continue, or a <code>String</code> containing a message if an <code>Exception</code>
     * should be thrown and parsing/binding aborted
     */
    public String onUnableToLoad(String message, String filename)
    {
        return message;
    }
}
