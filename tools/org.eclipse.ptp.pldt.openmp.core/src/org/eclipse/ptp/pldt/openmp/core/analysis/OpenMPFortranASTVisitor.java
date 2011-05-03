/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.core.analysis;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTNameNode;
import org.eclipse.photran.internal.core.parser.ASTVarOrFnRefNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.SourceInfo;

/**
 * This visitor collects OpenMP related constructs (currently function calls and constants), and
 * add markers to the source file for Fortran code.
 * 
 * @author Jeff Overbey
 * @author Beth Tibbitts
 */
public class OpenMPFortranASTVisitor extends GenericASTVisitor {
	private static final String PREFIX = "OMP_"; //$NON-NLS-1$

	private ScanReturn scanReturn;
	private String fileName;

	public OpenMPFortranASTVisitor(String fileName, ScanReturn scanReturn) {
		this.scanReturn = scanReturn;
		this.fileName = fileName;
	}

	@Override
	public void visitToken(Token node) {
		/*
		 * In Fortran, OpenMP directives are comments (e.g., !$omp parallel).
		 * Photran attaches comments to the following token. Since they appear
		 * before several types of statements (including END statements), it's
		 * easiest to just iterate through all the tokens in the AST and collect
		 * the preceding OpenMP directives.
		 */
		for (Token ompDirective : node.getOpenMPComments()) {
			addArtifact(ompDirective, Artifact.PRAGMA);
		}
	}

	@Override
	public void visitASTVarOrFnRefNode(ASTVarOrFnRefNode node) {
		ASTNameNode nameNode = node.getName();
		if (nameNode != null) { // function call
			addArtifact(nameNode.getName(), Artifact.FUNCTION_CALL);
		}
	}

	private void addArtifact(Token token, int artifactType) {
		String callname = token.getText().toUpperCase();
		if (artifactType == Artifact.PRAGMA || callname.startsWith(PREFIX)) {
			int start = token.getFileOffset();
			int end = token.getFileOffset() + token.getLength();
			SourceInfo si = new SourceInfo(token.getLine(), start, end, artifactType);
			scanReturn.addArtifact(new Artifact(fileName, token.getLine(), 1, callname, si));
		}
	}
}