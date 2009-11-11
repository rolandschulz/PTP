package org.eclipse.ptp.etfw.toolopts;

import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;


public class ToolIO implements IAppInput{
	/**
	 * This value indicates an output object that specifies a directory for output data
	 */
	public static final int DIRECTORY=0;
	/**
	 * This value indicates an output object that specifies a single file for output data
	 */
	public static final int FILE=1;
	
	public String ID=null;
	
	public ToolIO(){
		
	}
	
//	public String outputPath=null;
//	public boolean overridePath=false;
	/**
	 * The flag or command preceding the path to the I/O file or directory
	 */
	public String pathFlag=null;
//	public int outputType=0;
//	public String fileName=null;



	public String getArgument(ILaunchConfiguration configuration) {//TODO: What is this doing?
		String arg = "";
		if(pathFlag!=null){
			arg+=pathFlag+" ";
		}
		arg+=ID;
		return null;
	}
	
	public Map<String, String> getEnvVars(ILaunchConfiguration configuration) {//TODO: What about this?
		// TODO Auto-generated method stub
		return null;
	}

	
//	public String getArg()
//	{
//		String arg="";
//		if(pathFlag!=null)
//			arg+=pathFlag+" ";
//		arg+="{"+ID+"}";
//		
//		if(fileName!=null)
//			arg+=File.separator+fileName;
//		
//		
//		return arg;
//	}
	
//	public boolean setOutputPath(String path)
//	{
//		if(overridePath)
//			return false;
//		
//		outputPath=path;
//		return true;
//	}
//	
//	public String getPathOutputArg()
//	{
//		if(outputPath!=null&&pathFlag!=null)
//		{
//			return pathFlag+" "+outputPath;
//		}
//		return "";
//	}
}
