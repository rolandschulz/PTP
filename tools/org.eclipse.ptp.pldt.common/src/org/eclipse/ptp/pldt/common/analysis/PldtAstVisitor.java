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
 * This dom-walker helper collects interesting constructs (currently
 * function calls and constants), and adds markers to the source file for
 * C/C++ code. <br>
 * This base class encapsulates the common behaviors for both C and C++
 * code.
 * 
 * @author Beth Tibbitts
 * 
 */
public class PldtAstVisitor extends CASTVisitor {


	protected static String ARTIFACT_CALL = "Artifact Call";
	protected static String ARTIFACT_CONSTANT = "Artifact Constant";
	protected static String PREFIX = "";
	private static final boolean traceOn=false;

	/**
	 * List of include paths that we'll probably want to consider in the work that this visitor does.
	 * For example, only paths found in this list (specified in PLDT preferences) would be considered
	 * to be a path from which definitions of "Artifacts" would be found.
	 */
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
	public int visit(IASTStatement statement) { 
		if (preprocessorIncluded(statement)) {
			return ASTVisitor.PROCESS_SKIP;
		}
		return ASTVisitor.PROCESS_CONTINUE; 
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
	 * An artifact is a function name that was found in the MPI include path,
	 * as defined in the MPI preferences.
	 * 
	 * @param astExpr
	 * @param funcName
	 */
	public void processFuncName(IASTName funcName, IASTExpression astExpr) {
		IASTTranslationUnit tu = funcName.getTranslationUnit();

		if (isArtifact(funcName)) {
			SourceInfo sourceInfo = getSourceInfo(astExpr, Artifact.FUNCTION_CALL);
			if (sourceInfo != null) {
				if(traceOn) System.out.println("found MPI artifact: " + funcName.toString());
				String artName=funcName.toString();
				String rawName=funcName.getRawSignature();
				String bName=funcName.getBinding().getName();
				if(!artName.equals(rawName)) {
					if(rawName.length()==0)rawName="  ";
					artName=artName+"  ("+rawName+")"; // indicate orig pre-pre-processor value in parens
					// note: currently rawName seems to always be empty.
				}
				scanReturn.addArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1,
						artName, ARTIFACT_CALL, sourceInfo));

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
	 * An artifact is a function name that was found in the  include path (e.g. MPI or OpenMP),
	 * as defined in the PLDT preferences.
	 * 
	 * @param funcName
	 */
	protected boolean isArtifact(IASTName funcName) {
		String funcNameString=funcName.toString();
		IBinding binding = funcName.resolveBinding();
		String name=binding.getName(); 
		String rawSig=funcName.getRawSignature();
		if(name.length()==0) {
			name=funcName.getRawSignature();
		}

		IASTName[] decls = funcName.getTranslationUnit().getDeclarations(binding);//empty for C++

		for (int i = 0; i < decls.length; ++i) {
			// IASTFileLocation is file and range of lineNos
			IASTFileLocation loc = decls[i].getFileLocation();
			String filename = loc.getFileName();
			if(traceOn)System.out.println("PldtAstVisitor found filename " + filename);
			IPath path = new Path(filename);
			// is this path valid?
			if(traceOn)System.out.println("PldtAstVisitor found path " + path);

			if (isInIncludePath(path))
				return true;
		}
		return false;
	}
 
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
	}

	/**
	 * Is this path found in the include path in which we are interested?
	 * E.g. is it in the MPI include path specified in PLDT preferences,
	 * which would identify it as an MPI artifact of interest.
	 * 
	 * @param includeFilePath under consideration
	 * @return true if this is found in the include path from PLDT preferences
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
			}
			// handle the case e.g. #define foo MPI_fn - recognize foo() as MPI_fn()
			else if (locations[0] instanceof IASTMacroExpansion) {
				IASTMacroExpansion me=(IASTMacroExpansion)locations[0];
				astFileLocation=me.asFileLocation();
			}
			if(astFileLocation!=null) {
				String tmp=astFileLocation.toString();
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