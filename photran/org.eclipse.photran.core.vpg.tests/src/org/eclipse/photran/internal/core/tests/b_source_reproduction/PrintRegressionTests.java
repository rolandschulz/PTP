package org.eclipse.photran.internal.core.tests.b_source_reproduction;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.photran.internal.core.tests.SourceReproductionTestSuite;

public class PrintRegressionTests
{
    public static Test suite() throws FileNotFoundException, IOException
    {
        return new SourceReproductionTestSuite("reg_tests", false, true);
    }
}