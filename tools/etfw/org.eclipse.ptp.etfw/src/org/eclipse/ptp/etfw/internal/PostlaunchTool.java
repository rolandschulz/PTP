package org.eclipse.ptp.etfw.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.AbstractToolDataManager;
import org.eclipse.ptp.etfw.Activator;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.messages.Messages;
import org.eclipse.ptp.etfw.toolopts.PostProcTool;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class PostlaunchTool extends ToolStep implements IToolLaunchConfigurationConstants {

	String outputLocation;

	String currentFile;

	FileFilter dFil = new DirFilter();

	private PostProcTool tool = null;
	boolean externalTarget = false;
	String projName = null;

	public PostlaunchTool(ILaunchConfiguration conf, PostProcTool ppTool, String outLoc) throws CoreException {
		super(conf, Messages.PostlaunchTool_Analysis);
		tool = ppTool;
		projectLocation = outputLocation = outLoc;
	}

	/**
	 * Handle data collection and cleanup after an instrumented application has
	 * finished running
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	public void postlaunch(IProgressMonitor monitor) throws CoreException {

		// if (monitor.isCanceled()) {
		// cleanup();
		// throw new OperationCanceledException();
		// }

		// TODO: Restore tau performance data management
		// if(useTau)
		// {
		// TAULaunch.toolClean(thisCProject.getElementName(), configuration,
		// outputLocation);
		// }
		// else
		{
			// List
			// toolList=tool.analysisCommands;//configuration.getAttribute(TOOL_LIST,
			// (List)null);
			if (tool.analysisCommands != null && tool.analysisCommands.length > 0) {
				File projectLoc = new File(outputLocation);
				List<String> runTool;
				// String toolPath;
				for (int i = 0; i < tool.analysisCommands.length; i++) {
					// TODO: put internal in defined strings
					if (tool.analysisCommands[i].toolGroup == null || !tool.analysisCommands[i].toolGroup.equals("internal")) //$NON-NLS-1$
					{
						runTool = getToolCommandList(tool.analysisCommands[i], configuration);// tool.analysisCommands[i].toolCommand;
						// toolPath=BuildLaunchUtils.checkToolEnvPath(runTool);
						if (tool.forAllLike != null) {
							File getname = new File(currentFile);
							String name = getname.getName();
							if (name.contains(".")) { //$NON-NLS-1$
								name = name.substring(0, name.lastIndexOf(".")); //$NON-NLS-1$
							}
							for (int runDex = 0; runDex < runTool.size(); runDex++) {
								String s = runTool.get(runDex);
								s = s.replace("%%FILE%%", currentFile); //$NON-NLS-1$
								s = s.replace("%%FILENAME%%", name); //$NON-NLS-1$
								runTool.set(runDex, s);
							}
						}
						if (runTool != null) {
							if (tool.analysisCommands[i].isVisualizer)
								BuildLaunchUtils.runVis(runTool, null, projectLoc);
							else {
								BuildLaunchUtils.runTool(runTool, null, projectLoc, tool.analysisCommands[i].outToFile);
							}
						} else {
							System.out.println(Messages.PostlaunchTool_TheCommand + tool.analysisCommands[i].toolCommand
									+ Messages.PostlaunchTool_CouldNotRun);
						}
					} else {
						AbstractToolDataManager manager = Activator.getPerfDataManager(tool.analysisCommands[i].toolCommand);
						if (manager != null) {
							if (externalTarget) {

								// Display.getDefault().syncExec(new Runnable()
								// {
								//
								// public void run() {
								// InputDialog id = new
								// InputDialog(PlatformUI.getWorkbench()
								// .getDisplay()
								// .getActiveShell(), "Input Project Name", "",
								// "", null);
								//
								// int res=id.open();
								// if(res==id.OK)
								// {
								// projName=id.getValue();
								// }
								// }
								// });

								manager.setExternalTarget(true);
							} else {
								projName = thisCProject.getElementName();
							}
							manager.process(projName, configuration, outputLocation);
						}
					}
				}
			}
		}
		// cleanup();
		// System.out.println("Postlaunch job done");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		if (tool == null) {
			return new Status(IStatus.WARNING, "com.ibm.jdg2e.concurrency", IStatus.OK, Messages.PostlaunchTool_NoToolNoData, null); //$NON-NLS-1$
		}

		if (outputLocation == null) {
			if (tool.useDefaultLocation) {
				outputLocation = System.getProperty("user.home"); //$NON-NLS-1$
			} else {
				Display.getDefault().syncExec(new Runnable() {

					public void run() {
						Shell s = PlatformUI.getWorkbench().getDisplay().getActiveShell();
						if (s == null) {
							s = PlatformUI.getWorkbench().getDisplay().getShells()[0];
						}
						DirectoryDialog dl = new DirectoryDialog(s);
						dl.setText(Messages.PostlaunchTool_SelectPerfDir);
						outputLocation = dl.open();
					}
				});
				if (outputLocation == null) {
					return new Status(IStatus.OK, "com.ibm.jdg2e.concurrency", IStatus.OK, Messages.PostlaunchTool_NoData, null); //$NON-NLS-1$
				}
				externalTarget = true;
			}
		}

		try {

			if (tool.forAllLike != null) {
				File workDir = new File(outputLocation);

				FilenameFilter ffn = new FilenameFilter() {

					public boolean accept(File dir, String name) {
						if (name.toLowerCase().endsWith(tool.forAllLike.toLowerCase()))
							return true;
						else
							return false;
					}
				};

				LinkedHashSet<File> fileSet = new LinkedHashSet<File>();
				findFiles(fileSet, workDir, tool.depth, ffn, tool.useLatestFileOnly);
				// String[] files=workDir.list(ffn);
				if (fileSet.size() <= 0) {
					return new Status(IStatus.ERROR,
							"com.ibm.jdg2e.concurrency", IStatus.ERROR, Messages.PostlaunchTool_NoValidFiles, null); //$NON-NLS-1$
				}
				for (File f : fileSet) {
					currentFile = f.getCanonicalPath();
					postlaunch(monitor);
				}
			} else {
				postlaunch(monitor);
			}

		} catch (Exception e) {
			return new Status(IStatus.ERROR,
					"com.ibm.jdg2e.concurrency", IStatus.ERROR, Messages.PostlaunchTool_DataCollectError, e); //$NON-NLS-1$
		}
		return new Status(IStatus.OK, "com.ibm.jdg2e.concurrency", IStatus.OK, Messages.PostlaunchTool_DataCollected, null); //$NON-NLS-1$
	}

	class DirFilter implements FileFilter {
		public boolean accept(File file) {
			return file.isDirectory();
		}
	}

	// TODO: The use of set here might gain us nothing
	private void findFiles(Set<File> fileSet, File root, int depth, FilenameFilter filter, boolean latestOnly) {

		// ArrayList<File[]> fList=new ArrayList<File[]>();

		File[] files = root.listFiles(filter);

		for (File f : files) {
			if(latestOnly){
				if(fileSet.size()==0)
					fileSet.add(f);
				else{
					if(fileSet.iterator().next().lastModified()<f.lastModified()){
						fileSet.clear();
						fileSet.add(f);
					}
				}
			}
			else
			fileSet.add(f);
		}

		if (depth > 0 || depth < 0) {
			File[] roots = root.listFiles(dFil);
			for (int i = 0; i < roots.length; i++) {
				findFiles(fileSet, roots[i], depth - 1, filter,latestOnly);
			}
		}
	}

}
