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
    private Type getType(String expression) throws Exception
    {
        // We can only parse entire programs, so we embed the expression in
        // a parseable program, then extract the expression from the program's AST
        
        String program = "a = " + expression + "\nend";
        ASTExecutableProgramNode ast = new Parser().parse(LexerFactory.createLexer(new ByteArrayInputStream(program.getBytes()), "<literal text>", SourceForm.UNPREPROCESSED_FREE_FORM, false));
        assertNotNull(ast);
        ASTMainProgramNode mainProg = (ASTMainProgramNode)ast.getProgramUnitList().get(0);
        ASTAssignmentStmtNode assignmentStmt = (ASTAssignmentStmtNode)mainProg.getBody().get(0);
        IExpr exprNode = assignmentStmt.getRhs();
        return TypeChecker.getTypeOf(exprNode);
    }
    
    public void testConstantExpressionTypes() throws Exception
    {
        assertEquals(Type.INTEGER, getType("3"));
        //assertEquals(Type.INTEGER, getType("-500"));
        //assertEquals(Type.INTEGER, getType("300+500"));
    }
}
