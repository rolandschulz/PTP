package org.eclipse.photran.internal.core.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Creates a suite of {@link ParserTestCase} tests from Fortran source files in a particular directory.
 * 
 * @author joverbey
 */
public abstract class ParserTestSuite extends AbstractParserTestSuite
{
    public ParserTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
        super(directorySuffix, isFixedForm, mustExist);
    }

    @Override
    protected String describeTestAction()
    {
        return "Parse";
    }

    @Override
    protected AbstractParserTestCase createTestFor(File file, boolean isFixedForm, String fileDescription)
    {
        return new ParserTestCase(file, isFixedForm, fileDescription);
    }
}
