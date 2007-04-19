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

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.core.resources.IFile;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.ResourceCollector;

public class MPIResourceCollector extends ResourceCollector{

	public MPIResourceCollector(ICallGraph cg, IFile file){
		super(cg, file);
	}
	
	public int visit(IASTDeclaration declaration) 
	{
		String filename = declaration.getContainingFilename();
		//if(!filename.endsWith(".c") || !filename.endsWith(".C"))
		if(filename.endsWith(".h"))
			return PROCESS_SKIP;
		
		/*
		if(filename.endsWith("misc.c"))
			System.out.println("stop here");
		*/
		if (declaration instanceof IASTFunctionDefinition) {
			depth ++;
			IASTFunctionDefinition fd = (IASTFunctionDefinition)declaration;
			ICallGraphNode node = new MPICallGraphNode(file_, filename, fd);
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
				if(var.startsWith("MPI_") || var.startsWith("PMPI_") ||
						var.startsWith("MPIO_") || var.startsWith("PMPIO_"))
					continue;
				if(!env.contains(var)){
					env.add(var);
					IASTPointerOperator[] pops = declarators[j].getPointerOperators();
					if(pops != IASTPointerOperator.EMPTY_ARRAY)
						((MPICallGraph)CG_).getGVPointer().add(new Boolean(true));
					else
						((MPICallGraph)CG_).getGVPointer().add(new Boolean(false));
				}
			}
		}
		return PROCESS_CONTINUE;
	}
}
