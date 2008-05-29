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
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompoundStatement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierExpression.BarrierExpressionOP;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable.BarrierInfo;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IControlFlowGraph;
import org.eclipse.ptp.pldt.mpi.analysis.popup.actions.ShowMatchSet;

public class MPIBarrierMatching {
	protected ICallGraph cg_;
	protected IControlFlowGraph cfg_;
	protected MPICallGraphNode currentFunc_;
	protected String currentComm_;
	
	protected Hashtable<BarrierExpression,List<BarrierExpression>> visited_;
	protected BarrierTable barrierTable_; 
	protected List<ErrorMessage> barrierErrors_;
	protected boolean error;
	
	protected int mv = 0;
	protected int sv = 0;
	private static final boolean traceOn=false;
	
	public MPIBarrierMatching(ICallGraph cg, BarrierTable table){
		cg_ = cg;
		barrierTable_ = table;
		visited_ = new Hashtable<BarrierExpression,List<BarrierExpression>>();
		barrierErrors_ = new ArrayList<ErrorMessage>();
	}
	
	public List<ErrorMessage> getErrors(){
		return barrierErrors_;
	}
	
	public void run(){
		boolean errorAlreadyReported=false;
		for(ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()){
			MPICallGraphNode node = (MPICallGraphNode)n;
			if(!node.marked || !node.barrierRelated()) continue;
			//System.out.println(node.getFuncName());
			currentFunc_ = node;
			cfg_ = node.getCFG();
			Hashtable<String,BarrierExpression> barrierExpr = node.getBarrierExpr();
			for(Enumeration<String> e = barrierExpr.keys(); e.hasMoreElements();){
				currentComm_ = (String)e.nextElement();
				BarrierExpression be = barrierExpr.get(currentComm_);
				error = false;
				fixedLength(be);
				if(error){// BRT this reports an error dialog for *each* barrier matching error found!
					if (!errorAlreadyReported) {
						errorAlreadyReported=true;
						String errorMsg = "Found barrier synchronization error(s)!";
						MessageDialog.openInformation(ShowMatchSet
								.getStandardDisplay().getActiveShell(),
								"MPI Barrier Analysis", errorMsg);
					}
					
				}
				findMatches(be);
			}
		}
		//symmMatches();
		if(traceOn)printMatches();
		if(traceOn)System.out.println("mv = " + mv + ", sv = " + sv);
	}
	
	protected void fixedLength(BarrierExpression BE){
		//System.out.println(BE.prettyPrinter());
		BarrierExpressionOP OP = BE.getOP();
		if(OP == null){
			if(BE.isBot()){
				BE.setLength(0);
			}
			else if(BE.isBarrier()){
				BE.setLength(1);
			}
			else { //function call
				String funcName = BE.getFuncName();
				MPICallGraphNode fnode = (MPICallGraphNode)cg_.getNode(currentFunc_.getFileName(), funcName);
				if(fnode != null && fnode.barrierRelated()){
					if(fnode.isRecursive())
						BE.setLength(BarrierExpression.TOP);
					else{
						BarrierExpression fBE = fnode.getBarrierExpr().get(currentComm_);
						BE.setLength(fBE.getLength());
					}
				} else {
					BE.setLength(0);
				}
			}
		}
		else if(OP.getOperator() == BarrierExpressionOP.op_concat){
			BarrierExpression oprd1 = BE.getOP1();
			BarrierExpression oprd2 = BE.getOP2();
			fixedLength(oprd1);
			fixedLength(oprd2);
			int length1 = oprd1.getLength();
			int length2 = oprd2.getLength();

			if(length1 == BarrierExpression.TOP || length2 == BarrierExpression.TOP){
				BE.setLength(BarrierExpression.TOP);
			} else {
				BE.setLength(length1 + length2);
			}
		}
		else if(OP.getOperator() == BarrierExpressionOP.op_branch){
			/* need special treatment for conditional expression */
			BarrierExpression oprd1 = BE.getOP1();
			BarrierExpression oprd2 = BE.getOP2();
			fixedLength(oprd1);
			fixedLength(oprd2);
			int length1 = oprd1.getLength();
			int length2 = oprd2.getLength();
			MPIBlock cond = (MPIBlock)cfg_.getBlock(OP.getCondition(), OP.getStatement());
			if(cond.getMV()) mv ++;
			else sv ++;
			if(length1 == BarrierExpression.TOP || length2 == BarrierExpression.TOP){
				BE.setLength(BarrierExpression.TOP);
				if(cond.getMV()){
					BE.setErrorFlag(true);
					if(!(oprd1.getErrorFlag() || oprd2.getErrorFlag())){
						// We only report errors in the lowest level
						reportWarning(BE);
					}
				}
			} else if(length1 == length2){
				BE.setLength(length1);
			} else { // length1 != TOP && length2 != TOP && length1 != length2
				BE.setLength(BarrierExpression.TOP);
				if(cond.getMV()){
					BE.setErrorFlag(true);
					if(!(oprd1.getErrorFlag() || oprd2.getErrorFlag())){
						reportWarning(BE);
					}
					
				}
			}
		}
		else { //BarrierExpressionOP.op_repeat
			BarrierExpression oprd = BE.getOP1();
			fixedLength(oprd);
			MPIBlock cond = (MPIBlock)cfg_.getBlock(OP.getCondition(), OP.getStatement());
			if(cond.getMV()) mv ++;
			else sv ++;
			if(oprd.getLength() == 0){
				BE.setLength(0);
			} else {
				BE.setLength(BarrierExpression.TOP);
				if(cond.getMV()){
					BE.setErrorFlag(true);
					if(!oprd.getErrorFlag()){
						reportWarning(BE);
					}
					
				}
			}
		}
	}
	
