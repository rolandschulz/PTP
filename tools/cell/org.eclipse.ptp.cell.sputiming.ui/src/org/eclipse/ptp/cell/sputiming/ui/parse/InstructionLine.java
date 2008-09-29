/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.cell.sputiming.ui.parse;



/**
 * @author richardm
 *
 */
public class InstructionLine extends TimingFileLine
{

	static public int NO = 1;
	static public int COULDBE = 2;
	static public int YES = 4;
	
	private int    pipelineNumber;
	private int    parallelInfo;
	private String instruction;
	private String pipeTimingInfo;
	
	private TimeInfo timeInfo;
	
	public String toString() {
		return pipelineNumber + " - " + pipeTimingInfo + " ( " + instruction + " ) \n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * @param linenum
	 * @param grpnum
	 * @param pipenum
	 * @param parallelinfo
	 * @param pipeinfo
	 * @param instruction
	 */
	InstructionLine(int linenum, int grpnum, 
			int pipenum, int parallelinfo,
			String pipeinfo, String instruction)
	{
		super(linenum, grpnum);
		this.pipelineNumber = pipenum;
		this.parallelInfo   = parallelinfo;
		this.setPipeTimingInfo(this.pipeTimingInfo);
		this.setInstruction(instruction);
	}
	
	InstructionLine(int linenum, int grpnum)
	{
		super(linenum, grpnum);
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction.trim();
	}

	public int getParallelInfo() {
		return parallelInfo;
	}

	public void setParallelInfo(int parallelInfo) {
		this.parallelInfo = parallelInfo;
	}

	public int getPipelineNumber() {
		return pipelineNumber;
	}

	public void setPipelineNumber(int pipelineNumber) {
		this.pipelineNumber = pipelineNumber;
	}

	public String getPipeTimingInfo() {
		return pipeTimingInfo;
	}

	public void setPipeTimingInfo(String pipeTimingInfo) {
		this.pipeTimingInfo = pipeTimingInfo.trim();
	}

	public void setTimeInfo(TimeInfo timeInfo) {
		this.timeInfo = timeInfo;
	}
	
	public TimeInfo getTimeInfo() {
		return this.timeInfo;
	}
}
