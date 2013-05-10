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
package org.eclipse.ptp.internal.etfw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.etfw.ETFWUtils;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.toolopts.BuildTool;
import org.eclipse.ptp.etfw.toolopts.ExecTool;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;
import org.eclipse.ptp.etfw.toolopts.PostProcTool;
import org.eclipse.ptp.internal.etfw.messages.Messages;

public class ParametricToolLaunchManager {

	/**
	 * Contains the values for the parameters to be appended in a single
	 * execution of a parametric multi-execution
	 * 
	 * @author wspear
	 * 
	 */
	static class RunParams {
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

		public RunParams(int numProcs, String args, Map<String, String> vars) {
			super();
			this.args = args;
			this.numProcs = numProcs;
			this.vars = vars;
		}
	}

	// LaunchFactory lf = null;
	// LaunchConfigurationDelegate paraDel = null;

	// /**
	// * inherited constructor
	// *
	// * @param delegate
	// * @param appNameAtt
	// * @param projNameAtt
	// * @see org.eclipse.ptp.internal.etfw.ToolLaunchManager
	// */
	// public ParametricToolLaunchManager(LaunchConfigurationDelegate delegate,
	// LaunchFactory lf) {
	// this.lf=lf;
	// //this.paraDel=delegate;
	// //super(delegate);
	// }

	// /**
	// * inherited constructor
	// *
	// * @param delegate
	// * @param appNameAtt
	// * @param projNameAtt
	// * @param appPathAtt
	// * @see org.eclipse.ptp.internal.etfw.ToolLaunchManager
	// */
	// public ParametricToolLaunchManager(
	// LaunchConfigurationDelegate delegate, String appNameAtt,
	// String projNameAtt, String appPathAtt) {
	// super(delegate, appNameAtt, projNameAtt, appPathAtt);
	// }

	/**
	 * One object, two strings
	 * 
	 * @author wspear
	 * 
	 */
	static class StringPair {
		private String first;

		private String second;

		public StringPair(String first, String second) {
			super();
			this.first = first;
			this.second = second;
		}

		public String getFirst() {
			return first;
		}

		public String getSecond() {
			return second;
		}

		public void setFirst(String first) {
			this.first = first;
		}

		public void setSecond(String second) {
			this.second = second;
		}
	}

	private static final String NUMBER_MPI_PROCS = "org.eclipse.ptp.rm.mpi.openmpi.ui.launchAttributes.numProcs";//"org.eclipse.ptp.rm.orte.ui.launchAttributes.numProcs"; //$NON-NLS-1$

