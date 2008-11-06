package org.eclipse.ptp.perf.toolopts;

public class BuildTool extends PerformanceTool {

	
	/**
	 * If true the compiler command is entirely replaced rather than prepended
	 */
	public boolean replaceCompiler=false;
	


	
	protected ToolApp ccCompiler=null;
	protected ToolApp cxxCompiler=null;
	protected ToolApp f90Compiler=null;
	protected ToolApp allCompilers=null;
	
	public ToolApp getCcCompiler(){
		if(ccCompiler!=null)
			return ccCompiler;
		return allCompilers;
	}
	public ToolApp getCxxCompiler(){
		if(cxxCompiler!=null)
			return cxxCompiler;
		return allCompilers;
	}
	public ToolApp getF90Compiler(){
		if(ccCompiler!=null)
			return f90Compiler;
		return allCompilers;
	}
	public ToolApp getGlobalCompiler(){
		return allCompilers;
	}
	

}
