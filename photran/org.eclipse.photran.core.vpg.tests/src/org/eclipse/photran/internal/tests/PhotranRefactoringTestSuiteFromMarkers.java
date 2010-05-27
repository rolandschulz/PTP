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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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
 * files for <i>markers</i> (comma-separated lines starting with <tt>!&lt;&lt;&lt;&lt;&lt;</tt>),
 * and adding one test case to the suite for each marker.
 * <p>
 * Markers are expected to have one of the following forms:
 * <ol>
 * <li> <tt>!&lt;&lt;&lt;&lt;&lt; line, col, ...</tt>
 * <li> <tt>!&lt;&lt;&lt;&lt;&lt; fromLine, fromCol, toLine, toCol, ...</tt>
 * </ol>
 * That is, the first two fields in each marker are expected to be a line and column number; the
 * text selection passed to the refactoring will be the offset of that line and column.  The third
 * fourth fields may also be a line and column number; then, the selection passed to the refactoring
 * will extend from the first line/column to the second line/column.
 * <p>
 * The line and column numbers may be followed by an arbitrary number of fields that contain data
 * specific to the refactoring being invoked.  Many refactorings don't require any additional data;
 * the Extract Local Variable test suite uses one field for the new variable declaration; the Add
 * ONLY to USE Statement test suite uses these fields to list the module entities to add; etc.
 * <p>
 * The final field must be either &quot;pass&quot;, &quot;pass&quot;fail-initial&quot;pass&quot;,
 * or &quot;pass&quot;fail-final&quot;pass&quot;, indicating whether the refactoring should succeed,
 * fail its initial precondition check, or fail its final precondition check.
 * <p>
 * If the refactoring is expected to succeed, the Fortran program will be compiled and run before
 * and after the refactoring in order to ensure that the refactoring actually preserved behavior.
 *
 * @author Jeff Overbey
 */
