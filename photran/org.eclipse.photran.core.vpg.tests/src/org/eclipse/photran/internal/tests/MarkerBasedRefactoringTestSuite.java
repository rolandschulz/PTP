/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.TreeMap;

import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

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
public abstract class MarkerBasedRefactoringTestSuite<R extends SingleFileFortranRefactoring> extends TestSuite
{
    protected static final String MARKER = "!<<<<<";

    protected Class<R> refactoringClass;
    protected TreeMap<PhotranTokenRef, String> markers;

    protected MarkerBasedRefactoringTestSuite(String descriptionPrefix, String directory, Class<R> clazz) throws Exception
    {
        this.refactoringClass = clazz;

        File dir = new File(directory);
        assert dir.exists();
        assert dir.canRead();
        assert dir.isDirectory();

        for (File subdir : dir.listFiles())
        {
            if (subdir.getName().equalsIgnoreCase("CVS")) continue;
            
            String description = descriptionPrefix + " " + subdir.getName();
            String subdirPath = directory + "/" + subdir.getName();

            populateMarkers(subdirPath);

            TestSuite subsuite = new TestSuite(description);
            for (PhotranTokenRef tr : markers.keySet())
                subsuite.addTest(new IndividualRefactoringTestCase(description, subdirPath, tr, markers.get(tr)));

            this.addTest(subsuite);
        }
    }

    private void populateMarkers(String subdir) throws Exception
    {
        markers = new TreeMap<PhotranTokenRef, String>();

        for (File file : new File(subdir).listFiles())
        {
            String filename = file.getName();
            if (!filename.endsWith(".result") && !filename.equalsIgnoreCase("CVS"))
            {
                String fileContents = readTestFile(subdir, filename);
                for (int index = fileContents.indexOf(MARKER);
                     index >= 0;
                     index = fileContents.indexOf(MARKER, index+1))
                {
                    int eol = fileContents.indexOf('\n', index);
                    if (eol == 0) eol = fileContents.length();
                    int length = eol-index;

                    markers.put(
                        new PhotranTokenRef(filename, index, length),
                        fileContents.substring(index+MARKER.length(), eol).trim());
                }
            }
        }

        if (markers.isEmpty())
            throw new Exception("No markers of the form " + MARKER + " found in files in " + subdir);
    }

    private String readTestFile(String subdir, String filename) throws Exception
    {
        return new RefactoringTestCase()
        {
            @Override public String readTestFile(String subdir, String filename) throws IOException, URISyntaxException
            {
                return super.readTestFile(subdir, filename);
            }
        }.readTestFile(subdir, filename);
    }

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
        refactoring.initialize(file, selection);
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

    public class IndividualRefactoringTestCase extends RefactoringTestCase
    {
        private String description;
        private String subdir;
        private PhotranTokenRef marker;
        private String markerText;

        private TreeMap<String, IFile> files = new TreeMap<String, IFile>();

        public IndividualRefactoringTestCase(String description, String subdir, PhotranTokenRef marker, String markerText) throws Exception
        {
            this.description = description;
            this.subdir = subdir;
            this.marker = marker;
            this.markerText = markerText;
            this.setName("test");
        }

        public void test() throws Exception
        {
            importFiles(subdir);

            NullProgressMonitor pm = new NullProgressMonitor();
            project.refreshLocal(IResource.DEPTH_INFINITE, pm);
            String before = compileAndRunFortranProgram(
                "compilation before transformation",
                files.keySet().toArray(new String[files.size()]));

            R refactoring = createRefactoring();

            IFile file = files.get(marker.getFilename());
            String[] markerStrings = markerText.split(",");
            assertTrue(markerStrings.length >= 2);
            int line = Integer.parseInt(markerStrings[0]);
            int col = Integer.parseInt(markerStrings[1]);
            int offset = getLineColOffset(marker.getFilename(), new LineCol(line, col));
            TextSelection selection = new TextSelection(offset, 0);

            description += " (" + marker.getFilename() + " " + Arrays.toString(markerStrings) + ")";

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
                String after = compileAndRunFortranProgram(
                    "compilation after transformation",
                    files.keySet().toArray(new String[files.size()]));
                System.out.println(after);
                assertEquals(before, after);

                if (markers.size() == 1)
                {
                    for (String filename : files.keySet())
                    {
                        assertEquals(
                            readTestFile(subdir, filename + ".result").replaceAll("\\r", ""), // expected result
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
                if (!filename.endsWith(".result") && !filename.equalsIgnoreCase("CVS"))
                {
                    IFile thisFile = importFile(subdir, filename);
                    files.put(filename, thisFile);
                }
            }

            IProgressMonitor pm = new NullProgressMonitor();
            project.refreshLocal(IResource.DEPTH_INFINITE, pm);
            PhotranVPG.getInstance().ensureVPGIsUpToDate(pm);
        }

        protected String compileAndRunFortranProgram(String description, String... filenamesOpt) throws Exception
        {
            try
            {
                return super.compileAndRunFortranProgram(filenamesOpt);
            }
            catch (Exception e)
            {
                throw new Exception(description + " failed " + description + ":\n" + e.getMessage(), e);
            }
        }
    }
}
