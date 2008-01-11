package org.eclipse.ptp.tau.toolopts;


public class ToolIO{
	/**
	 * This value indicates an output object that specifies a directory for output data
	 */
	public static final int DIRECTORY=0;
	/**
	 * This value indicates an output object that specifies a single file for output data
	 */
	public static final int FILE=1;
	
	public String ID=null;
	
	
	
//	public String outputPath=null;
//	public boolean overridePath=false;
	public String pathFlag=null;
//	public int outputType=0;
//	public String fileName=null;

	
	
	
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
