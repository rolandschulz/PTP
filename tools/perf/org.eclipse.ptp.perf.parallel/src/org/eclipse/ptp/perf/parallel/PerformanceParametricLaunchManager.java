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
package org.eclipse.ptp.perf.parallel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.internal.BuildLaunchUtils;
import org.eclipse.ptp.perf.internal.PerfBuilder;
import org.eclipse.ptp.perf.internal.PerfLauncher;
import org.eclipse.ptp.perf.internal.PerfPostlaunch;
import org.eclipse.ptp.perf.internal.PerfStep;
import org.eclipse.ptp.perf.internal.PerformanceLaunchManager;

public class PerformanceParametricLaunchManager extends
		PerformanceLaunchManager {

	private static final String tmp = "org.eclipse.ptp.rm.orte.ui.launchAttributes.numProcs";

	/**
	 * Keeps track of the configurations created for each run so they can be
	 * deleted in sequence
	 */
	final Queue<ILaunchConfiguration> configs = new LinkedList<ILaunchConfiguration>();

	/**
	 * Keeps track of the performance analysis step jobs so they can be
	 * scheduled in sequence
	 */
	final Queue<PerfStep> steps = new LinkedList<PerfStep>();

	/**
	 * inherited constructor
	 * 
	 * @param delegate
	 * @param appNameAtt
	 * @param projNameAtt
	 * @see org.eclipse.ptp.perf.internal.PerformanceLaunchManager
	 */
	public PerformanceParametricLaunchManager(
			LaunchConfigurationDelegate delegate, String appNameAtt,
			String projNameAtt) {
		super(delegate, appNameAtt, projNameAtt);
	}

	/**
	 * inherited constructor
	 * 
	 * @param delegate
	 * @param appNameAtt
	 * @param projNameAtt
	 * @param appPathAtt
	 * @see org.eclipse.ptp.perf.internal.PerformanceLaunchManager
	 */
	public PerformanceParametricLaunchManager(
			LaunchConfigurationDelegate delegate, String appNameAtt,
			String projNameAtt, String appPathAtt) {
		super(delegate, appNameAtt, projNameAtt, appPathAtt);
	}

	/**
	 * One object, two strings
	 * 
	 * @author wspear
	 * 
	 */
	static class StringPair {
		public String getFirst() {
			return first;
		}

		public void setFirst(String first) {
			this.first = first;
		}

		public String getSecond() {
			return second;
		}

		public void setSecond(String second) {
			this.second = second;
		}

		public StringPair(String first, String second) {
			super();
			this.first = first;
			this.second = second;
		}

		private String first;
		private String second;
	}

	/**
	 * Contains the values for the parameters to be appended in a single
	 * execution of a parametric multi-execution
	 * 
	 * @author wspear
	 * 
	 */
	static class RunParams {
		public RunParams(int numProcs, String args, Map<String, String> vars) {
			super();
			this.args = args;
			this.numProcs = numProcs;
			this.vars = vars;
		}

		/**
		 * The number of processors to use
		 */
		int numProcs;
		/**
		 * The string of arguments to append to the application
		 */
		String args;
		/**
		 * The map of environment variables to append to the environment
		 */
		Map<String, String> vars;
	}

	/**
	 * Given two strings of comma-separated substrings returns a list of string
	 * pairs for every possible combination of one substring from the first
	 * string and one substring from the second.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	static List<StringPair> getStringCombinations(String a, String b) {
		List<StringPair> combos = new ArrayList<StringPair>();

		List<String> aList = getComArgs(a);
		List<String> bList = getComArgs(b);

		for (String first : aList) {
			for (String second : bList) {
				combos.add(new StringPair(first, second));
			}
		}

		return combos;
	}

	/**
	 * Creates a list of lists of stringpairs. Each list of stringpairs contains
	 * each combination of substrings of the same-indexed strings from lists a
	 * and b.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	static List<List<StringPair>> getListCombinations(List<String> a,
			List<String> b) {

		List<List<StringPair>> comCom = new ArrayList<List<StringPair>>();

		if (a.size() != b.size()) {
			return comCom;
		}

		for (int i = 0; i < a.size(); i++) {
			comCom.add(getStringCombinations(a.get(i), b.get(i)));
		}

		return comCom;
	}

	static List<List<StringPair>> unifyCombos(
			final List<List<StringPair>> combos, int curDex) {

		List<List<StringPair>> allCom = new ArrayList<List<StringPair>>();

		if (combos.size() == 0) {
			return allCom;
		}

		/**
		 * The real base-case. When we hit the final index we just want to
		 * return with the last sets of combinations
		 */
		if (curDex == combos.size() - 1) {

			for (int i = 0; i < combos.get(curDex).size(); i++) {
				ArrayList<StringPair> jList = new ArrayList<StringPair>();
				jList.add(0, combos.get(curDex).get(i));
				allCom.add(jList);
			}

			return allCom;// new ArrayList<List<StringPair>>();// allCom=new
							// ArrayList<StringPair>();
		}

		List<List<StringPair>> someCom = unifyCombos(combos, curDex + 1);

		for (int i = 0; i < combos.get(curDex).size(); i++) {
			for (int j = 0; j < someCom.size(); j++) {
				ArrayList<StringPair> jList = new ArrayList<StringPair>(someCom
						.get(j));
				jList.add(0, combos.get(curDex).get(i));
				allCom.add(jList);
			}
		}

		return allCom;
	}

	/**
	 * Given a list of lists of string pairs, returns a list of strings where
	 * each string is the concatenated values of one of the lists of string
	 * pairs.
	 * 
	 * @param combos
	 * @return
	 */
	static List<String> getComboStrings(List<List<StringPair>> combos) {
		List<String> allCom = new ArrayList<String>();

		for (List<StringPair> sl : combos) {
			String agg = "";
			for (StringPair sp : sl) {
				agg += sp.first + sp.second + " ";
			}
			allCom.add(agg);
		}

		return allCom;
	}

	/**
	 * Given a list of lists of string pairs, converts it into a list of maps,
	 * where each map contains the mapped values of the first string in a pair
	 * to the second for each StringPair in one of the lists.
	 * 
	 * @param combos
	 * @return
	 */
	static List<Map<String, String>> getComboMaps(List<List<StringPair>> combos) {
		List<Map<String, String>> allCom = new ArrayList<Map<String, String>>();

		for (List<StringPair> sl : combos) {
			Map<String, String> agg = new LinkedHashMap<String, String>();
			for (StringPair sp : sl) {
				agg.put(sp.first, sp.second);
			}
			allCom.add(agg);
		}

		return allCom;
	}

	
	/**
	 * Given a string representing all desired values for the number of
	 * processors, and 4 arrays of strings representing the desired values for
	 * program argument names and values and environment variable names and
	 * values, returns an array of RunParams containing one RunParams for each
	 * unique combination of the presented values.
	 * 
	 * @param procs
	 * @param argNames
	 * @param argVars
	 * @param envNames
	 * @param envVars
	 * @return
	 */
	static List<RunParams> getWeakParams(String procs, List<String> argNames,
			List<String> argVars, List<String> envNames, List<String> envVars) {
		List<RunParams> params = new ArrayList<RunParams>();

		List<String> numProcs = getComArgs(procs);
		
		//int i=0;
		
		
		/*
		 * Get list combinations returns a list of one list of string pairs per
		 * argument, unify combos transforms that
		 */

		//List<String> args = new ArrayList<String>();

		//ArrayList<HashMap<String, String>> vars = new ArrayList<HashMap<String,String>>();

		for (int i=0;i<numProcs.size();i++) {
			String num=numProcs.get(i);
			String arg=null;
			Map<String,String> var=null;

			for (int j=0;j<argNames.size();j++) {
				if(arg==null){
					arg="";
				}
				List<String> av=getComArgs(argVars.get(j));
				arg+=" "+argNames.get(j)+av.get(i);

			}

			for (int j=0;j<envNames.size();j++) {
				if(var==null){
					var=new HashMap<String,String>();
				}
				List<String> ev=getComArgs(envVars.get(j));
				var.put(envNames.get(j),ev.get(i));
			}
			params.add(new RunParams(Integer.parseInt(num), arg, var));
		}

		return params;
	}
	
	
	/**
	 * Given a string representing all desired values for the number of
	 * processors, and 4 arrays of strings representing the desired values for
	 * program argument names and values and environment variable names and
	 * values, returns an array of RunParams containing one RunParams for each
	 * unique combination of the presented values.
	 * 
	 * @param procs
	 * @param argNames
	 * @param argVars
	 * @param envNames
	 * @param envVars
	 * @return
	 */
	static List<RunParams> getRunParams(String procs, List<String> argNames,
			List<String> argVars,List<String> argBools, List<String> envNames, List<String> envVars,List<String> envBools) {
		List<RunParams> params = new ArrayList<RunParams>();

		List<String> numProcs = getComArgs(procs);

		List<StringPair> checkedArgs=new ArrayList<StringPair>();
		
		int i=0;
		
		while(i<argBools.size()){
			if(argBools.get(i).equals("1")){
				argBools.remove(i);
				checkedArgs.add(new StringPair(argNames.remove(i),argVars.remove(i)));
			}
			else{
				i++;
			}
		}
		
		List<StringPair> checkedVars=new ArrayList<StringPair>();
		
		i=0;
		
		while(i<envBools.size()){
			if(envBools.get(i).equals("1")){
				envBools.remove(i);
				checkedVars.add(new StringPair(envNames.remove(i),envVars.remove(i)));
			}
			else{
				i++;
			}
		}
		
		/*
		 * Get list combinations returns a list of one list of string pairs per
		 * argument, unify combos transforms that
		 */

		List<String> args = getComboStrings(unifyCombos(getListCombinations(
				argNames, argVars), 0));

		List<Map<String, String>> vars = getComboMaps(unifyCombos(
				getListCombinations(envNames, envVars), 0));

		if (args.size() == 0) {
			args.add(null);
		}
		if (vars.size() == 0) {
			vars.add(null);
		}

		for (i=0;i<numProcs.size();i++) {
			String num=numProcs.get(i);
			for (String arg : args) {
				for (Map<String, String> var : vars) {
					
					if(checkedArgs.size()>0){
						if(arg==null){
							arg="";
						}
						for(StringPair sp:checkedArgs){
							arg+=" "+sp.getFirst()+getComArgs(sp.getSecond()).get(i);
						}
					}
					
					if(checkedVars.size()>0){
						if(var==null){
							var=new HashMap<String,String>();
						}
						for(StringPair sp:checkedVars){
							var.put(sp.getFirst(),getComArgs(sp.getSecond()).get(i));
						}
					}
					
					params.add(new RunParams(Integer.parseInt(num), arg, var));
				}
			}
		}

		return params;
	}

	/**
	 * Given a string of comma separated strings, returns an array of the
	 * strings
	 * 
	 * @param combined
	 *            The string to be tokenized by commas
	 * @return
	 */
	static List<String> getComArgs(String combined) {
		StringTokenizer st = new StringTokenizer(combined, ",");
		List<String> numProcesses = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			numProcesses.add(st.nextToken());
		}
		return numProcesses;
	}

	
	public static String makeTauAtt(String name,String value){
		String att="\n<tau:attribute>\n<tau:name>"+name+"</tau:name>\n<tau:value>"+value+"</tau:value>\n</tau:attribute>";
		return att;
	}
	
	public static String mapToString(Map<String,String> map){
		String out="";
		
		Iterator<Entry<String,String>> it = map.entrySet().iterator();
		Entry<String,String> e;
		while(it.hasNext()){
			e=it.next();
			out+=e.getKey()+"="+e.getValue();
			if(it.hasNext()){
				out+=", ";
			}
		}
		
		return out;
	}
	
	public static String makeTauAtts(Map<String,String> map, String prefix){
		String atts="";
		
		Iterator<Entry<String,String>> it = map.entrySet().iterator();
		Entry<String,String> e;
		while(it.hasNext()){
			e=it.next();
			atts+=makeTauAtt(prefix+e.getKey(),e.getValue());
		}
		
		return atts;
	}
	
	public static String getTauMetadata(Map<String,String>build,RunParams par){
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tau:metadata xmlns:tau=\"http://www.cs.uoregon.edu/research/tau\">\n<tau:CommonProfileAttributes>";
		
		if(build!=null)
			xml+=makeTauAtts(build,"BUILD:");
		
		if(par.args!=null)
			xml+=makeTauAtt("ApplicationArguments",par.args);
		
		if(par.vars!=null)
			xml+=makeTauAtts(par.vars, "ENV:");
		
		xml+="\n</tau:CommonProfileAttributes>\n</tau:metadata>";
		
		return xml;
	}
	
