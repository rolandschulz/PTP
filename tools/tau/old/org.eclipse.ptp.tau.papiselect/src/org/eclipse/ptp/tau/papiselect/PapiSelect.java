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
package org.eclipse.ptp.tau.papiselect;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;

public class PapiSelect {

	private LinkedHashSet avCounters=null;
	//private String defCounters=null;
	private Vector counterNames=new Vector(256);
	private Vector counterDefs=new Vector(256);
	//private LinkedHashSet avNative=null;
	//private String defNative=null;
	//private LinkedHashSet checked = null;
	//private LinkedHashSet greyed = null;
	private String location="";
	private int countType=0;
	
	public PapiSelect(String papiLocation, int papiCountType){
		location=papiLocation;
		if(papiCountType==0)
			findPresetAvail();
		else
		{
			findNativeAvail();
			countType=1;
		}
		//checked=new LinkedHashSet();
		//greyed = new LinkedHashSet();
	}
	
	/*Returns the subset of all good/available counters in test*/
	/*public LinkedHashSet getGood(LinkedHashSet test){
		String reject = "";
		
		//Iterator ittest=test.iterator();
		//while(ittest.hasNext())System.out.println(ittest.next());
		
		while(reject !=null)
		{
			test.remove(reject);
			reject=getReject(test,null);
		}
		return test;
	}*/
	
	/*Given a list of already selected and already rejected counters, returns all available remaining counters*/
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
		//Object temp=null;
		
		//Iterator acti = active.iterator();
		//while(acti.hasNext())System.out.println("In Active: "+acti.next());
		/*
		Iterator checkall = notgrey.iterator();
		while(checkall.hasNext())
		{
			if( (temp=getReject(active,(String)checkall.next()))!=null )
			{
				greyset.add(temp);
				temp=null;
			}
		}*/

		
		
		greyset.addAll(getRejects(active));
		
		//System.out.println("Active: "+active.toString());
		//System.out.println("Grey: "+greyset.toString());
		
		//System.out.println(greyset.size()+" grey entries");
		if(greyed !=null && greyed.length>0)
		{
			greyset.add(Arrays.asList(greyed));
			//System.out.println(greyed[0]);
		}
		//System.out.println(greyset.size()+" total grey entries");
		
