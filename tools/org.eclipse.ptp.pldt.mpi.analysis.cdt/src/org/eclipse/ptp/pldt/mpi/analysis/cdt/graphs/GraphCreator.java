/**********************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.CallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.ResourceCollector;

/**
 * Convenience class for constructing various graphs from C source files
 * 
 * @author Beth Tibbitts
 * 
 */
public class GraphCreator {
	/**
	 * Convenience method for initializing and computing the call graph in one place. <br>
	 * This is done in two steps: <br>
	 * (1) initCallGraph(): initialize call graph with function information <br>
	 * (2) computeCallGraph(): compute caller/callee/recursive etc. info on the graph
	 * 
	 * @param resource
	 * @return
	 */
	public ICallGraph createCallGraph(IResource resource) {
		ICallGraph cg = initCallGraph(resource);
		computeCallGraph(cg);
		return cg;

	}

	/**
	 * Create call graph structure from resources (C source files) but caller/callee
	 * calculations are not done yet; will descend to children of a container if
	 * called with a folder or project argument.
	 * 
	 * @param resource
	 * @return call graph initialized with basic function information
	 */
	public ICallGraph initCallGraph(IResource resource) {
		ICallGraph callGraph = new CallGraph();
		callGraph = initCallGraph(resource, callGraph);
		return callGraph;

	}

	/**
	 * Add information to an existing call graph. <br>
	 * Will descend to children if this is a container (folder or project)
	 * 
	 * @param resource
	 *            contains source file(s) whose functions will be found and added to the call graph.
	 * @param callGraph
	 * @return
	 */
	public ICallGraph initCallGraph(IResource resource, ICallGraph callGraph) {
		boolean foundError = resourceCollector(resource, callGraph);
		if (foundError) {
			System.out.println("Error occurred during call graph creation."); //$NON-NLS-1$
		}
		return callGraph;
	}

	/**
	 * Create an empty call graph, ready to fill with function information
	 * later with subsequent calls to initCallGraph(resource, callGraph)
	 * 
	 * @return
	 */
	public ICallGraph initCallGraph() {
		return new CallGraph();
	}

	/**
	 * Calculate the caller/callee and recursive properties of the call graph
	 * 
	 * @return
	 */
	public ICallGraph computeCallGraph(ICallGraph callGraph) {
		callGraph.buildCG();
		return callGraph;
	}

	/**
	 * Run analysis ("Resource collector") on a resource (e.g. File or Folder)
	 * and add the function information found in/under the given resource (file or container)
	 * to the given call graph <br>
	 * Will descend to members of folder
	 * 
	 * @param resource
	 *            the resource selected by the user
	 * @param callGraph
	 *            the call graph to which the information will be appended
	 * @return
	 */
	public boolean resourceCollector(IResource resource, ICallGraph callGraph) {

		boolean foundError = false;

		// if it's a C file, collect info in the call graph for this file
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			String filename = file.getName();
			if (filename.endsWith(".c")) { //$NON-NLS-1$
				ResourceCollector rc = new ResourceCollector(callGraph, file);
				rc.run();
			}
			// if it's a container, run resourceCollector on each of its members
		} else if (resource instanceof IContainer) {
			IContainer container = (IContainer) resource;
			try {
				IResource[] mems = container.members();
				for (int i = 0; i < mems.length; i++) {
					boolean err = resourceCollector(mems[i], callGraph);
					foundError = foundError || err;
				}
			} catch (CoreException e) {
				e.printStackTrace();
				foundError = true;
			}
		} else {
			// ?????
			String name = ""; //$NON-NLS-1$
			if (resource instanceof IResource) {
				IResource res = (IResource) resource;
				// name=res.getName(); // simple filename only, no path info
				IPath path = res.getProjectRelativePath();
				name = path.toString();
			}
			System.out.println("Cancelled by User, aborting analysis on subsequent files... " //$NON-NLS-1$
					+ name);
		}

		return foundError;
	}

	/**
	 * Print a call graph structure
	 * 
	 * @param cg
	 *            the call graph to print
	 */
	public void showCallGraph(ICallGraph cg) {
		System.out.println("Show call graph"); //$NON-NLS-1$
		List<ICallGraphNode> nodes = cg.getAllNodes();
		for (Iterator<ICallGraphNode> iterator = nodes.iterator(); iterator.hasNext();) {
			ICallGraphNode cgNode = iterator.next();
			printCGNode(cgNode, ""); //$NON-NLS-1$
			// System.out.println("  callers: ==>");

			for (Iterator<ICallGraphNode> iterator2 = cgNode.getCallers().iterator(); iterator2.hasNext();) {
				ICallGraphNode caller = iterator2.next();
				printCGNode(caller, "    caller: "); //$NON-NLS-1$
			}
			// System.out.println("  <== callees:");
			for (Iterator<ICallGraphNode> iterator3 = cgNode.getCallees().iterator(); iterator3.hasNext();) {
				ICallGraphNode callee = iterator3.next();
				printCGNode(callee, "    callee: "); //$NON-NLS-1$

			}
			System.out.println(" "); //$NON-NLS-1$

		}
		List<List<ICallGraphNode>> cycles = cg.getCycles();
		System.out.println("Recursive cycles:"); //$NON-NLS-1$
		for (List<ICallGraphNode> cycle : cycles) {
			System.out.println("Cycle: "); //$NON-NLS-1$
			for (Iterator<ICallGraphNode> iterator = cycle.iterator(); iterator.hasNext();) {
				ICallGraphNode fn = iterator.next();
				System.out.print(" " + fn.getFuncName()); //$NON-NLS-1$

			}
			System.out.println(" \n"); //$NON-NLS-1$
		}
		List<String> vars = cg.getEnv();
		System.out.println("Global variables:"); //$NON-NLS-1$
		for (Iterator<String> varit = vars.iterator(); varit.hasNext();) {
			String var = varit.next();
			System.out.println("Global var: " + var); //$NON-NLS-1$

		}

	}

	public void printCGNode(ICallGraphNode cgNode, String prefix) {
		System.out.println(prefix + " " + cgNode.getFuncName() + " in " + cgNode.getFileName()); //$NON-NLS-1$ //$NON-NLS-2$
		cgNode.getCFG();
	}
}
