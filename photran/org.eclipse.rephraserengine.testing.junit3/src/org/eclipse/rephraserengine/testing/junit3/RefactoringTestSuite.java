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
import java.util.LinkedList;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.testing.junit3.GeneralTestSuiteFromMarkers;

/**
 * Test suite for a refactoring.
 * <p>
 * The test suite is constructed by importing files from a directory in the source tree, searching
 * its files for <i>markers,</i> and adding one test case to the suite for each marker.
 * <p>
 * The prefix and suffix of the marker are passed as the <code>marker</code> and
 * <code>markerEnd</code> arguments to the constructor. Assuming <code>marker</code> is
 * &quot;!&lt;&lt;&lt;&lt;&lt;&quot; and <code>markerEnd</code> is &quot;\n&quot;, markers are
 * expected to have one of the following forms:
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
 * the refactoring in order to ensure that the refactoring actually preserved behavior.  See the
 * documentation for {@link RefactoringTestCase} for more information.
 * 
 * @author Jeff Overbey
 * 
 * @see RefactoringTestCase
 * @see GeneralTestSuiteFromMarkers
 * 
 * @since 3.0
 */
public class RefactoringTestSuite<R extends Refactoring> extends GeneralTestSuiteFromMarkers {

    private Class<R> refactoringClass;
    private EclipseVPG<?, ?, ?> vpg;
    private FilenameFilter filenameFilter;

    public RefactoringTestSuite(Class<R> refactoringClass,
                                String marker,
                                String markerEnd,
                                File fileOrDirectory,
                                FilenameFilter filenameFilter) throws Exception {
        this(refactoringClass, marker, markerEnd, fileOrDirectory, filenameFilter, null);
    }

    public RefactoringTestSuite(Class<R> refactoringClass,
                                String marker,
                                String markerEnd,
                                File fileOrDirectory,
                                FilenameFilter filenameFilter,
                                EclipseVPG<?,?,?> vpg) throws Exception {
        super(refactoringClass.getName(),
              marker,
              markerEnd,
              fileOrDirectory,
              filenameFilter,
              // Initialization data:
              refactoringClass,
              filenameFilter,
              vpg);
    }

    // Callback method which is invoked before adding tests to this test suite.
    @SuppressWarnings("unchecked")
    @Override
    protected void initialize(Object... initializationData)
    {
        this.refactoringClass = (Class<R>)initializationData[0];
        this.filenameFilter = (FilenameFilter)initializationData[1];
        this.vpg = (EclipseVPG<?,?,?>)initializationData[2];
    }

    @Override
    protected Test createTestFor(File fileContainingMarker, int markerOffset, String markerText)
            throws Exception {
        return new IndividualRefactoringTestCase(
                fileContainingMarker,
                markerOffset,
                markerText,
                refactoringClass,
                vpg,
                filenameFilter);
    }
    
    public class IndividualRefactoringTestCase extends RefactoringTestCase<R> {
        private final Class<R> refactoringClass;
        
        public IndividualRefactoringTestCase(File fileContainingMarker, int markerOffset, String markerText, Class<R> refactoringClass, EclipseVPG<?,?,?> vpg, FilenameFilter filenameFilter) throws Exception {
            super(fileContainingMarker, markerOffset, markerText, vpg, filenameFilter);
            this.refactoringClass = refactoringClass;
        }
        
        @Override
        protected R createRefactoring() throws Exception {
            return refactoringClass.newInstance();
        }

        @Override
        protected boolean initializeRefactoring(R refactoring, IFile file, TextSelection selection, LinkedList<String> markerFields) {
            RefactoringTestSuite.this.initializeRefactoring(refactoring, file, selection, markerFields);
            return super.initializeRefactoring(refactoring, file, selection, markerFields);
        }

        @Override
        protected boolean configureRefactoring(R refactoring, IFile file, TextSelection selection, LinkedList<String> markerFields) {
            RefactoringTestSuite.this.configureRefactoring(refactoring, file, selection, markerFields);
            return super.configureRefactoring(refactoring, file, selection, markerFields);
        }

        @Override
        protected boolean shouldCompile(IFile fileContainingMarker)
        {
            return RefactoringTestSuite.this.shouldCompile(fileContainingMarker);
        }

        @Override
        protected String compileAndRunProgram(Map<String, IFile> files) throws Exception
        {
            return RefactoringTestSuite.this.compileAndRunProgram(files);
        }

        @Override
        protected void deinitializeRefactoring(R refactoring, IFile file, TextSelection selection, LinkedList<String> markerFields) throws Exception {
            RefactoringTestSuite.this.deinitializeRefactoring(refactoring, file, selection, markerFields);
            super.deinitializeRefactoring(refactoring, file, selection, markerFields);
        }
    }
    
    /**
     * This method is invoked after the refactoring is created ({@link #createRefactoring()}) but
     * before {@link Refactoring#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)}
     * is invoked.  Override if necessary.
     */
    protected void initializeRefactoring(R refactoring, IFile file, TextSelection selection, LinkedList<String> markerFields) {
        ;
    }

    /**
     * This method is invoked after initial preconditions have been checked
     * ({@link Refactoring#checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)})
     * but before final preconditions are checked
     * ({@link Refactoring#checkFinalConditions(org.eclipse.core.runtime.IProgressMonitor)}).
     * Override if necessary to supply user input.
     */
    protected void configureRefactoring(R refactoring, IFile file, TextSelection selection, LinkedList<String> markerFields) {
        ;
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
    protected void deinitializeRefactoring(R refactoring, IFile file, TextSelection selection, LinkedList<String> markerFields) throws Exception {
        ;
    }
}
