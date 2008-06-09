package org.eclipse.photran.internal.core.tests;

import java.io.File;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

/**
 * A test case which runs the parser over a file, expecting a successful parse.
 * Created by {@link ParserTestSuite}.
 * 
 * @author joverbey
 */
public class ParserTestCase extends AbstractParserTestCase
{
    public ParserTestCase(File file, boolean isFixedForm, String testCaseDescription)
    {
        super(file, isFixedForm, testCaseDescription);
    }

    @Override
    protected void handleAST(ASTExecutableProgramNode ast)
    {
        ;
    }
    
    public ParserTestCase() { super(null, false, ""); } // to keep JUnit quiet
}
