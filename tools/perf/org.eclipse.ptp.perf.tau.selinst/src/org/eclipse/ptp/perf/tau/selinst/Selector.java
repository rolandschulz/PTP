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
package org.eclipse.ptp.perf.tau.selinst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IFunctionDeclaration;

/**
 * Manages reading, writing and adding/removing commands to selective instrumentation files
 * @author wspear
 *
 */
public class Selector {
	

	private LinkedHashSet routInc;
	private LinkedHashSet routEx;
	private LinkedHashSet fileInc;
	private LinkedHashSet fileEx;
	private LinkedHashSet instSec;
	private String selString;
	
	/**
	 * Initializes a new selector object
	 * @param path path to the selective instrumentation file to be used or created
	 */
	public Selector(String path){
		routInc=new LinkedHashSet();
		routEx=new LinkedHashSet();
		fileInc=new LinkedHashSet();
		fileEx=new LinkedHashSet();
		instSec=new LinkedHashSet();
		selString=path+File.separator+"tau.selective";
		readSelFile();
		
	}
	
	public static String getRoutine(IFunctionDeclaration fun)
	{
		return "routine=\""+getFullSigniture(fun)+"\"";
	}
	
	public static String getFullSigniture(IFunctionDeclaration fun)
	{
		String returntype = Selector.fixStars(fun.getReturnType());
		String signature;
		try {
			signature = Selector.fixStars(fun.getSignature());
			return returntype+" "+signature+"#";
		} catch (CModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		
	}
	
	/**
	 * Spaces out star (*) characters so function signatures can be read by TAU's selective instrumentation file parser
	 * @param signature function signature
	 * @return corrected function signature
	 */
	public static String fixStars(String signature){
		int star = signature.indexOf('*');
		
		while(star>=1){
			if(signature.charAt(star-1)!='*')
			{
				signature=signature.substring(0,star)+" "+signature.substring(star);
				star++;
			}
			star=signature.indexOf('*', star+1);
		}
		return signature;
	}
	
	/**
	 * Adds the given set of files to the include list of this object's selective instrumentation 
	 * file and removes them from its exclude list if they are present
	 * @param incfiles The set of routines to be included
	 */
	public void includeFile(HashSet incfiles){
		fileInc.addAll(incfiles);
		fileEx.removeAll(incfiles);
		writeSelFile();
	}
	
	/**
	 * Adds the given set of files to the exclude list of this object's selective instrumentation 
	 * file and removes them from its include list if they are present
	 * @param exfiles The set of routines to be excluded
	 */
	public void excludeFile(HashSet exfiles){
		fileEx.addAll(exfiles);
		fileInc.removeAll(exfiles);
		writeSelFile();
	}
	
	/**
	 * Adds the given set of routines to the include list of this object's selective instrumentation 
	 * file and removes them from its exclude list if they are present
	 * @param incrouts The set of routines to be included
	 */
	public void includeRout(HashSet incrouts){
		routInc.addAll(incrouts);
		routEx.removeAll(incrouts);
		writeSelFile();
	}
	
	/**
	 * Adds the given set of routines to the exclude list of this object's selective instrumentation 
	 * file and removes them from its include list if they are present
	 * @param exrouts The set of routines to be excluded
	 */
	public void excludeRout(HashSet exrouts){
		routEx.addAll(exrouts);
		routInc.removeAll(exrouts);
		writeSelFile();
	}
	
	/**
	 * Adds the given list of instrumentation commands to this object's selective instrumentation file
	 * @param instlines The list of instrument commands to add
	 */
	public void addInst(HashSet instlines){
		instSec.addAll(instlines);
		writeSelFile();
	}
	
	/**
	 * Removes the given set of 'instrument' and/or 'exclude' file commands from this Selector's selective instrumentation file
	 * @param remfile The set of 'instrument' and/or 'eclude' file commands to be removed
	 */
	public void clearFile(HashSet remfile){
		fileInc.removeAll(remfile);
		fileEx.removeAll(remfile);
		
		writeSelFile();
	}
	
	/**
	 * Removes the given set of 'instrument' and/or 'exclude' routine commands from this Selector's selective instrumentation file
	 * @param remrouts The set of 'instrument' and/or 'eclude' routine commands to be removed
	 */
	public void clearRout(HashSet remrouts){
		routInc.removeAll(remrouts);
		routEx.removeAll(remrouts);
		
		writeSelFile();
	}
	
	
	public void clearInstrumentSection(HashSet elementNames)
	{
		Iterator elementIt=elementNames.iterator();
		Iterator selectiveIt;
		HashSet toRemove=new HashSet();
		String curElement="";
		String curSelLine="";
		while(elementIt.hasNext())
		{
			curElement=(String)elementIt.next();
			selectiveIt=instSec.iterator();
			while(selectiveIt.hasNext())
			{
				curSelLine=(String)selectiveIt.next();
				if(curSelLine.indexOf(curElement)>=0)
				{
					toRemove.add(curSelLine);
				}
			}
		}
		instSec.removeAll(toRemove);
		writeSelFile();
	}
	
	
	/**
	 * Removes the indicated selective instrumentation commands from the 
	 * 'selective instrumentation' section from this Selector's selective instrumentation file
	 * @param remlines The set of selective instrumentation commands to be removed
	 */
	public void clearGenInst(HashSet remlines){
		try{
		Iterator remit = remlines.iterator();
		Iterator removal;
		HashSet removethese=new HashSet();
		String remtem ="";
		String remcan = "";
		while(remit.hasNext())
		{
			remtem = ((String)remit.next());
			removal = instSec.iterator();
			while(removal.hasNext())
			{
				remcan=(String)removal.next();
				if(remcan.indexOf(remtem)==0)
				{
					removethese.add(remcan);
				}
			}
		}
		instSec.removeAll(removethese);
		writeSelFile();
		}catch(Exception e){e.printStackTrace();}
	}
	
	/**
	 * Removes the given set of 'instrument' commands from this Selector's selective instrumentation file
	 * @param remlines The set of 'instrument' commands to be removed
	 */
	public void remInst(HashSet remlines){
		
		instSec.removeAll(remlines);
		writeSelFile();
		
	}
	
	/**
	 * Reads a complete selective instrumentation file, with individual selection types being placed in their respective sets
	 *
	 */
	private void readSelFile()
	{
		try
		{
			//WorkspaceDescription.
			File selfile = new File(selString);
			
			if(!selfile.exists())
			{
				return;
			}
			BufferedReader in = new BufferedReader (new FileReader(selfile));
			String ourline=in.readLine();
			while(ourline!=null)
			{
				if(ourline.equals("BEGIN_EXCLUDE_LIST"))
				{
					ourline=in.readLine();
					while(!ourline.equals("END_EXCLUDE_LIST")&&!ourline.equals(null))
					{
						routEx.add(ourline);
						ourline=in.readLine();
					}
				}
				
				if(ourline.equals("BEGIN_INCLUDE_LIST"))
				{
					ourline=in.readLine();
					while(!ourline.equals("END_INCLUDE_LIST")&&!ourline.equals(null))
					{
						routInc.add(ourline);
						ourline=in.readLine();
					}
				}
				
				if(ourline.equals("BEGIN_FILE_INCLUDE_LIST"))
				{
					ourline=in.readLine();
					while(!ourline.equals("END_FILE_INCLUDE_LIST")&&!ourline.equals(null))
					{
						fileInc.add(ourline);
						ourline=in.readLine();
					}
				}
				
				if(ourline.equals("BEGIN_FILE_EXCLUDE_LIST"))
				{
					ourline=in.readLine();
					while(!ourline.equals("END_FILE_EXCLUDE_LIST")&&!ourline.equals(null))
					{
						fileEx.add(ourline);
						ourline=in.readLine();
					}
				}
				
				if(ourline.equals("BEGIN_INSTRUMENT_SECTION"))
				{
					ourline=in.readLine();
					while(!ourline.equals("END_INSTRUMENT_SECTION")&&!ourline.equals(null))
					{
						instSec.add(ourline);
						ourline=in.readLine();
					}
				}
				ourline=in.readLine();
			}
		}
		catch(IOException e){e.printStackTrace();}
	}

	/**
	 * Writes a complete selective instrumentation file, with section content provided by the individual sets provided
	 *
	 */
	private void writeSelFile()
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(selString));
		
