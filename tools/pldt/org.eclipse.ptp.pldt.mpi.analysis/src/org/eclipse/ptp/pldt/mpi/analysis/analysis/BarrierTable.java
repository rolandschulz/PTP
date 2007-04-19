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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.util.SourceInfo;

public class BarrierTable {
	/** 
	 * Set of barriers for a communicator.
	 * Key: <String>communicator, Value: set of <BarrierInfo>barriers 
	 */
	protected Hashtable<String,List<BarrierInfo>> table_; 
	protected int commCounter = 0;
	
	public BarrierTable(){
		table_ = new Hashtable<String,List<BarrierInfo>>();
	}
	
	public Hashtable<String,List<BarrierInfo>> getTable(){
		return table_;
	}
	public boolean isEmpty() {return table_.isEmpty();}
	
	public BarrierInfo addBarrier(IASTFunctionCallExpression barE, 
			int id, IResource res, String func){
		BarrierInfo bar = new BarrierInfo(barE, id, res, func);
		if(table_.containsKey(bar.getComm())){
			List<BarrierInfo> list = table_.get(bar.getComm());
			list.add(bar);
		} else {
			List<BarrierInfo> list = new ArrayList<BarrierInfo>();
			list.add(bar);
			table_.put(bar.getComm(), list);
		}
		return bar;
	}
	
	/*
	 * @return: barrier ID if it is a barrier; -1 otherwise
	 */ 
	public int isBarrier(IASTFunctionCallExpression funcE){
		IASTExpression funcname = funcE.getFunctionNameExpression();
		String signature = funcname.getRawSignature();
		if(!signature.equals("MPI_Barrier")) return -1;
		for(Enumeration e = table_.elements(); e.hasMoreElements();){
			ArrayList list = (ArrayList)e.nextElement();
			for(Iterator i = list.iterator(); i.hasNext();){
				BarrierInfo bar = (BarrierInfo)i.next();
				if(bar.getFunc() == funcE)
					return bar.getID();
			}
		}
		return -1;
	}

	public String getComm(int id){
		for(Enumeration e = table_.elements(); e.hasMoreElements();){
			ArrayList list = (ArrayList)e.nextElement();
			for(Iterator i = list.iterator(); i.hasNext();){
				BarrierInfo bar = (BarrierInfo)i.next();
				if(bar.getID() == id)
					return bar.getComm();
			}
		}
		return null;
	}
	
	public BarrierInfo searchBarrierbyID(int id){
		for(Enumeration e = table_.elements(); e.hasMoreElements();){
			ArrayList list = (ArrayList)e.nextElement();
			for(Iterator i = list.iterator(); i.hasNext();){
				BarrierInfo bar = (BarrierInfo)i.next();
				if(bar.getID() == id)
					return bar;
			}
		}
		return null;
	}
	
	
	
	public class BarrierInfo{
		protected String comm_ = null;
		protected IASTFunctionCallExpression barrier_ = null;
		protected int id = -1;
		protected String fileName_ = null;
		protected SourceInfo sourceInfo_ = null;
		protected List<BarrierInfo> matchingSet_ = null;
		protected IResource resource_ = null;
		protected String enclosingFunc_ = null;
		
		public BarrierInfo(IASTFunctionCallExpression funcE, int id, IResource res, String func){
			barrier_ = funcE;
			this.id = id;
			setComm();
			setSourceInfo();
			matchingSet_ = new ArrayList<BarrierInfo>();
			resource_ = res;
			enclosingFunc_ = func;
		}
		
		/**
		 * Determine the communicator from the the barrier function call
		 * ("MPI_Barrier(communicator)"). <br>
		 * If the communicator is the default (MPI_COMM_WORLD), then the type of
		 * the returned object from getOperand() depends on how the header file
		 * defines it (In windows, it is IASTLiteralExpression, and in Linux, it
		 * becomes IASTIdExpression). So we account for that here.
		 */
		protected void setComm() { 
			IASTExpression parameter = barrier_.getParameterExpression();

			if (parameter instanceof IASTUnaryExpression) {
				IASTUnaryExpression commExpr = (IASTUnaryExpression) parameter;
				if (commExpr.getOperand() instanceof IASTUnaryExpression) {
					IASTUnaryExpression commOprd = (IASTUnaryExpression) commExpr
							.getOperand();
					if (commOprd.getOperand() instanceof IASTLiteralExpression) {
						IASTLiteralExpression comm = (IASTLiteralExpression) commOprd.getOperand();
						comm_ = comm.toString();
					} else if (commOprd.getOperand() instanceof IASTIdExpression) {
						IASTIdExpression comm = (IASTIdExpression) commOprd.getOperand();
						comm_ = comm.getName().toString();
					} else if (commOprd.getOperand() instanceof IASTName) {
						comm_ = commOprd.getOperand().toString();
					} else {
						comm_ = "COMM_" + commCounter;
						commCounter++;
					}
				} else {
					comm_ = "COMM_" + commCounter;
					commCounter++;
				}
			} else if (parameter instanceof IASTIdExpression) {
				IASTIdExpression idE = (IASTIdExpression) parameter;
				comm_ = idE.getName().toString();
				if (!comm_.equals("MPI_COMM_WORLD")) {
					comm_ = "COMM_" + commCounter;
					commCounter++;
				}
			} else {
				comm_ = "COMM_" + commCounter;
				commCounter++;
			}
		}
	
		
		protected void setSourceInfo() {
			IASTExpression funcNameE = barrier_.getFunctionNameExpression();
			IASTNodeLocation[] locations = funcNameE.getNodeLocations();
			if (locations.length == 1) {
				IASTFileLocation astFileLocation = null;
				if (locations[0] instanceof IASTFileLocation) {
					astFileLocation = (IASTFileLocation) locations[0];
					fileName_ = astFileLocation.getFileName();
					//System.out.println(fileName_);
					sourceInfo_ = new SourceInfo();
					sourceInfo_.setStartingLine(astFileLocation.getStartingLineNumber());
					sourceInfo_.setStart(astFileLocation.getNodeOffset());
					sourceInfo_.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
					sourceInfo_.setConstructType(Artifact.FUNCTION_CALL);
				}
			}
		}
		
		public String getComm() {return comm_;}
		public IASTFunctionCallExpression getFunc() {return barrier_;}
		public int getID() {return id;}
		public String getFileName() {return fileName_;}
		public SourceInfo getSourceInfo() {return sourceInfo_;}
		public List<BarrierInfo> getMatchingSet() {return matchingSet_;}
		public IResource getResource() {return resource_;}
		public String getEnclosingFunc() {return enclosingFunc_;}
	}
	
}
