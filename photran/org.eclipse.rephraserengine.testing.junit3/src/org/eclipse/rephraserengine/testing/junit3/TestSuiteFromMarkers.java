package org.eclipse.rephraserengine.testing.junit3;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A {@link TestSuite} containing a test for each <i>marker</i> found in the files in a particular
 * directory.  A <i>marker</i> is an arbitrary string (Photran uses <pre>!&lt;&lt;&lt;&lt;&lt;</pre>),
 * since <pre>!</pre> is the comment character in Fortran) followed by arbitrary text which continues
 * to the end of the line or the next marker, whichever appears first.
 * <p>
 * Subclasses must override {@link #createTestFor(List, File, int, String))}.
 * <p>
 * To run an entire suite of tests for the given file, or to construct an arbitrary TestCase,
 * subclass from {@link GeneralTestSuiteFromMarkers} instead.
 * 
 * @author Jeff Overbey
 */
public abstract class TestSuiteFromMarkers extends GeneralTestSuiteFromMarkers
{
    public TestSuiteFromMarkers(String description, String marker, String fileOrDirectory, FilenameFilter filenameFilter) throws Exception
    {
        super(description, marker, fileOrDirectory, filenameFilter);
    }

    public TestSuiteFromMarkers(String description, String marker, String fileOrDirectory, final String filenameExtension) throws Exception
    {
        super(description, marker, fileOrDirectory, filenameExtension);
    }

    @Override
    protected final Test createTestFor(List<File> allFiles, File fileContainingMarker, int markerOffset, String markerText)
    {
        TestSuite suite = new TestSuite(fileContainingMarker.getName() + " - " + markerText);
        suite.addTest(new SimpleFileTestCase(allFiles, fileContainingMarker, markerOffset, markerText) {});
        return suite;
    }

    public abstract class SimpleFileTestCase extends TestCase
    {
        protected final List<File> allFiles;
        protected final File fileContainingMarker;
        protected final int markerOffset;
        protected final String markerText;

        public SimpleFileTestCase(List<File> allFiles, File fileContainingMarker, int markerOffset, String markerText)
        {
            super("test");
            this.allFiles = allFiles;
            this.fileContainingMarker = fileContainingMarker;
            this.markerOffset = markerOffset;
            this.markerText = markerText;
        }

        public void test() throws Exception
        {
            TestSuiteFromMarkers.this.test(allFiles, fileContainingMarker, markerOffset, markerText);
        }
    }

    protected abstract void test(List<File> allFiles, File fileContainingMarker, int markerOffset, String markerText) throws Exception;
}