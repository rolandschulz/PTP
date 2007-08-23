package org.eclipse.photran.internal.core.tests;

import java.io.File;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

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
