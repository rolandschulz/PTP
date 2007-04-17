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
package org.eclipse.ptp.tau.selinst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
//import java.util.Vector;
import java.util.LinkedHashSet;

//import org.eclipse.core.internal.resources.ProjectInfo;
//import org.eclipse.core.resources.ResourcesPlugin;

//import org.eclipse.core.internal.resources.Workspace;
//import org.eclipse.core.internal.resources.WorkspaceDescription;

//import org.eclipse.cdt.*;

public class Selector {
	

	private LinkedHashSet routInc;//=new HashSet<String>();
	private LinkedHashSet routEx;//=new Vector<String>();
	private LinkedHashSet fileInc;//=new Vector<String>();
	private LinkedHashSet fileEx;//=new Vector<String>();
	private LinkedHashSet instSec;//=new Vector<String>();
	private String selString;
	
	
	public Selector(String path){
		routInc=new LinkedHashSet();
		routEx=new LinkedHashSet();
		fileInc=new LinkedHashSet();
		fileEx=new LinkedHashSet();
		instSec=new LinkedHashSet();
		//System.out.println("Path "+path);
		selString=path+File.separator+"tau.selective";
		//selString+="tau.selective";
		//System.out.println("File "+selString);
		//Activator.this.
		//System.out.println("AT: "+ResourcesPlugin.getWorkspace().getRoot().);  
		//ResourcesPlugin.getWorkspace().getRoot().
		readSelFile();
		
	}
	
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
	
	public void includeFile(HashSet incfiles){
		fileInc.addAll(incfiles);
		fileEx.removeAll(incfiles);
		//readSelFile();
		writeSelFile();
	}
	
	public void excludeFile(HashSet exfiles){
		fileEx.addAll(exfiles);
		fileInc.removeAll(exfiles);
		//readSelFile();
		writeSelFile();
	}
	
	public void includeRout(HashSet incrouts){
		routInc.addAll(incrouts);
		routEx.removeAll(incrouts);
		//readSelFile();
		writeSelFile();
	}
	
	public void excludeRout(HashSet exrouts){
		routEx.addAll(exrouts);
		routInc.removeAll(exrouts);
		//readSelFile();
		writeSelFile();
	}
	
	public void addInst(HashSet instlines){
		instSec.addAll(instlines);
		writeSelFile();
	}
	
	
	public void clearFile(HashSet remfile){
		fileInc.removeAll(remfile);
		fileEx.removeAll(remfile);
		
		writeSelFile();
	}
	
	public void clearRout(HashSet remrouts){
		routInc.removeAll(remrouts);
		routEx.removeAll(remrouts);
		
		writeSelFile();
	}
	
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
			//System.out.println(remtem);
			//remcan = remcan.substring(0,remcan.indexOf("routine="));
			removal = instSec.iterator();
			while(removal.hasNext())
			{
				remcan=(String)removal.next();
				//System.out.println(remcan+" "+remcan.indexOf(remtem));
				//System.out.println(instSec.contains(remcan));
				if(remcan.indexOf(remtem)==0)
				{
					removethese.add(remcan);
					//instSec.remove(remcan);
					//System.out.println("removing "+remcan);
				}
			}
		}
		instSec.removeAll(removethese);
		writeSelFile();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public void remInst(HashSet remlines){
		
		instSec.removeAll(remlines);
		
		writeSelFile();
		
		/*try{
		instSec.removeAll(remlines);
		Iterator remit = remlines.iterator();
		Iterator removal;
		String remtem ="";
		String remcan = "";
		while(remit.hasNext())
		{
			remtem = ((String)remit.next());
			System.out.println(remtem);
			//remcan = remcan.substring(0,remcan.indexOf("routine="));
			removal = instSec.iterator();
			while(removal.hasNext())
			{
				remcan=(String)removal.next();
				System.out.println(remcan+" "+remcan.indexOf(remtem));
				System.out.println(instSec.contains(remcan));
				if(remcan.indexOf(remtem)==0)
				{
					instSec.remove(remcan);
					System.out.println("removed "+remcan);
				}
			}
		}
		writeSelFile();
		}catch(Exception e){e.printStackTrace();}*/
	}
	
	//public void remRoutLoops(HashSet remlines){
		/*Iterator rem = remlines.iterator();
		while(rem.hasNext())
			System.out.println(rem.next());
		Iterator inst = remlines.iterator();
		while(inst.hasNext())
			System.out.println(inst.next());*/
	//	instSec.removeAll(remlines);
		
	//	writeSelFile();
	//}
	
	//public void ad);
	
	private void readSelFile()
	{
		try
		{
			//WorkspaceDescription.
			File selfile = new File(selString);
			
			if(!selfile.exists())
			{
				//System.out.println("No selfile yet!");
				return;
			}
			BufferedReader in = new BufferedReader (new FileReader(selfile));
			String ourline=in.readLine();
			//System.out.println("Read " +ourline);
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

	private void writeSelFile()
	{
		//org.eclipse.cdt.core.resources.
		try
		{
			//System.out.println("Yo... "+new File(selString).getAbsolutePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(selString));
		
			//String print = "";
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
				//for(int i=0;i<routInc.size();i++)
				//	out.write((String)routInc.get(i)+"\n");
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
