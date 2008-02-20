/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.perf.tau.papiselect;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;

/**
 * Logic for providing lists of available PAPI counters
 * @author wspear
 *
 */
public class PapiSelect {

	public static final int PRESET=0;
	public static final int NATIVE=1;
	
	private LinkedHashSet avCounters=null;
	private Vector counterNames=new Vector(256);
	private Vector counterDefs=new Vector(256);
	private String location="";
	private int countType=0;
	
	/**
	 * Creates a PapiSelect object that will use the utilities at the given directory to determing
	 * PAPI counter availability
	 * @param papiLocation Directory containing PAPI utilities
	 * @param papiCountType Determines if counters requested are preset or native type
	 */
	public PapiSelect(String papiLocation, int papiCountType){
		location=papiLocation;
		if(papiCountType==PRESET)
			findPresetAvail();
		else
		{
			findNativeAvail();
			countType=NATIVE;
		}
	}
	
	/**
	 * Given a list of already selected and already rejected counters, returns all available remaining counters
	 * */
	public LinkedHashSet getGrey(Object[] checked, Object[] greyed){
		LinkedHashSet active = new LinkedHashSet();
		LinkedHashSet greyset = new LinkedHashSet();
		LinkedHashSet notgrey = new LinkedHashSet(avCounters);
		
		if(checked.length>0)
			active.addAll(Arrays.asList(checked));
		if(greyed !=null && greyed.length>0)
			active.removeAll(Arrays.asList(greyed));
		notgrey.removeAll(active);
		if(greyed !=null && greyed.length>0)
			notgrey.removeAll(Arrays.asList(greyed));
		
		greyset.addAll(getRejects(active));
		
		if(greyed !=null && greyed.length>0)
		{
			greyset.add(Arrays.asList(greyed));
		}
		return greyset;
	}
	
	/**
	 * Gets the list of available counters
	 * @return available counters
	 */
	public LinkedHashSet getAvail(){
		return avCounters;
	}
	
	/**
	 * Gets the list of counter definitions in the same order as returned by getCounterNames
	 * @return counter definitions
	 */
	public Vector getCounterDefs(){
		return counterDefs;
	}
	/**
	 * Gets the list of counter names in the same order as returned by getCounterDefs
	 * @return counter names
	 */
	public Vector getCounterNames(){
		return counterNames;
	}
	
	/**
	 * Returns all (preset) counters available on the system
	 * */
	private void findPresetAvail(){
		String papi_avail=location+File.separator+"papi_avail";
		String s = null;
		
		LinkedHashSet avail = new LinkedHashSet();
		String holdcounter=null;
		
		try {
			Process p = Runtime.getRuntime().exec(papi_avail, null, null);
			
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//read the output from the command
			while ((s = stdInput.readLine()) != null) 
			{
				if(s.indexOf("PAPI_")==0 && s.indexOf("\tYes\t")>0)
				{
					holdcounter=s.substring(0,s.indexOf("\t"));
					avail.add(holdcounter);
					counterNames.add(holdcounter);
					String defCounter=s.substring(s.lastIndexOf("\t")+1);
					int lendex=55;
					int freespace=0;
					while(lendex<defCounter.length()){
						freespace = defCounter.lastIndexOf(' ', lendex-1);
						defCounter=defCounter.substring(0,freespace+1)+'\n'+defCounter.substring(freespace+1);
						lendex+=55;
					}
					counterDefs.add(defCounter);
				}
			}
			boolean fault=false;
			while ((s = stdErr.readLine()) != null) 
			{
				fault=true;
			}
			if(fault){p.destroy(); avCounters= null;}
			
			p.destroy();
		}
		catch (Exception e) {System.out.println(e);}
		avCounters= avail;
	}
	
	/**
	 * Returns all (native) counters available on the system
	 * */
	private void findNativeAvail(){
		String papi_avail=location+File.separator+"papi_native_avail";
		String s = null;
		
		LinkedHashSet avail = new LinkedHashSet();
		String holdcounter=null;
		try {
			Process p = Runtime.getRuntime().exec(papi_avail, null, null);
			
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((s = stdInput.readLine()) != null) 
			{
				if(s.indexOf("   0x")>0)
				{
					holdcounter=s.substring(0,s.indexOf(" "));
					avail.add(holdcounter);
					counterNames.add(holdcounter);
					String defCounter=s.substring(s.lastIndexOf("   ")+3);
					int lendex=55;
					int freespace=0;
					while(lendex<defCounter.length()){
						freespace = defCounter.lastIndexOf(' ', lendex-1);
						defCounter=defCounter.substring(0,freespace+1)+'\n'+defCounter.substring(freespace+1);
						lendex+=55;
					}
					counterDefs.add(defCounter);
				}
			}
			boolean fault=false;
			while ((s = stdErr.readLine()) != null) 
			{
				fault=true;
			}
			if(fault){p.destroy(); avCounters = null;}
			
			p.destroy();
		}
		catch (Exception e) {System.out.println(e);}
		avCounters = avail;
	}
	
	/**
	 * Returns the set of counters rejected given the selected set
	 * */
	private LinkedHashSet getRejects(LinkedHashSet selected)
	{
		int entryIndex=14;
		int entryLines=1;
		
		if(countType==1)
		{
			entryIndex=13;
			entryLines=5;
		}
		
		String counterString="PRESET";
		if(countType!=0)
			counterString="NATIVE";
			
		String papi_event_chooser = location+File.separator+"papi_event_chooser "+counterString;
		if(selected!=null && selected.size()>0)
		{
			Iterator itsel=selected.iterator();
			while(itsel.hasNext())
			{
				papi_event_chooser += " "+(String)itsel.next();
			}
		}
		else
		{
			return new LinkedHashSet(1);
		}
		
		String s=null;
		LinkedHashSet result = new LinkedHashSet(avCounters);
		result.removeAll(selected);
		try {
			Process p = Runtime.getRuntime().exec(papi_event_chooser, null, null);
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			int countLines=0;
			int tabDex=0;
			while ((s = stdInput.readLine()) != null) 
			{
				countLines++;
				if(countLines>=entryIndex&&(countLines-entryIndex)%entryLines==0)
				{
					tabDex=s.indexOf("\t");
					if(tabDex==-1)
					{	
						if(countLines==entryIndex)
						{
							
						}
						countLines=0;
					}
					else
					{
						result.remove(s.substring(0,tabDex));
					}
				}
			}

			while ((s = stdErr.readLine()) != null) 
			{
			}
			p.destroy();
		}
		catch (Exception e) {System.out.println(e);}
		
		return result;
	}
}