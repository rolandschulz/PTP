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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.etfw.ETFWUtils;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.toolopts.BuildTool;
import org.eclipse.ptp.etfw.toolopts.ExecTool;
import org.eclipse.ptp.etfw.toolopts.ExternalTool;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;
import org.eclipse.ptp.etfw.toolopts.PostProcTool;
import org.eclipse.ptp.internal.etfw.jaxb.data.AnalysisToolType;
import org.eclipse.ptp.internal.etfw.jaxb.data.BuildToolType;
import org.eclipse.ptp.internal.etfw.jaxb.data.EtfwToolProcessType;
import org.eclipse.ptp.internal.etfw.jaxb.data.ExecToolType;
import org.eclipse.ptp.internal.etfw.jaxb.util.ExternalToolProcessUtil;
import org.eclipse.ptp.internal.etfw.jaxb.util.JAXBExtensionUtils;
import org.eclipse.ptp.internal.etfw.messages.Messages;

public class ToolLaunchManager {

	protected static final String buildText = Messages.ToolLaunchManager_InstrumentingAndBuilding;
	protected static final String launchText = Messages.ToolLaunchManager_ExecutingInstrumentedProject;
	protected static final String collectText = Messages.ToolLaunchManager_CollectingPerfData;

	protected LaunchConfigurationDelegate paraDel;

	private ILaunchFactory lf = null;
	private IBuildLaunchUtils utilBlob = null;

	public ToolLaunchManager(LaunchConfigurationDelegate delegate, ILaunchFactory lf, IBuildLaunchUtils utilBlob) {
		paraDel = delegate;
		this.lf = lf;
		this.utilBlob = utilBlob;
	}

