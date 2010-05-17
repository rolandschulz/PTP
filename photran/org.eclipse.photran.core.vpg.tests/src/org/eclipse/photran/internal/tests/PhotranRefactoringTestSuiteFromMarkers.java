/*******************************************************************************
 * Copyright (c) 2009-2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranResourceRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.vpg.refactoring.VPGResourceRefactoring;
import org.eclipse.rephraserengine.testing.junit3.GeneralTestSuiteFromMarkers;

/**
 * A test suite constructed by importing files from a directory in the source tree, searching its
 * files for <i>markers</i> (comma-separated lines starting with <pre>!&lt;&lt;&lt;&lt;&lt;</pre>),
 * and adding one test case to the suite for each marker.
 * <p>
 * The first two fields in each marker are expected to be a line and column number; the text
 * selection passed to the refactoring will be the offset of that line and column.
 *
 * @author Jeff Overbey
 */
public abstract class PhotranRefactoringTestSuiteFromMarkers<R extends VPGResourceRefactoring<IFortranAST, Token, PhotranVPG>>
              extends GeneralTestSuiteFromMarkers
{
    protected static final String MARKER = "!<<<<<";

    protected static final FilenameFilter FORTRAN_FILE_FILTER = new FilenameFilter()
    {
        public boolean accept(File dir, String filename)
        {
            return !filename.endsWith(".result")
                && !filename.equalsIgnoreCase("CVS")
                && !filename.equalsIgnoreCase(".svn");
        }
    };
    
    protected Plugin activator;
    protected Class<R> refactoringClass;

    protected PhotranRefactoringTestSuiteFromMarkers(Plugin activator, String descriptionPrefix, String directory, Class<R> clazz) throws Exception
    {
        super(descriptionPrefix, MARKER, new File(directory), FORTRAN_FILE_FILTER,
              activator, // initializationData[0]
              clazz);    // initializationData[1]
    }

    @SuppressWarnings("unchecked")
    @Override protected void initialize(Object... initializationData)
    {
        this.activator = (Plugin)initializationData[0];
        this.refactoringClass = (Class<R>)initializationData[1];
    }

    
    @Override protected Test createTestFor(File fileContainingMarker, int markerOffset, String markerText) throws Exception
    {
        return new IndividualRefactoringTestCase(activator, description, fileContainingMarker, markerText);
    }
    
//    private String readTestFile(Plugin activator, String subdir, String filename) throws Exception
//    {
//        return new RefactoringTestCase()
//        {
//            @Override public String readTestFile(Plugin activator, String subdir, String filename) throws IOException, URISyntaxException
//            {
//                return super.readTestFile(activator, subdir, filename);
//            }
//        }.readTestFile(activator, subdir, filename);
//    }

    /**
     * This method is invoked to instantiate the refactoring class.  Override if necessary.
     */
    protected R createRefactoring() throws InstantiationException, IllegalAccessException
    {
        return refactoringClass.newInstance();
    }

    /**
     * This method is invoked after the refactoring is created ({@link #createRefactoring()}) but
     * before {@link Refactoring#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)}
     * is invoked.  Override if necessary.
     */
    protected void initializeRefactoring(R refactoring, IFile file, TextSelection selection, String[] markerText)
    {
        if (refactoring instanceof FortranEditorRefactoring)
            ((FortranEditorRefactoring)refactoring).initialize(file, selection);
        else if (refactoring instanceof FortranResourceRefactoring)
            ((FortranResourceRefactoring)refactoring).initialize(Collections.singletonList(file));
        else
            throw new IllegalStateException();
    }

    /**
     * This method is invoked after initial preconditions have been checked
     * ({@link Refactoring#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)})
     * but before final preconditions are checked
     * ({@link Refactoring#checkFinalConditions(org.eclipse.core.runtime.IProgressMonitor)}).
     * Override if necessary to supply user input.
     *
     * @return true iff the refactoring is expected to pass final precondition checking
     */
    protected boolean configureRefactoring(R refactoring, IFile file, TextSelection selection, String[] markerText)
    {
        return true;
    }

    public class IndividualRefactoringTestCase extends PhotranWorkspaceTestCase
    {
        private Plugin activator;
        private String description;
        private File fileContainingMarker;
        private String markerText;

        private TreeMap<String, IFile> files = new TreeMap<String, IFile>();

        public IndividualRefactoringTestCase(Plugin activator, String description, File fileContainingMarker, String markerText) throws Exception
        {
            this.activator = activator;
            this.description = description;
            this.fileContainingMarker = fileContainingMarker;
            this.markerText = markerText;
            this.setName("test");
        }

        public void test() throws Exception
        {
            importFiles(fileContainingMarker.getParent());

            NullProgressMonitor pm = new NullProgressMonitor();
            project.refreshLocal(IResource.DEPTH_INFINITE, pm);
            System.out.println("Compiling and running program prior to transformation - " + description);
            String before = compileAndRunFortranProgram(
                files.keySet().toArray(new String[files.size()]));

            R refactoring = createRefactoring();

            IFile file = files.get(fileContainingMarker.getName());
            String[] markerStrings = markerText.split(",");
            assertTrue(markerStrings.length >= 2);
            int line = Integer.parseInt(markerStrings[0]);
            int col = Integer.parseInt(markerStrings[1]);
            int offset = getLineColOffset(fileContainingMarker.getName(), new LineCol(line, col));
            TextSelection selection = new TextSelection(offset, 0);

            description += " (" + fileContainingMarker.getName() + " " + Arrays.toString(markerStrings) + ")";

            initializeRefactoring(refactoring, file, selection, markerStrings);
            RefactoringStatus status = refactoring.checkInitialConditions(pm);
            assertTrue(description + " failed initial precondition check: " + status.toString(), !status.hasError());

            boolean shouldSucceed = configureRefactoring(refactoring, file, selection, markerStrings);
            status = refactoring.checkFinalConditions(pm);
            if (shouldSucceed)
                assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError());
            else
                assertTrue(description + " should have failed final precondition check but did not: " + status.toString(), status.hasError());

            Change change = refactoring.createChange(pm);
            assertNotNull(description + " returned null Change object", change);
            assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
            change.perform(pm);

            project.refreshLocal(IResource.DEPTH_INFINITE, pm);

            if (shouldSucceed)
            {
                System.out.println("Compiling and running program after transformation - " + description);
                String after = compileAndRunFortranProgram(
                    files.keySet().toArray(new String[files.size()]));
                System.out.println(after);
                assertEquals(before, after);

                if (new File(fileContainingMarker.getPath() + ".result").exists())
                {
                    for (String filename : files.keySet())
                    {
                        assertEquals(
                            readTestFile(activator, fileContainingMarker.getParent(), filename + ".result").replaceAll("\\r", ""), // expected result
                            readWorkspaceFile(filename).replaceAll("\\r", ""));               // actual refactored file
                    }
                }
            }
        }

        private void importFiles(String subdir) throws Exception
        {
            PhotranVPG.getDatabase().clearDatabase();

            files = new TreeMap<String, IFile>();

            for (File file : new File(subdir).listFiles())
            {
                String filename = file.getName();
                if (!filename.endsWith(".result")
                    && !filename.equalsIgnoreCase("CVS")
                    && !filename.equalsIgnoreCase(".svn"))
                {
                    IFile thisFile = importFile(activator, subdir, filename);
                    files.put(filename, thisFile);
                }
            }

            IProgressMonitor pm = new NullProgressMonitor();
            project.refreshLocal(IResource.DEPTH_INFINITE, pm);
            PhotranVPG.getInstance().ensureVPGIsUpToDate(pm);
        }
    }
}
