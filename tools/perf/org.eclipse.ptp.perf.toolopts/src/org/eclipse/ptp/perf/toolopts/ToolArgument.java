package org.eclipse.ptp.perf.toolopts;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

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
	
	public static int ARG=0;
	public static int VAR=1;
	private int type = ARG;
	
	public void setType(int t){
		type = t;
	}
	
	public Map<String, String> getEnvVars(ILaunchConfiguration configuration) {
		if(type!=VAR||flag==null)
			return null;
		Map<String,String> map = new LinkedHashMap<String,String>();
		
		String val="";
		if(value!=null)
		{
			if(localFile)
			{
				val+=ToolsOptionsConstants.PROJECT_BUILD+File.separator;
			}
			val+=value;
		}
		
		if(isUseConfValue())
		{
			String cval="";
			try {
				cval=configuration.getAttribute(getConfValue(), "");
			} catch (CoreException e) {
				e.printStackTrace();
			}
			val=val.replace(ToolsOptionsConstants.CONF_VALUE, cval);
		}
		
		map.put(flag, val);
		return map;
	}
	
	/**
	 * Builds and returns the argument from the elements defined in this object
	 */
	public String getArgument(ILaunchConfiguration configuration){
		
		if(type!=ARG){
			return null;
		}
		
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
	
	private String getArg(String buildDir, String rootDir){
		String arg=getArg();
		arg=arg.replaceAll(ToolsOptionsConstants.PROJECT_BUILD, buildDir);
		arg=arg.replaceAll(ToolsOptionsConstants.PROJECT_ROOT, rootDir);
		return arg;
	}
	
	private String getArg(){
		
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
				arg+=ToolsOptionsConstants.PROJECT_BUILD+File.separator;
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
