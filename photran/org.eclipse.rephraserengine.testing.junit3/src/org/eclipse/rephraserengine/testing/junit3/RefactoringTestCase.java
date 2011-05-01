/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.testing.junit3;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.rephraserengine.core.refactorings.IEditorRefactoring;
import org.eclipse.rephraserengine.core.refactorings.IResourceRefactoring;
import org.eclipse.rephraserengine.core.util.StringUtil;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * Test case for a refactoring (used in a {@link GeneralTestSuiteFromMarkers}).
 * <p>
 * Most clients will want to subclass {@link RefactoringTestSuite} instead of manually constructing
 * a suite of {@link RefactoringTestCase}s.
 * <p>
 * Assuming the marker prefix is &quot;!&lt;&lt;&lt;&lt;&lt;&quot; and the marker suffix is
 * &quot;\n&quot;, markers are expected to have one of the following forms:
 * <ol>
 * <li> <tt>!&lt;&lt;&lt;&lt;&lt; line, col, ..., pass</tt>
 * <li> <tt>!&lt;&lt;&lt;&lt;&lt; fromLine, fromCol, toLine, toCol, ..., pass</tt>
 * </ol>
 * That is, the first two fields in each marker are expected to be a line and column number; the
 * text selection passed to the refactoring will be the offset of that line and column. The third
 * fourth fields may also be a line and column number; then, the selection passed to the refactoring
 * will extend from the first line/column to the second line/column.
 * <p>
 * The line and column numbers may be followed by an arbitrary number of fields that contain data
 * specific to the refactoring being invoked. Many refactorings don't require any additional data;
 * the Extract Local Variable test suite uses one field for the new variable declaration; the Add
 * ONLY to USE Statement test suite uses these fields to list the module entities to add; etc.
 * <p>
 * The final field must be either &quot;pass&quot;, &quot;fail-initial&quot;, or
 * &quot;fail-final&quot;, indicating whether the refactoring should succeed, fail its initial
 * precondition check, or fail its final precondition check.
 * <p>
 * If the refactoring is expected to succeed, the program may be compiled and run before and after
 * the refactoring in order to ensure that the refactoring actually preserved behavior. See the
 * documentation for {@link RefactoringTestCase} for more information.
 * 
 * @author Jeff Overbey
 * 
 * @see RefactoringTestSuite
 * @see GeneralTestSuiteFromMarkers
 * 
 * @since 3.0
 */
public abstract class RefactoringTestCase<R extends Refactoring> extends WorkspaceTestCase
{
    /** Text of the last marker field when a refactoring is pass all precondition checks */
    protected static final String PASS = "pass"; //$NON-NLS-1$
    /** Text of the last marker field when a refactoring is expected to fail initial precondition check */
    protected static final String FAIL_INITIAL = "fail-initial"; //$NON-NLS-1$
    /** Text of the last marker field when a refactoring is expected to fail final precondition check */
    protected static final String FAIL_FINAL = "fail-final"; //$NON-NLS-1$

    private final File jioFileContainingMarker;
    private final String markerText;
    private final FilenameFilter filenameFilter;
    private String description;
    private Map<String, IFile> files;

    public RefactoringTestCase(File fileContainingMarker,
                               int markerOffset,
                               String markerText,
                               FilenameFilter filenameFilter) throws Exception {
        this(fileContainingMarker, markerOffset, markerText, null, filenameFilter);
    }

    public RefactoringTestCase(File fileContainingMarker,
                               int markerOffset,
                               String markerText,
                               EclipseVPG<?,?,?> vpg,
                               FilenameFilter filenameFilter) throws Exception {
        super("test", vpg); //$NON-NLS-1$
        this.jioFileContainingMarker = fileContainingMarker;
        this.markerText = markerText;
        this.filenameFilter = filenameFilter;
        this.description = fileContainingMarker.getName();
    }

    public void test() throws Exception
    {
        this.files = importFiles();
        
        IFile fileContainingMarker = files.get(jioFileContainingMarker.getName());
        assertNotNull(fileContainingMarker);
        
        R refactoring = createRefactoring();

        LinkedList<String> markerFields = MarkerUtil.parseMarker(markerText);
        TextSelection selection = MarkerUtil.determineSelection(markerFields, createDocument(fileContainingMarker));

        appendFilenameToDescription(markerFields);

        RefactoringStatus status =
            checkInitialConditions(refactoring,
                initializeRefactoring(refactoring, fileContainingMarker, selection, markerFields));

        if (!status.hasFatalError())
        {
            String before = shouldCompile(fileContainingMarker)
                ? compileAndRunProgram(files)
                : ""; //$NON-NLS-1$
            
            status = checkFinalConditions(refactoring,
                configureRefactoring(refactoring, fileContainingMarker, selection, markerFields));

            if (!status.hasFatalError())
            {
                performChange(refactoring);
                refreshProject();
                
                if (!status.hasError() && shouldCompile(fileContainingMarker))
                    assertEquals(before, compileAndRunProgram(files));
                compareAgainstResultFile();
            }
        }
        
        deinitializeRefactoring(refactoring, fileContainingMarker, selection, markerFields);
    }