			out.write("#Generated by the TAU PTP plugin\n");
			if(routEx.size()>0)
			{
				out.write("BEGIN_EXCLUDE_LIST\n");
				Iterator routExIt=routEx.iterator();
				while(routExIt.hasNext())
					out.write((String)routExIt.next()+"\n");
				out.write("END_EXCLUDE_LIST\n");
			}
		
			if(routInc.size()>0)
			{
				out.write("BEGIN_INCLUDE_LIST\n");
				Iterator routIncIt = routInc.iterator();
				
				while(routIncIt.hasNext())
					out.write((String)routIncIt.next()+"\n");
				out.write("END_INCLUDE_LIST\n");
			}
		
			if(fileEx.size()>0)
			{
				out.write("BEGIN_FILE_EXCLUDE_LIST\n");
				Iterator fileExIt = fileEx.iterator();
				while(fileExIt.hasNext())
					out.write((String)fileExIt.next()+"\n");
				out.write("END_FILE_EXCLUDE_LIST\n");
			}
		
			if(fileInc.size()>0)
			{
				out.write("BEGIN_FILE_INCLUDE_LIST\n");
				Iterator fileIncIt = fileInc.iterator();
				while(fileIncIt.hasNext())
					out.write((String)fileIncIt.next()+"\n");
				out.write("END_FILE_INCLUDE_LIST\n");
			}
			
			if(instSec.size()>0)
			{
				out.write("BEGIN_INSTRUMENT_SECTION\n");
				Iterator instSecIt=instSec.iterator();
				while(instSecIt.hasNext())
					out.write((String)instSecIt.next()+"\n");
				out.write("END_INSTRUMENT_SECTION\n");
			}
			out.close();
		}
		catch (IOException e) {e.printStackTrace();}
	}
}
