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
	private IBuildLaunchUtils utilBlob=null;

	public ToolLaunchManager(LaunchConfigurationDelegate delegate, ILaunchFactory lf,IBuildLaunchUtils utilBlob) {// ,
																						// String
																						// appNameAtt,String
																						// projNameAtt){
		paraDel = delegate;
		this.lf = lf;
		this.utilBlob=utilBlob;
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
		final ILaunch launch = launchIn;

		// This is the main chunk of data for the workflow being launched
		ExternalToolProcess pproc = Activator.getTool(configuration.getAttribute(IToolLaunchConfigurationConstants.SELECTED_TOOL,
				(String) null));

		// Are we using parametric launching? Then go do that instead.
		boolean useParam = configuration.getAttribute(org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants.PARA_USE_PARAMETRIC,
				false) || (pproc.para != null && pproc.para.runParametric);
		if (useParam) {
			ParametricToolLaunchManager.launch(configuration, paraDel, lf, mode, launchIn, monitor,utilBlob);// tool,
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
			builder = new BuilderTool(configuration, bt,utilBlob);
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
			PostlaunchTool analyzer = new PostlaunchTool(configuration, ppt, null,utilBlob);
			runStep(analyzer);
			return;
		}

		// If there is no recompilation step to do...
		if (!pproc.recompile) {
			// Make and run a new null-builder to initialize the stuff we need
			// from the build step anyway
			builder = new BuilderTool(configuration, null,utilBlob);
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
				launcher = new LauncherTool(configuration, null, bProgPath, paraDel, launch,utilBlob);

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
				builder = new BuilderTool(configuration, (BuildTool) t,utilBlob);
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
					launcher = new LauncherTool(configuration, null, bProgPath, paraDel, launch,utilBlob);

					if (!runStep(launcher)) {
						return;
					}
					ran = true;
				}
			} else if (t instanceof ExecTool) {
				/**
				 * Execute the program specified in the build step
				 */
				launcher = new LauncherTool(configuration, (ExecTool) t, bProgPath, paraDel, launch,utilBlob);

				if (!runStep(launcher)) {
					return;
				}
			} else if (t instanceof PostProcTool) {
				/**
				 * Collect performance data from the execution handled in the
				 * run step
				 */
				final PostlaunchTool analyzer = new PostlaunchTool(configuration, (PostProcTool) t, bOutLoc,utilBlob);

				if (!runStep(analyzer)) {
					return;
				}
			}
		}
	}
}
