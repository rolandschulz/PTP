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
import org.eclipse.cdt.internal.ui.search.CSearchMatch;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.DOMDisplaySearchNames;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.search.ui.text.Match;

/**
 * This dom-walker helper collects interested constructs (currently function calls and constants), and add markers to
 * the source file for C/C++ code. It encapsulates the common behaviors for both C and C++ code.
 * 
 */
public class GeneralASTVisitorBehavior
{
    private static final String MPI_CALL     = "MPI call";
    private static final String MPI_CONSTANT = "MPI constant";
    /**
     * 
     */
    private final List          includes_;
    private final String        fileName;
    private final ScanReturn scanReturn;
    private static final String STRING_QUOTE = "\"";

    public GeneralASTVisitorBehavior(List includes, String fileName, ScanReturn scanReturn)
    {
        this.includes_ = includes;
        this.fileName = fileName;
        this.scanReturn = scanReturn;
    }

    /**
     * Skip statements that are included.
     */
    public int visit(IASTStatement statement)
    {
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
    public int visit(IASTDeclaration declaration)
    {
        if (preprocessorIncluded(declaration)) {
            return ASTVisitor.PROCESS_SKIP;
        }
        return ASTVisitor.PROCESS_CONTINUE;
    }

    public void processFuncName(IASTName funcName, IASTExpression astExpr)
    {
        DeclarationFinder finder = searchDeclaration(funcName);
        // check if finder is null, this is the case if the indexer has never been turned on.
        // Thus, declaration cannot be found for the funcName
        if (finder != null) {
            CSearchResult result = finder.getResult();
            if (isMPICall(result)) {
                // CSearchMatch match =
                // (CSearchMatch)result.getMatches(result.getElements()[0])[0];
                // System.out.println("start="+match.getOffset());
                // System.out.println("length="+match.getLength());
                SourceInfo sourceInfo = getSourceInfo(astExpr, Artifact.FUNCTION_CALL);
                if (sourceInfo != null) {
                    scanReturn.addMpiArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1, // column:
                            // unused
                            // in
                            // MpiArtifact
                            funcName.toString(), MPI_CALL, sourceInfo));
                }
            }
        }
    }

    public void processMacroLiteral(IASTLiteralExpression expression)
    {
        IASTNodeLocation[] locations = expression.getNodeLocations();
        if ((locations.length == 1) && (locations[0] instanceof IASTMacroExpansion)) {
            // found a macro, does it come from MPI include?
            IASTMacroExpansion astMacroExpansion = (IASTMacroExpansion) locations[0];
            IASTPreprocessorMacroDefinition preprocessorMacroDefinition = astMacroExpansion.getMacroDefinition();
            // String shortName = preprocessorMacroDefinition.getName().toString()+'='+literal;
            String shortName = preprocessorMacroDefinition.getName().toString();
            IASTNodeLocation[] preprocessorLocations = preprocessorMacroDefinition.getNodeLocations();
            while ((preprocessorLocations.length == 1) && (preprocessorLocations[0] instanceof IASTMacroExpansion)) {
                preprocessorLocations = ((IASTMacroExpansion) preprocessorLocations[0]).getMacroDefinition()
                        .getNodeLocations();
            }

            if ((preprocessorLocations.length == 1)
                    && isMPIInclude(new Path(preprocessorLocations[0].asFileLocation().getFileName()))) {
                SourceInfo sourceInfo = getSourceInfo(astMacroExpansion);
                if (sourceInfo != null) {
                    scanReturn.addMpiArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1, // column:
                            // unused
                            // in
                            // MpiArtifact
                            shortName, MPI_CONSTANT, sourceInfo));
                }

            }
        }
    }

    /**
     * If a search result's location is under one of the MPI include paths from preference page, it is considered an MPI
     * call.
     * 
     * @param searchResult
     * @return
     */
    private boolean isMPICall(CSearchResult searchResult)
    {
        if ((searchResult != null) && (searchResult.getMatchCount() > 0)) {
            // loop thru the matches
            Match[] matches = searchResult.getMatches(searchResult.getElements()[0]);
            for (int i = 0; i < matches.length; i++) {
                // expecting be CSearchMatch
                CSearchMatch cMatch = (CSearchMatch) matches[i];
                // System.out.println(cMatch.getSearchMatch().getLocation());
                IPath includeFilePath = cMatch.getSearchMatch().getLocation();
                if (isMPIInclude(includeFilePath)) return true;
            }
        }
        return false;
    }

    /**
     * Returns if the path is under any one of the MPI include paths from preference page.
     * 
     * @param includeFilePath
     * @return
     */
    private boolean isMPIInclude(IPath includeFilePath)
    {

        // java5
        // for (String includeDir : mpiIncludes_) {
        // IPath includePath = new Path(includeDir);
        // if (includePath.isPrefixOf(includeFilePath)) return true;
        // }
        for (Iterator it = includes_.iterator(); it.hasNext();) {
            String includeDir = (String) it.next();
            IPath includePath = new Path(includeDir);
            if (includePath.isPrefixOf(includeFilePath)) return true;
        }
        return false;
    }

    private DeclarationFinder searchDeclaration(IASTName funcName)
    {
        DeclarationFinder findDeclaration = null;
        StringBuffer pattern = new StringBuffer(STRING_QUOTE);
        if (funcName.toString() != null) pattern.append(funcName.toString());
        pattern.append(STRING_QUOTE);
        IASTName[] names = funcName.getTranslationUnit().getDeclarations(funcName.resolveBinding());
        if (names.length > 0) {
            DOMDisplaySearchNames query = new DOMDisplaySearchNames(names, "Find Declarations", pattern.toString());
            findDeclaration = new DeclarationFinder(query);
            // NewSearchUI.activateSearchResultView();
            CommonPlugin.getStandardDisplay().syncExec(findDeclaration);
        }
        return findDeclaration;
    }

    /**
     * Get exact source locational info for a function call
     * 
     * @param astExpr
     * @param constructType
     * @return
     */
    private SourceInfo getSourceInfo(IASTExpression astExpr, int constructType)
    {
        SourceInfo sourceInfo = null;
        IASTNodeLocation[] locations = astExpr.getNodeLocations();
        if (locations.length == 1) {
            IASTFileLocation astFileLocation = null;
            if (locations[0] instanceof IASTFileLocation) {
                astFileLocation = (IASTFileLocation) locations[0];
                sourceInfo = new SourceInfo();
                sourceInfo.setStartingLine(astFileLocation.getStartingLineNumber());
                sourceInfo.setStart(astFileLocation.getNodeOffset());
                sourceInfo.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
                sourceInfo.setConstructType(constructType);
            }
        }
        return sourceInfo;
    }

    /**
     * Get exact source locational info for a constant originated from Macro expansion(s)
     * 
     * @param iASTMacroExpansion represents the original macro
     * @return
     */
    private SourceInfo getSourceInfo(IASTMacroExpansion iASTMacroExpansion)
    {
        SourceInfo sourceInfo = null;
        IASTFileLocation iASTFileLocation = iASTMacroExpansion.asFileLocation();
        sourceInfo = new SourceInfo();
        sourceInfo.setStartingLine(iASTFileLocation.getStartingLineNumber());
        sourceInfo.setStart(iASTFileLocation.getNodeOffset());
        sourceInfo.setEnd(iASTFileLocation.getNodeOffset() + iASTFileLocation.getNodeLength());
        sourceInfo.setConstructType(Artifact.CONSTANT);

        return sourceInfo;
    }

    private boolean preprocessorIncluded(IASTNode astNode)
    {
        if(astNode.getFileLocation() == null)
            return false;
        
        String location = astNode.getFileLocation().getFileName();
        String tuFilePath = astNode.getTranslationUnit().getFilePath();
        // System.err.println("node="+astNode.getRawSignature());
        // System.err.println("location="+location);
        // System.err.println("tu="+tuFilePath);
        return !location.equals(tuFilePath);
    }

    public void processIdExprAsLiteral(IASTIdExpression expression)
    {
        IASTName name = expression.getName();
        DeclarationFinder finder = searchDeclaration(name);
        // check if finder is null, this is the case if the indexer has never been turned on.
        // Thus, declaration cannot be found for the funcName
        if (finder != null) {
            CSearchResult result = finder.getResult();
            if (isMPICall(result)) {
                // CSearchMatch match =
                // (CSearchMatch)result.getMatches(result.getElements()[0])[0];
                // System.out.println("start="+match.getOffset());
                // System.out.println("length="+match.getLength());
                SourceInfo sourceInfo = getSourceInfo(expression, Artifact.CONSTANT);
                if (sourceInfo != null) {
                    scanReturn.addMpiArtifact(new Artifact(fileName, sourceInfo.getStartingLine(), 1, // column:
                            // unused
                            // in
                            // MpiArtifact
                            name.toString(), MPI_CONSTANT, sourceInfo));
                }
            }
        }
    }
}