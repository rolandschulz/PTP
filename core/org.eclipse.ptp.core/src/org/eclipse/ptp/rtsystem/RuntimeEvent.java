/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.rtsystem;

import org.eclipse.ptp.core.util.BitList;

public class RuntimeEvent {
	private int eventNumber;
	private String text, alttext;
	private String[] keys, values;
	private BitList procsList;
	private int[] procArray;

	//public static final int EVENT_NODE_STATUS_CHANGE = 1;

	public static final int EVENT_PROCESS_OUTPUT = 1;

	public static final int EVENT_JOB_EXITED = 2;

	public static final int EVENT_JOB_STATE_CHANGED = 3;

	public static final int EVENT_NEW_JOB = 4;
	
	public static final int EVENT_NODE_GENERAL_CHANGE = 5;
	
	public static final int EVENT_PROCESS_ATTRIB_CHANGE = 6;

	public RuntimeEvent(int eventNumber) {
		this.eventNumber = eventNumber;
		text = new String("");
	}

	public int getEventNumber() {
		return eventNumber;
	}

	public String getText() {
		return text;
	}

	public String getAltText() {
		return alttext;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setAltText(String text) {
		this.alttext = text;
	}
	
	public void setAttributeKeys(String[] keys) {
		this.keys = keys;
	}
	
	public String[] getAttributeKeys() {
		return keys;
	}
	
	public void setAttributeValues(String[] values) {
		this.values = values;
	}
	
	public String[] getAttributeValues() {
		return values;
	}
	
	public void setProcList(BitList procs) {
		this.procsList = procs;
	}
	
	public BitList getProcList() {
		return procsList;
	}
	
	public void setProcArray(int[] procs) {
		this.procArray = procs;
	}
	
	public int[] getProcArray() {
		return this.procArray;
	}
}
