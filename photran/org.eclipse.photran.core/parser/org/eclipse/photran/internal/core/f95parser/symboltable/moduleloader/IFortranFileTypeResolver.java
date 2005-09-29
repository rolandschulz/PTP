package org.eclipse.photran.internal.core.f95parser.symboltable.moduleloader;

/**
 * An <code>IFortranFileTypeResolver</code> can determine
 * whether or not a given filename corresponds to a Fortran
 * source file, usually by looking at its filename extension.
 * 
 * This is used by the <code>ModuleLoader</code> to avoid
 * parsing non-Fortran files on the module path.
 * 
 * FIXME-Jeff: Write an actual resolver that looks at Eclipse settings
 * 
 * @author Jeff Overbey
 */
public interface IFortranFileTypeResolver
{
	/**
	 * @return true if the file with the given name should
	 * be (attempted to be) parsed as Fortran source (and
	 * searched for modules, in the case of the
	 * <code>ModuleLoader</code>).
	 * 
	 * In a real Eclipse environment, this can be determined
	 * by looking at the filename extensions registered with
	 * Eclipse as being Fortran source code.  In JUnit
	 * testing, either a predefined set of extensions may
	 * be used, or the method may just
	 * <code>return true</code>, and all files on the module
	 * path will be parsed.
	 */
	public boolean isFortranSourceFile(String filename);
}
