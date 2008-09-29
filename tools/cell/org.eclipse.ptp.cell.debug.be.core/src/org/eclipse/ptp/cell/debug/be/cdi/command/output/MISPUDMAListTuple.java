/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.cdi.command.output;

import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIValue;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 * 
 */
public class MISPUDMAListTuple {
	
	public static final String EMPTY = ""; //$NON-NLS-1$

	public static final String DMA_OPCODE = "opcode"; //$NON-NLS-1$
	
	public static final String DMA_TAG = "tag"; //$NON-NLS-1$
	
	public static final String DMA_TID = "tid"; //$NON-NLS-1$
	
	public static final String DMA_RID = "rid"; //$NON-NLS-1$
	
	public static final String DMA_EA = "ea"; //$NON-NLS-1$
	
	public static final String DMA_LSA = "lsa"; //$NON-NLS-1$
	
	public static final String DMA_SIZE = "size"; //$NON-NLS-1$
	
	public static final String DMA_LSTADDR = "lstaddr"; //$NON-NLS-1$
	
	public static final String DMA_LSTSIZE = "lstsize"; //$NON-NLS-1$
	
	public static final String DMA_ERROR_P = "error_p"; //$NON-NLS-1$
	
	private String opcode;
	
	private String tag;
	
	private String tid;
	
	private String rid;
	
	private String ea;
	
	private String lsa;
	
	private String size;
	
	private String lstaddr;
	
	private String lstsize;
	
	private String error_p;
	
	
	public MISPUDMAListTuple(String opcode, String tag, String tid, String rid, String ea, String lsa, String size, String lstaddr, String lstsize, String error_p) {
		this.opcode = opcode;
		this.tag = tag;
		this.tid = tid;
		this.rid = rid;
		this.ea = ea;
		this.lsa = lsa;
		this.size = size;
		this.lstaddr = lstaddr;
		this.lstsize = lstsize;
		this.error_p = error_p;
	}

	public MISPUDMAListTuple(MIResult[] results) {
		parseResults(results);
	}
	
	protected void parseResults(MIResult[] results) {
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			if (var.equals(DMA_OPCODE)) {
				if (value != null && value instanceof MIConst) {
					setOpcode(((MIConst)value).getCString());
				} else {
					setOpcode(EMPTY);
				}
			} else if (var.equals(DMA_TAG)) {
				if (value != null && value instanceof MIConst) {
					setTag(((MIConst)value).getCString());
				} else {
					setTag(EMPTY);
				}
			} else if (var.equals(DMA_TID)) {
				if (value != null && value instanceof MIConst) {
					setTid(((MIConst)value).getCString());
				} else {
					setTid(EMPTY);
				}
			} else if (var.equals(DMA_RID)) {
				if (value != null && value instanceof MIConst) {
					setRid(((MIConst)value).getCString());
				} else {
					setRid(EMPTY);
				}
			} else if (var.equals(DMA_EA)) {
				if (value != null && value instanceof MIConst) {
					setEa(((MIConst)value).getCString());
				} else {
					setEa(EMPTY);
				}
			} else if (var.equals(DMA_LSA)) {
				if (value != null && value instanceof MIConst) {
					setLsa(((MIConst)value).getCString());
				} else {
					setLsa(EMPTY);
				}
			} else if (var.equals(DMA_SIZE)) {
				if (value != null && value instanceof MIConst) {
					setSize(((MIConst)value).getCString());
				} else {
					setSize(EMPTY);
				}
			} else if (var.equals(DMA_LSTADDR)) {
				if (value != null && value instanceof MIConst) {
					setLstaddr(((MIConst)value).getCString());
				} else {
					setLstaddr(EMPTY);
				}
			} else if (var.equals(DMA_LSTSIZE)) {
				if (value != null && value instanceof MIConst) {
					setLstsize(((MIConst)value).getCString());
				} else {
					setLstsize(EMPTY);
				}
			} else if (var.equals(DMA_ERROR_P)) {
				if (value != null && value instanceof MIConst) {
					setError_p(((MIConst)value).getCString());
				} else {
					setError_p(EMPTY);
				}
			}
		}
	}

	public String getEa() {
		return ea;
	}

	public void setEa(String ea) {
		this.ea = ea;
	}

	public String getError_p() {
		return error_p;
	}

	public void setError_p(String error_p) {
		this.error_p = error_p;
	}

	public String getLsa() {
		return lsa;
	}

	public void setLsa(String lsa) {
		this.lsa = lsa;
	}

	public String getLstaddr() {
		return lstaddr;
	}

	public void setLstaddr(String lstaddr) {
		this.lstaddr = lstaddr;
	}

	public String getLstsize() {
		return lstsize;
	}

	public void setLstsize(String lstsize) {
		this.lstsize = lstsize;
	}

	public String getOpcode() {
		return opcode;
	}

	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}
	
}