	protected void reportWarning(BarrierExpression BE){
		error = true;
		
		IASTStatement stmt = BE.getOP().getStatement();
		if(traceOn)System.out.println("Barrier mismatching found in " + stmt);
		IASTExpression cond = null;
		
		if(stmt instanceof IASTIfStatement)
			cond = ((IASTIfStatement)stmt).getConditionExpression();
		else if(stmt instanceof IASTDoStatement)
			cond = ((IASTDoStatement)stmt).getCondition();
		else if(stmt instanceof IASTForStatement)
			cond = ((IASTForStatement)stmt).getConditionExpression();
		else if(stmt instanceof IASTWhileStatement)
			cond = ((IASTWhileStatement)stmt).getCondition();
		else if(stmt instanceof IASTSwitchStatement)
			cond = ((IASTSwitchStatement)stmt).getControllerExpression();
		else{
			System.out.println("Barrier Expression doesn't have valid condition");
			return;
		}
		
		int line = -1;
		IASTNodeLocation[] locations = cond.getNodeLocations();
		if (locations.length == 1) {
			IASTFileLocation astFileLocation = null;
			if (locations[0] instanceof IASTFileLocation){
				astFileLocation = (IASTFileLocation) locations[0];
				line = astFileLocation.getStartingLineNumber();
			}
		}
		
		try{
			// BRT barrierMarker change to problem marker here?
			IMarker m = currentFunc_.getResource().createMarker(IMarker.PROBLEM);
			//IMarker m = currentFunc_.getResource().createMarker(IDs.errorMarkerID);
			m.setAttribute(IMarker.LINE_NUMBER, line);
			m.setAttribute(IMarker.MESSAGE, "Barrier Synchronization Error");
			m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		}catch(CoreException e){
			System.out.println("RM: exception creating markers.");
			e.printStackTrace();
		}

		ErrorMessage err = new ErrorMessage(cond, stmt, currentFunc_.getFuncName(), 
				currentFunc_.getFileName(), currentFunc_.getResource());
		counterExample(BE, err);
		barrierErrors_.add(err);
	}

	private boolean changed = false;
	
