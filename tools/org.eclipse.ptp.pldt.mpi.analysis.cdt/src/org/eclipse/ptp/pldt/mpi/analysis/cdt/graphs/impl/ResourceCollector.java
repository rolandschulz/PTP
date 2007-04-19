/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl;

import java.util.List;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;

/**
 * The resource collector collects all functions, but does not
 * calculate the caller and callee relations.
 * 
 * @author Yuan Zhang
 *
 */
public class ResourceCollector extends ASTVisitor {
	protected ICallGraph CG_; 
	protected IFile file_;
	protected int depth;
	
	public ResourceCollector(ICallGraph cg, IFile file){
		CG_ = cg;
		file_ = file;
	}
	
	public void run(){
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
		IASTTranslationUnit ast_ = null;
		try {
            ast_ = CDOM.getInstance().getASTService().getTranslationUnit(file_,
                    CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES));
        } catch (IASTServiceProvider.UnsupportedDialectException e) {
        }
        depth = 0;
        ast_.accept(this);
	}
	
	public int visit(IASTDeclaration declaration) 
	{
		String filename = declaration.getContainingFilename();
		//if(!filename.endsWith(".c") || !filename.endsWith(".C"))
		if(filename.endsWith(".h"))
			return PROCESS_SKIP;
		
		if (declaration instanceof IASTFunctionDefinition) {
			depth ++;
			IASTFunctionDefinition fd = (IASTFunctionDefinition)declaration;
			ICallGraphNode node = new CallGraphNode(file_, filename, fd);
			CG_.addNode(node);
			return PROCESS_SKIP;
		}
		else if (declaration instanceof IASTSimpleDeclaration){
			if(depth > 0) return PROCESS_SKIP; //not global
			IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration)declaration;
			/* if the declarator is null, then it is a structure specifier*/
			if(sdecl.getDeclarators() == null) return PROCESS_CONTINUE;
			IASTDeclSpecifier spec = sdecl.getDeclSpecifier();
			if(spec instanceof IASTCompositeTypeSpecifier ||
					spec instanceof IASTElaboratedTypeSpecifier ||
					spec instanceof IASTEnumerationSpecifier)
				return PROCESS_SKIP;
			
			List<String> env = CG_.getEnv();
			IASTDeclarator[] declarators = sdecl.getDeclarators();
			for(int j=0; j<declarators.length; j++){
				if(declarators[j] instanceof IASTFunctionDeclarator)
					continue;
				IASTName n = declarators[j].getName();
				String var = n.toString();
				if(!env.contains(var)) 
					env.add(var);
			}
		}
		return PROCESS_CONTINUE;
	}
	
	public int leave(IASTDeclaration declaration) 
	{
		String filename = declaration.getContainingFilename();
		//if(!filename.endsWith(".c") || !filename.endsWith(".C"))
		if(filename.endsWith(".h"))
			return PROCESS_SKIP;
		
		if (declaration instanceof IASTFunctionDefinition) {
			depth --;
			return PROCESS_SKIP;
		}
		return PROCESS_CONTINUE;
	}
}
