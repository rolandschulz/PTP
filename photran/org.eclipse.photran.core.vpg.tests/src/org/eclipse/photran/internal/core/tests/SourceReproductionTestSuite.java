package org.eclipse.photran.internal.core.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class SourceReproductionTestSuite extends AbstractParserTestSuite
{
    public SourceReproductionTestSuite(String directorySuffix, boolean isFixedForm, boolean mustExist) throws FileNotFoundException, IOException
    {
        super(directorySuffix, isFixedForm, mustExist);
    }

    @Override
    protected String describeTestAction()
    {
        return "Reproduce source code for";
    }

    @Override
    protected AbstractParserTestCase createTestFor(File file, boolean isFixedForm, String fileDescription)
    {
        return new SourceReproductionTestCase(file, isFixedForm, fileDescription);
    }
}
