/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
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

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.util.SourceInfo;

public class BarrierTable {
	/**
	 * Set of barriers for a communicator. Key: <String>communicator, Value: set
	 * of <BarrierInfo>barriers
	 */
	protected Hashtable<String, List<BarrierInfo>> table_;
	protected int commCounter = 0;
	private static boolean dbg_barrier = false;

	public BarrierTable() {
		table_ = new Hashtable<String, List<BarrierInfo>>();
	}

	public Hashtable<String, List<BarrierInfo>> getTable() {
		return table_;
	}

	public boolean isEmpty() {
		return table_.isEmpty();
	}

	public BarrierInfo addBarrier(IASTFunctionCallExpression barE,
			int id, IResource res, String func) {
		BarrierInfo bar = new BarrierInfo(barE, id, res, func);
		if (table_.containsKey(bar.getComm())) {
			List<BarrierInfo> list = table_.get(bar.getComm());
			list.add(bar);
		} else {
			List<BarrierInfo> list = new ArrayList<BarrierInfo>();
			list.add(bar);
			table_.put(bar.getComm(), list);
		}
		return bar;
	}

	/**
	 * @return barrier ID if it is a barrier; -1 otherwise <br>
	 *         BRT barrier ids may not match with OpenMPI 1.3.3 headers?? see
	 *         setComm()
	 */
	public int isBarrier(IASTFunctionCallExpression funcE) {
		IASTExpression funcname = funcE.getFunctionNameExpression();
		String signature = funcname.getRawSignature();
		if (!signature.equals("MPI_Barrier"))return -1; //$NON-NLS-1$
		for (Enumeration e = table_.elements(); e.hasMoreElements();) {
			ArrayList list = (ArrayList) e.nextElement();
			for (Iterator i = list.iterator(); i.hasNext();) {
				BarrierInfo bar = (BarrierInfo) i.next();
				if (bar.getFunc() == funcE)
					return bar.getID();
			}
		}
		return -1;
	}

	public String getComm(int id) {
		for (Enumeration e = table_.elements(); e.hasMoreElements();) {
			ArrayList list = (ArrayList) e.nextElement();
			for (Iterator i = list.iterator(); i.hasNext();) {
				BarrierInfo bar = (BarrierInfo) i.next();
				if (bar.getID() == id)
					return bar.getComm();
			}
		}
		return null;
	}

	public BarrierInfo searchBarrierbyID(int id) {
		for (Enumeration e = table_.elements(); e.hasMoreElements();) {
			ArrayList list = (ArrayList) e.nextElement();
			for (Iterator i = list.iterator(); i.hasNext();) {
				BarrierInfo bar = (BarrierInfo) i.next();
				if (bar.getID() == id)
					return bar;
			}
		}
		return null;
	}

	public class BarrierInfo {
		protected String comm_ = null;
		protected IASTFunctionCallExpression barrier_ = null;
		protected int id = -1;
		protected String fileName_ = null;
		protected SourceInfo sourceInfo_ = null;
		protected List<BarrierInfo> matchingSet_ = null;
		protected IResource resource_ = null;
		protected String enclosingFunc_ = null;

		public BarrierInfo(IASTFunctionCallExpression funcE, int id, IResource res, String func) {
			barrier_ = funcE;
			this.id = id;
			setComm();
			setSourceInfo();
			matchingSet_ = new ArrayList<BarrierInfo>();
			resource_ = res;
			enclosingFunc_ = func;
		}

