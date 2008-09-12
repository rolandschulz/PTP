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

public class PerfPostlaunch extends PerfStep implements IPerformanceLaunchConfigurationConstants{
	
	String outputLocation;
	
	public PerfPostlaunch(ILaunchConfiguration conf, String projnameatt,String outLoc) throws CoreException{
		super(conf,"Analysis",projnameatt);
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
						runTool=getToolCommand(tool.analysisCommands[i],configuration);//tool.analysisCommands[i].toolCommand;
						//toolPath=BuildLaunchUtils.checkToolEnvPath(runTool);
						if(runTool!=null)
						{
							BuildLaunchUtils.runTool(runTool, null, projectLoc);
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
							manager.process(thisCProject.getElementName(), configuration, outputLocation);
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
		try{
			postlaunch(monitor);
		}catch(Exception e){
			return new Status(IStatus.ERROR,"com.ibm.jdg2e.concurrency",IStatus.ERROR,"Data Collection Error",e);
		}
		return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"Data Collected",null);
	}	

}
