package org.eclipse.photran.internal.core.f95parser.symboltable.moduleloader;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.photran.internal.core.f95parser.FortranProcessor;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.ModuleEntry;

/**
 * The <code>ModuleLoader</code> is responsible for finding
 * Fortran modules contained in other files on the module
 * path and returning a <code>ModuleEntry</code>, i.e.,
 * the symbol table for that module.
 * 
 * FIXME-Jeff: Is this complete?
 * 
 * @author Jeff Overbey
 */
public class ModuleLoader
{
	private static ModuleLoader instance = null; // Singleton
	
	private static Runnable initializationRunnable = null;
	
	private String[] modulePaths = null;
	
	private IFortranFileTypeResolver resolver = null;
	
	private ModuleDB db = null;

	/**
	 * Provides
	 * @param initializationRunnable
	 */
	public static void setInitializationRunnable(Runnable initializationRunnable)
	{
		ModuleLoader.initializationRunnable = initializationRunnable;
	}
	
	/**
	 * Initializes the (Singleton) <code>ModuleLoader</code>,
	 * instructing it to search the given paths for modules.
	 * Must be called before <code>findModule</code>.
	 * 
	 * @param modulePaths list of directories, not <code>null</code>
	 * @param resolver not <code>null</code>
	 * @param pm possibly <code>null</code>
	 */
	public static void initialize(String[] modulePaths, IFortranFileTypeResolver resolver, IProgressMonitor pm)
	{
		ModuleLoader.instance = new ModuleLoader(modulePaths, resolver, pm);
	}
	
	public static ModuleLoader getDefault()
	{
		if (instance == null)
		{
			if (initializationRunnable == null)
				throw new Error("setInitializationRunnable() must be called with a non-null argument before calling getDefault");
			
			initializationRunnable.run();
		}
		
		return instance;
	}

	private ModuleLoader(String[] modulePaths, IFortranFileTypeResolver resolver, IProgressMonitor pm)
	{
		this.modulePaths = modulePaths;
		this.resolver = resolver;
		this.db = new ModuleDB();
	}
	
	public ModuleEntry findModule(String moduleName, IProgressMonitor pm)
	{
		if (instance == null)
			throw new Error("ModuleLoader#initialize must be called before ModuleLoader#findModule");
		
		db.open();
		try
		{
			return findModuleInModulePaths(moduleName, pm);
		}
		finally
		{
			db.close();
		}
	}
	
	private ModuleEntry findModuleInModulePaths(String moduleName, IProgressMonitor pm)
	{
		for (int i = 0; i < modulePaths.length && (pm == null || !pm.isCanceled()); i++)
		{
			ModuleEntry result = findModuleInDirectory(moduleName, new File(modulePaths[i]), pm);
			if (result != null) return result;
		}
		return null;
	}

    /**
     * If <code>file</code> is a Fortran source file and it contains
     * a module with the given name, return a <code>ModuleEntry</code>
     * corresponding to that module.  If <code>file</code> is a directory,
     * recursively call this function on each file in the directory,
     * returning a <code>ModuleEntry</code> as soon as one is found or
     * <code>null</code> if no files in this directory contain a module
     * with the given name.
     */
    private ModuleEntry findModuleInFileOrDirectory(String moduleName, File fileOrDirectory, IProgressMonitor pm)
    {
		if (fileOrDirectory.isDirectory())
            return findModuleInDirectory(moduleName, fileOrDirectory, pm);
		else
			return findModuleInFile(moduleName, fileOrDirectory, pm);
    }

    private ModuleEntry findModuleInDirectory(String moduleName, File file, IProgressMonitor monitor)
    {
            File[] files = file.listFiles();
            ModuleEntry result = null;
            for (int i = 0; i < files.length && (monitor == null || !monitor.isCanceled()); i++)
            {
                result = findModuleInFileOrDirectory(moduleName, files[i], monitor);
                if (result != null) return result;
            }
            return null;
    }

    private ModuleEntry findModuleInFile(String moduleName, File file, IProgressMonitor monitor)
    {
            String filePath;
            try { filePath = file.getCanonicalPath(); } catch (IOException e1) { throw new Error(e1); }
            
            if (!resolver.isFortranSourceFile(filePath))
            	return null;
            
            if (monitor != null) monitor.subTask("Searching " + file.getName() + "...");

            // Process...
            return findModuleInModuleDB(moduleName, file);
    }

	private ModuleEntry findModuleInModuleDB(String moduleName, File file) {
        String filePath;
        try { filePath = file.getCanonicalPath(); } catch (IOException e1) { throw new Error(e1); }
        
        long fileLastModified = file.lastModified();
		long dbLastModified = db.getTimeFileLastModifiedWhenRecordedInDB(filePath);
		
		if (fileLastModified != dbLastModified)
			parseFileAndUpdateDatabase(file);
		
		return db.getModuleEntryForFile(moduleName, filePath);
	}

	private void parseFileAndUpdateDatabase(final File file) {
		try
		{
			String filePath = file.getCanonicalPath();

			db.deleteEntriesForFile(filePath);
			
			SymbolTable symTbl = new FortranProcessor().parseAndCreateSymbolTableFor(filePath);
			symTbl.stripParseTreesAndReferences();
			symTbl.visitUsing(new SymbolTableVisitor()
				{
					public void visit(ModuleEntry entry) {
						db.storeModuleEntryForFile(entry, file);
					}
				});
		}
		catch (Exception e) {} // Ignore exceptions
	}
}
