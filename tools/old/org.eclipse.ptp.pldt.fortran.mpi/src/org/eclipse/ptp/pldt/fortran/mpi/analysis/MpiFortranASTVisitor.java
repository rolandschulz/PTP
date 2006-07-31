/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.fortran.mpi.analysis;

import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.photran.core.programrepresentation.CompleteProgram;
import org.eclipse.photran.internal.core.f95parser.RefactoringFortranProcessor;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95refactoringparser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95refactoringparser.ParseTreeVisitor;
import org.eclipse.photran.internal.core.f95refactoringparser.Token;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.SourceInfo;

/**
 * This dom-walker collects MPI related constructs, and adds markers to the
 * source file for Fortran code.
 * 
 * @author Beth Tibbitts
 */
public class MpiFortranASTVisitor // extends CASTVisitor // not really!!
{
	ScanReturn scanReturn;

	String fileName;

	private static final String MPI_PREFIX = "MPI";

	public MpiFortranASTVisitor(List mpiIncludes, String fileName, ScanReturn msr) {
		scanReturn = msr;
		this.fileName = fileName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */

	public void doit(IFile file) {
		// IDocument doc = getDocument(file);
		RefactoringFortranProcessor rfp = new RefactoringFortranProcessor();
//		try {
//			SymbolTable st = rfp.parseAndCreateSymbolTableFor(file.getName());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// rfp.parseAndCreateSymbolTableFor(doc, filename);
		ParseTreeNode ptn = null;
		try {
			ptn = rfp.parse(file.getContents(), file.getName());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.openError(null, "Fortran Error", e.getMessage());
		}
		// CompleteProgram prog = rfp.parseAndCreateSymbolTableFor(doc, file
		// .getName());
		// ParseTreeNode ptn = prog.getParseTree();
		ptn.visitUsing(new ParseTreeVisitor() {
			@Override
			public void visitXsubroutinenameuse(ParseTreeNode subRefNode) {
				// craig says most MPI artifacts are SUBROUTINES (one function?)
				Token subNameToken = subRefNode.findFirstIdentifier();
				// String signature = fnNameToken.getText();
				// System.out.println("func signature=" + signature);
				// int offsetOfFnName = fnNameToken.getOffset();
				// int lengthOfFnName = fnNameToken.getLength();
				// String funcName=signature;
				processFuncName(subNameToken);
			}

			@Override
			public void visitXfunctionreference(ParseTreeNode fnRefNode) {
				Token fnNameToken = fnRefNode.findFirstIdentifier();
				// String signature = fnNameToken.getText();
				// System.out.println("func signature=" + signature);
				// int offsetOfFnName = fnNameToken.getOffset();
				// int lengthOfFnName = fnNameToken.getLength();
				// String funcName=signature;
				processFuncName(fnNameToken);
			}

		});

	}

	/**
	 * modeled from GeneralASTVisitorBehavior; do it more completely like that
	 * later
	 * 
	 * @param funcName
	 * @param astExpr
	 */
	String MPI_CALL = "MPI Call";

	String MPI_CONSTANT = "MPI Constant";

	public void processFuncName(Token fnNameToken) {
		String funcName = fnNameToken.getText();
		if (isMPICall(funcName)) {
			int lineNo = fnNameToken.getStartLine(); // 0 vs 1??
			int offsetStart = fnNameToken.getOffset();
			int len = fnNameToken.getLength();
			int artType = Artifact.FUNCTION_CALL;
			SourceInfo sourceInfo = new SourceInfo(lineNo, offsetStart, offsetStart + len, artType);
			scanReturn.addArtifact(new Artifact(fileName, lineNo, 1, // column:
					funcName.toString(), MPI_CALL, sourceInfo));
		}
	}

	/**
	 * eventually determine is this is a real mpi call, from include path<br>
	 * modeled from GeneralASTVisitorBehavior; when we handle fortran includes
	 * correctly, do it like that
	 * 
	 * @param searchResult
	 * @return
	 */

	private boolean isMPICall(String funcName) {
		return funcName.startsWith(MPI_PREFIX);

	}

	/**
	 * getDocument - get document using IFile
	 * 
	 * @param file -
	 *            IFile
	 * @return IDocument <br>
	 *         from org.eclipse.ptp.mptools.openmp.analysis.Utility <br>
	 *         The type org.eclipse.jface.text.IDocument cannot be resolved. It
	 *         is indirectly referenced from required .class files
	 */
	public static IDocument getDocument(IFile file) {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(file.getFullPath());
		IDocument document = null;
		if (textFileBuffer != null) {
			document = textFileBuffer.getDocument();
		}
		return document;
	}
}