		//@formatter:off
		/**
		 * Determine the communicator from the the barrier function call
		 * ("MPI_Barrier(communicator)"). <br>
		 * If the communicator is the default (MPI_COMM_WORLD), then the type of
		 * the returned object from getOperand() depends on how the header file
		 * defines it (In windows, it is IASTLiteralExpression, and in Linux, it
		 * becomes IASTIdExpression). So we account for that here.
		 * <br>
		 * BRT clarification: 09/09/09<br>
		 * OpenMPI < 1.3? defines MPI_COMM_WORLD as:<br>
		 *   #define MPI_COMM_WORLD (&ompi_mpi_comm_world)<br>
		 * OpenMPI 1.3.3 defines MPI_COMM_WORLD as:<br>
		 *   #define MPI_COMM_WORLD OMPI_PREDEFINED_GLOBAL( MPI_Comm, ompi_mpi_comm_world)<br>
		 * which makes the CDT objects for the barrier/communicator show up differently when it gets here.
		 *   It shows up as an IASTCastExpression, so we recognize that and get its name,
		 *   instead of relying on the default fallback plan here, which seems to always
		 *   make each barrier/communicator found be unique, in which case no barriers match, because their
		 *   communicators all look different.
		 *   
		 *   Problem: for the case of #define newcomm MPI_COMM_WORLD
		 *   I don't want the getRawSignature() on the communicator arg, i want the
		 *   pre-processed value.  How to get that?
		 */
		//@formatter:on
		protected void setComm() {
			IASTExpression parameter = barrier_.getParameterExpression();
			IASTInitializerClause[] newParms = barrier_.getArguments(); // BRT could fix deprecation?

			if (parameter instanceof IASTUnaryExpression) {
				IASTUnaryExpression commExpr = (IASTUnaryExpression) parameter;
				IASTExpression commOp = commExpr.getOperand();
				if (commOp instanceof IASTUnaryExpression) {
					if (dbg_barrier)
						System.out.println("setComm(): communicator is IASTUnaryExpression"); //$NON-NLS-1$
					// IASTUnaryExpression commOprd = (IASTUnaryExpression) commOp;
					if (commOp instanceof IASTLiteralExpression) {// Yuan says windows
						if (dbg_barrier)
							System.out.println();
						IASTLiteralExpression comm = (IASTLiteralExpression) commOp;
						comm_ = comm.toString();
					} else if (commOp instanceof IASTIdExpression) { // Yuan says linux
						IASTIdExpression comm = (IASTIdExpression) commOp;
						comm_ = comm.getName().toString();
					} else if (commOp instanceof IASTName) {// ?
						comm_ = commOp.toString();
					} else {

						if (commOp instanceof IASTUnaryExpression) {// Mac OSX Openmpi < 1.3 (/usr/include/mpi.h) // 4/14/10: got
																	// here OMPI 1.3.3
							IASTUnaryExpression iastUnaryExpression = (IASTUnaryExpression) commOp;
							if (dbg_barrier)
								System.out.println("bdbg: communicator is IASTUnaryExpression"); //$NON-NLS-1$
							comm_ = iastUnaryExpression.getRawSignature();

						} else {
							// last resort: use a unique name, but it won't
							// match anything??
							comm_ = "COMM_" + commCounter; //$NON-NLS-1$
							commCounter++;
						}
					}
				} else {
					if (commOp instanceof IASTCastExpression) {// MAC OSX Openmpi 1.3.3 ;mpich2
						IASTCastExpression iastCastExpression = (IASTCastExpression) commOp;
						comm_ = iastCastExpression.getRawSignature();
					}
					else {
						comm_ = "COMM_" + commCounter; //$NON-NLS-1$
						commCounter++;
					}

				}
			} else if (parameter instanceof IASTIdExpression) {// windows mpich 1.2
				IASTIdExpression idE = (IASTIdExpression) parameter;
				comm_ = idE.getName().toString();

				/*
				 * BRT 9/9/09: why hide the actual name? no non-mpi-comm-world comms will match!
				 * if (!comm_.equals("MPI_COMM_WORLD")) {
				 * comm_ = "COMM_" + commCounter;
				 * commCounter++;
				 * }
				 */
			} else if (parameter instanceof IASTLiteralExpression) {// added 9/9/09 for windows/mpich
				IASTLiteralExpression iastLiteralExpression = (IASTLiteralExpression) parameter;

				String str = iastLiteralExpression.getRawSignature();
				comm_ = iastLiteralExpression.getRawSignature();
			}
			else {
				comm_ = "COMM_" + commCounter; //$NON-NLS-1$
				commCounter++;
			}
			if (dbg_barrier)
				System.out.println("setComm(): communicator: " + comm_); //$NON-NLS-1$
		}

		protected void setSourceInfo() {
			IASTExpression funcNameE = barrier_.getFunctionNameExpression();
			IASTNodeLocation[] locations = funcNameE.getNodeLocations();
			if (locations.length == 1) {
				IASTFileLocation astFileLocation = null;
				if (locations[0] instanceof IASTFileLocation) {
					astFileLocation = (IASTFileLocation) locations[0];
					fileName_ = astFileLocation.getFileName();
					// System.out.println(fileName_);
					sourceInfo_ = new SourceInfo();
					sourceInfo_.setStartingLine(astFileLocation.getStartingLineNumber());
					sourceInfo_.setStart(astFileLocation.getNodeOffset());
					sourceInfo_.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
					sourceInfo_.setConstructType(Artifact.FUNCTION_CALL);
				}
			}
		}

		public String getComm() {
			return comm_;
		}

		public IASTFunctionCallExpression getFunc() {
			return barrier_;
		}

		public int getID() {
			return id;
		}

		public String getFileName() {
			return fileName_;
		}

		public SourceInfo getSourceInfo() {
			return sourceInfo_;
		}

		public List<BarrierInfo> getMatchingSet() {
			return matchingSet_;
		}

		public IResource getResource() {
			return resource_;
		}

		public String getEnclosingFunc() {
			return enclosingFunc_;
		}
	}

}
