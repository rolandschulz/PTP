package org.eclipse.photran.internal.core.tests.g_type_check;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.analysis.types.TypeChecker;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.Parser;

public class TypeCheckerTestCase extends TestCase
{
    /** Parses a Fortran expression.  Does not bind identifiers, etc. */
    private IExpr parse(String expression) throws Exception
    {
        // Hack: We can only parse entire programs, so we embed the expression in a parseable program
        String program = "a = " + expression + "\nend";

        // Then we extract the expression from the program
        ASTExecutableProgramNode ast = new Parser().parse(LexerFactory.createLexer(new ByteArrayInputStream(program.getBytes()), "<literal text>", SourceForm.UNPREPROCESSED_FREE_FORM, false));
        assertNotNull(ast);
        ASTMainProgramNode mainProg = (ASTMainProgramNode)ast.getProgramUnitList().get(0);
        ASTAssignmentStmtNode assignmentStmt = (ASTAssignmentStmtNode)mainProg.getBody().get(0);
        return assignmentStmt.getRhs();
    }
    
    private void checkType(String expression, Type type) throws Exception
    {
        assertEquals(type, TypeChecker.getTypeOf(parse(expression)));
    }
    
    public void testConstantExpressionTypes() throws Exception
    {
        checkType("3", Type.INTEGER);
        checkType("3.5", Type.REAL);
        checkType("\"Hello\"", Type.CHARACTER);
        checkType("'Hello'", Type.CHARACTER);
        checkType(".true.", Type.LOGICAL);
        
        checkType("3+4", Type.INTEGER);
        checkType("3+4*5**6", Type.INTEGER);
        checkType("3.2+4*5**6.7", Type.REAL);
        checkType("(3.2+4)*5**6.7", Type.REAL);
    }
}
