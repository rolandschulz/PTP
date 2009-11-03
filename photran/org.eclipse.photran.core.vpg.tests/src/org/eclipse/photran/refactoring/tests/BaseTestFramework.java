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
package org.eclipse.photran.refactoring.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * Copied and modified from org.eclipse.cdt.core.tests.BaseTestFramework
 * 
 * @author aniefer
 * @author Jeff Overbey - Modified so that every test case creates a new project
 *                        (hack to resolve weird file deletion problems in Windows)
 */
abstract public class BaseTestFramework extends TestCase {
    static protected NullProgressMonitor	monitor;
    static protected IWorkspace 			workspace;
    static protected IProject 				project;
    static protected ICProject				cproject;
    static protected FileManager 			fileManager;
	static protected boolean				indexDisabled=false;
	
	static int n = 0;
	
	static void initProject() {
		if (project != null) {
			return;
		}
        if( CCorePlugin.getDefault() != null && CCorePlugin.getDefault().getCoreModel() != null){
			//(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
			monitor = new NullProgressMonitor();
			
			workspace = ResourcesPlugin.getWorkspace();
			
	        try {
	            cproject = CProjectHelper.createCCProject("PhotranTestProject" + (++n), "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$
	        
	            project = cproject.getProject();
	            
	            /*project.setSessionProperty(SourceIndexer.activationKey, Boolean.FALSE );
	        	//Set the id of the source indexer extension point as a session property to allow
	    		//index manager to instantiate it
	    		project.setSessionProperty(IndexManager.indexerIDKey, sourceIndexerID);*/
	    		
	 
	    		
	        } catch ( CoreException e ) {
	            /*boo*/
	        }
			if (project == null)
				fail("Unable to create project"); //$NON-NLS-1$
	
			//Create file manager
			fileManager = new FileManager();
        }
	}
            
    public BaseTestFramework()
    {
        super();
    }
    /**
     * @param name
     */
    public BaseTestFramework(String name)
    {
        super(name);
    }
      
    public void cleanupProject() throws Exception {
        try{
	        project.delete( true, false, monitor );
	    } catch( Throwable e ){
	        /*boo*/
	    } finally {
	    	project= null;
	    }
    }
    
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initProject();
	}

	protected void tearDown() throws Exception {
        if( project == null || !project.exists() )
            return;
        
//        IResource [] members = project.members();
//        for( int i = 0; i < members.length; i++ ){
//            if( members[i].getName().equals( ".project" ) || members[i].getName().equals( ".cproject" ) ) //$NON-NLS-1$ //$NON-NLS-2$
//                continue;
//            if (members[i].getName().equals(".settings"))
//            	continue;
//            try{
//                members[i].delete( false, monitor );
//            } catch( Throwable e ){
//                /*boo*/
//            }
//        }
        
        try {
            project.delete(true, true, new NullProgressMonitor());
        } catch( Throwable e ){
            /* boo */
            project.close(new NullProgressMonitor());
            // To speed things up a bit and conserve memory...
            PhotranVPG.getInstance().releaseAllASTs();
            PhotranVPG.getDatabase().clearDatabase();
        }
        project = null;
	}
    protected IFile importFile(String fileName, String contents ) throws Exception{
		//Obtain file handle
		IFile file = project.getProject().getFile(fileName);
		
		InputStream stream = new ByteArrayInputStream( contents.getBytes() );
		//Create file input stream
		if( file.exists() )
		    file.setContents( stream, false, false, monitor );
		else
			file.create( stream, false, monitor );
		
		fileManager.addFile(file);
		
		return file;
	}

}