	protected void counterExample(BarrierExpression BE, ErrorMessage err){
		BarrierExpressionOP op = BE.getOP();
		if(op.getOperator() == BarrierExpressionOP.op_branch){
			List<PathNode> path1 = new ArrayList<PathNode>();
			List<PathNode> path2 = new ArrayList<PathNode>();
			boolean first = true;
			while(true){
				changed = false;
				traverseBarrierExpr(path1, BE.getOP1(), cfg_);
				traverseBarrierExpr(path2, BE.getOP2(), cfg_);
				if(differentLength(path1, path2))
					break;
				if(!changed && !first){
					System.out.println("We cannot find the counter example!");
				}
				if(first) first = true;
				path1 = new ArrayList<PathNode>();
				path2 = new ArrayList<PathNode>();
			}
			err.setPath1(path1);
			err.setPath2(path2);
			
			if(traceOn)System.out.println("Path 1: ");
			for(Iterator<PathNode> i=path1.iterator(); i.hasNext();){
				PathNode pn = i.next();
				pn.print();
				if(traceOn)System.out.print(", ");
			}
			if(traceOn)System.out.println(" ");
			if(traceOn)System.out.println("Path 2: ");
			for(Iterator<PathNode> i=path2.iterator(); i.hasNext();){
				PathNode pn = i.next();
				pn.print();
				if(traceOn)System.out.print(", ");
			}
			if(traceOn)System.out.println(" ");
		}
		else if(op.getOperator() == BarrierExpressionOP.op_repeat){
			List<PathNode> path1 = new ArrayList<PathNode>();
			traverseBarrierExpr(path1, BE.getOP1(), cfg_);
			for(Iterator<PathNode> i = path1.iterator(); i.hasNext();){
				PathNode pn = i.next();
				pn.setRepeat(true);
			}
			err.setPath1(path1);
			
			if(traceOn)System.out.println("Path: ");
			for(Iterator<PathNode> i=path1.iterator(); i.hasNext();){
				PathNode pn = i.next();
				pn.print();
				if(traceOn)System.out.print(", ");
			}
			if(traceOn)System.out.println(" ");
		}
	}
	
	protected void traverseBarrierExpr(List<PathNode> path, BarrierExpression BE, 
			IControlFlowGraph cfg){
		//System.out.println(BE.prettyPrinter());
		BarrierExpressionOP op = BE.getOP();
		if(op == null){
			if(BE.isBot()){
				return;
			}
			else if(BE.isBarrier()){
				BarrierInfo barrier = barrierTable_.searchBarrierbyID(BE.getBarrierID());
				PathNode pn = new PathNode(barrier, false);
				path.add(pn);
			}
			else { //function call
				String funcName = BE.getFuncName();
				MPICallGraphNode fnode = (MPICallGraphNode)cg_.getNode(currentFunc_.getFileName(), funcName);
				if(fnode != null && fnode.barrierRelated()){
					BarrierExpression fBE = (BarrierExpression)fnode.getBarrierExpr().get(currentComm_);
					traverseBarrierExpr(path, fBE, fnode.getCFG());
				} else {
					return;
				}
			}
		}
		else if(op.getOperator() == BarrierExpressionOP.op_branch){
			MPIBlock cond = (MPIBlock)cfg.getBlock(op.getCondition(), op.getStatement());
			if(cond.getMV()){ 
				/* Concurrent branch. Both branches should have same lengths, 
				 * thus it doesn't matter which branch to choose. By default 
				 * we choose the left branch. 
				 */
				traverseBarrierExpr(path, BE.getOP1(), cfg);
			} else if(BE.getLength() != BarrierExpression.TOP){
				/* Both branches have same length that are not \TOP */
				traverseBarrierExpr(path, BE.getOP1(), cfg);
			} else if(changed){
				/* Some change has been made somewhere else. So keep the 
				 * previous choice at this point
				 */
				if(BE.getOP2().ceVisited)
					traverseBarrierExpr(path, BE.getOP2(), cfg);
				else
					traverseBarrierExpr(path, BE.getOP1(), cfg);
			} else {
				/* Nothing changed till now */
				if(BE.getOP2().ceVisited){
					/* We cannot make any change. Keep the previous choice */
					traverseBarrierExpr(path, BE.getOP2(), cfg);
				} else if(BE.getOP1().ceVisited){
					/* Make change here */
					traverseBarrierExpr(path, BE.getOP2(), cfg);
					changed = true;
				} else {
					/* This BE is visited the first time */
					traverseBarrierExpr(path, BE.getOP1(), cfg);
				}
			}
		}
		else if(op.getOperator() == BarrierExpressionOP.op_concat){
			traverseBarrierExpr(path, BE.getOP1(), cfg);
			traverseBarrierExpr(path, BE.getOP2(), cfg);
		}
		else if(op.getOperator() == BarrierExpressionOP.op_repeat){
			traverseBarrierExpr(path, BE.getOP1(), cfg);
			for(Iterator<PathNode> i = path.iterator(); i.hasNext();){
				PathNode pn = i.next();
				pn.setRepeat(true);
			}
		}
		BE.ceVisited = true;
	}
	
