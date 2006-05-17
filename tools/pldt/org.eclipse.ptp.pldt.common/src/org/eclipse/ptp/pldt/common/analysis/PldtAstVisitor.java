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

package org.eclipse.ptp.pldt.common.analysis;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.SourceInfo;

/**
 * @author tibbitts
 * 
 */
public class PldtAstVisitor extends CASTVisitor {
	/**
	 * This dom-walker helper collects interesting constructs (currently
	 * function calls and constants), and add markers to the source file for
	 * C/C++ code. <br>
	 * This base class encapsulates the common behaviors for both C and C++
	 * code.
	 * 
	 * @author Beth Tibbitts
	 * 
	 */

	protected static String ARTIFACT_CALL = "Artifact Call";
	protected static String ARTIFACT_CONSTANT = "Artifact Constant";
	protected static String PREFIX = "";

	private final List includes_;
	private final String fileName;
	private final ScanReturn scanReturn;
	private static final String STRING_QUOTE = "\"";

	/**
	 * 
	 * @param includes
	 *            list of include paths that we'll probably want to consider in
	 *            the work that this visitor does
	 * @param fileName
	 *            the name of the file that this visitor is visiting(?)
	 * @param scanReturn
	 *            the ScanReturn object to which the artifacts that we find will
	 *            be appended.
	 */
	public PldtAstVisitor(List includes, String fileName, ScanReturn scanReturn) {
		this.includes_ = includes;
		this.fileName = fileName;
		this.scanReturn = scanReturn;
	}

	/**
	 * Skip statements that are included.
	 */
	public int visit(IASTStatement statement) {//called
		if (preprocessorIncluded(statement)) {
			return ASTVisitor.PROCESS_SKIP;
		}
		return ASTVisitor.PROCESS_CONTINUE;//path taken
	}

	/**
	 * Skip decls that are included.
	 * 
	 * @param declaration
	 * @return
	 */
	public int visit(IASTDeclaration declaration) {//called; both paths get taken
		if (preprocessorIncluded(declaration)) {
			return ASTVisitor.PROCESS_SKIP;
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}

	/**
	 * Process a function name from an expression and determine if it should be
	 * marked as an Artifact. If so, append it to the scanReturn object that
	 * this visitor is populating.
	 * 
	 * @param astExpr
	 */
	public void processFuncName(IASTName funcName, IASTExpression astExpr) {
		// IASTName funcName = ((IASTIdExpression) astExpr).getName();
		IASTTranslationUnit tu = funcName.getTranslationUnit();

		if (isArtifact(funcName)) {
			SourceInfo sourceInfo = getSourceInfo(astExpr, Artifact.FUNCTION_CALL);
			if (sourceInfo != null) {
				System.out.println("found MPI artifact: " + funcName.toString());

				scanReturn.addArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1,
						funcName.toString(), ARTIFACT_CALL, sourceInfo));

			}
		}

	}

	/**
	 * 
	 * @param astExpr
	 */
	public void processExprWithConstant(IASTExpression astExpr) {
		IASTName funcName = ((IASTIdExpression) astExpr).getName();
		IASTTranslationUnit tu = funcName.getTranslationUnit();

		if (isArtifact(funcName)) {
			SourceInfo sourceInfo = getSourceInfo(astExpr, Artifact.FUNCTION_CALL);
			if (sourceInfo != null) {
				System.out.println("found MPI artifact: " + funcName.toString());

				scanReturn.addArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1,
						funcName.toString(), ARTIFACT_CALL, sourceInfo));

			}
		}

	}

	/**
	 * Determines if the funcName is an instance of the type of artifact in
	 * which we are interested. <br>
	 * That is, is it declared in the include path?
	 * 
	 * @param funcName
	 */
	protected boolean isArtifact(IASTName funcName) {
		IBinding binding = funcName.resolveBinding();
		IASTName[] decls = funcName.getTranslationUnit().getDeclarations(binding);

		for (int i = 0; i < decls.length; ++i) {
			// IASTFileLocation is file and range of lineNos
			IASTFileLocation loc = decls[i].getFileLocation();
			String filename = loc.getFileName();
			System.out.println("PldtAstVisitor found filename " + filename);
			IPath path = new Path(filename);
			// is this path valid?
			System.out.println("PldtAstVisitor found path " + path);

			if (isInIncludePath(path))
				return true;
		}
		return false;
	}
	//called
	public void processMacroLiteral(IASTLiteralExpression expression) {
		IASTNodeLocation[] locations = expression.getNodeLocations();
		if ((locations.length == 1) && (locations[0] instanceof IASTMacroExpansion)) {//path taken &not
			// found a macro, does it come from MPI include?
			IASTMacroExpansion astMacroExpansion = (IASTMacroExpansion) locations[0];
			IASTPreprocessorMacroDefinition preprocessorMacroDefinition = astMacroExpansion
					.getMacroDefinition();
			// String shortName =
			// preprocessorMacroDefinition.getName().toString()+'='+literal;
			String shortName = preprocessorMacroDefinition.getName().toString();
			IASTNodeLocation[] preprocessorLocations = preprocessorMacroDefinition
					.getNodeLocations();
			while ((preprocessorLocations.length == 1)
					&& (preprocessorLocations[0] instanceof IASTMacroExpansion)) {
				preprocessorLocations = ((IASTMacroExpansion) preprocessorLocations[0])
						.getMacroDefinition().getNodeLocations();
			}

			if ((preprocessorLocations.length == 1)
					&& isInIncludePath(new Path(preprocessorLocations[0].asFileLocation()
							.getFileName()))) {
				SourceInfo sourceInfo = getSourceInfo(astMacroExpansion);
				if (sourceInfo != null) {
					scanReturn.addArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1, // column:
							shortName, ARTIFACT_CONSTANT, sourceInfo));
				}

			}
		}
	}// run from here gives J9 exception:  5/15/06 2:05 pm
