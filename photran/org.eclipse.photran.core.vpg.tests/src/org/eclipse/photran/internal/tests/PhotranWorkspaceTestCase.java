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
package org.eclipse.photran.internal.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.FProjectNature;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.util.Spawner;

/**
 * Base class for a test case that imports files into the runtime workspace and then operates on the
 * runtime workspace.
 * <p>
 * Most refactorings are tested in this way. However, refactoring tests should generally be created
 * by subclassing {@link PhotranRefactoringTestSuiteFromMarkers}, not by subclassing this class
 * directly.
 * <p>
 * This class is based on org.eclipse.cdt.core.tests.BaseTestFramework.
 * 
 * @author aniefer
 * @author Jeff Overbey - Modified so that every test case creates a new project - Added line/column
 *         computation when importing files - Added {@link #compileAndRunFortranProgram(String...)}
 *         Also added marker infrastructure.
 */
public abstract class PhotranWorkspaceTestCase extends PhotranTestCase {

    /** The marker to search for */
    public static final String MARKER = "!<<<<<";

    /** Filter that determines which files will be imported into the runtime workspace */
    public static final FilenameFilter FORTRAN_FILE_FILTER = new FilenameFilter()
    {
        public boolean accept(File dir, String filename)
        {
            return !filename.endsWith(".result")
                && !filename.equalsIgnoreCase("CVS")
                && !filename.equalsIgnoreCase(".svn");
        }
    };

    static protected NullProgressMonitor	monitor;
    static protected IWorkspace 			workspace;
    static protected IProject 				project;
    static protected ICProject				cproject;
    static protected FileManager 			fileManager;
	static protected boolean				indexDisabled=false;
	
	private static int n = 0;
	
    private HashMap<String, ArrayList<Integer>> lineMaps = new HashMap<String, ArrayList<Integer>>();