//private boolean finalRun=false;
	
	/**
	 * The number of runs that must complete before the last set
	 */
	//private int firstRuns=0;
	//private int curRuns=0;
	//private int topRun=-1;
	
	/**
	 * Manages job execution order
	 */
	JobChangeAdapter tauChange = new JobChangeAdapter() {
		public void done(IJobChangeEvent event) {

			// TODO: Add checks for successful compilation/execution
//			if (event.getJob() instanceof PerfPostlaunch)
//			{
//				curRuns++;//TODO: This is not the right place to increment this!
//			}
			//System.out.println("FR: "+firstRuns+" CR: "+curRuns);
			if (event.getJob() instanceof PerfPostlaunch){// && curRuns>=topRun) {
				try {
					configs.poll().delete();
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}

			if (!steps.isEmpty()) {
				System.out.println("About to run job " + steps.size() + " "
						+ steps.element().getName());
				steps.poll().schedule();
				System.out.println("Done.  Jobs left: " + steps.size());
			}
		}
	};

	
	/**
	 * The primary launch command of this launch configuration delegate. The
	 * operations in this function are divided into three jobs: Building,
	 * Running and Data collection
	 */
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launchIn, IProgressMonitor monitor) throws CoreException // ,
																				// TAULaunch
																				// bLTool
	{

		// here is where the loop(s) for the parametric study should go - for
		// runtime parameters, like #procs
		String processorOptionString = configuration.getAttribute(
				IPerformanceLaunchConfigurationConstants.PARA_NUM_PROCESSORS,
				"1");// "1,2";//,4,8";
		// TODO: validate this string first?
		List<String> argNames = configuration.getAttribute(IPerformanceLaunchConfigurationConstants.PARA_ARG_NAMES,(List) null);
		List<String> argVars = configuration.getAttribute(
				IPerformanceLaunchConfigurationConstants.PARA_ARG_VALUES,
				(List) null);
		
		List<String> argBools = configuration.getAttribute(
				IPerformanceLaunchConfigurationConstants.PARA_ARG_BOOLS,
				(List) null);
		
		List<String> varNames = configuration.getAttribute(
				IPerformanceLaunchConfigurationConstants.PARA_VAR_NAMES,
				(List) null);
		List<String> varVars = configuration.getAttribute(
				IPerformanceLaunchConfigurationConstants.PARA_VAR_VALUES,
				(List) null);
		List<String> varBools = configuration.getAttribute(
				IPerformanceLaunchConfigurationConstants.PARA_VAR_BOOLS,
				(List) null);
		
		String timestamp=BuildLaunchUtils.getNow();
		
		boolean allCom=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.PARA_ALL_COMBO, false);

		List<RunParams> params = null;
		
		if(!allCom){
			params=getWeakParams(processorOptionString,
					argNames, argVars, varNames, varVars);
		}
		else{
			params=getRunParams(processorOptionString,
				argNames, argVars, argBools, varNames, varVars, varBools);
		}
		
		System.out.println(params.size());
		for (RunParams param : params) {
			System.out.println("Num Processors: " + param.numProcs);
			if (param.args != null)
				System.out.println("Program args: " + param.args);
			System.out.println("Env args: ");
			if (param.vars != null)
				for (Map.Entry<String, String> env : param.vars.entrySet()) {
					System.out.println(env);
				}
		}

		
		String optLevStr = configuration.getAttribute(
				IPerformanceLaunchConfigurationConstants.PARA_OPT_LEVELS,
				"");
		
		List<String> optLevs=getComArgs(optLevStr);
		
		List<Map<String,String>> buildopts = new ArrayList<Map<String,String>>();
		if(optLevs.size()==0){
			buildopts.add(null);
		}
		else
		{
			for(String s:optLevs){
				String lev=null;
				Map<String,String> hm=null;
				switch(Integer.parseInt(s)){
					case 0: lev= "None (-O0)";break;
					case 1: lev= "Optimize (-O1)";break;
					case 2: lev= "Optimize more (-O2)";break;
					case 3: lev= "Optimize most (-O3)";break;
					default: continue;
				}
				if(lev!=null)
				{
					hm=new HashMap<String,String>();
					hm.put("Optimization Level", lev);
					buildopts.add(hm);
				}
			}
		}
		
		
		
