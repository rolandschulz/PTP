package org.eclipse.photran.internal.core.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;

public class SourceReproductionTestCase extends AbstractParserTestCase
{
    public SourceReproductionTestCase(File file, boolean isFixedForm, String testCaseDescription)
    {
        super(file, isFixedForm, testCaseDescription);
    }

    @Override
    protected void handleAST(ASTExecutableProgramNode ast) throws IOException
    {
        String originalSourceCode = getSourceCodeFromFile(file);
        String reproducedSourceCode = getSourceCodeFromAST(ast);
        assertEquals(originalSourceCode, reproducedSourceCode);
    }

    private String getSourceCodeFromFile(File file) throws IOException
    {
        StringBuffer sb = new StringBuffer(4096);
        BufferedReader in = new BufferedReader(new FileReader(file));
        for (int ch = in.read(); ch >= 0; ch = in.read())
            sb.append((char)ch);
        in.close();
        return sb.toString();
    }
    
    private String getSourceCodeFromAST(ASTExecutableProgramNode ast)
    {
        return SourcePrinter.getSourceCodeFromAST(ast);
    }

    public SourceReproductionTestCase() { super(null, false, ""); } // to keep JUnit quiet
}
