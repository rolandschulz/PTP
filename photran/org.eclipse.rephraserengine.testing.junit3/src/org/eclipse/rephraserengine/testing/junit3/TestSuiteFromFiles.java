package org.eclipse.rephraserengine.testing.junit3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A {@link TestSuite} containing a test for each file found in the subdirectories of a particular
 * directory.
 * <p>
 * Subclasses must override {@link #test(File)}.
 * <p>
 * To run an entire suite of tests for the given file, or to construct an arbitrary TestCase,
 * subclass from {@link GeneralTestSuiteFromFiles} instead.
 * 
 * @author Jeff Overbey
 * 
 * @since 2.0
 */
public abstract class TestSuiteFromFiles extends GeneralTestSuiteFromFiles
{
    public TestSuiteFromFiles(String description, String directorySuffix, FilenameFilter filenameFilter,
        boolean mustExist) throws FileNotFoundException, IOException
    {
        super(description, directorySuffix, filenameFilter, mustExist);
    }

    public TestSuiteFromFiles(String description, String directorySuffix, FilenameFilter filenameFilter)
        throws FileNotFoundException, IOException
    {
        super(description, directorySuffix, filenameFilter);
    }

    public TestSuiteFromFiles(String description, String directorySuffix, final String filenameExtension,
        boolean mustExist) throws FileNotFoundException, IOException
    {
        super(description, directorySuffix, filenameExtension, mustExist);
    }

    public TestSuiteFromFiles(String description, String directorySuffix, final String filenameExtension)
        throws FileNotFoundException, IOException
    {
        super(description, directorySuffix, filenameExtension);
    }

    @Override
    protected final Test createTestFor(final File file)
    {
        return new SimpleFileTestCase(file) {};
    }

    public abstract class SimpleFileTestCase extends TestCase
    {
        protected File file;

        public SimpleFileTestCase(File file)
        {
            super("test");
            this.file = file;
        }

        public void test() throws Exception
        {
            TestSuiteFromFiles.this.test(file);
        }
    }

    protected abstract void test(File file) throws Exception;
}