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

import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;

public class MPICFGBuilder {
	protected ICallGraph cg_;

	public MPICFGBuilder(ICallGraph cg) {
		cg_ = cg;
	}

	public void run() {
		for (ICallGraphNode n = cg_.topEntry(); n != null; n = n.topNext()) {
			MPICallGraphNode node = (MPICallGraphNode) n;
			if (!node.marked)
				continue;
			// if(node.getFuncName().equals("yyparse"))
			// System.out.println(node.getFuncName());
			MPIControlFlowGraph cfg = new MPIControlFlowGraph(node.getFuncDef().getBody());
			cfg.buildCFG();
			n.setCFG(cfg);
			// cfg.print();
		}
		MPIBlock.clean();
	}
}