    protected Map<String, IFile> importFiles() throws Exception
    {
        Map<String, IFile> result = importAllFiles(jioFileContainingMarker.getParentFile(), filenameFilter);
        refreshProject();
        return result;
    }

    protected void refreshProject() throws CoreException
    {
        NullProgressMonitor pm = new NullProgressMonitor();
        project.refreshLocal(IResource.DEPTH_INFINITE, pm);
        if (vpg != null) vpg.ensureVPGIsUpToDate(pm);
    }

    /**
     * This method is invoked to instantiate the refactoring class.
     */
    protected abstract R createRefactoring() throws Exception;

    private void appendFilenameToDescription(LinkedList<String> markerStrings)
    {
        description += " (" + jioFileContainingMarker.getName() + " " + markerStrings + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * This method is invoked after the refactoring is created ({@link #createRefactoring()}) but
     * before {@link Refactoring#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)}
     * is invoked.  Override if necessary.
     *
     * @return true iff the refactoring is expected to pass initial precondition checking
     */
    protected boolean initializeRefactoring(R refactoring, IFile file, TextSelection selection, LinkedList<String> markerFields)
    {
        if (refactoring instanceof IEditorRefactoring)
            ((IEditorRefactoring)refactoring).initialize(file, selection);
        else if (refactoring instanceof IResourceRefactoring)
            ((IResourceRefactoring)refactoring).initialize(Collections.singletonList(file));
        else
            throw new IllegalStateException("Must override #initializeRefactoring"); //$NON-NLS-1$
        
        if (lastMarkerField(markerFields).equals(PASS) || lastMarkerField(markerFields).equals(FAIL_FINAL))
            return true;
        else if (lastMarkerField(markerFields).equals(FAIL_INITIAL))
            return false;
        else
            throw new IllegalStateException("Last marker field should be pass, fail-initial, or fail-final"); //$NON-NLS-1$
    }

    protected String lastMarkerField(LinkedList<String> markerFields)
    {
        return markerFields.getLast();
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
    protected boolean configureRefactoring(R refactoring, IFile file, TextSelection selection, LinkedList<String> markerFields)
    {
        if (lastMarkerField(markerFields).equals(PASS))
            return true;
        else if (lastMarkerField(markerFields).equals(FAIL_FINAL))
            return false;
        else
            throw new IllegalStateException();
    }

    private RefactoringStatus checkInitialConditions(R refactoring, boolean shouldSucceed) throws OperationCanceledException, CoreException
    {
        RefactoringStatus status = refactoring.checkInitialConditions(new NullProgressMonitor());
        if (shouldSucceed)
            assertTrue(description + " failed initial precondition check: " + status.toString(), !status.hasError()); //$NON-NLS-1$
        else
            assertTrue(description + " should have failed initial precondition check but did not: " + status.toString(), status.hasError()); //$NON-NLS-1$
        return status;
    }

    private RefactoringStatus checkFinalConditions(R refactoring, boolean shouldSucceed) throws OperationCanceledException, CoreException
    {
        RefactoringStatus status;
        status = refactoring.checkFinalConditions(new NullProgressMonitor());
        if (shouldSucceed)
            assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError()); //$NON-NLS-1$
        else
            assertTrue(description + " should have failed final precondition check but did not: " + status.toString(), status.hasError()); //$NON-NLS-1$
        return status;
    }

    private void performChange(R refactoring) throws CoreException
    {
        IProgressMonitor pm = new NullProgressMonitor();
        Change change = refactoring.createChange(pm);
        assertNotNull(description + " returned null Change object", change); //$NON-NLS-1$
        assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK()); //$NON-NLS-1$
        change.perform(pm);
    }

    private void compareAgainstResultFile() throws IOException, URISyntaxException, CoreException
    {
        for (String filename : files.keySet())
        {
            if (resultFileFor(filename).exists())
            {
                String expected = StringUtil.read(resultFileFor(filename)).replaceAll("\\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
                String actual = readWorkspaceFile(filename).replaceAll("\\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
                assertEquals(expected, actual);
            }
        }
    }

    private File resultFileFor(String filename)
    {
        return new File(jioFileContainingMarker.getParent()
            + File.separator
            + filename
            +  ".result"); //$NON-NLS-1$
    }

    /**
     * @return true iff the program should be compiled and run using {@link #compileAndRunProgram(Map)}
     */
    protected boolean shouldCompile(IFile fileContainingMarker)
    {
        return false;
    }

    /**
     * Compiles and runs the test program.
     * <p>
     * This method is invoked iff {@link #shouldCompile(IFile)} returns <code>true</code>.
     * 
     * @return the output of running the program
     * 
     * @throws Exception if compilation or execution fails
     */
    protected String compileAndRunProgram(Map<String, IFile> files) throws Exception
    {
        throw new UnsupportedOperationException("Must override #compileAndRunProgram if #shouldCompile can return true"); //$NON-NLS-1$
    }

    /**
     * This method is invoked after the refactoring has been performed (after
     * {@link Refactoring#createChange(IProgressMonitor)} and
     * {@link Change#perform(IProgressMonitor)}. Override if necessary.
     * 
     * @throws Exception 
     */
    protected void deinitializeRefactoring(R refactoring, IFile file, TextSelection selection, LinkedList<String> markerFields) throws Exception
    {
    }
}