	/**
	 * Given a string of comma separated strings, returns an array of the
	 * strings
	 * 
	 * @param combined
	 *            The string to be tokenized by commas
	 * @return
	 */
	static List<String> getComArgs(String combined) {
		final List<String> numProcesses = new ArrayList<String>();
		if (combined == null) {
			return numProcesses;
		}

		final StringTokenizer st = new StringTokenizer(combined, ","); //$NON-NLS-1$

		while (st.hasMoreTokens()) {
			numProcesses.add(st.nextToken());
		}
		return numProcesses;
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
		final List<Map<String, String>> allCom = new ArrayList<Map<String, String>>();

		for (final List<StringPair> sl : combos) {
			final Map<String, String> agg = new LinkedHashMap<String, String>();
			for (final StringPair sp : sl) {
				agg.put(sp.first, sp.second);
			}
			allCom.add(agg);
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
		final List<String> allCom = new ArrayList<String>();

		for (final List<StringPair> sl : combos) {
			String agg = ""; //$NON-NLS-1$
			for (final StringPair sp : sl) {
				agg += sp.first + sp.second + " "; //$NON-NLS-1$
			}
			allCom.add(agg);
		}

		return allCom;
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
	static List<List<StringPair>> getListCombinations(List<String> a, List<String> b) {

		final List<List<StringPair>> comCom = new ArrayList<List<StringPair>>();

		if (a.size() != b.size()) {
			return comCom;
		}

		for (int i = 0; i < a.size(); i++) {
			comCom.add(getStringCombinations(a.get(i), b.get(i)));
		}

		return comCom;
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
	static List<RunParams> getRunParams(String procs, List<String> argNames, List<String> argVars, List<String> argBools,
			List<String> envNames, List<String> envVars, List<String> envBools) {
		final List<RunParams> params = new ArrayList<RunParams>();

		final List<String> numProcs = getComArgs(procs);

		final List<StringPair> checkedArgs = new ArrayList<StringPair>();

		int i = 0;

		while (i < argBools.size()) {
			if (argBools.get(i).equals("1")) { //$NON-NLS-1$
				argBools.remove(i);
				checkedArgs.add(new StringPair(argNames.remove(i), argVars.remove(i)));
			} else {
				i++;
			}
		}

		final List<StringPair> checkedVars = new ArrayList<StringPair>();

		i = 0;

		while (i < envBools.size()) {
			if (envBools.get(i).equals("1")) { //$NON-NLS-1$
				envBools.remove(i);
				checkedVars.add(new StringPair(envNames.remove(i), envVars.remove(i)));
			} else {
				i++;
			}
		}

		/*
		 * Get list combinations returns a list of one list of string pairs per
		 * argument, unify combos transforms that
		 */

		final List<String> args = getComboStrings(unifyCombos(getListCombinations(argNames, argVars), 0));

		final List<Map<String, String>> vars = getComboMaps(unifyCombos(getListCombinations(envNames, envVars), 0));

		if (args.size() == 0) {
			args.add(null);
		}
		if (vars.size() == 0) {
			vars.add(null);
		}

		for (i = 0; i < numProcs.size(); i++) {
			final String num = numProcs.get(i);
			for (String arg : args) {
				for (Map<String, String> var : vars) {

					if (checkedArgs.size() > 0) {
						if (arg == null) {
							arg = ""; //$NON-NLS-1$
						}
						for (final StringPair sp : checkedArgs) {
							arg += " " + sp.getFirst() + getComArgs(sp.getSecond()).get(i); //$NON-NLS-1$
						}
					}

					if (checkedVars.size() > 0) {
						if (var == null) {
							var = new HashMap<String, String>();
						}
						for (final StringPair sp : checkedVars) {
							var.put(sp.getFirst(), getComArgs(sp.getSecond()).get(i));
						}
					}

					params.add(new RunParams(Integer.parseInt(num), arg, var));
				}
			}
		}

		return params;
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
		final List<StringPair> combos = new ArrayList<StringPair>();

		final List<String> aList = getComArgs(a);
		final List<String> bList = getComArgs(b);

		for (final String first : aList) {
			for (final String second : bList) {
				combos.add(new StringPair(first, second));
			}
		}

		return combos;
	}

	public static String getTauMetadata(Map<String, String> build, RunParams par) {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tau:metadata xmlns:tau=\"http://www.cs.uoregon.edu/research/tau\">\n<tau:CommonProfileAttributes>"; //$NON-NLS-1$

		if (build != null)
		{
			xml += makeTauAtts(build, "BUILD:"); //$NON-NLS-1$
		}

		if (par.args != null)
		{
			xml += makeTauAtt("ApplicationArguments", par.args); //$NON-NLS-1$
		}

		if (par.vars != null)
		{
			xml += makeTauAtts(par.vars, "ENV:"); //$NON-NLS-1$
		}

		xml += "\n</tau:CommonProfileAttributes>\n</tau:metadata>"; //$NON-NLS-1$

		return xml;
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
	static List<RunParams> getWeakParams(String procs, List<String> argNames, List<String> argVars, List<String> envNames,
			List<String> envVars) {
		final List<RunParams> params = new ArrayList<RunParams>();

		final List<String> numProcs = getComArgs(procs);

		// int i=0;

		/*
		 * Get list combinations returns a list of one list of string pairs per
		 * argument, unify combos transforms that
		 */

		// List<String> args = new ArrayList<String>();

		// ArrayList<HashMap<String, String>> vars = new
		// ArrayList<HashMap<String,String>>();

		for (int i = 0; i < numProcs.size(); i++) {
			final String num = numProcs.get(i);
			String arg = null;
			Map<String, String> var = null;

			for (int j = 0; j < argNames.size(); j++) {
				if (arg == null) {
					arg = ""; //$NON-NLS-1$
				}
				final List<String> av = getComArgs(argVars.get(j));
				arg += " " + argNames.get(j) + av.get(i); //$NON-NLS-1$

			}

			for (int j = 0; j < envNames.size(); j++) {
				if (var == null) {
					var = new HashMap<String, String>();
				}
				final List<String> ev = getComArgs(envVars.get(j));
				var.put(envNames.get(j), ev.get(i));
			}
			params.add(new RunParams(Integer.parseInt(num), arg, var));
		}

		return params;
	}

	/**
	 * The primary launch command of this launch configuration delegate. The
	 * operations in this function are divided into three jobs: Building,
	 * Running and Data collection
	 */
	@SuppressWarnings("unchecked")
	public static void launch(ILaunchConfiguration configuration, LaunchConfigurationDelegate paraDel, ILaunchFactory lf,
			String mode, ILaunch launchIn, IProgressMonitor monitor, IBuildLaunchUtils utilBlob) throws CoreException // ,
	// TAULaunch
	// bLTool
	{

		/**
		 * Keeps track of the configurations created for each run so they can be
		 * deleted in sequence
		 */
		final Queue<ILaunchConfiguration> configs = new LinkedList<ILaunchConfiguration>();

		/**
		 * Keeps track of the performance analysis step jobs so they can be
		 * scheduled in sequence
		 */
		final Queue<ToolStep> steps = new LinkedList<ToolStep>();

		/**
		 * Manages job execution order
		 */
		final JobChangeAdapter tauChange = new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {

				// TODO: Add checks for successful compilation/execution
				// if (event.getJob() instanceof PostlaunchTool)
				// {
				// curRuns++;//TODO: This is not the right place to increment
				// this!
				// }
				// System.out.println("FR: "+firstRuns+" CR: "+curRuns);
				if (event.getJob() instanceof PostlaunchTool) {// &&
																// curRuns>=topRun)
																// {
					try {
						configs.poll().delete();
					} catch (final CoreException e) {
						e.printStackTrace();
					}
				}

				if (!steps.isEmpty()) {
					System.out.println("About to run job " + steps.size() + " " //$NON-NLS-1$ //$NON-NLS-2$
							+ steps.element().getName());
					steps.poll().schedule();
					System.out.println("Done.  Jobs left: " + steps.size()); //$NON-NLS-1$
				}
			}
		};

		final ExternalToolProcess pproc = ETFWUtils.getTool(configuration.getAttribute(
				IToolLaunchConfigurationConstants.SELECTED_TOOL,
				(String) null));
		// paraDel=lcd;
		// this.lf=lf;
		// here is where the loop(s) for the parametric study should go - for
		// runtime parameters, like #procs
		String processorOptionString = null;
		List<String> argVars = null;
		List<String> argNames = null;
		List<String> argBools = null;
		List<String> varNames = null;
		List<String> varVars = null;
		List<String> varBools = null;
		String optLevStr = null;
		boolean allCom = false;
		final boolean parallel = lf != null && lf.getType().equals(ILaunchFactory.PARALLEL);

		if (pproc.para != null && pproc.para.runParametric) {
			processorOptionString = pproc.para.mpiProcs;
			argNames = pproc.para.argNames;
			argVars = pproc.para.argValues;
			argBools = pproc.para.argWeakBools;

			varNames = pproc.para.varNames;
			varVars = pproc.para.varValues;
			varBools = pproc.para.varWeakBools;

			optLevStr = pproc.para.compileropt;
			allCom = !pproc.para.weakScaling;
		} else {
			processorOptionString = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_NUM_PROCESSORS, "1");// "1,2";//,4,8"; //$NON-NLS-1$
			// TODO: validate this string first?
			argNames = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_ARG_NAMES, (List<String>) null);
			argVars = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_ARG_VALUES, (List<String>) null);
			argBools = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_ARG_BOOLS, (List<String>) null);

			varNames = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_VAR_NAMES, (List<String>) null);
			varVars = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_VAR_VALUES, (List<String>) null);
			varBools = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_VAR_BOOLS, (List<String>) null);
			optLevStr = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_OPT_LEVELS, ""); //$NON-NLS-1$
			allCom = configuration.getAttribute(IToolLaunchConfigurationConstants.PARA_ALL_COMBO, false);
		}

		final String timestamp = BuildLaunchUtils.getNow();

		List<RunParams> params = null;
		if (!parallel) {
			if (allCom) {
				processorOptionString = "1"; //$NON-NLS-1$
			} else {
				int min = -1;

				if (argVars != null) {
					for (int i = 0; i < argVars.size(); i++) {
						final int count = getComArgs(argVars.get(i)).size();
						if (min == -1 || min > count) {
							min = count;
						}
					}
				}
				if (varVars != null) {
					for (int i = 0; i < varVars.size(); i++) {
						final int count = getComArgs(varVars.get(i)).size();
						if (min == -1 || min > count) {
							min = count;
						}
					}
				}

				if (min == -1) {
					min = 0;
				}

				String agg = ""; //$NON-NLS-1$
				for (int i = 0; i < min; i++) {
					agg += "1"; //$NON-NLS-1$
					if (i < min - 1) {
						agg += ","; //$NON-NLS-1$
					}
				}
				if (agg.length() == 0) {
					agg = "1"; //$NON-NLS-1$
				}
				processorOptionString = agg;
			}
		}
		if (!allCom) {
			params = getWeakParams(processorOptionString, argNames, argVars, varNames, varVars);
		} else {
			params = getRunParams(processorOptionString, argNames, argVars, argBools, varNames, varVars, varBools);
		}

		System.out.println(params.size());
		for (final RunParams param : params) {
			System.out.println("Num Processors: " + param.numProcs); //$NON-NLS-1$
			if (param.args != null)
			{
				System.out.println("Program args: " + param.args); //$NON-NLS-1$
			}
			System.out.println("Env args: "); //$NON-NLS-1$
			if (param.vars != null) {
				for (final Map.Entry<String, String> env : param.vars.entrySet()) {
					System.out.println(env);
				}
			}
		}

		final List<String> optLevs = getComArgs(optLevStr);

		final List<Map<String, String>> buildopts = new ArrayList<Map<String, String>>();
		if (optLevs.size() == 0) {
			buildopts.add(null);
		} else {
			for (final String s : optLevs) {
				String lev = null;
				Map<String, String> hm = null;
				switch (Integer.parseInt(s)) {
				case 0:
					lev = Messages.ParametricToolLaunchManager_OpNone;
					break;
				case 1:
					lev = Messages.ParametricToolLaunchManager_Op1;
					break;
				case 2:
					lev = Messages.ParametricToolLaunchManager_Op2;
					break;
				case 3:
					lev = Messages.ParametricToolLaunchManager_Op3;
					break;
				default:
					continue;
				}
				if (lev != null) {
					hm = new HashMap<String, String>();
					hm.put(Messages.ParametricToolLaunchManager_OptimizationLevel, lev);
					buildopts.add(hm);
				}
			}
		}

		// {
		// firstRuns=buildopts.size()-1*params.size();
		// }

		/*
		 * TODO: Make this robust! (Allow for or explicitly prohibit advanced
		 * tool combinations)
		 */
		final BuildTool bTool = pproc.getFirstBuilder(configuration);// .perfTools.get(0);
		final ExecTool eTool = pproc.getFirstRunner(configuration);
		final PostProcTool pTool = pproc.getFirstAnalyzer(configuration);

		int numruns = 0;
		// here is where the outer loop for the parametric study should go - for
		// build parameters, like optimization.
		for (int bDex = 0; bDex < buildopts.size(); bDex++) {

			final Map<String, String> optM = buildopts.get(bDex);
			final BuilderTool builder = new BuilderTool(configuration, bTool, optM, utilBlob);

			// if(optM!=null)
			// builder.setBuildMods(optM);
			// builder.setSomeAtt(str);

			steps.add(builder);
			builder.addJobChangeListener(tauChange);

			// builder.schedule();

			try {
				builder.join();
			} catch (final InterruptedException ie) {
				ie.printStackTrace();
			}

			// int i = 0;
			for (int lDex = 0; lDex < params.size(); lDex++) {

				// if(bDex==buildopts.size()-1&&topRun==-1)
				// {
				// /*
				// * This should be the first run at which it is safe to start
				// deleting build configurations.
				// */
				// topRun=numruns;
				// }

				final RunParams param = params.get(lDex);

				// get a working copy
				final ILaunchConfigurationWorkingCopy wc = configuration.copy(configuration.getName() + " ParameterSet_" + numruns); //$NON-NLS-1$
				// TODO: need this constant!
				if (parallel) {
					final int numProcs = param.numProcs;

					// System.out.println("doing " + numProcs +
					// " processors...");
					wc.setAttribute(NUMBER_MPI_PROCS, numProcs);
				}

				final String argConfigTag = wc.getAttribute(IToolLaunchConfigurationConstants.EXTOOL_ATTR_ARGUMENTS_TAG, ""); //$NON-NLS-1$

				/* Set up application arguments for this run */
				if (param.args != null) {
					String arg = wc.getAttribute(argConfigTag, "");// ICDTLaunchConfigurationConstants.// ATTR_PROGRAM_ARGUMENTS //$NON-NLS-1$
					arg += " " + param.args; //$NON-NLS-1$
					wc.setAttribute(argConfigTag, arg);
				}

				/* Set up environment variables for this run */
				if (param.vars != null) {
					Map<String, Object> envvars = null;
					envvars = wc.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String, Object>) null);
					if (envvars == null) {
						envvars = new HashMap<String, Object>();
					}
					envvars.putAll(param.vars);
					wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envvars);
				}

				/*
				 * TAU Specific settings. These will only be used by the TAU
				 * analysis phase. TODO: Move tool specific settings out of the
				 * core plugin
				 */
				wc.setAttribute(IToolLaunchConfigurationConstants.EXTOOL_XML_METADATA, getTauMetadata(optM, param));

				wc.setAttribute(IToolLaunchConfigurationConstants.EXTOOL_EXPERIMENT_APPEND, timestamp);

				/* The final analysis run should also launch perfexplorer */
				wc.setAttribute(IToolLaunchConfigurationConstants.EXTOOL_LAUNCH_PERFEX, false);
				if (bDex == buildopts.size() - 1 && lDex == params.size() - 1) {
					wc.setAttribute(IToolLaunchConfigurationConstants.EXTOOL_LAUNCH_PERFEX, true);
				}

				final ILaunchConfiguration tmpConfig = wc.doSave();
				configs.add(tmpConfig);

				/**
				 * Execute the program specified in the build step
				 */

				ILaunch launch = launchIn;
				if (lf != null) {
					launch = lf.makeLaunch(tmpConfig, launchIn.getLaunchMode(), launchIn.getSourceLocator());// launchIn
				}

				// ;

				final LauncherTool launcher = new LauncherTool(tmpConfig, eTool, builder.getProgramPath(), paraDel, launch,
						utilBlob);
				steps.add(launcher);
				launcher.addJobChangeListener(tauChange);

				/**
				 * Collect performance data from the execution handled in the
				 * run step
				 */

				// if(pTool!=null){
				final PostlaunchTool analyzer = new PostlaunchTool(tmpConfig, pTool, builder.getOutputLocation(), utilBlob);
				steps.add(analyzer);
				analyzer.addJobChangeListener(tauChange);
				// }

				numruns++;
			} // end of inner loop for runtime parameters
		}// done with build options
			// Prime the scheduler sequence
		if (steps.size() > 0) {
			steps.poll().schedule();
		} else {
			System.out.println(Messages.ParametricToolLaunchManager_NoPerformanceAnalysisJobsConstructed);
		}

		/**
		 * Need something to launch PerfExplorer and run a script.
		 */
	}

	public static String makeTauAtt(String name, String value) {
		final String att = "\n<tau:attribute>\n<tau:name>" + name + "</tau:name>\n<tau:value>" + value + "</tau:value>\n</tau:attribute>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return att;
	}

	public static String makeTauAtts(Map<String, String> map, String prefix) {
		String atts = ""; //$NON-NLS-1$

		final Iterator<Entry<String, String>> it = map.entrySet().iterator();
		Entry<String, String> e;
		while (it.hasNext()) {
			e = it.next();
			atts += makeTauAtt(prefix + e.getKey(), e.getValue());
		}

		return atts;
	}

	public static String mapToString(Map<String, String> map) {
		String out = ""; //$NON-NLS-1$

		final Iterator<Entry<String, String>> it = map.entrySet().iterator();
		Entry<String, String> e;
		while (it.hasNext()) {
			e = it.next();
			out += e.getKey() + "=" + e.getValue(); //$NON-NLS-1$
			if (it.hasNext()) {
				out += ", "; //$NON-NLS-1$
			}
		}

		return out;
	}

	// private boolean finalRun=false;

	/**
	 * The number of runs that must complete before the last set
	 */
	// private int firstRuns=0;
	// private int curRuns=0;
	// private int topRun=-1;

	static List<List<StringPair>> unifyCombos(final List<List<StringPair>> combos, int curDex) {

		final List<List<StringPair>> allCom = new ArrayList<List<StringPair>>();

		if (combos.size() == 0) {
			return allCom;
		}

		/**
		 * The real base-case. When we hit the final index we just want to
		 * return with the last sets of combinations
		 */
		if (curDex == combos.size() - 1) {

			for (int i = 0; i < combos.get(curDex).size(); i++) {
				final ArrayList<StringPair> jList = new ArrayList<StringPair>();
				jList.add(0, combos.get(curDex).get(i));
				allCom.add(jList);
			}

			return allCom;// new ArrayList<List<StringPair>>();// allCom=new
							// ArrayList<StringPair>();
		}

		final List<List<StringPair>> someCom = unifyCombos(combos, curDex + 1);

		for (int i = 0; i < combos.get(curDex).size(); i++) {
			for (int j = 0; j < someCom.size(); j++) {
				final ArrayList<StringPair> jList = new ArrayList<StringPair>(someCom.get(j));
				jList.add(0, combos.get(curDex).get(i));
				allCom.add(jList);
			}
		}

		return allCom;
	}
}
