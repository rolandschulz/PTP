/**********************************************************************
 * Copyright (c) 2007,2008 IBM Corporation.
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
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;

/**
 * The resource collector collects all functions for the call graph, but does not
 * calculate the caller and callee relations.
 * 
 * @author Yuan Zhang, Beth Tibbitts
 *
 */
public class ResourceCollector extends ASTVisitor {
	protected ICallGraph CG_; 
	protected IFile file_;
	protected int depth;
	private static final boolean traceOn=false;
	
	/**
	 * Resource collector finds all functions in a given source file,
	 * and adds them to the given call graph
	 * @param cg call graph that will have this information added to it
	 * @param file source file whose functions will be discovered and catalogued
	 */
	public ResourceCollector(ICallGraph cg, IFile file){
		CG_ = cg;
		file_ = file;
	}
	
	/**
	 * Use an ASTVisitor to discover the functions in the the given source and add them to the call graph
	 */
	public void run(){
		this.shouldVisitDeclarations = true;
		this.shouldVisitTranslationUnit = true;
		if(traceOn)System.out.println("ResourceCollector.run()  file: "+file_+"  exists? "+file_.exists()); //$NON-NLS-1$ //$NON-NLS-2$
		IASTTranslationUnit ast_ = null;
		try {
            ast_ = CDOM.getInstance().getASTService().getTranslationUnit(file_,
                    CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES));
        } catch (IASTServiceProvider.UnsupportedDialectException e) {
        } catch (NullPointerException npe) {
        	System.out.println("ResourceCollector: no ast available from CDOM.. remote project? will try alt. approach.");
        }
        if(traceOn)System.out.println("     initial ast construction: ast_="+ast_); //$NON-NLS-1$
        boolean temp=true;
        
        if(temp && ast_==null) {
        	// newer way to get ast
        	if(file_ instanceof IAdaptable) {
        		ICElement ce = (ICElement) file_.getAdapter(ICElement.class);
        		if(traceOn)System.out.println("     ICElement: ="+ce); //$NON-NLS-1$
        		if(ce instanceof ITranslationUnit) {
        			ITranslationUnit cetu=(ITranslationUnit)ce;
        			IASTTranslationUnit ast;
					try {
						ast = cetu.getAST();
						if(traceOn)System.out.println("ast: "+ast); //$NON-NLS-1$
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        			
        		}
        		ITranslationUnit tu=(ITranslationUnit)file_.getAdapter(ITranslationUnit.class);
        		try {
					ast_=tu.getAST();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        if(traceOn)System.out.println("     ast_="+ast_); //$NON-NLS-1$
        depth = 0;
        ast_.accept(this);
	}
	
	/**
	 * visits declaration nodes to catalog the function declarations
	 */
	public int visit(IASTDeclaration declaration) {
		String filename = declaration.getContainingFilename();
		// if(!filename.endsWith(".c") || !filename.endsWith(".C"))
		if (filename.endsWith(".h")) //$NON-NLS-1$
			return PROCESS_SKIP;

		if (declaration instanceof IASTFunctionDefinition) {
			depth++;
			IASTFunctionDefinition fd = (IASTFunctionDefinition) declaration;
			ICallGraphNode node = addCallGraphNode(file_, filename, fd);
			CG_.addNode(node);
			return PROCESS_SKIP;
		} else if (declaration instanceof IASTSimpleDeclaration) {
			if (depth > 0)
				return PROCESS_SKIP; // not global
			IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) declaration;
			/* if the declarator is null, then it is a structure specifier */
			if (sdecl.getDeclarators() == null)
				return PROCESS_CONTINUE;
			IASTDeclSpecifier spec = sdecl.getDeclSpecifier();
			if (spec instanceof IASTCompositeTypeSpecifier
					|| spec instanceof IASTElaboratedTypeSpecifier
					|| spec instanceof IASTEnumerationSpecifier)
				return PROCESS_SKIP;

			List<String> env = CG_.getEnv();
			IASTDeclarator[] declarators = sdecl.getDeclarators();
			for (int j = 0; j < declarators.length; j++) {
				if (declarators[j] instanceof IASTFunctionDeclarator)
					continue;
				IASTName n = declarators[j].getName();
				String var = n.toString();
				if (doQuickOptionalTest(var))
					continue;
				if (!env.contains(var))
					env.add(var);
				doOtherDeClaratorStuff(declarators[j]);
			}
		}
		return PROCESS_CONTINUE;
	}
	

	public int leave(IASTDeclaration declaration) 
	{
		String filename = declaration.getContainingFilename();
		//if(!filename.endsWith(".c") || !filename.endsWith(".C"))
		if(filename.endsWith(".h")) //$NON-NLS-1$
			return PROCESS_SKIP;
		
		if (declaration instanceof IASTFunctionDefinition) {
			depth --;
			return PROCESS_SKIP;
		}
		return PROCESS_CONTINUE;
	}
	/**
	 * Can be overridden by subclasses to create a specific kind of call graph
	 * node if required
	 * 
	 * @return call graph node created
	 */
	protected ICallGraphNode addCallGraphNode(IFile file, String filename,
			IASTFunctionDefinition fd) {
		ICallGraphNode cgnode = new CallGraphNode(file, filename, fd);
		return cgnode;
	}
	/** 
     * extra optional test that derived class can do
     */
	protected boolean doQuickOptionalTest(String var){
	 return true;
	 }
	 
	/**
	 * optional stuff that derived class may want to do at this point
	 * @param declarator
	 */
	 protected void doOtherDeClaratorStuff(IASTDeclarator declarator){}
}
