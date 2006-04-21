/**
 * 
 */
package org.eclipse.ptp.pldt.openmp.core.analysis;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.GeneralASTVisitorBehavior;


/**
 * This dom-walker collects OpenMP related constructs (currently function calls and constants), and add markers to the
 * source file for C code. Currently, it delegates work to MpiGeneralASTVisitorBehavior.
 * 
 */
public class OpenMPCASTVisitor extends CASTVisitor
{
    {
        this.shouldVisitExpressions = true;
        this.shouldVisitStatements = true;
        this.shouldVisitDeclarations = true;
        this.shouldVisitTranslationUnit = true;
    }

    private GeneralASTVisitorBehavior generalVisitorBehavior;

    public OpenMPCASTVisitor(List mpiIncludes, String fileName, ScanReturn msr)
    {
        generalVisitorBehavior = new GeneralASTVisitorBehavior(mpiIncludes, fileName, msr);
    }

    public int visit(IASTStatement statement)
    {
        return generalVisitorBehavior.visit(statement);
    }

    public int visit(IASTDeclaration declaration)
    {
        return generalVisitorBehavior.visit(declaration);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    private static final String PREFIX="omp_";
    public int visit(IASTExpression expression)
    {
        if (expression instanceof IASTFunctionCallExpression) {
            IASTExpression astExpr = ((IASTFunctionCallExpression) expression).getFunctionNameExpression();
            String signature = astExpr.getRawSignature();
            //System.out.println("func signature=" + signature);
            if (signature.startsWith(PREFIX)) {
                if (astExpr instanceof IASTIdExpression) {
                    IASTName funcName = ((IASTIdExpression) astExpr).getName();
                    generalVisitorBehavior.processFuncName(funcName, astExpr);
                }
            }
        } else if (expression instanceof IASTLiteralExpression) {
            generalVisitorBehavior.processMacroLiteral((IASTLiteralExpression) expression);
        }
        return PROCESS_CONTINUE;
    }
}