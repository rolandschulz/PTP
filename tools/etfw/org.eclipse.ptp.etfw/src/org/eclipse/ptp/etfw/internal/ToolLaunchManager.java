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
package org.eclipse.ptp.etfw.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.etfw.Activator;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.jaxb.data.BuildToolType;
import org.eclipse.ptp.etfw.jaxb.data.EtfwToolProcessType;
import org.eclipse.ptp.etfw.jaxb.data.ExecToolType;
import org.eclipse.ptp.etfw.jaxb.data.PostProcToolType;
import org.eclipse.ptp.etfw.jaxb.util.ExternalToolProcessUtil;
import org.eclipse.ptp.etfw.jaxb.util.JAXBExtensionUtils;
import org.eclipse.ptp.etfw.messages.Messages;
import org.eclipse.ptp.etfw.toolopts.BuildTool;
import org.eclipse.ptp.etfw.toolopts.ExecTool;
import org.eclipse.ptp.etfw.toolopts.ExternalTool;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;
import org.eclipse.ptp.etfw.toolopts.PostProcTool;

public class ToolLaunchManager {

	protected static final String buildText = Messages.ToolLaunchManager_InstrumentingAndBuilding;
	protected static final String launchText = Messages.ToolLaunchManager_ExecutingInstrumentedProject;
	protected static final String collectText = Messages.ToolLaunchManager_CollectingPerfData;

	// protected String appNameAttribute;
	// protected String projNameAttribute;
	// protected String appPathAttribute=null;
	protected LaunchConfigurationDelegate paraDel;

	private ILaunchFactory lf = null;
	private IBuildLaunchUtils utilBlob = null;

	public ToolLaunchManager(LaunchConfigurationDelegate delegate, ILaunchFactory lf, IBuildLaunchUtils utilBlob) {// ,
		// String
		// appNameAtt,String
		// projNameAtt){
		paraDel = delegate;
		this.lf = lf;
		this.utilBlob = utilBlob;
		// appNameAttribute=appNameAtt;
		// projNameAttribute=projNameAtt;
	}

	// public ToolLaunchManager(LaunchConfigurationDelegate delegate, String
	// appNameAtt,String projNameAtt, String appPathAtt){
	// paraDel=delegate;
	// //appNameAttribute=appNameAtt;
	// //appPathAttribute=appPathAtt;
	// //projNameAttribute=projNameAtt;
	// }

