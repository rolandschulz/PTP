package org.eclipse.ptp.internal.etfw;

//import java.io.File;
//import java.io.FileFilter;
//import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.AbstractToolDataManager;
import org.eclipse.ptp.etfw.ETFWUtils;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.toolopts.PostProcTool;
import org.eclipse.ptp.etfw.toolopts.ToolApp;
import org.eclipse.ptp.internal.etfw.messages.Messages;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class PostlaunchTool extends ToolStep implements IToolLaunchConfigurationConstants {

	// String outputLocation;

	public static MessageConsole findConsole(String name) {
		final ConsolePlugin plugin = ConsolePlugin.getDefault();
		final IConsoleManager conMan = plugin.getConsoleManager();
		final IConsole[] existing = conMan.getConsoles();
		for (final IConsole element : existing) {
			if (name.equals(element.getName())) {
				return (MessageConsole) element;
			}
		}
		// no console found, so create a new one
		final MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	// FileFilter dFil = new DirFilter();

	String currentFile;
	private PostProcTool tool = null;
	/**
	 * True only if the directory containing performance data is user-specified and not strictly part of the project.
	 */
	boolean externalTarget = false;
	String projName = null;
	private final IBuildLaunchUtils utilBLob;

	private String syncProjectLocation = null;

	public PostlaunchTool(ILaunchConfiguration conf, PostProcTool ppTool, String outLoc, IBuildLaunchUtils utilBlob)
			throws CoreException {
		super(conf, Messages.PostlaunchTool_Analysis, utilBlob);
		tool = ppTool;
		this.utilBLob = utilBlob;
		if (outLoc == null || outLoc.equals(EMPTY_STRING)) {
			syncProjectLocation = projectLocation;
		}
		projectLocation = outputLocation = outLoc;

		// String wdir = utilBlob.getWorkingDirectory();
		// if(wdir!=null){
		// outputLocation=wdir;
		// }
	}

	// TODO: The use of set here might gain us nothing
	private void findFiles(Set<IFileStore> fileSet, IFileStore root, int depth, String matchSuffix, boolean latestOnly) {

		final List<IFileStore> files = listFiles(root, matchSuffix);

		for (final IFileStore f : files) {
			if (latestOnly) {
				if (fileSet.size() == 0) {
					fileSet.add(f);
				} else {
					if (fileSet.iterator().next().fetchInfo().getLastModified() < f.fetchInfo().getLastModified()) {
						fileSet.clear();
						fileSet.add(f);
					}
				}
			} else {
				fileSet.add(f);
			}
		}

		if (depth > 0 || depth < 0) {
			final List<IFileStore> roots = listDirectories(root);
			for (final IFileStore r : roots) {
				findFiles(fileSet, r, depth - 1, matchSuffix, latestOnly);
			}
		}
	}

	private List<IFileStore> listDirectories(IFileStore root) {
		final List<IFileStore> files = new ArrayList<IFileStore>();
		IFileStore[] filea = null;
		try {
			filea = root.childStores(EFS.NONE, null);
		} catch (final CoreException e) {
			e.printStackTrace();
		}

		for (final IFileStore f : filea) {
			if (f.fetchInfo().isDirectory()) {
				files.add(f);
			}
		}

		return files;
	}

	private List<IFileStore> listFiles(IFileStore root, String matchSuffix) {
		final List<IFileStore> files = new ArrayList<IFileStore>();
		IFileStore[] filea = null;
		try {
			if (root.fetchInfo().exists() && root.fetchInfo().isDirectory()) {
				filea = root.childStores(EFS.NONE, null);
			}
			else {
				return files;
			}
		} catch (final CoreException e) {
			e.printStackTrace();
		}
		final String test = matchSuffix.toLowerCase();
		if (filea != null) {
			for (final IFileStore f : filea) {
				if (f.getName().endsWith(test)) {
					files.add(f);
				}
			}
		}

		return files;
	}

	/**
	 * Handle data collection and cleanup after an instrumented application has
	 * finished running
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	public void postlaunch(IProgressMonitor monitor) throws CoreException {
		{
			if (tool.analysisCommands == null || tool.analysisCommands.length <= 0) {
				return;
			}
			List<String> runTool;
			/*
			 * For every analysis command in our list...
			 */
			for (final ToolApp anap : tool.analysisCommands) {
				/*
				 * If the tool has no group or the group is, at least, not internal just run with what is defined in the xml
				 */
				if (anap.toolGroup == null || !anap.toolGroup.equals(INTERNAL))
				{
					runTool = getToolCommandList(anap, configuration);
					if (tool.forAllLike != null) {
						final IFileStore getname = utilBlob.getFile(currentFile);
						String name = getname.getName();
						if (name.contains(DOT)) {
							name = name.substring(0, name.lastIndexOf(DOT));
						}
						for (int runDex = 0; runDex < runTool.size(); runDex++) {
							String s = runTool.get(runDex);
							s = s.replace(FILE_SWAP, currentFile);
							s = s.replace(FILENAME_SWAP, name);
							runTool.set(runDex, s);
						}
					}
					if (runTool != null) {
						if (anap.isVisualizer) {
							utilBLob.runVis(runTool, null, outputLocation);
						} else {

							if (anap.outToFile != null)
							{
								utilBLob.runTool(runTool, null, outputLocation, anap.outToFile);
							}
							else {
								byte[] utout = null;
								final MessageConsole mc = findConsole("ETFw");
								mc.clearConsole();
								final OutputStream os = mc.newOutputStream();
								utout = utilBlob.runToolGetOutput(runTool, null, outputLocation, true);

								try {
									if (utout != null) {
										os.write(utout);
									}
									os.close();
								} catch (final IOException e) {
									e.printStackTrace();
								}
								mc.activate();
							}
						}
					} else {
						System.out.println(Messages.PostlaunchTool_TheCommand + anap.toolCommand
								+ Messages.PostlaunchTool_CouldNotRun);
					}
				}
				/*
				 * Otherwise, if we have an alternative tool defined in a plugin
				 */
				else {
					final AbstractToolDataManager manager = ETFWUtils.getPerfDataManager(anap.toolCommand);
					if (manager != null) {
						if (externalTarget) {
							manager.setExternalTarget(true);
						} else {
							projName = thisCProject.getElementName();
							manager.setExternalTarget(false);
						}

						// TODO: This is sort of ok, but we should probably change the API to accept both the output dir and the
						// project dir.
						String outdir = outputLocation;
						if (this.syncProjectLocation != null) {
							outdir = syncProjectLocation;
						}

						manager.process(projName, configuration, outdir);
						manager.cleanup();
					}
				}
			}
		}

	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		/*
		 * If there is no tool we have a failure
		 */
		if (tool == null) {
			return new Status(IStatus.WARNING,
					"com.ibm.jdg2e.concurrency", IStatus.OK, Messages.PostlaunchTool_NoToolNoAnalysis, null); //$NON-NLS-1$
		}

		/*
		 * If we have not defined output location...
		 */
		if (outputLocation == null) {
			/*
			 * If we've said to use the default we just use the home directory
			 */
			if (tool.useDefaultLocation) {
				outputLocation = System.getProperty("user.home"); //$NON-NLS-1$
			}
			/*
			 * Otherwise we need to ask the user where the performance data is located
			 */
			else {
				Display.getDefault().syncExec(new Runnable() {

					public void run() {
						Shell s = PlatformUI.getWorkbench().getDisplay().getActiveShell();
						if (s == null) {
							s = PlatformUI.getWorkbench().getDisplay().getShells()[0];
						}
						if (utilBlob.isRemote())
						{
							outputLocation = utilBlob.askToolPath(utilBlob.getWorkingDirectory(), "Performance Data Location");
						}
						else {
							final DirectoryDialog dl = new DirectoryDialog(s);
							dl.setText(Messages.PostlaunchTool_SelectPerfDir);
							outputLocation = dl.open();
						}
					}
				});
				if (outputLocation == null) {
					return new Status(IStatus.OK, "com.ibm.jdg2e.concurrency", IStatus.OK, Messages.PostlaunchTool_NoData, null); //$NON-NLS-1$
				}
				/*
				 * This means we have specified data potentially outside of the workspace.
				 */
				externalTarget = true;
			}
		}
		else {
			String customOutLoc = null;

			try {
				customOutLoc = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, (String) null);
			} catch (final CoreException e1) {
				e1.printStackTrace();
			}

			if (customOutLoc != null)
			{
				outputLocation = customOutLoc;
			}
			else if (utilBlob.isRemote()) {
				outputLocation = utilBlob.getWorkingDirectory();
			}
		}

		try {

			if (tool.forAllLike != null) {
				final IFileStore workDir = utilBlob.getFile(outputLocation);

				final LinkedHashSet<IFileStore> fileSet = new LinkedHashSet<IFileStore>();
				findFiles(fileSet, workDir, tool.depth, tool.forAllLike, tool.useLatestFileOnly);
				if (fileSet.size() <= 0) {
					return new Status(IStatus.ERROR,
							"com.ibm.jdg2e.concurrency", IStatus.ERROR, Messages.PostlaunchTool_NoValidFiles, null); //$NON-NLS-1$
				}
				for (final IFileStore f : fileSet) {
					currentFile = f.toURI().getPath();
					postlaunch(monitor);
				}
			} else {
				postlaunch(monitor);
			}

		} catch (final Exception e) {
			return new Status(IStatus.ERROR, "com.ibm.jdg2e.concurrency", IStatus.ERROR, Messages.PostlaunchTool_AnalysisError, e); //$NON-NLS-1$
		}
		return new Status(IStatus.OK, "com.ibm.jdg2e.concurrency", IStatus.OK, Messages.PostlaunchTool_AnalysisSuccessful, null); //$NON-NLS-1$
	}

}
