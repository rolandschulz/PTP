package org.eclipse.ptp.perf.tau.papiselect.papic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class PapiCSelect {
	PapiInfoParser pparser;
	String toolPath;
	private static final String papiApp="papi_xml_event_info";
	SAXParser sp;

	PapiCSelect(String tpath){
		toolPath=tpath+File.separator+papiApp;
		try {
			sp = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		pparser = new PapiInfoParser();
	}



	private EventTree parseETree(InputStream is){
		sp.reset();
		try {
			sp.parse(is, pparser);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pparser.getEventTree();
	}

	public EventTree getEventTree(){
		//String papi_avail=location+File.separator+"papi_avail";
		//String s = null;
		//InputStream is=null;
		EventTree et=null;
		try {
			Process p = Runtime.getRuntime().exec(toolPath, null, null);

			//BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			//is=p.getInputStream();
			pparser.reset();
			et=parseETree(p.getInputStream());//  new FileInputStream(new File(toolPath)));//
			
			//BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

//			boolean fault=false;
//			while ((s = stdErr.readLine()) != null) 
//			{
//			fault=true;
//			}

			p.destroy();
		}
		catch (Exception e) {e.printStackTrace();}
		return et;
	}

	public Set<Integer>[] getAvailable(int component,Set<String> checked){
		Set<Integer>[] index=new HashSet[2];
		pparser.reset();
		EventTree et;
		
		ArrayList<String> cAl = new ArrayList(checked);
		
		cAl.add(0,component+"");
		cAl.add(0,"-c");
		cAl.add(0, toolPath);
		
		
		String[]a=new String[cAl.size()];
		cAl.toArray(a);
		printCommand(a);
		String s;
		Process p=null;
		try {
			p = Runtime.getRuntime().exec(a);
			
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			//boolean fault=false;
			while ((s = stdErr.readLine()) != null) 
			{
			//fault=true;
			System.out.println(s);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		et=parseETree(p.getInputStream());
		p.destroy();
		
		index[0]=((EventSet)et.children.get(0).children.get(0)).fullSet;
		index[1]=((EventSet)et.children.get(0).children.get(1)).fullSet;
		
		return index;
	}
	private static void printCommand(String[] com){
		String s = "";
		
		for(int i=0;i<com.length;i++){
			s+=com[i]+" ";
		}
		
		System.out.println(s);
	}
}
