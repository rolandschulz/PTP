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

import org.eclipse.core.runtime.Preferences;

public class MonitoringSystemChoices {
	
	/* HOWTO ADD A NEW MS:
	 * 1: Add an ID number for the new MS - static public final int . . . 
	 * 2: Add a String name for this MS and put it in the MSChoices[] array
	 * 3: Add the ID variable from step #1 to the MSIDs[] array IN THE SAME
	 *    INDEX PLACE as in step #2
	 * 4: Marvel
	 */
	
	/* ID numbers for each of the MSs */
	static public final int SIMULATED = 100;
	static public final int ORTE = 101;
	static public final int LAMPI = 102;
	static public final int LAMMPI = 103;
	static public final int MPICH1 = 104;
	static public final int MPICH2 = 105;
	
	static private String[] NonDevelMSChoices = new String[] {
			"Open Runtime Environment (ORTE)",
			/*
			"Los Alamos MPI (LAMPI)",
			"LAM-MPI",
			"MPICH 1.x",
			"MPICH 2.x (MPD)"
			*/ 
	};
	
	static private int[] NonDevelMSIDs = new int[] {
			ORTE,
			/*
			LAMPI,
			LAMMPI,
			MPICH1,
			MPICH2
			*/
	};
	
	static private String[] DevelMSChoices = new String[] {
		"Simulated", 
		"Open Runtime Environment (ORTE)",
		/*
		"Los Alamos MPI (LAMPI)",
		"LAM-MPI",
		"MPICH 1.x",
		"MPICH 2.x (MPD)"
		*/ 
};

	static private int[] DevelMSIDs = new int[] {
		SIMULATED,
		ORTE,
		/*
		LAMPI,
		LAMMPI,
		MPICH1,
		MPICH2
		*/
	};
	
	public static boolean queryDevelMode()
	{
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		return preferences.getBoolean(PreferenceConstants.DEVELOPER_MODE);
	}
	
	public static String[] queryMSChoices()
	{
		if(queryDevelMode()) return DevelMSChoices;
		return NonDevelMSChoices;
	}
	
	public static int[] queryMSIDs()
	{
		if(queryDevelMode()) return DevelMSIDs;
		return NonDevelMSIDs;
	}
	
	
	public static String[] getMSStrings()
	{
		return queryMSChoices();
	}

	/* returns -1 if not found */
	public static int getMSArrayIndexByName(String MSname)
	{
		int i;
		
		/* find the index number by the name */
		for(i=0; i<queryMSChoices().length; i++) {
			if(MSname.equals(queryMSChoices()[i])) return i;
		}
		
		return -1;
	}
	
	public static int getMSArrayIndexByID(int ID)
	{
		int i;
		
		for(i=0; i<queryMSIDs().length; i++) {
			if(ID == queryMSIDs()[i]) return i;
		}
		
		return -1;
	}
	
	public static int getMSIDByName(String MSname)
	{
		int idx;
		
		idx = getMSArrayIndexByName(MSname);
		if(idx < 0) return idx;
		
		return queryMSIDs()[idx];
	}
	
	public static int getMSIDByIndex(int idx)
	{

		if(idx < 0 || idx >= queryMSIDs().length) return -1;
		return queryMSIDs()[idx];
	}
	
	public static String getMSNameByID(int ID)
	{
		int i;
		
		for(i=0; i<queryMSIDs().length; i++) {
			if(ID == queryMSIDs()[i]) return queryMSChoices()[i];
		}
		
		return "<UNDEFINED MS>";
	}
	
	public static String getMSNameByIndex(int idx)
	{
		if(idx < 0 || idx >= queryMSChoices().length) return "<UNDEFINED MS>";
		return queryMSChoices()[idx];
	}
}
