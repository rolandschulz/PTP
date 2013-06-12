package org.eclipse.ptp.etfw.toolopts;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author wspear
 *Encapsulates the commands, arguments and settings for compile operations to be used in an ETFw workflow
 */
public class BuildTool extends ExternalTool {

	/**
	 * If true the compiler command is entirely replaced rather than prepended
	 */
	public boolean replaceCompiler = false;

	protected ToolApp ccCompiler = null;
	protected ToolApp cxxCompiler = null;
	protected ToolApp f90Compiler = null;
	protected ToolApp allCompilers = null;
	protected ToolApp upcCompiler = null;

	/**
	 * @return a list of the ui pane objects, if any, for all compilers associated with this tool
	 */
	public ArrayList<IToolUITab> getAllCompilerPanes() {

		final ArrayList<IToolUITab> allPanes = new ArrayList<IToolUITab>();

		if (ccCompiler != null && ccCompiler.toolPanes != null) {
			allPanes.addAll(Arrays.asList(ccCompiler.toolPanes));
		}
		if (cxxCompiler != null && cxxCompiler.toolPanes != null) {
			allPanes.addAll(Arrays.asList(cxxCompiler.toolPanes));
		}
		if (f90Compiler != null && f90Compiler.toolPanes != null) {
			allPanes.addAll(Arrays.asList(f90Compiler.toolPanes));
		}
		if (upcCompiler != null && upcCompiler.toolPanes != null) {
			allPanes.addAll(Arrays.asList(upcCompiler.toolPanes));
		}
		if (allCompilers != null && allCompilers.toolPanes != null) {
			allPanes.addAll(Arrays.asList(allCompilers.toolPanes));
		}

		return allPanes;
	}

	/**
	 * @return the cc compiler application or the one for all compilers if none are cc specific
	 */
	public ToolApp getCcCompiler() {
		if (ccCompiler != null) {
			return ccCompiler;
		}
		return allCompilers;
	}

	/**
	 * @return the cxx compiler application or the one for all compilers if none are cxx specific
	 */
	public ToolApp getCxxCompiler() {
		if (cxxCompiler != null) {
			return cxxCompiler;
		}
		return allCompilers;
	}

	/**
	 * @return the f90 compiler application or the one for all compilers if none are f90 specific
	 */
	public ToolApp getF90Compiler() {
		if (f90Compiler != null) {
			return f90Compiler;
		}
		return allCompilers;
	}

	/**
	 * @return the compiler application for all compilers
	 */
	public ToolApp getGlobalCompiler() {
		return allCompilers;
	}

	/**
	 * @return the upc compiler application or the one for all compilers if none are upc specific
	 */
	public ToolApp getUPCCompiler() {
		if (upcCompiler != null) {
			return upcCompiler;
		}
		return allCompilers;
	}

	/**
	 * Associate the input ToolApp with all compilers
	 * @since 5.0
	 */
	public void setAllCompilers(ToolApp allCompilers) {
		this.allCompilers = allCompilers;
	}

	/**
	 * Associate the input ToolApp with the cc compiler
	 * @since 5.0
	 */
	public void setCcCompiler(ToolApp ccCompiler) {
		this.ccCompiler = ccCompiler;
	}

	/**
	 * Associate the input ToolApp with the cxx compiler
	 * @since 5.0
	 */
	public void setCxxCompiler(ToolApp cxxCompiler) {
		this.cxxCompiler = cxxCompiler;
	}

	/**
	 * Associate the input ToolApp with the f90 compiler
	 * @since 5.0
	 */
	public void setF90Compiler(ToolApp f90Compiler) {
		this.f90Compiler = f90Compiler;
	}

	/**
	 * Associate the input ToolApp with the upc compiler
	 * @since 5.0
	 */
	public void setUpcCompiler(ToolApp upcCompiler) {
		this.upcCompiler = upcCompiler;
	}

}
