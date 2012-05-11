/**********************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openshmem.analysis;


import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.analysis.PldtAstVisitor;
import org.eclipse.ptp.pldt.openshmem.Activator;
import org.eclipse.ptp.pldt.openshmem.OpenSHMEMIDs;
import org.eclipse.ptp.pldt.openshmem.messages.Messages;


/**
 * This dom-walker collects "artifacts" related to the specific domain <br>
 * (e.g. OpenSHMEM). Currently these artifacts include function calls
 * and constants. It add markers to the source file for C code, marking the
 * position of the artifacts found.
 * 
 * This version extends PldtAstVisitor instead of delegating to<br>
 * MpiGeneralASTVisitorBehavior.
 *
 * @author Beth Tibbitts
 * 
 */
public class OpenSHMEMCASTVisitor extends PldtAstVisitor 
{
	/** fixme PAMI_ is prefix for fns but pami_ is prefix for vars.  Consider adding case-insensitivity. true for shmem too????? */
    private static final String PREFIX="shmem_"; //$NON-NLS-1$

    {
        this.shouldVisitExpressions = true;
        this.shouldVisitStatements = true;
        this.shouldVisitDeclarations = true;
        this.shouldVisitTranslationUnit = true;
    }

    public OpenSHMEMCASTVisitor(List<String> includes, String fileName,  boolean allowPrefixOnlyMatch, ScanReturn msr)
    {
        super(includes, fileName, allowPrefixOnlyMatch, msr);
		ARTIFACT_CALL = Messages.OpenSHMEMCASTVisitor_openshmem_call;
		ARTIFACT_CONSTANT=Messages.OpenSHMEMCASTVisitor_openshmem_constant; 
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
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



	/**
	 * needs to be overridden for derived classes that need to dynamically update the pref store 
	 * e.g. for the includes path
	 * @return
	 */
	@Override
	protected String getIncludesPrefID() {
		return OpenSHMEMIDs.OpenSHMEM_INCLUDES;
	}



	/**
	 * needs to be overridden for derived classes that need to dynamically update the pref store 
	 * e.g. for the includes path
	 * @return
	 */
	@Override
	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
	/**
	 * @since 5.0
	 */
	@Override
	public boolean matchesPrefix(String name) {
		return name.startsWith(PREFIX);
	}
}