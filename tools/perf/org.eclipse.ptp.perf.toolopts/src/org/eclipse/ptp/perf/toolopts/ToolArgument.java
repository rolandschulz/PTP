package org.eclipse.ptp.perf.toolopts;

import java.io.File;

public class ToolArgument {
	private String argument=null;
	//private String flag=null;
	//private String value=null;
	private String separator="";
	private boolean localFile=false;
	
	public ToolArgument(String arg){
		argument=arg;
	}
	
	public ToolArgument(String flag, String value, String sep, boolean local){
		//this.flag=flag;
		//this.value=value;
		if(sep!=null)
			separator=sep;
		this.localFile=local;
		String arg="";
		if(flag!=null)
		{
			arg+=flag;
			arg+=separator;
		}
		if(value!=null)
		{
			if(localFile)
			{
				arg+=ToolsOptionsConstants.PROJECT_LOCATION+File.separator;
			}
			arg+=value;
		}
		argument=arg;
	}
	
	public String getArg(String valLoc){
		String arg=getArg();
		arg=arg.replaceAll(ToolsOptionsConstants.PROJECT_LOCATION, valLoc);
		return arg;
	}
	
	public String getArg(){
		
		return argument;
	}

}