	private static boolean runJAXBStep(ETFWToolStep step) {
		step.schedule();
		try {
			step.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		boolean result = step.getResult().isOK();
		step.setSuccessAttribute(Boolean.toString(result));
		return result;
	}

	private static boolean runStep(ToolStep step) {
		step.schedule();
		try {
			step.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		return step.getResult().isOK();
	}

	/**
	 * The primary launch command of this launch configuration delegate. The
	 * operations in this function are divided into three jobs: Buildig, Running
	 * and Data collection
	 * 
	 * @throws InterruptedException
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor)
			throws CoreException {
		String whichParser = configuration.getAttribute(IToolLaunchConfigurationConstants.ETFW_VERSION,
				IToolLaunchConfigurationConstants.EMPTY_STRING);
		if (whichParser.equals(IToolLaunchConfigurationConstants.USE_SAX_PARSER)) {
			launchSAXTool(configuration, mode, launchIn, monitor);
		} else {
			launchJAXBTool(configuration, mode, launchIn, monitor);
		}
	}

	private void launchJAXBTool(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor)
			throws CoreException {
		final ILaunch launch = launchIn;

		// This is the main chunk of data for the workflow being launched
		EtfwToolProcessType etfwTool = JAXBExtensionUtils.getTool(configuration.getAttribute(
				IToolLaunchConfigurationConstants.SELECTED_TOOL,
				(String) null));

		// TODO handle the parametric case with JAXB
		// Are we using parametric launching? Then go do that instead.
		boolean useParam = configuration.getAttribute(org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants.PARA_USE_PARAMETRIC,
				false);
		if (useParam) {
			ParametricToolLaunchManager.launch(configuration, paraDel, lf, mode, launchIn, monitor, utilBlob);// tool,
			return;
		}

		// The path to the built program
		String bProgPath = null;
		// The built executable
		String bOutLoc = null;
		// Has the executable been executed?
		boolean ran = false;

		// This workflow should only build, not run any execute or analyze steps
		boolean buildOnly = configuration.getAttribute(IToolLaunchConfigurationConstants.BUILDONLY, false);

		// This workflow should only analyze and skip any build or execute steps
		boolean analyzeOnly = configuration.getAttribute(IToolLaunchConfigurationConstants.ANALYZEONLY, false);

		BuildToolType bt = ExternalToolProcessUtil.getBuildTool(etfwTool, configuration, 0);
		ExecToolType et = ExternalToolProcessUtil.getExecTool(etfwTool, configuration, 0);
		AnalysisToolType ppt = ExternalToolProcessUtil.getAnalysisTool(etfwTool, configuration, 0);

		// If build only, just run the first build process and we're done
		if (buildOnly) {
			ETFWBuildTool builder = new ETFWBuildTool(configuration, bt, utilBlob);
			runJAXBStep(builder);
			return;
		}

		// If analyzeOnly has been set, or we have only analysis tools and no
		// exec tools then this is an analyzeOnly case
		analyzeOnly = analyzeOnly || (bt == null && ppt != null && et == null);

		// If analyze only just run the first analysis step and we're done.
		// //TODO: There may be cases where we have multiple analysis steps and
		// nothing else!
		if (analyzeOnly) {
			ETFWAnalysisTool analyzer = new ETFWAnalysisTool(configuration, ppt, null, utilBlob);
			runJAXBStep(analyzer);
			return;
		}

		// If there is no recompilation step to do...
		if (!etfwTool.isRecompile()) {
			// Make and run a new null-builder to initialize the stuff we need
			// from the build step anyway
			ETFWBuildTool builder = new ETFWBuildTool(configuration, null, utilBlob);
			if (!runJAXBStep(builder)) {
				return;
			}
			bProgPath = builder.getProgramPath();
			bOutLoc = builder.getOutputLocation();
			// If there is no special execution instruction and the first
			// workflow step is not an execution step... //TODO: And we haven't
			// indicated to ignore this
			if (!etfwTool.isPrependExecution() && !(etfwTool.getExecToolOrAnalysisToolOrBuildTool().get(0) instanceof ExecToolType)
					&& !etfwTool.isExplicitExecution()) {
				// Run the newly built executable
				ETFWLaunchTool launcher = new ETFWLaunchTool(configuration, null, bProgPath, paraDel, launch, utilBlob);
				if (!runJAXBStep(launcher)) {
					return;
				}
				ran = true;
			}
		}

		// Now for every performance step...
		boolean globalState = true; // Once set to false causes failure to cascade (to mimic old behavior) unless tool-state is specified 
		for (int i = 0; i < etfwTool.getExecToolOrAnalysisToolOrBuildTool().size(); i++) {// pproc.externalTools.size(); i++) {
			Object t = etfwTool.getExecToolOrAnalysisToolOrBuildTool().get(i);

			// If this step is a build tool...
			if (t instanceof BuildToolType) {
				BuildToolType buildTool = (BuildToolType) t;
				if (!ExternalToolProcessUtil.canRun(globalState, buildTool, configuration)) {
					continue;
				}
				/**
				 * Uses the specified tool's build settings on the build manager
				 * for this project, producing a new build configuration and
				 * instrumented binary file
				 */
				ETFWBuildTool builder = new ETFWBuildTool(configuration, (BuildToolType) t, utilBlob);
				globalState &= runJAXBStep(builder); // Accumulate global state by anding in result
				/* If the step's execution fails then skip the remainder of its processing (to mimic old behavior) */
				if (globalState) {
					bProgPath = builder.getProgramPath();
					bOutLoc = builder.getOutputLocation();
	
					// If there is no exec step specified and the next step is not
					// an exec step, we'd better perform the execution ourselves...
					if (!etfwTool.isPrependExecution() && !ran && i < etfwTool.getExecToolOrAnalysisToolOrBuildTool().size() - 1
							&& !(etfwTool.getExecToolOrAnalysisToolOrBuildTool().get(i + 1) instanceof ExecToolType)
							&& !etfwTool.isExplicitExecution()) {
						ETFWLaunchTool launcher = new ETFWLaunchTool(configuration, null, bProgPath, paraDel, launch, utilBlob);
						globalState &= runJAXBStep(launcher);
						if (globalState) {
							ran = true;
						}
					}
				}
			} else if (t instanceof ExecToolType) {
				ExecToolType execTool = (ExecToolType) t;
				if (!ExternalToolProcessUtil.canRun(globalState, execTool, configuration)) {
					continue;
				}
				/**
				 * Execute the program specified in the build step
				 */
				ETFWLaunchTool launcher = new ETFWLaunchTool(configuration, execTool, bProgPath, paraDel, launch, utilBlob);
				globalState &= runJAXBStep(launcher); // Accumulate global state by anding in result
				/* If the step's execution fails then skip the remainder of its processing (to mimic old behavior) */
				if (globalState) {
					if (launcher.outputLocation != null)
					{
						bOutLoc = launcher.outputLocation;
					}
				}
			} else if (t instanceof AnalysisToolType) {
				AnalysisToolType analysisTool = (AnalysisToolType) t;
				if (!ExternalToolProcessUtil.canRun(globalState, analysisTool, configuration)) {
					continue;
				}
				/**
				 * Collect performance data from the execution handled in the
				 * run step
				 */
				ETFWAnalysisTool analyzer = new ETFWAnalysisTool(configuration, analysisTool, bOutLoc, utilBlob);
				globalState &= runJAXBStep(analyzer); // Accumulate global state by anding in result
			}
		}

	}

	private void launchSAXTool(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor)
			throws CoreException {
		final ILaunch launch = launchIn;

		// This is the main chunk of data for the workflow being launched
		ExternalToolProcess pproc = ETFWUtils.getTool(configuration.getAttribute(IToolLaunchConfigurationConstants.SELECTED_TOOL,
				(String) null));

		// Are we using parametric launching? Then go do that instead.
		boolean useParam = configuration.getAttribute(org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants.PARA_USE_PARAMETRIC,
				false) || (pproc.para != null && pproc.para.runParametric);
		if (useParam) {
			ParametricToolLaunchManager.launch(configuration, paraDel, lf, mode, launchIn, monitor, utilBlob);// tool,
			return;
		}

		// The path to the built program
		String bProgPath = null;
		// The built executable
		String bOutLoc = null;
		// Has the executable been executed?
		boolean ran = false;
		BuilderTool builder = null;
		LauncherTool launcher = null;

		// This workflow should only build, not run any execute or analyze steps
		boolean buildOnly = configuration.getAttribute(IToolLaunchConfigurationConstants.BUILDONLY, false);

		// This workflow should only analyze and skip any build or execute steps
		boolean analyzeOnly = configuration.getAttribute(IToolLaunchConfigurationConstants.ANALYZEONLY, false);

		BuildTool bt = pproc.getFirstBuilder(configuration);
		PostProcTool ppt = pproc.getFirstAnalyzer(configuration);
		ExecTool et = pproc.getFirstRunner(configuration);

		// If build only, just run the first build process and we're done
		if (buildOnly) {
			builder = new BuilderTool(configuration, bt, utilBlob);
			runStep(builder);
			return;
		}

		// If analyzeOnly has been set, or we have only analysis tools and no
		// exec tools then this is an analyzeOnly case
		analyzeOnly = analyzeOnly || (bt == null && ppt != null && et == null);

		// If analyze only just run the first analysis step and we're done.
		// //TODO: There may be cases where we have multiple analysis steps and
		// nothing else!
		if (analyzeOnly) {
			PostlaunchTool analyzer = new PostlaunchTool(configuration, ppt, null, utilBlob);
			runStep(analyzer);
			return;
		}

		// If there is no recompilation step to do...
		if (!pproc.recompile) {
			// Make and run a new null-builder to initialize the stuff we need
			// from the build step anyway
			builder = new BuilderTool(configuration, null, utilBlob);
			if (!runStep(builder)) {
				return;
			}
			bProgPath = builder.getProgramPath();
			bOutLoc = builder.getOutputLocation();
			// If there is no special execution instruction and the first
			// workflow step is not an execution step... //TODO: And we haven't
			// indicated to ignore this
			if (!pproc.prependExecution && !(pproc.externalTools.get(0) instanceof ExecTool) && !pproc.explicitExecution) {
				// Run the newly built executable
				launcher = new LauncherTool(configuration, null, bProgPath, paraDel, launch, utilBlob);
				if (!runStep(launcher)) {
					return;
				}
				ran = true;
			}
		}

		// Now for every performance step...
		for (int i = 0; i < pproc.externalTools.size(); i++) {
			ExternalTool t = pproc.externalTools.get(i);

			// If this step isn't activated, skip it
			if (!t.canRun(configuration)) {
				continue;
			}

			// If this step is a build tool...
			if (t instanceof BuildTool) {
				/**
				 * Uses the specified tool's build settings on the build manager
				 * for this project, producing a new build configuration and
				 * instrumented binary file
				 */
				builder = new BuilderTool(configuration, (BuildTool) t, utilBlob);
				if (!runStep(builder)) {
					return;
				}
				bProgPath = builder.getProgramPath();
				bOutLoc = builder.getOutputLocation();

				// If there is no exec step specified and the next step is not
				// an exec step, we'd better perform the execution ourselves...
				if (!pproc.prependExecution && !ran && i < pproc.externalTools.size() - 1
						&& !(pproc.externalTools.get(i + 1) instanceof ExecTool) && !pproc.explicitExecution) {
					launcher = new LauncherTool(configuration, null, bProgPath, paraDel, launch, utilBlob);

					if (!runStep(launcher)) {
						return;
					}
					ran = true;
				}
			} else if (t instanceof ExecTool) {
				/**
				 * Execute the program specified in the build step
				 */
				launcher = new LauncherTool(configuration, (ExecTool) t, bProgPath, paraDel, launch, utilBlob);
				if (!runStep(launcher)) {
					return;
				}
				if (launcher.outputLocation != null)
				{
					bOutLoc = launcher.outputLocation;
				}
			} else if (t instanceof PostProcTool) {
				/**
				 * Collect performance data from the execution handled in the
				 * run step
				 */
				final PostlaunchTool analyzer = new PostlaunchTool(configuration, (PostProcTool) t, bOutLoc, utilBlob);
				if (!runStep(analyzer)) {
					return;
				}
			}
		}
	}
}