//		{
//			firstRuns=buildopts.size()-1*params.size();
//		}

		int numruns=0;
		// here is where the outer loop for the parametric study should go - for
		// build parameters, like optimization.
		for (int bDex=0;bDex<buildopts.size();bDex++) {

			Map<String,String> optM =buildopts.get(bDex);
			final PerfBuilder builder = new PerfBuilder(configuration,
					projNameAttribute, appPathAttribute,optM);

//			if(optM!=null)
//				builder.setBuildMods(optM);
			// builder.setSomeAtt(str);


			steps.add(builder);
			builder.addJobChangeListener(tauChange);

			 //builder.schedule();
			
			 try {
			 builder.join();
			 } catch (InterruptedException e) {
			 e.printStackTrace();
			 }

			//int i = 0; 
			for (int lDex=0;lDex< params.size();lDex++) {
				
//				if(bDex==buildopts.size()-1&&topRun==-1)
//				{
//					/*
//					 * This should be the first run at which it is safe to start deleting build configurations.
//					 */
//					topRun=numruns;
//				}
				
				RunParams param =params.get(lDex);
				
				int numProcs = param.numProcs;

				System.out.println("doing " + numProcs + " processors...");

				// get a working copy
				ILaunchConfigurationWorkingCopy wc = configuration
						.copy(configuration.getName() + " ParameterSet_" + numruns);
				// TODO: need this constant!
				wc.setAttribute(tmp, numProcs);
				
				wc.setAttribute(IPerformanceLaunchConfigurationConstants.PERF_XML_METADATA, getTauMetadata(optM,param));
				
				wc.setAttribute(IPerformanceLaunchConfigurationConstants.PERF_EXPERIMENT_APPEND, timestamp);

				/* Set up application arguments for this run */
				if (param.args != null) {
					String arg = wc
							.getAttribute(
									IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS,
									"");// ICDTLaunchConfigurationConstants.
										// ATTR_PROGRAM_ARGUMENTS
					arg += " " + param.args;
					wc.setAttribute(
							IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS,
							arg);
				}

				/* Set up environment variables for this run */
				if (param.vars != null) {
					Map<String, Object> envvars = null;
					envvars = wc.getAttribute(
							ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
							(Map) null);
					if (envvars == null) {
						envvars = new HashMap<String, Object>();
					}
					envvars.putAll(param.vars);
					wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
							envvars);
				}
				
				/*The final analysis run should also launch perfexplorer*/
				wc.setAttribute(IPerformanceLaunchConfigurationConstants.PERF_LAUNCH_PERFEX, false);
				if(bDex==buildopts.size()-1&&lDex==params.size()-1){
					wc.setAttribute(IPerformanceLaunchConfigurationConstants.PERF_LAUNCH_PERFEX, true);
				}

				final ILaunchConfiguration tmpConfig = wc.doSave();
				configs.add(tmpConfig);

				/**
				 * Execute the program specified in the build step
				 */
				final ILaunch launch = new PLaunch(tmpConfig, launchIn
						.getLaunchMode(), launchIn.getSourceLocator());// launchIn
																		// ;

				final PerfLauncher launcher = new PerfLauncher(tmpConfig,
						appNameAttribute, projNameAttribute, appPathAttribute,
						builder.getProgramPath(), paraDel, launch);
				steps.add(launcher);

				/**
				 * Collect performance data from the execution handled in the
				 * run step
				 */

				PerfPostlaunch analyzer = new PerfPostlaunch(tmpConfig,
						projNameAttribute, builder.getOutputLocation());
				steps.add(analyzer);

				launcher.addJobChangeListener(tauChange);
				analyzer.addJobChangeListener(tauChange);
				
				numruns++;
			} // end of inner loop for runtime parameters
		}// done with build options
		// Prime the scheduler sequence
		if (steps.size() > 0) {
			steps.poll().schedule();
		} else {
			System.out.println("No performance analysis jobs constructed");
		}

		/**
		 * Need something to launch PerfExplorer and run a script.
		 */
	}
}