public abstract class PhotranRefactoringTestSuiteFromMarkers<R extends VPGResourceRefactoring<IFortranAST, Token, PhotranVPG>>
              extends GeneralTestSuiteFromMarkers
{
    /** Text of the last marker field when a refactoring is pass all precondition checks */
    private static final String PASS = "pass";
    /** Text of the last marker field when a refactoring is expected to fail initial precondition check */
    private static final String FAIL_INITIAL = "fail-initial";
    /** Text of the last marker field when a refactoring is expected to fail final precondition check */
    private static final String FAIL_FINAL = "fail-final";

    /** The marker to search for */
    protected static final String MARKER = "!<<<<<";

    /** Filter that determines which files will be imported into the runtime workspace */
    protected static final FilenameFilter FORTRAN_FILE_FILTER = new FilenameFilter()
    {
        public boolean accept(File dir, String filename)
        {
            return !filename.endsWith(".result")
                && !filename.equalsIgnoreCase("CVS")
                && !filename.equalsIgnoreCase(".svn");
        }
    };
    
    /** The activator class that will be used to load test files from the source tree */
    protected Plugin activator;
    
    /** The refactoring under test */
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
     *
     * @return true iff the refactoring is expected to pass initial precondition checking
     */
    protected boolean initializeRefactoring(R refactoring, IFile file, TextSelection selection, String[] markerText)
    {
        if (refactoring instanceof FortranEditorRefactoring)
            ((FortranEditorRefactoring)refactoring).initialize(file, selection);
        else if (refactoring instanceof FortranResourceRefactoring)
            ((FortranResourceRefactoring)refactoring).initialize(Collections.singletonList(file));
        else
            throw new IllegalStateException();
        
        if (lastMarkerField(markerText).equals(PASS)
                || lastMarkerField(markerText).equals(FAIL_FINAL)
                || lastMarkerField(markerText).equals("true")
                || lastMarkerField(markerText).equals("false"))
            return true;
        else if (lastMarkerField(markerText).equals(FAIL_INITIAL))
            return false;
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
        if (lastMarkerField(markerText).equals(PASS) || lastMarkerField(markerText).equals("true"))
            return true;
        else if (lastMarkerField(markerText).equals(FAIL_FINAL) || lastMarkerField(markerText).equals("false"))
            return false;
        else
            throw new IllegalStateException();
    }

    private String lastMarkerField(String[] markerText)
    {
        return markerText[markerText.length-1];
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
            IFile fileContainingMarker = importFiles();
            R refactoring = createRefactoring();

            String[] markerFields = parseMarker();
            TextSelection selection = determineSelection(markerFields);

            appendFilenameToDescription(markerFields);

            RefactoringStatus status =
                checkInitialConditions(refactoring,
                    initializeRefactoring(refactoring, fileContainingMarker, selection, markerFields));

            if (!status.hasFatalError())
            {
                String before = compileAndRunFortranProgram();
                
                status = checkFinalConditions(refactoring,
                    configureRefactoring(refactoring, fileContainingMarker, selection, markerFields));
    
                if (!status.hasFatalError())
                {
                    performChange(refactoring);
                    
                    if (!status.hasError())
                        assertEquals(before, compileAndRunFortranProgram());
                    compareAgainstResultFile();
                }
            }
        }

        private IFile importFiles() throws Exception
        {
            importFiles(fileContainingMarker.getParent());
            return files.get(fileContainingMarker.getName());
        }

        private String compileAndRunFortranProgram() throws Exception
        {
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

            System.out.println("Compiling and running Fortran program");
            return compileAndRunFortranProgram(files.keySet().toArray(new String[files.size()]));
        }

        private String[] parseMarker()
        {
            String[] markerStrings = markerText.split(",");
            for (int i = 0; i < markerStrings.length; i++)
                markerStrings[i] = markerStrings[i].trim();
            return markerStrings;
        }

        private TextSelection determineSelection(String[] markerStrings) throws IOException, CoreException
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
            TextSelection selection = new TextSelection(createDocument(),  fromOffset, length);
            return selection;
        }

        private IDocument createDocument() throws IOException, CoreException
        {
            return new Document(readWorkspaceFile(fileContainingMarker.getName()));
        }

        private void appendFilenameToDescription(String[] markerStrings)
        {
            description += " (" + fileContainingMarker.getName() + " " + Arrays.toString(markerStrings) + ")";
        }

        private RefactoringStatus checkInitialConditions(R refactoring, boolean shouldSucceed)
        {
            RefactoringStatus status = refactoring.checkInitialConditions(new NullProgressMonitor());
            if (shouldSucceed)
                assertTrue(description + " failed initial precondition check: " + status.toString(), !status.hasError());
            else
                assertTrue(description + " should have failed initial precondition check but did not: " + status.toString(), status.hasError());
            return status;
        }

        private RefactoringStatus checkFinalConditions(R refactoring, boolean shouldSucceed)
        {
            RefactoringStatus status;
            status = refactoring.checkFinalConditions(new NullProgressMonitor());
            if (shouldSucceed)
                assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError());
            else
                assertTrue(description + " should have failed final precondition check but did not: " + status.toString(), status.hasError());
            return status;
        }

        private void performChange(R refactoring) throws CoreException
        {
            IProgressMonitor pm = new NullProgressMonitor();
            Change change = refactoring.createChange(pm);
            assertNotNull(description + " returned null Change object", change);
            assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
            change.perform(pm);
        }

        private void compareAgainstResultFile() throws IOException, URISyntaxException, CoreException
        {
            for (String filename : files.keySet())
            {
                if (resultFileFor(filename).exists())
                {
                    String expected = readTestFile(activator, fileContainingMarker.getParent(), filename + ".result").replaceAll("\\r", "");
                    String actual = readWorkspaceFile(filename).replaceAll("\\r", "");
                    assertEquals(expected, actual);               // actual refactored file
                }
            }
        }

        private File resultFileFor(String filename)
        {
            return new File(fileContainingMarker.getParent()
                + File.separator
                + filename
                +  ".result");
        }

        /**
         * @return true iff {@link Integer#parseInt(String)} can successfully parse the given
         *         string can be parsed as an integer
         */
        private boolean isInteger(String string)
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
