package org.eclipse.ptp.perf.internal;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.perf.AbstractPerformanceDataManager;
import org.eclipse.ptp.perf.Activator;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.toolopts.PostProcTool;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class PerfPostlaunch extends PerfStep implements IPerformanceLaunchConfigurationConstants{
	
	String outputLocation;
	private PostProcTool tool=null;
	boolean externalTarget=false;
	String projName=null;
	
	public PerfPostlaunch(ILaunchConfiguration conf, PostProcTool ppTool, String projnameatt,String outLoc) throws CoreException{
		super(conf,"Analysis",projnameatt);
		tool=ppTool;
		outputLocation=outLoc;
	}
	
	/**
	 * Handle data collection and cleanup after an instrumented application has finished running
	 * @param monitor
	 * @throws CoreException
	 */
	public void postlaunch(IProgressMonitor monitor) throws CoreException{
		
//		if (monitor.isCanceled()) {
//			cleanup();
//			throw new OperationCanceledException();
//		}
		
		//TODO:  Restore tau performance data management
//		if(useTau)
//		{
//			TAULaunch.toolClean(thisCProject.getElementName(), configuration, outputLocation);
//		}
//		else
		{
			//List toolList=tool.analysisCommands;//configuration.getAttribute(TOOL_LIST, (List)null);
			if(tool.analysisCommands!=null&&tool.analysisCommands.length>0)
			{
				File projectLoc=new File(outputLocation);
				String runTool;
				//String toolPath;
				for(int i=0;i<tool.analysisCommands.length;i++)
				{
					//TODO: put internal in defined strings
					if(tool.analysisCommands[i].toolGroup==null||!tool.analysisCommands[i].toolGroup.equals("internal"))
					{
						runTool=getToolCommand(tool.analysisCommands[i],configuration,outputLocation);//tool.analysisCommands[i].toolCommand;
						//toolPath=BuildLaunchUtils.checkToolEnvPath(runTool);
						if(runTool!=null)
						{
							BuildLaunchUtils.runTool(runTool, null, projectLoc,tool.analysisCommands[i].outToFile);
						}
						else
						{
							System.out.println("The command "+tool.analysisCommands[i].toolCommand+" could not be run because the application is not in your path.");
						}
					}
					else
					{
						AbstractPerformanceDataManager manager=Activator.getPerfDataManager(tool.analysisCommands[i].toolCommand);
						if(manager!=null)
						{
							if(externalTarget){
								
//								Display.getDefault().syncExec(new Runnable() {
//									
//									public void run() {
//										InputDialog id = new InputDialog(PlatformUI.getWorkbench()
//												.getDisplay()
//												.getActiveShell(), "Input Project Name", "", "", null);
//										
//										int res=id.open();
//										if(res==id.OK)
//										{
//											projName=id.getValue();
//										}
//									}
//								});
								
								manager.setExternalTarget(true);
							}
							else
							{
								projName=thisCProject.getElementName();
							}
							manager.process(projName, configuration, outputLocation);
						}
					}
				}
			}
		}
		//cleanup();
		System.out.println("Postlaunch job done");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		if(outputLocation==null)
		{
			Display.getDefault().syncExec(new Runnable() {
		
				public void run() {
					DirectoryDialog dl = new DirectoryDialog(PlatformUI.getWorkbench()
							.getDisplay()
							.getActiveShell());
					dl.setText("Select the directory containing performance data");
					outputLocation=dl.open();
				}
			});
			if(outputLocation==null){
				return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"No Data Specified",null);
			}
			externalTarget=true;
		}
		
		try{
			postlaunch(monitor);
		}catch(Exception e){
			return new Status(IStatus.ERROR,"com.ibm.jdg2e.concurrency",IStatus.ERROR,"Data Collection Error",e);
		}
		return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"Data Collected",null);
	}	

}