    static void initProject() {
		if (project != null) {
			return;
		}
        if( CCorePlugin.getDefault() != null && CCorePlugin.getDefault().getCoreModel() != null){
			//(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
			monitor = new NullProgressMonitor();
			
			workspace = ResourcesPlugin.getWorkspace();
			
	        try {
	            PhotranVPG.getInstance().releaseAllASTs();
	            PhotranVPG.getDatabase().clearDatabase();
	            
	            cproject = CProjectHelper.createCCProject("PhotranTestProject" + (++n), "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$
	            CProjectHelper.addNatureToProject(cproject.getProject(), FProjectNature.F_NATURE_ID, null);
	        
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
            
    public PhotranWorkspaceTestCase()
    {
        super();
    }

    public PhotranWorkspaceTestCase(String name)
    {
        super(name);
    }
    
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initProject();
	}

	@Override
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

    protected IFile importFile(String fileName, String contents) throws Exception
    {
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

    /**
     * If the environment variables COMPILER and EXECUTABLE are set during the JUnit run,
     * compiles and runs the Fortran program being refactored and returns the output of
     * the compilation and execution.  Otherwise, returns the empty string.
     * <p>
     * (This is intended to allow developers creating refactorings to test their refactorings
     * by actually compiling the code, while not forcing other developers to do so, since
     * it can be time-consuming.  Tests should NOT assume that a compiler is available.)
     * <p>
     * This will compile the file(s) that are currently being refactored (i.e., the files
     * imported into the test project using {@link #importFile(Plugin, String, String)}) into a
     * single executable, and then run that executable.  This can be invoked before and
     * after a refactoring is performed to make sure runtime behavior is actually preserved
     * for the test program.  If a specific ordering of the filenames is desired, it
     * must be passed as an argument to this method; otherwise, all *.f* resources in the
     * test project will be passed to the compiler in no specific order.
     * <p>
     * COMPILER must be set to the path of your Fortran compiler, e.g.,
     * /usr/local/gfortran/bin/gfortran
     * <p>
     * EXECUTABLE gives a path for the generated executable, e.g.,
     * /Users/joverbey/fortran-test-program.exe
     * <p>
     * The actual command line that is invoked is printed to standard output.
     * <p>
     * NOTE: Occasionally, the last part of the output from the Fortran program may seem to
     * be missing.  (This may be a gfortran-specific problem.)  You should include a
     * statement to manually flush the output to ensure that your test cases always pass.
     * In gfortran, you can "CALL FLUSH" to invoke the (proprietary) flush intrinsic; or
     * there is also a FLUSH statement in Fortran 2003.
     *
     * @return the output from the compiler and executable
     * @throws Exception
     */
    protected String compileAndRunFortranProgram(String... filenamesOpt) throws Exception
    {
        String compiler = System.getenv("COMPILER");
        if (compiler == null) return "";

        String exe = System.getenv("EXECUTABLE");
        if (exe == null) return "";

        ArrayList<String> args = new ArrayList<String>(8);
        args.add(compiler);

        args.add("-falign-functions");
        
        args.add("-o");
        args.add(exe);
        if (filenamesOpt == null || filenamesOpt.length == 0)
        {
            for (IResource res : project.members())
                if (res instanceof IFile && res.getFileExtension().startsWith("f"))
                    args.add(res.getName());
        }
        else
        {
            for (String filename : filenamesOpt)
                args.add(filename);
        }

        System.out.println(toString(args));
        String output = Spawner.run(project.getLocation().toFile(), args);
        //System.out.println(output);

        System.out.println(exe);
        String output2 = Spawner.run(project.getLocation().toFile(), exe);
        //System.out.println(output2);
        
        // Wait a few milliseconds for the file lock on a.exe to be released
        // Otherwise Windows 7 + Cygwin may give a "Device or resource busy"
        // error when we compile to a.exe in the next test case
        Thread.sleep(250); // 1/4 of a second seems to be sufficient

        return output + "\n" + output2;
    }

    private String toString(ArrayList<String> args)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++)
            sb.append((i == 0 ? "" : " ") + args.get(i));
        return sb.toString();
    }

    protected IFile importFile(Plugin activator, String srcDir, String filename) throws Exception
    {
        //project.getProject().getFile(filename).delete(true, new NullProgressMonitor());
        IFile result = importFile(filename, readTestFile(activator, srcDir, filename));
        //project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        return result;
    }

    protected String readTestFile(Plugin activator, String srcDir, String filename) throws IOException, URISyntaxException
    {
        ArrayList<Integer> lineMap = new ArrayList<Integer>(50);
        lineMaps.put(filename, lineMap);
        lineMap.add(0); // Offset of line 1
        URL resource = activator.getBundle().getResource(srcDir + "/" + filename);
        assertNotNull(resource);
        return readStream(lineMap, resource.openStream());
    }

    protected String readStream(InputStream inputStream) throws IOException
    {
        return readStream(null, inputStream);
    }

    private String readStream(ArrayList<Integer> lineMap, InputStream inputStream) throws IOException
    {
        StringBuffer sb = new StringBuffer(4096);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        for (int offset = 0, ch = in.read(); ch >= 0; ch = in.read())
        {
            sb.append((char)ch);
            offset++;

            if (ch == '\n' && lineMap != null)
            {
                //System.out.println("Line " + (lineMap.size()+1) + " starts at offset " + offset);
                lineMap.add(offset);
            }
        }
        in.close();
        return sb.toString();
    }

    protected String readWorkspaceFile(String filename) throws IOException, CoreException
    {
        return readStream(project.getFile(filename).getContents(true));
    }

    /**
     * @param filename
     * @param line line number, starting at 1
     * @param col column number, starting at 1
     */
    protected int getLineColOffset(String filename, LineCol lineCol)
    {
        return lineMaps.get(filename).get(lineCol.getLine()-1) + (lineCol.getCol()-1);
    }

    protected String[] parseMarker(String markerText)
    {
        String[] markerStrings = markerText.split(",");
        for (int i = 0; i < markerStrings.length; i++)
            markerStrings[i] = markerStrings[i].trim();
        return markerStrings;
    }

    protected TextSelection determineSelection(String[] markerStrings, IFile fileContainingMarker) throws IOException, CoreException
    {
        assertTrue(markerStrings.length >= 2);
        int fromLine = Integer.parseInt(markerStrings[0]);
        int fromCol = Integer.parseInt(markerStrings[1]);
        int fromOffset = getLineColOffset(fileContainingMarker.getName(), new LineCol(fromLine, fromCol));
        int length = 0;
        if (markerStrings.length >= 4 && isInteger(markerStrings[2]) && isInteger(markerStrings[3]))
        {
            int toLine = Integer.parseInt(markerStrings[2]);
            int toCol = Integer.parseInt(markerStrings[3]);
            int toOffset = getLineColOffset(fileContainingMarker.getName(), new LineCol(toLine, toCol));
            length = toOffset - fromOffset;
        }
        TextSelection selection = new TextSelection(createDocument(fileContainingMarker),  fromOffset, length);
        return selection;
    }

    /**
     * @return true iff {@link Integer#parseInt(String)} can successfully parse the given
     *         string can be parsed as an integer
     */
    protected boolean isInteger(String string)
    {
        try
        {
            Integer.parseInt(string);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    private IDocument createDocument(IFile fileContainingMarker) throws IOException, CoreException
    {
        return new Document(readWorkspaceFile(fileContainingMarker.getName()));
    }
}
