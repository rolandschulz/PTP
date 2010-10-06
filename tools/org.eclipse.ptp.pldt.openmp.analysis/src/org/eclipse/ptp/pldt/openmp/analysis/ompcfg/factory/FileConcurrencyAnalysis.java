/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;


/**
 * Build an analysis for a given file
 * @author pazel
 *
 */
public class FileConcurrencyAnalysis
{
    protected IASTTranslationUnit         astTransUnit_ = null;
    protected IFile                       iFile_        = null;
    protected PASTOMPPragma     []        pragmas_      = null;
    protected FunctionConcurrencyAnalysis   [] analyses_     = null;
    
    protected static final boolean traceOn = false;
    
    /**
     * FileConcurrencyAnalysis - Holds analysis for an entire file
     * @param astTransUnit
     * @param iFile
     * @param ompPragmas
     */
    public FileConcurrencyAnalysis(IASTTranslationUnit astTransUnit, 
                                   IFile               iFile,
                                   PASTOMPPragma []    ompPragmas)
    {
        astTransUnit_   = astTransUnit;
        iFile_          = iFile;
        pragmas_        = ompPragmas;
        buildFileAnalysis();
    }
    
    /**
     * getNodesConcurrentTo - get all nodes concurrent to given node
     * @param node - IASTNode
     * @return Set
     */
    public Set getNodesConcurrentTo(IASTNode node)
    {
        // designed so we use the 1st non-null. 
        for(int i=0; i<analyses_.length; i++) {
            Set s = analyses_[i].getNodesConcurrentTo(node);
            if (s!=null)  return s;
        }
        return new HashSet();
    }

    
    /**
     * buildFileAnalysis - concurrency analysis
     *
     */
    protected void buildFileAnalysis()
    {
        DefVisitor dv = new DefVisitor(astTransUnit_, iFile_, pragmas_);
        analyses_ = dv.buildAnalyses();
        
        // build the phase analysis
        for(int i=0; i<analyses_.length; i++) {
            FunctionConcurrencyAnalysis oca = analyses_[i];
            oca.doPhaseAnalysis();
            if (traceOn) oca.printAnalysis(System.out);
        }
    }

    //-------------------------------------------------------------------------
    // Class: DefVisitor - used to locate functions within file, for 
    //        concurrency analysis
    //-------------------------------------------------------------------------
    private static class DefVisitor extends ASTVisitor
    {
        private IASTTranslationUnit     transUnit_    = null;
        private IFile                   iFile_        = null;
        private PASTOMPPragma       []  pList_        = null;
        private LinkedList              analysisList_ = new LinkedList();
        
        public DefVisitor(IASTTranslationUnit transUnit, IFile iFile, PASTOMPPragma [] pList)
        { transUnit_ = transUnit; pList_ = pList; iFile_=iFile; }
        
        /**
         * buildAnalyses - build one analysis object per function found in file
         * @return
         */
        public FunctionConcurrencyAnalysis [] buildAnalyses()
        {
            shouldVisitDeclarations = true;
            transUnit_.accept(this);
            
            FunctionConcurrencyAnalysis [] list = new FunctionConcurrencyAnalysis[analysisList_.size()];
            int count = 0;
            for(Iterator i=analysisList_.iterator(); i.hasNext();)
                list[count++] = (FunctionConcurrencyAnalysis)i.next();
            return list;
        }
        
        /**
         * visit - ASTVisitor method capture to feed information into analysis
         * @param declaration - IASTDeclaration
         * @return int
         */
        public int visit(IASTDeclaration declaration) 
        {
            if (declaration instanceof IASTFunctionDefinition) {
                IASTFunctionDefinition fDef = (IASTFunctionDefinition)declaration;
                
                // If the file the functionDef is in is the same as THIS file, add it to our list.
                // Note: 1 Sept 2010: this doesn't match even for local files now, so ... nothing is added??
                URI uri=iFile_.getLocationURI();
                String str2=uri.getPath();
                            
                // str1 will be abs path to the file on whatever file system it's on (can't tell local vs. remote)
                String str1 = fDef.getContainingFilename();
                // Note: this is a best-guess if they match. Files on different systems with exactly the same path string will "match" when 
                // in reality they probably shouldn't.              
                if (str1.equals(str2)) {
                    FunctionConcurrencyAnalysis analysis = new FunctionConcurrencyAnalysis(fDef, pList_);
                    analysisList_.add(analysis);
                }
            }
            return PROCESS_CONTINUE;
        }

    }


}
