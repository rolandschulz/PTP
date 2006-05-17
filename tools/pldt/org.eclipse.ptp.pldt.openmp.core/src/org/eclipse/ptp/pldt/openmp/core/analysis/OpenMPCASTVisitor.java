/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.core.analysis;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;


/**
 * This dom-walker collects OpenMP related constructs (currently function calls and constants), and add markers to the
 * source file for C code. Currently, it delegates work to MpiGeneralASTVisitorBehavior.
 * 
 */
public class OpenMPCASTVisitor extends PldtAstVisitor
{
    {
        this.shouldVisitExpressions = true;
        this.shouldVisitStatements = true;
        this.shouldVisitDeclarations = true;
        this.shouldVisitTranslationUnit = true;
    }


    public OpenMPCASTVisitor(List includes, String fileName, ScanReturn msr)
    {
        super(includes, fileName, msr);
        ARTIFACT_CALL = "OpenMP Call";
		ARTIFACT_CONSTANT="OpenMP Constant";
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
                    processFuncName(funcName, astExpr);
                }
            }
        } else if (expression instanceof IASTLiteralExpression) {
            processMacroLiteral((IASTLiteralExpression) expression);
        }
        return PROCESS_CONTINUE;
    }
}