	protected boolean differentLength(List<PathNode> path1, List<PathNode> path2){
		if(path1.size() != path2.size()) return true;
		for(int i = 0; i<path1.size(); i++){
			if(path1.get(i).isRepeat() || path2.get(i).isRepeat())
				return true;
		}
		return false;
	}
	
	public class PathNode{
		private BarrierInfo barrier_;
		private boolean repeat;
		
		public PathNode(BarrierInfo barrier, boolean repeat){
			this.barrier_ = barrier;
			this.repeat = repeat;
		}
		
		public PathNode(BarrierInfo barrier){
			this.barrier_ = barrier;
			repeat = false;
		}
		
		public void setRepeat(boolean val){
			repeat = val;
		}
		
		public boolean isRepeat(){
			return repeat;
		}
		
		public BarrierInfo getBarrier(){
			return barrier_;
		}
		
		public void print(){
			if(traceOn)System.out.print(barrier_.getID());
			if(repeat)
				if(traceOn)System.out.print("(*)");
		}
	}
	
	public class ErrorMessage{
		private IASTExpression position_;
		private IASTStatement errStmt_;
		private String funcName_;
		private String fileName_;
		private SourceInfo sourceInfo_;
		private SourceInfo path1SourceInfo_;
		private SourceInfo path2SourceInfo_;
		private IResource resource_;
		private List<PathNode> path1_ = null;
		private List<PathNode> path2_ = null;
		private int length1 = 0;
		private int length2 = 0;
		
		public ErrorMessage(IASTExpression pos, IASTStatement stmt, String funcName, 
				String fileName, IResource res){
			position_ = pos;
			errStmt_ = stmt;
			funcName_ = funcName;
			fileName_ = fileName;
			resource_ = res;
			sourceInfo_ = getSourceInfo(position_);
			if(errStmt_ instanceof IASTIfStatement){
				IASTIfStatement ifS = (IASTIfStatement)errStmt_;
				path1SourceInfo_ = getSourceInfo(ifS.getThenClause());
				path2SourceInfo_ = getSourceInfo(ifS.getElseClause());
			} else if(errStmt_ instanceof IASTDoStatement){
				IASTDoStatement doS = (IASTDoStatement)errStmt_;
				path1SourceInfo_ = getSourceInfo(doS.getBody());
				path2SourceInfo_ = null;
			} else if(errStmt_ instanceof IASTForStatement){
				IASTForStatement forS = (IASTForStatement)errStmt_;
				path1SourceInfo_ = getSourceInfo(forS.getBody());
				path2SourceInfo_ = null;
			} else if(errStmt_ instanceof IASTWhileStatement){
				IASTWhileStatement whileS = (IASTWhileStatement)errStmt_;
				path1SourceInfo_ = getSourceInfo(whileS.getBody());
				path2SourceInfo_ = null;
			} else if(errStmt_ instanceof IASTSwitchStatement){
				IASTSwitchStatement switchS = (IASTSwitchStatement)errStmt_;
				path1SourceInfo_ = getSourceInfo(switchS.getBody());
				path2SourceInfo_ = path1SourceInfo_;
			}
		}
		
		SourceInfo getSourceInfo(IASTNode node){
			SourceInfo sourceInfo = new SourceInfo();
			if(node == null) return sourceInfo;
			IASTNodeLocation[] locations = node.getNodeLocations();
			if (locations.length == 1) {
				IASTFileLocation astFileLocation = null;
				if (locations[0] instanceof IASTFileLocation){
					astFileLocation = (IASTFileLocation) locations[0];
					sourceInfo.setStartingLine(astFileLocation.getStartingLineNumber());
					sourceInfo.setStart(astFileLocation.getNodeOffset());
					sourceInfo.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
					sourceInfo.setConstructType(Artifact.NONE);
				}
			}
			else {
				//System.out.println("MPIBarrierMatching.getSourceInfo().. ! More than one IASTNodeLocation !");
				// CASTCompoundStatement causes this.  Probably ok? assuming it descends into the child nodes
				if(traceOn) {
				if(node instanceof CASTCompoundStatement) {
					CASTCompoundStatement cstmt = (CASTCompoundStatement)node;
					System.out.println("Compound stmt: "+cstmt.getRawSignature());
					IASTNodeLocation[] locs = cstmt.getNodeLocations();
					for (int i = 0; i < locs.length; i++) {
						IASTNodeLocation loc = locs[i];
						System.out.println("  subnode: "+loc.toString());			
					}
				}
					
				}
			}
			return sourceInfo; 
		}
		
