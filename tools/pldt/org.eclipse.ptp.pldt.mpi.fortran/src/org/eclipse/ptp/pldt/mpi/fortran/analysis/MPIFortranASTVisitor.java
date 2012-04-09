/**********************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.mpi.fortran.analysis;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTCallStmtNode;
import org.eclipse.photran.internal.core.parser.ASTNameNode;
import org.eclipse.photran.internal.core.parser.ASTVarOrFnRefNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.SourceInfo;

/**
 * This dom-walker collects "artifacts" related to the specific domain <br>
 * (e.g. MPI, OpenMP, etc.). Currently these artifacts include function calls
 * and constants. It adds markers to the source file for C code, marking the
 * position of the artifacts found.
 * 
 * @author Beth Tibbitts
 * @since 4.0
 * 
 */
@SuppressWarnings("restriction")
public class MPIFortranASTVisitor extends GenericASTVisitor {
	private static final String PREFIX = "MPI_"; //$NON-NLS-1$

	@SuppressWarnings("unused")
	private static final boolean traceOn = false;
	private final ScanReturn scanReturn;
	private final String fileName;

	public MPIFortranASTVisitor(String fileName, ScanReturn scanReturn) {
		super();
		this.scanReturn = scanReturn;
		this.fileName = fileName;
	}

	@Override
	public void visitASTCallStmtNode(ASTCallStmtNode node) {
		Token subroutineName = node.getSubroutineName();
		addArtifact(subroutineName, Artifact.FUNCTION_CALL);
	}

	@Override
	public void visitASTVarOrFnRefNode(ASTVarOrFnRefNode node) {
		ASTNameNode nameNode = node.getName();
		if (nameNode != null) {
			Token varName = nameNode.getName();
			addArtifact(varName, Artifact.CONSTANT);
		}
	}

	private void addArtifact(Token subroutineName, int artifactType) {
		String callname = subroutineName.getText().toUpperCase();
		if (callname.startsWith(PREFIX)) {
			int start = subroutineName.getFileOffset();
			int end = subroutineName.getFileOffset() + subroutineName.getLength();
			SourceInfo si = new SourceInfo(subroutineName.getLine(), start, end, artifactType);
			scanReturn.addArtifact(new Artifact(fileName, subroutineName.getLine(), 1, callname, si));
		}
	}
}