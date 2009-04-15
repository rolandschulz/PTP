package org.eclipse.ptp.perf.toolopts;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public abstract class PerformanceTool {
	public String toolID=null;
	public String toolName=null;
	public String toolType=null;
	public String requireTrue=null;
	public ToolApp global = null;
	
	public boolean canRun(ILaunchConfiguration configuration){
		
		if(requireTrue==null||configuration==null){
			return true;
		}
		boolean res = false;
		try {
			res= configuration.getAttribute(requireTrue, false);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
}