		public void setPath1(List<PathNode> path){
			path1_ = path;
			int count = 0;
			for(Iterator<PathNode> i = path.iterator(); i.hasNext();){
				PathNode pn = i.next();
				if(pn.isRepeat()){
					length1 = -1;
					return;
				} else {
					count ++;
				}
			}
			length1 = count;
		}
		
		public void setPath2(List<PathNode> path){
			path2_ = path;
			int count = 0;
			for(Iterator<PathNode> i = path.iterator(); i.hasNext();){
				PathNode pn = i.next();
				if(pn.isRepeat()){
					length2 = -1;
					return;
				} else {
					count ++;
				}
			}
			length2 = count;
		}
		
		public IASTExpression getPosition(){
			return position_;
		}
		
		public String getFuncName(){
			return funcName_;
		}
		
		public String getFileName(){
			return fileName_;
		}
		
		public SourceInfo getSourceInfo(){
			return sourceInfo_;
		}
		
		public SourceInfo getPath1SourceInfo(){
			return path1SourceInfo_;
		}
		
		public SourceInfo getPath2SourceInfo(){
			return path2SourceInfo_;
		}
		
		public IResource getResource(){
			return resource_;
		}
		
		public List<PathNode> getPath1(){
			return path1_;
		}
		
		public List<PathNode> getPath2(){
			return path2_;
		}
		
		public int getLength1(){
			return length1;
		}
		
		public int getLength2(){
			return length2;
		}
	}
	
	protected void findMatches(BarrierExpression BE){
		if(traceOn)System.out.println(BE.prettyPrinter());
		BarrierExpressionOP OP = BE.getOP();
		if(BE.visited) return;
		BE.visited = true;
		if(OP == null){
			if(BE.isFunc()){
				String funcName = BE.getFuncName();
				MPICallGraphNode fnode = (MPICallGraphNode)cg_.getNode(currentFunc_.getFileName(), funcName);
				if(fnode != null && fnode.barrierRelated()){
					BarrierExpression fBE = (BarrierExpression)fnode.getBarrierExpr().get(currentComm_);
					findMatches(fBE);
				} 
			} else {
				lstack_ = new Stack<BarrierExpression>();
				sstack_ = new Stack<BarrierExpression>();
				workList_ = new LinkedList<Work>();
				match(BE, BE, down);
				while(!workList_.isEmpty()){
					Work w = workList_.remove();
					match(w.BE1_, w.BE2_, w.direction);
				}
			}
		}
		else if(OP.getOperator() == BarrierExpressionOP.op_concat){
			BarrierExpression oprd1 = BE.getOP1();
			BarrierExpression oprd2 = BE.getOP2();
			findMatches(oprd1);
			findMatches(oprd2);
		}
		else if(OP.getOperator() == BarrierExpressionOP.op_branch){
			BarrierExpression oprd1 = BE.getOP1();
			BarrierExpression oprd2 = BE.getOP2();
			MPIBlock cond = (MPIBlock)cfg_.getBlock(OP.getCondition(), OP.getStatement());
			if(cond.getMV()){ // concurrent branch
				if(!BE.getErrorFlag()){
					lstack_ = new Stack<BarrierExpression>();
					sstack_ = new Stack<BarrierExpression>();
					workList_ = new LinkedList<Work>();
					match(oprd1, oprd2, down);
					while(!workList_.isEmpty()){
						Work w = workList_.remove();
						match(w.BE1_, w.BE2_, w.direction);
					}
				}
			}
			if(!BE.getErrorFlag()){
				findMatches(oprd1);
				findMatches(oprd2);
			}
		}
		else { //BarrierExpressionOP.op_repeat
			if(!BE.getErrorFlag()){
				BarrierExpression oprd = BE.getOP1();
				findMatches(oprd);
			}
		}
	}
	
