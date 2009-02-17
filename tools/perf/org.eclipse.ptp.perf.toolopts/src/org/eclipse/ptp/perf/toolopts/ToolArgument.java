package org.eclipse.ptp.perf.toolopts;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public class ToolArgument implements IAppInput{
	//private String argument=null;
	private String flag=null;
	private String value=null;
	private String separator="";
	private String confVal=null;
	private boolean localFile=false;
	private boolean useConfValue=false;
	
	/**
	 * Builds and returns the argument from the elements defined in this object
	 */
	public String getArgument(ILaunchConfiguration configuration){
		if(isUseConfValue())
		{
			String carg=getArg();
			String cval="";
			try {
				cval=configuration.getAttribute(getConfValue(), "");
			} catch (CoreException e) {
				e.printStackTrace();
			}
			carg=carg.replace(ToolsOptionsConstants.CONF_VALUE, cval);
			return carg;
		}
		else
		{
			return getArg();
		}
	}
	
	/**
	 * If true the value string is a key for the actual value to be used from the launch configuration object
	 * @return
	 */
	public boolean isUseConfValue() {
		return useConfValue;
	}

	public void setUseConfValue(boolean useConfValue) {
		this.useConfValue = useConfValue;
	}

	public ToolArgument(String arg){
		value=arg;
	}
	
	public ToolArgument(String flag, String value, String sep, boolean local){
		this.flag=flag;
		this.value=value;
		if(sep!=null)
			separator=sep;
		this.localFile=local;
		
	}
	
	public String getArg(String valLoc){
		String arg=getArg();
		arg=arg.replaceAll(ToolsOptionsConstants.PROJECT_LOCATION, valLoc);
		return arg;
	}
	
	public String getArg(){
		
		String arg="";
		if(flag!=null)
		{
			arg+=flag;
			arg+=separator;
		}
		if(useConfValue)
		{
			arg+=ToolsOptionsConstants.CONF_VALUE;
		}
		else
		if(value!=null)
		{
			if(localFile)
			{
				arg+=ToolsOptionsConstants.PROJECT_LOCATION+File.separator;
			}
			arg+=value;
		}
		//argument=arg;
		
		return arg;
	}

	public void setConfValue(String cval) {
		confVal=cval; 
		
	}
	public String getConfValue(){
		return confVal;
	}

}
