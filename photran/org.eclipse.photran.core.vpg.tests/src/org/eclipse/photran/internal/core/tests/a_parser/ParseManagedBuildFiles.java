package org.eclipse.photran.internal.core.tests.a_parser;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.photran.internal.core.tests.ParserTestSuite;

public class ParseManagedBuildFiles
{
    public static Test suite() throws FileNotFoundException, IOException
    {
        return new ParserTestSuite("managed-build", false, true);
    }
}