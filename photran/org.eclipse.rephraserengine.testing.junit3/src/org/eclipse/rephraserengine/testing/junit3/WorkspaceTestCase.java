/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/*
 * Created on Oct 4, 2004
 */
package org.eclipse.rephraserengine.testing.junit3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.rephraserengine.core.util.StringUtil;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * Base class for a test case that imports files into the runtime workspace and then operates on the
 * runtime workspace.
 * <p>
 * This class is based on org.eclipse.cdt.core.tests.BaseTestFramework.
 * 
 * @author aniefer
 * @author Jeff Overbey
 * 
 * @since 3.0
 */
public abstract class WorkspaceTestCase extends TestCase
{
    /** Used to give each project a new name */
    protected static int n = 0;
    
    protected IWorkspace workspace;
    protected IProject project;
    protected final EclipseVPG<?,?,?> vpg;
    
    public WorkspaceTestCase()
    {
        super();
        this.vpg = null;
    }

    public WorkspaceTestCase(String name)
    {
        super(name);
        this.vpg = null;
    }

    public WorkspaceTestCase(String name, EclipseVPG<?,?,?> vpg)
    {
        super(name);
        this.vpg = vpg;
    }
    
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

        if (project != null) return;

        if (vpg != null)
        {
            vpg.releaseAllASTs();
            vpg.clearDatabase();
        }
        
        workspace = ResourcesPlugin.getWorkspace();
        project = createProject("TestProject" + (++n)); //$NON-NLS-1$
        if (project == null)
            fail("Unable to create project"); //$NON-NLS-1$
	}
            
    protected static IProject createProject(final String projectName) throws CoreException
    {
        class CreateProject implements IWorkspaceRunnable
        {
            IProject project = null;
            
            public void run(IProgressMonitor monitor) throws CoreException {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                project = workspace.getRoot().getProject(projectName);
                if (!project.exists())
                    project.create(monitor);
                else
                    project.refreshLocal(IResource.DEPTH_INFINITE, null);
                if (!project.isOpen())
                    project.open(monitor);
            }
        }
        
        CreateProject runnable = new CreateProject();
        runnable.run(new NullProgressMonitor());
        return runnable.project;
    }

	@Override
	protected void tearDown() throws Exception
	{
        if (project == null || !project.exists()) return;
        
        try
        {
            project.delete(true, true, new NullProgressMonitor());
        }
        catch (Throwable e)
        {
            project.close(new NullProgressMonitor());
        }
        finally
        {
            if (vpg != null)
            {
                // To speed things up a bit and conserve memory...
                vpg.releaseAllASTs();
                vpg.clearDatabase();
            }

            project = null;
        }
	}

    protected IFile importFile(String fileName, String contents) throws Exception
    {
        IFile file = project.getProject().getFile(fileName);
        InputStream stream = new ByteArrayInputStream(contents.getBytes());

        if (file.exists())
            file.setContents(stream, false, false, new NullProgressMonitor());
        else
            file.create(stream, false, new NullProgressMonitor());

        return file;
    }

    protected IFile importFile(String fileName, File fileToCopyIntoWorkspace) throws Exception
    {
        return importFile(fileName, StringUtil.read(fileToCopyIntoWorkspace));
    }

    protected IFile importFile(File fileToCopyIntoWorkspace) throws Exception
    {
        return importFile(fileToCopyIntoWorkspace.getName(), fileToCopyIntoWorkspace);
    }

    protected IFile importFile(Plugin activator, String path) throws Exception
    {
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String filename = path.substring(lastSeparator+1);
        
        //project.getProject().getFile(filename).delete(true, new NullProgressMonitor());
        IFile result = importFile(filename, readTestFile(activator, path));
        //project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        return result;
    }

    protected String readTestFile(Plugin activator, String path) throws IOException, URISyntaxException
    {
        URL resource = activator.getBundle().getResource(path);
        assertNotNull(resource);
        return StringUtil.read(resource.openStream());
    }

    protected Map<String, IFile> importAllFiles(File directory, FilenameFilter filenameFilter) throws Exception
    {
        Map<String, IFile> filesImported = new TreeMap<String, IFile>();
        for (File file : directory.listFiles(filenameFilter))
        {
            IFile thisFile = importFile(file);
            filesImported.put(thisFile.getName(), thisFile);
        }
        return filesImported;
    }

    protected String readWorkspaceFile(String filename) throws IOException, CoreException
    {
        return StringUtil.read(project.getFile(filename).getContents(true));
    }

    protected IDocument createDocument(IFile file) throws IOException, CoreException
    {
        return new Document(readWorkspaceFile(file.getName()));
    }
}
