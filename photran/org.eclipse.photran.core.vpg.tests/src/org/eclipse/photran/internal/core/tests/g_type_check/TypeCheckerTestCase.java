package org.eclipse.photran.internal.core.tests.g_type_check;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.analysis.types.TypeChecker;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;

public class TypeCheckerTestCase extends TestCase
{
    private ASTExecutableProgramNode parse(String program) throws Exception
    {
        ASTExecutableProgramNode ast = new Parser().parse(LexerFactory.createLexer(new ByteArrayInputStream(program.getBytes()), "<literal text>", SourceForm.UNPREPROCESSED_FREE_FORM));
        assertTrue(ast != null);
        
        // TODO: Type and bind
        
        return ast;
    }
    
    private void checkType(String expression, Type type) throws Exception
    {
        String program = "a = " + expression + "\nend";
        ASTExecutableProgramNode ast = parse(program);
        assertEquals(TypeChecker.getTypeOf(ast.getProgramUnit(0).getMainProgram().getBody().getBodyConstruct(0).getExecutableConstruct().getAssignmentStmt().getExpr()), type);
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