	protected final int down = 0;
	protected final int up = 1;
	protected Stack<BarrierExpression> lstack_;
	protected Stack<BarrierExpression> sstack_;
	protected LinkedList<Work> workList_;
	
	class Work{
		BarrierExpression BE1_;
		BarrierExpression BE2_;
		int direction;
		
		Work(BarrierExpression be1, BarrierExpression be2, int direction){
			BE1_ = be1;
			BE2_ = be2;
			this.direction = direction;
		}
	}
	
	protected void match(BarrierExpression BE1, BarrierExpression BE2, int dir){
		BarrierExpression T1 = BE1;
		BarrierExpression T2 = BE2;
		int direction = dir;
		while(true){
			if(traceOn)System.out.println(T1.prettyPrinter());
			if(traceOn)System.out.println(T2.prettyPrinter());
			if(direction == down) {if(traceOn)System.out.println("down");}
			else {if(traceOn)System.out.println("up");}
			
			if(direction == down && !pairVisited(T1, T2))
				addVisitedPair(T1, T2);
		
			BarrierExpressionOP OP1 = T1.getOP();
			BarrierExpressionOP OP2 = T2.getOP();
			
			if(direction == up && T1 == BE1 && T2 == BE2) break;
			else if(direction == down && OP1 == null && OP2 == null && 
					T1.isBot() && T2.isBot())
				direction = up;
			else if(direction == down && OP1 == null && OP2 == null && 
					T1.isBarrier() && T2.isBarrier()){
				addMatchedPair(T1, T2);
				addMatchedPair(T1, T1);
				addMatchedPair(T2, T2);
				direction = up;
			} else if(direction == down && OP1 == null && T1.isFunc()){ //rule 7
				String funcName = T1.getFuncName();
				MPICallGraphNode fnode = (MPICallGraphNode)cg_.getNode(currentFunc_.getFileName(), funcName);
				if(fnode == null || !fnode.barrierRelated()){
					System.out.println("Error in call graph");
				}
				BarrierExpression fBE = (BarrierExpression)fnode.getBarrierExpr().get(currentComm_);
				lstack_.push(T1);
				T1 = fBE;
			} else if(direction == down && OP2 == null && T2.isFunc()){ //rule 7
				String funcName = T2.getFuncName();
				MPICallGraphNode fnode = (MPICallGraphNode)cg_.getNode(currentFunc_.getFileName(), funcName);
				if(fnode == null || !fnode.barrierRelated()){
					System.out.println("Error in call graph");
				}
				BarrierExpression fBE = (BarrierExpression)fnode.getBarrierExpr().get(currentComm_);
				sstack_.push(T2);
				T2 = fBE;
			} else if(direction == down && OP1 != null && 
					OP1.getOperator() == BarrierExpressionOP.op_concat){ //rule 2
				T1 = T1.getOP1();
			} else if(direction == down && OP2 != null && 
					OP2.getOperator() == BarrierExpressionOP.op_concat){ //rule 2
				T2 = T2.getOP1();
			} else if(direction == up && T1.getParent() == null){ //rule 8
				T1 = lstack_.pop();
			} else if(direction == up && T2.getParent() == null){ //rule 8
				T2 = sstack_.pop();
			} else if(direction == up && 
					T1.getParent().getOP().getOperator() == BarrierExpressionOP.op_concat
					&& T1 == T1.getParent().getOP2()){ //rule 3
				T1 = T1.getParent();
				direction = up;
			} else if(direction == up && 
					T2.getParent().getOP().getOperator() == BarrierExpressionOP.op_concat
					&& T2 == T2.getParent().getOP2()){ //rule 3
				T2 = T2.getParent();
				direction = up;
			} else if(direction == up && T1.getParent().getOP().getOperator() == BarrierExpressionOP.op_concat
					&& T1 == T1.getParent().getOP1()
					&& T2.getParent().getOP().getOperator() == BarrierExpressionOP.op_concat
					&& T2 == T2.getParent().getOP1()){ //rule 4
				T1 = T1.getParent().getOP2();
				T2 = T2.getParent().getOP2();
				direction = down;
			} else if(direction == down && OP1 != null && 
					OP1.getOperator() == BarrierExpressionOP.op_branch){ //rule 5
				workList_.add(new Work(T1.getOP2(), T2, down));
				T1 = T1.getOP1();
			} else if(direction == down && OP2 != null && 
					OP2.getOperator() == BarrierExpressionOP.op_branch){ //rule 5
				workList_.add(new Work(T1, T2.getOP2(), down));
				T2 = T2.getOP1();
			} else if(direction == up && 
					T1.getParent().getOP().getOperator() == BarrierExpressionOP.op_branch){ 
				//rule 6
				T1 = T1.getParent();
			} else if(direction == up &&
					T2.getParent().getOP().getOperator() == BarrierExpressionOP.op_branch){ 
				//rule 6
				T2 = T2.getParent();
			} else{
				System.out.println("cannot find rules for " + T1.prettyPrinter() 
					+ " and " + T2.prettyPrinter() + " and direction = " + direction);
			}
		}
	}
	
