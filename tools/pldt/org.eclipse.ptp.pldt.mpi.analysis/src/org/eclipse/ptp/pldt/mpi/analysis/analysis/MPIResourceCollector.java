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

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.core.resources.IFile;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.ResourceCollector;

/**
 * Resource collection functions specific for MPI analysis
 * 
 * @author Yuan Zhang, Beth Tibbitts
 * 
 */
public class MPIResourceCollector extends ResourceCollector {

	public MPIResourceCollector(ICallGraph cg, IFile file) {
		super(cg, file);
	}

	/**
	 * Create a specific kind of call graph node , the MPICallGraphNode
	 * 
	 * @return call graph node created
	 */
	protected ICallGraphNode addCallGraphNode(IFile file, String filename,
			IASTFunctionDefinition fd) {
		ICallGraphNode cgnode = new MPICallGraphNode(file, filename, fd);
		return cgnode;
	}

	/**
	 * extra optional test that derived class can do
	 */
	protected boolean doQuickOptionalTest(String var) {
		boolean result = var.startsWith("MPI_") || var.startsWith("PMPI_") || //$NON-NLS-1$ //$NON-NLS-2$
				var.startsWith("MPIO_") || var.startsWith("PMPIO_"); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}

	protected void doOtherDeclaratorStuff(IASTDeclarator declarator) {
		IASTPointerOperator[] pops = declarator.getPointerOperators();
		if (pops != IASTPointerOperator.EMPTY_ARRAY)
			((MPICallGraph) CG_).getGVPointer().add(new Boolean(true));
		else
			((MPICallGraph) CG_).getGVPointer().add(new Boolean(false));

	}
}
