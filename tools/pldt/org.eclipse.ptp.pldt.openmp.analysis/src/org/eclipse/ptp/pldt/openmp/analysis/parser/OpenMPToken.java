/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.parser;

/**
 * Token used in parsing OpenMP pragma line
 * 
 * @author pazel
 */
public class OpenMPToken
{
	private int type_ = tUNDEFINED;
	private String image_ = "";
	private int onset_ = -1;

	// for backtracking
	private OpenMPToken next_ = null;

	/**
	 * OpenMPToken - constructor
	 * 
	 * @param image
	 *            : String
	 * @param onset
	 *            : int
	 * @param type
	 *            : int
	 */
	public OpenMPToken(String image, int onset, int type)
	{
		image_ = image;
		onset_ = onset;
		type_ = type;
	}

	/**
	 * OpenMPToken - constructor
	 * 
	 * @param type
	 *            : int
	 * @param onset
	 *            : int
	 */
	public OpenMPToken(int type, int onset)
	{
		type_ = type;
		onset_ = onset;
	}

	public int getType()
	{
		return type_;
	}

	public String getImage()
	{
		return image_;
	}

	public int getOnset()
	{
		return onset_;
	}

	public int getLength()
	{
		return image_.length();
	}

	public int getEndOnset()
	{
		return onset_ + getLength() - 1;
	}

	public void setNext(OpenMPToken next)
	{
		next_ = next;
	}

	public OpenMPToken getNext()
	{
		return next_;
	}

	// Token types
	static public final int tUNDEFINED = -1;

	static public final int tIDENTIFIER = 1;

	static public final int tINTEGER = 2;

	static public final int tCOLONCOLON = 3;

	static public final int tCOLON = 4;

	static public final int tSEMI = 5;

	static public final int tCOMMA = 6;

	static public final int tQUESTION = 7;

	static public final int tLPAREN = 8;

	static public final int tRPAREN = 9;

	static public final int tLBRACKET = 10;

	static public final int tRBRACKET = 11;

	static public final int tLBRACE = 12;

	static public final int tRBRACE = 13;

	static public final int tPLUSASSIGN = 14;

	static public final int tINCR = 15;

	static public final int tPLUS = 16;

	static public final int tMINUSASSIGN = 17;

	static public final int tDECR = 18;

	static public final int tARROWSTAR = 19;

	static public final int tARROW = 20;

	static public final int tMINUS = 21;

	static public final int tSTARASSIGN = 22;

	static public final int tSTAR = 23;

	static public final int tMODASSIGN = 24;

	static public final int tMOD = 25;

	static public final int tXORASSIGN = 26;

	static public final int tXOR = 27;

	static public final int tAMPERASSIGN = 28;

	static public final int tAND = 29;

	static public final int tAMPER = 30;

	static public final int tBITORASSIGN = 31;

	static public final int tOR = 32;

	static public final int tBITOR = 33;

	static public final int tCOMPL = 34;

	static public final int tNOTEQUAL = 35;

	static public final int tNOT = 36;

	static public final int tEQUAL = 37;

	static public final int tASSIGN = 38;

	static public final int tSHIFTL = 40;

	static public final int tLTEQUAL = 41;

	static public final int tLT = 42;

	static public final int tSHIFTRASSIGN = 43;

	static public final int tSHIFTR = 44;

	static public final int tGTEQUAL = 45;

	static public final int tGT = 46;

	static public final int tSHIFTLASSIGN = 47;

	static public final int tELLIPSIS = 48;

	static public final int tDOTSTAR = 49;

	static public final int tDOT = 50;

	static public final int tDIVASSIGN = 51;

	static public final int tDIV = 52;

	static public final int t_and = 54;

	static public final int t_and_eq = 55;

	static public final int t_asm = 56;

}