		return greyset;
	}
	
	public LinkedHashSet getAvail(){
		return avCounters;
	}
	public Vector getCounterDefs(){
		return counterDefs;
	}
	public Vector getCounterNames(){
		return counterNames;
	}
	/*public String getNativeDefs(){
		return defNative;
	}*/
	/*Returns all (preset) counters available on the system*/
	private void findPresetAvail(){
		String papi_avail=location+File.separator+"papi_avail";
		String s = null;
		
		LinkedHashSet avail = new LinkedHashSet();
		//avail.add("GET_TIME_OF_DAY");
		String holdcounter=null;
		//defCounters="";
		
		try {
			Process p = Runtime.getRuntime().exec(papi_avail, null, null);
			
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//read the output from the command
			//int z=0;
			while ((s = stdInput.readLine()) != null) 
			{
				//System.out.println(s.indexOf("PAPI_")+" "+s.indexOf("\tYes\t"));
				if(s.indexOf("PAPI_")==0 && s.indexOf("\tYes\t")>0)
				{
					holdcounter=s.substring(0,s.indexOf("\t"));
					avail.add(holdcounter);
					//System.out.println(holdcounter);
					//defCounters+=holdcounter+": "+s.substring(s.lastIndexOf("\t"))+"\n";
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
					//System.out.println(z+++" "+holdcounter);
				}
				//System.out.println(s);
			}
			//System.out.println(helpinfo);
			boolean fault=false;
			while ((s = stdErr.readLine()) != null) 
			{
				//System.out.println("ERROR: "+s);
				fault=true;
			}
			if(fault){p.destroy(); avCounters= null;}
			
			p.destroy();
			//return avail;
		}
		catch (Exception e) {System.out.println(e);}
		avCounters= avail;
	}
	
	/*public LinkedHashSet getNativeAvail(){
		return avNative;
	}*/
	/*Returns all (preset) counters available on the system*/
	private void findNativeAvail(){
		String papi_avail=location+File.separator+"papi_native_avail";
		String s = null;
		
		LinkedHashSet avail = new LinkedHashSet();
		//avail.add("GET_TIME_OF_DAY");
		String holdcounter=null;
		//defCounters="";
		
		try {
			Process p = Runtime.getRuntime().exec(papi_avail, null, null);
			
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//read the output from the command
			//int z=0;
			while ((s = stdInput.readLine()) != null) 
			{
				//System.out.println(s);
				//System.out.println(s.indexOf("PAPI_")+" "+s.indexOf("\tYes\t"));
				if(s.indexOf("   0x")>0)//s.indexOf("PAPI_")==0 && s.indexOf("\tYes\t")>0)
				{
					holdcounter=s.substring(0,s.indexOf(" "));
					avail.add(holdcounter);
					//System.out.println(holdcounter);
					//defCounters+=holdcounter+": "+s.substring(s.lastIndexOf("   "))+"\n";
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
					//System.out.println(z+++" "+holdcounter);
				}
				//System.out.println(s);
			}
			//System.out.println(defNative);
			boolean fault=false;
			while ((s = stdErr.readLine()) != null) 
			{
				//System.out.println("ERROR: "+s);
				fault=true;
			}
			if(fault){p.destroy(); avCounters = null;}
			
			p.destroy();
			//return avail;
		}
		catch (Exception e) {System.out.println(e);}
		avCounters = avail;
	}
	
	/*Returns the set of events rejected given the selected set*/
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
		//System.out.println(papi_event_chooser);
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
							
							//System.out.println("No Valid!");
							//result.removeAll(result);
						}
						countLines=0;
					}
					else
					{
						//System.out.println(countLines+" "+s.substring(0,tabDex));
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
	
	
	/*Returns the last unacceptable counter in selected, or null if all counters pass.  If
	 * checkcounter != null it is appended to selected*/
	/*private String getReject(LinkedHashSet selected, String checkcounter){
		String counterString="PRESET";
		if(countType!=0)
			counterString="NATIVE";
			
		String papi_event_chooser = location+File.separator+"papi_event_chooser "+counterString;
		if(selected!=null)
		{
		Iterator itsel=selected.iterator();
		while(itsel.hasNext())
		{
			papi_event_chooser += " "+(String)itsel.next();
		}}
		if(checkcounter!=null)
			papi_event_chooser+=" "+checkcounter;
		
		String s=null;
		String result = null;
		try {
			Process p = Runtime.getRuntime().exec(papi_event_chooser, null, null);
			
			//System.out.println(papi_event_chooser);
			
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//read the output from the command
			//int z=0;
			while ((s = stdInput.readLine()) != null) 
			{
				//System.out.println(s.indexOf("PAPI_")+" "+s.indexOf("\tYes\t"));
				//if(s.indexOf("PAPI_")==0 && s.indexOf("\tYes\t")>0)
			//	{
				//	holdcounter=s.substring(0,s.indexOf("\t"));
					//avail.add(holdcounter);
					//System.out.println(z+++" "+holdcounter);
			//	}
				//System.out.println(s);
			}
		
			//boolean fault=false;
			//System.out.println("error: ");
			while ((s = stdErr.readLine()) != null) 
			{
				//System.out.println(s);
				if(s.indexOf("Event ")==0)
				{
					int start = s.indexOf(" ");
					int stop = s.indexOf(" ",start+1);
					result=s.substring(start+1, stop);
					//System.out.println(result);
				}
			}
			p.destroy();
			//return avail;
		}
		catch (Exception e) {System.out.println(e);}
		return result;
	}*/

	
	/**
	 * @param args
	 */
	/*Testing driver*/
	public static void main(String[] args) {
		String papi_utils="/usr/local/packages/papi/bin/";
		PapiSelect test = new PapiSelect(papi_utils,1);
		
		//String papi_avail = papi_utils+File.separator+"papi_avail";
		LinkedHashSet input = new LinkedHashSet();//test.getAvail();//new LinkedHashSet();
		//System.out.println("Counters Available: "+input.size());
		input.add("FP_ADD_PIPE");
		test.getRejects(input);
		
		/*
		LinkedHashSet input2 = new LinkedHashSet(input);
		LinkedHashSet input3 = new LinkedHashSet(input);
		Iterator i1 = input.iterator();
		Iterator i2;
		Iterator i3;
		String hold1;
		String hold2;
		String hold3;
		String res;
		while(i1.hasNext()){
			hold1=(String)i1.next();
			i2=input2.iterator();
			while(i2.hasNext()){
				hold2=(String)i2.next();
				i3=input3.iterator();
				while(i3.hasNext())
				{
					hold3=(String)i3.next();
					res=test.getReject(null, hold1+" "+hold2+" "+hold3);
					if((res==null)&&(!hold1.equals(hold2)))
						System.out.println(hold1+" vs. "+hold2+" vs."+hold3+" : ");
				}
			}
		}*/
		//System.out.println(test.getReject(input,null));
		//input.add("PAPI_L1_DCmM");
		//getAvail(papi_utils,input);
		//getBad(papi_utils,input,null);
		
	}
}