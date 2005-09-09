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

package org.eclipse.ptp.core;

public class ControlSystemChoices {
	
	/* HOWTO ADD A NEW CS:
	 * 1: Add an ID number for the new CS - static public final int . . . 
	 * 2: Add a String name for this CS and put it in the CSChoices[] array
	 * 3: Add the ID variable from step #1 to the CSIDs[] array IN THE SAME
	 *    INDEX PLACE as in step #2
	 * 4: Marvel
	 */
	
	/* ID numbers for each of the CSs */
	static public final int SIMULATED_ID = 100;
	static public final int ORTE = 101;
	static public final int LAMPI = 102;
	static public final int LAMMPI = 103;
	static public final int MPICH1 = 104;
	static public final int MPICH2 = 105;
	
	static private String[] CSChoices = new String[] {
			"Simulated", 
			"Open Runtime Environment (ORTE)",
			"Los Alamos MPI (LAMPI)",
			"LAM-MPI",
			"MPICH 1.x",
			"MPICH 2.x (MPD)" 
	};
	
	static private int[] CSIDs = new int[] {
			SIMULATED_ID,
			ORTE,
			LAMPI,
			LAMMPI,
			MPICH1,
			MPICH2
	};
	
	public static String[] getCSStrings()
	{
		return CSChoices;
	}

	/* returns -1 if not found */
	public static int getCSArrayIndexByName(String CSname)
	{
		int i;
		
		/* find the index number by the name */
		for(i=0; i<CSChoices.length; i++) {
			if(CSname.equals(CSChoices[i])) return i;
		}
		
		return -1;
	}
	
	public static int getCSArrayIndexByID(int ID)
	{
		int i;
		
		for(i=0; i<CSIDs.length; i++) {
			if(ID == CSIDs[i]) return i;
		}
		
		return -1;
	}
	
	public static int getCSIDByName(String CSname)
	{
		int idx;
		
		idx = getCSArrayIndexByName(CSname);
		if(idx < 0) return idx;
		
		return CSIDs[idx];
	}
	
	public static int getCSIDByIndex(int idx)
	{

		if(idx < 0 || idx >= CSIDs.length) return -1;
		return CSIDs[idx];
	}
	
	public static String getCSNameByID(int ID)
	{
		int i;
		
		for(i=0; i<CSIDs.length; i++) {
			if(ID == CSIDs[i]) return CSChoices[i];
		}
		
		return "<UNDEFINED CS>";
	}
	
	public static String getCSNameByIndex(int idx)
	{
		if(idx < 0 || idx >= CSChoices.length) return "<UNDEFINED CS>";
		return CSChoices[idx];
	}
}