	protected void addVisitedPair(BarrierExpression b1, BarrierExpression b2){
		List<BarrierExpression> visitedPair = visited_.get(b1);
		if(visitedPair == null){
			visitedPair = new ArrayList<BarrierExpression>();
			visitedPair.add(b2);
			visited_.put(b1, visitedPair);
		} else {
			if(!visitedPair.contains(b2)) visitedPair.add(b2);
		}
		
		visitedPair = visited_.get(b2);
		if(visitedPair == null){
			visitedPair = new ArrayList<BarrierExpression>();
			visitedPair.add(b1);
			visited_.put(b2, visitedPair);
		} else {
			if(!visitedPair.contains(b1)) visitedPair.add(b1);
		}
		
	}
	
	protected boolean pairVisited(BarrierExpression b1, BarrierExpression b2){
		List<BarrierExpression> pair = visited_.get(b1);
		if(pair == null || !pair.contains(b2))
			return false;
		else
			return true;
	}
	
	protected void addMatchedPair(BarrierExpression b1, BarrierExpression b2){
		BarrierInfo bar1 = barrierTable_.searchBarrierbyID(b1.getBarrierID());
		BarrierInfo bar2 = barrierTable_.searchBarrierbyID(b2.getBarrierID());
		List<BarrierInfo> set1 = bar1.getMatchingSet();
		List<BarrierInfo> set2 = bar2.getMatchingSet();
		if(!set1.contains(bar2)) set1.add(bar2);
		if(!set2.contains(bar1)) set2.add(bar1);
	}
	
	protected void symmMatches(){
		for(Enumeration<List<BarrierInfo>> e = barrierTable_.getTable().elements(); e.hasMoreElements();){
			List<BarrierInfo> list = e.nextElement();
			for(Iterator<BarrierInfo> i = list.iterator(); i.hasNext();){
				BarrierInfo bar = i.next();
				if(traceOn)System.out.println("bar = " + bar.getID());
				for(Iterator<BarrierInfo> ii = bar.getMatchingSet().iterator(); ii.hasNext();){
					BarrierInfo matchedBar = ii.next();
					if(traceOn)System.out.println("matchedBar = " + matchedBar.getID());
					if(!matchedBar.getMatchingSet().contains(bar)){
						if(traceOn)System.out.println("bar = " + bar.getID() + ", matchedBar = " + matchedBar.getID());
						matchedBar.getMatchingSet().add(bar);
					}
				}
			}
		}
	}
	
	protected void printMatches(){
		for(Enumeration<List<BarrierInfo>> e = barrierTable_.getTable().elements(); e.hasMoreElements();){
			List<BarrierInfo> list = e.nextElement();
			for(Iterator<BarrierInfo> i = list.iterator(); i.hasNext();){
				BarrierInfo bar = i.next();
				if(traceOn)System.out.print("Barrier " + bar.getID() + " matches to : ");
				for(Iterator<BarrierInfo> ii = bar.getMatchingSet().iterator(); ii.hasNext();){
					BarrierInfo matchedBar = ii.next();
					if(traceOn)System.out.print(matchedBar.getID() + ", ");
				}
			}		
			if(traceOn)System.out.println(" ");
		}
	}
}