//	Unhandled exception
//	Type=Segmentation error vmState=0x00000000
//	Target=2_30_20051027_03723_lHdSMR (Windows XP 5.1 build 2600 Service Pack 1)
//	CPU=x86 (1 logical CPUs) (0x3feec000 RAM)
//	J9Generic_Signal_Number=00000004 ExceptionCode=c0000005 ExceptionAddress=70A41318 ContextFlags=0001003f
//	Handler1=70C1FF10 Handler2=70B76A30 InaccessibleAddress=00000018
//	EDI=00000000 ESI=00000000 EAX=00000003 EBX=051DBFF0
//	ECX=06FC039A EDX=00000000
//	EIP=70A41318 ESP=0174FDD0 EBP=01CB16FC
//	Module=C:\java2-ibm-sdk-50\jre\bin\j9jit23.dll
//	Module_base_address=70840000 Offset_in_DLL=00201318
//	JVMDUMP006I Processing Dump Event "gpf", detail "" - Please Wait.
//	JVMDUMP007I JVM Requesting System Dump using 'C:\ecl\eclipse-3.2rc4\eclipse\core.20060515.140418.4084.dmp'
//	JVMDUMP010I System Dump written to C:\ecl\eclipse-3.2rc4\eclipse\core.20060515.140418.4084.dmp
//	JVMDUMP007I JVM Requesting Snap Dump using 'C:\ecl\eclipse-3.2rc4\eclipse\Snap0001.20060515.140418.4084.trc'
//	JVMDUMP010I Snap Dump written to C:\ecl\eclipse-3.2rc4\eclipse\Snap0001.20060515.140418.4084.trc
//	JVMDUMP007I JVM Requesting Java Dump using 'C:\ecl\eclipse-3.2rc4\eclipse\javacore.20060515.140418.4084.txt'
//	JVMDUMP010I Java Dump written to C:\ecl\eclipse-3.2rc4\eclipse\javacore.20060515.140418.4084.txt
//	JVMDUMP013I Processed Dump Event "gpf", detail "".

	/**
	 * Returns if the path is under any one of the artifact include paths from
	 * preference page.
	 * 
	 * @param includeFilePath
	 * @return
	 */
	/**
	 * @param includeFilePath
	 * @return
	 */
	/**
	 * @param includeFilePath
	 * @return
	 */
	private boolean isInIncludePath(IPath includeFilePath) {
		if (includeFilePath == null)
			return false;
		// java5
		// for (String includeDir : mpiIncludes_) {
		// IPath includePath = new Path(includeDir);
		// if (includePath.isPrefixOf(includeFilePath)) return true;
		// }
		for (Iterator it = includes_.iterator(); it.hasNext();) {
			String includeDir = (String) it.next();
			IPath includePath = new Path(includeDir);
			if (includePath.isPrefixOf(includeFilePath))
				return true;
		}
		return false;
	}

	/**
	 * Get exact source locational info for a function call
	 * 
	 * @param astExpr
	 * @param constructType
	 * @return
	 */
	private SourceInfo getSourceInfo(IASTExpression astExpr, int constructType) {
		SourceInfo sourceInfo = null;
		IASTNodeLocation[] locations = astExpr.getNodeLocations();
		if (locations.length == 1) {
			IASTFileLocation astFileLocation = null;
			if (locations[0] instanceof IASTFileLocation) {
				astFileLocation = (IASTFileLocation) locations[0];
				sourceInfo = new SourceInfo();
				sourceInfo.setStartingLine(astFileLocation.getStartingLineNumber());
				sourceInfo.setStart(astFileLocation.getNodeOffset());
				sourceInfo
						.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
				sourceInfo.setConstructType(constructType);
			}
		}
		return sourceInfo;
	}

	/**
	 * Get exact source locational info for a constant originated from Macro
	 * expansion(s)
	 * 
	 * @param iASTMacroExpansion
	 *            represents the original macro
	 * @return
	 */
	private SourceInfo getSourceInfo(IASTMacroExpansion iASTMacroExpansion) {
		SourceInfo sourceInfo = null;
		IASTFileLocation iASTFileLocation = iASTMacroExpansion.asFileLocation();
		sourceInfo = new SourceInfo();
		sourceInfo.setStartingLine(iASTFileLocation.getStartingLineNumber());
		sourceInfo.setStart(iASTFileLocation.getNodeOffset());
		sourceInfo.setEnd(iASTFileLocation.getNodeOffset() + iASTFileLocation.getNodeLength());
		sourceInfo.setConstructType(Artifact.CONSTANT);

		return sourceInfo;
	}
	//called
	private boolean preprocessorIncluded(IASTNode astNode) {
		if (astNode.getFileLocation() == null)
			return false;

		String location = astNode.getFileLocation().getFileName();
		String tuFilePath = astNode.getTranslationUnit().getFilePath();
		// System.err.println("node="+astNode.getRawSignature());
		// System.err.println("location="+location);
		// System.err.println("tu="+tuFilePath);
		return !location.equals(tuFilePath);
	}

	public void processIdExprAsLiteral(IASTIdExpression expression) {

		// when does this get called?
		IASTName name = expression.getName();

		if (isArtifact(name)) {
			SourceInfo sourceInfo = getSourceInfo(expression, Artifact.CONSTANT);
			if (sourceInfo != null) {
				scanReturn.addArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1, // column:

						name.toString(), ARTIFACT_CONSTANT, sourceInfo));
			}
		}
	}

}