	private static boolean runJAXBStep(ETFWToolStep step) {
		step.schedule();
		try {
			step.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		return step.getResult().isOK();
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
		// ExternalToolProcess pproc = Activator.getTool(configuration.getAttribute(IToolLaunchConfigurationConstants.SELECTED_TOOL,
		// (String) null));

		EtfwToolProcessType etfwTool = JAXBExtensionUtils.getTool(configuration.getAttribute(
				IToolLaunchConfigurationConstants.SELECTED_TOOL,
				(String) null));

		// TODO handle the parametric case with JAXB
		// Are we using parametric launching? Then go do that instead.
		boolean useParam = configuration.getAttribute(org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants.PARA_USE_PARAMETRIC,
				false);// || (pproc.para != null && pproc.para.runParametric);
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
		// BuilderTool builder = null;
		// BuilderTool builder = null;
		ETFWBuildTool builder = null;
		ETFWLaunchTool launcher = null;

		// This workflow should only build, not run any execute or analyze steps
		boolean buildOnly = configuration.getAttribute(IToolLaunchConfigurationConstants.BUILDONLY, false);

		// This workflow should only analyze and skip any build or execute steps
		boolean analyzeOnly = configuration.getAttribute(IToolLaunchConfigurationConstants.ANALYZEONLY, false);

		BuildToolType bt = ExternalToolProcessUtil.getBuildTool(etfwTool, configuration, 0);
		ExecToolType et = ExternalToolProcessUtil.getExecTool(etfwTool, configuration, 0);
		PostProcToolType ppt = ExternalToolProcessUtil.getPostProcTool(etfwTool, configuration, 0);
		// BuildTool bt = pproc.getFirstBuilder(configuration);
		// PostProcTool ppt = pproc.getFirstAnalyzer(configuration);
		// ExecTool et = pproc.getFirstRunner(configuration);

		// If build only, just run the first build process and we're done
		if (buildOnly) {
			// builder = new BuilderTool(configuration, bt, utilBlob);
			// runStep(builder);
			builder = new ETFWBuildTool(configuration, bt, utilBlob);
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
			// PostlaunchTool analyzer = new PostlaunchTool(configuration, ppt, null, utilBlob);
			ETFWPostProcessTool analyzer = new ETFWPostProcessTool(configuration, ppt, null, utilBlob);
			runJAXBStep(analyzer);
			return;
		}

		// If there is no recompilation step to do...
		if (!etfwTool.isRecompile()) {
			// Make and run a new null-builder to initialize the stuff we need
			// from the build step anyway
			// builder = new BuilderTool(configuration, null, utilBlob);
			// if (!runStep(builder)) {
			// return;
			// }
			builder = new ETFWBuildTool(configuration, null, utilBlob);
			if (!runJAXBStep(builder)) {
				return;
			}
			bProgPath = builder.getProgramPath();
			bOutLoc = builder.getOutputLocation();
			// If there is no special execution instruction and the first
			// workflow step is not an execution step... //TODO: And we haven't
			// indicated to ignore this
			if (!etfwTool.isPrependExecution() && !(etfwTool.getExecToolOrPostProcToolOrBuildTool().get(0) instanceof ExecToolType)
					&& !etfwTool.isExplicitExecution()) {
				// Run the newly built executable
				launcher = new ETFWLaunchTool(configuration, null, bProgPath, paraDel, launch, utilBlob);

				if (!runJAXBStep(launcher)) {
					return;
				}
				ran = true;
			}
		}

		// Now for every performance step...
		for (int i = 0; i < etfwTool.getExecToolOrPostProcToolOrBuildTool().size(); i++) {// pproc.externalTools.size(); i++) {
			// ExternalTool t = pproc.externalTools.get(i);
			Object t = etfwTool.getExecToolOrPostProcToolOrBuildTool().get(i);

			// If this step isn't activated, skip it
			if (t instanceof BuildToolType) {
				BuildToolType buildTool = (BuildToolType) t;
				if (!ExternalToolProcessUtil.canRun(buildTool, configuration)) {
					continue;
				}
				builder = new ETFWBuildTool(configuration, (BuildToolType) t, utilBlob);
				if (!runJAXBStep(builder)) {
					return;
				}
				bProgPath = builder.getProgramPath();
				bOutLoc = builder.getOutputLocation();
				// built=true;

				// If there is no exec step specified and the next step is not
				// an exec step, we'd better perform the execution ourselves...
				if (!etfwTool.isPrependExecution() && !ran && i < etfwTool.getExecToolOrPostProcToolOrBuildTool().size() - 1
						&& !(etfwTool.getExecToolOrPostProcToolOrBuildTool().get(i + 1) instanceof ExecToolType)
						&& !etfwTool.isExplicitExecution()) {
					launcher = new ETFWLaunchTool(configuration, null, bProgPath, paraDel, launch, utilBlob);
					if (!runJAXBStep(launcher)) {
						return;
					}
					ran = true;
				}

				// if (!pproc.prependExecution && !ran && i < pproc.externalTools.size() - 1
				// && !(pproc.externalTools.get(i + 1) instanceof ExecTool) && !pproc.explicitExecution) {
				// launcher = new LauncherTool(configuration, null, bProgPath, paraDel, launch, utilBlob);

				// if (!runStep(launcher)) {
				// return;
				// }
				// ran = true;
				// }

				// if (!t.canRun(configuration)) {
				// continue;
				// }

				// If this step is a build tool...
				// if (t instanceof BuildTool) {
				/**
				 * Uses the specified tool's build settings on the build manager
				 * for this project, producing a new build configuration and
				 * instrumented binary file
				 */
				// builder = new BuilderTool(configuration, (BuildTool) t, utilBlob);
				// if (!runStep(builder)) {
				// return;
				// }
				// bProgPath = builder.getProgramPath();
				// bOutLoc = builder.getOutputLocation();
				// built=true;

				// If there is no exec step specified and the next step is not
				// an exec step, we'd better perform the execution ourselves...
				// if (!pproc.prependExecution && !ran && i < pproc.externalTools.size() - 1
				// && !(pproc.externalTools.get(i + 1) instanceof ExecTool) && !pproc.explicitExecution) {
				// launcher = new LauncherTool(configuration, null, bProgPath, paraDel, launch, utilBlob);

				// if (!runStep(launcher)) {
				// return;
				// }
				// ran = true;
				// }
			} else if (t instanceof ExecToolType) {
				ExecToolType execTool = (ExecToolType) t;
				if (!ExternalToolProcessUtil.canRun(execTool, configuration)) {
					continue;
				}
				/**
				 * Execute the program specified in the build step
				 */
				launcher = new ETFWLaunchTool(configuration, execTool, bProgPath, paraDel, launch, utilBlob);

				if (!runJAXBStep(launcher)) {
					return;
				}
				if (launcher.outputLocation != null)
				{
					bOutLoc = launcher.outputLocation;
				}
			} else if (t instanceof PostProcToolType) {
				PostProcToolType postProcTool = (PostProcToolType) t;
				if (!ExternalToolProcessUtil.canRun(postProcTool, configuration)) {
					continue;
				}
				/**
				 * Collect performance data from the execution handled in the
				 * run step
				 */
				final ETFWPostProcessTool analyzer = new ETFWPostProcessTool(configuration, postProcTool, bOutLoc, utilBlob);

				if (!runJAXBStep(analyzer)) {
					return;
				}
			}
		}

	}

	private void launchSAXTool(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor)
			throws CoreException {
		final ILaunch launch = launchIn;

		// This is the main chunk of data for the workflow being launched
		ExternalToolProcess pproc = Activator.getTool(configuration.getAttribute(IToolLaunchConfigurationConstants.SELECTED_TOOL,
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
				// built=true;

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
