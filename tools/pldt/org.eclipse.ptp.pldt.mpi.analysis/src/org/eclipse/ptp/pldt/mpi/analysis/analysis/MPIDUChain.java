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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IControlFlowGraph;

/**
 * Calculate the DUchain -- data dependence
 * @author Yuan Zhang
 *
 */
public class MPIDUChain{
	protected ICallGraph cg_;
	
	protected Hashtable<String,List<IBlock>> defTable_;
	protected IControlFlowGraph cfg_;
	
	public MPIDUChain(ICallGraph cg){
		cg_ = cg;
	}
	
	/**
	 * The DUchain calculation contains three steps:<br>
	 * (1) Calculate the set of used and defined variables in each block<br>
	 * (2) Add \phi functions<br>
	 * (3) Construct the DUchain
	 */
	public void run(){
		UseDefBuilder ud = new UseDefBuilder(cg_);
		ud.run();
		MPISSA ssa = new MPISSA(cg_);
		ssa.run();
		DUChain();
	}

	/** Calculate the Gen and Kill sets for each block */
	protected void GenKillSet(){
		for(IBlock b = cfg_.getEntry(); b != null; b = b.topNext()){
			MPIBlock block = (MPIBlock)b;
			for(Enumeration<String> e = defTable_.keys(); e.hasMoreElements();){
				String var = e.nextElement();
				block.getGen().put(var, new ArrayList<IBlock>());
				block.getKill().put(var, new ArrayList<IBlock>());
				block.getIn().put(var, new ArrayList<IBlock>());
				block.getOut().put(var, new ArrayList<IBlock>());
				block.getDUPred().put(var, new ArrayList<IBlock>());
				block.getDUSucc().put(var, new ArrayList<IBlock>());
			}
			List<String> def = block.getDef();
			for(Iterator<String> i = def.iterator(); i.hasNext();){
				String var = i.next();
				List<IBlock> genlist = new ArrayList<IBlock>();
				genlist.add(block);
				block.getGen().put(var, genlist);
				List<IBlock> killlist = (List<IBlock>)((ArrayList<IBlock>)defTable_.get(var)).clone();
				killlist.remove(block);
				block.getKill().put(var, killlist);
			}
		}
	}
	
	/** Calculate the reaching definition */
	protected void ReachingDefinition(){
		for(IBlock b = cfg_.getEntry(); b != null; b = b.topNext()){
			MPIBlock block = (MPIBlock)b;
			block.setOut((Hashtable<String,List<IBlock>>)block.getGen().clone());
		}
		boolean change = true;
		while(change){
			change = false;
			for(IBlock b = cfg_.getEntry(); b != null; b = b.topNext()){
				MPIBlock block = (MPIBlock)b;
				Hashtable<String,List<IBlock>> in = block.getIn();
				for(Iterator<IBlock> i = block.getPreds().iterator(); i.hasNext();){
					MPIBlock pred = (MPIBlock)i.next();
					for(Enumeration<String> e = in.keys(); e.hasMoreElements();){
						String var = e.nextElement();
						in.put(var, Util.Union(in.get(var), pred.getOut().get(var)));
					}
				}
				Hashtable<String,List<IBlock>> out = block.getOut();
				for(Enumeration<String> e = out.keys(); e.hasMoreElements();){
					String var = e.nextElement();
					List outlist = Util.Union(block.getGen().get(var), 
							Util.Minus(in.get(var), 
									block.getKill().get(var)));
					if(!Util.equals(outlist, out.get(var))){ 
						change = true;
						out.put(var, outlist);
					}
				}
			}
		}
	}
	
	/** Construct the DUchain */
	protected void FlowDependence(){
		for(IBlock b = cfg_.getEntry(); b != null; b = b.topNext()){
			MPIBlock block = (MPIBlock)b;
			Hashtable<String,List<IBlock>> in = block.getIn();
			for(Enumeration<String> e = in.keys(); e.hasMoreElements();){
				String var = e.nextElement();
				if(!block.getUse().contains(var)) continue;
				List<IBlock> pred = block.getDUPred().get(var);
				for(Iterator<IBlock> i = in.get(var).iterator(); i.hasNext();){
					MPIBlock rd = (MPIBlock)i.next();
					List<IBlock> succ = rd.getDUSucc().get(var);
					pred.add(rd);
					succ.add(block);
				}
			}
		}
	}
	
	protected void DUChain(){
		for(ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()){
			MPICallGraphNode node = (MPICallGraphNode)n;
			if(!node.marked) continue;
			if(node.getFuncName().equals("Moc_KWayFM"))
				System.out.println("MPIDUChain.DUChain(): Moc_KWayFM "+node.getFuncName());
			cfg_ = node.getCFG();
			defTable_ = node.getDefTable();
			GenKillSet();
			ReachingDefinition();
			FlowDependence();
			//((MPIControlFlowGraph)cfg_).print();
		}
	}
	
	

}
