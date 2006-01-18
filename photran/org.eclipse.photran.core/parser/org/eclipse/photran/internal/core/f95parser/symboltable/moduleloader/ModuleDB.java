package org.eclipse.photran.internal.core.f95parser.symboltable.moduleloader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.photran.internal.core.f95parser.symboltable.entries.ModuleEntry;

/**
 * Public interface to the module database.
 * 
 * The database associates canonical filenames
 * (see File#getCanonicalPath) of Fortran source files
 * with <code>ModuleEntry</code> objects, one for each
 * module in the file.  The database also records the
 * file's modification timestamp, which can be used to
 * determine whether or not the information in the database
 * is out of date.
 * 
 * Module names should be treated as <i>case insensitive.</i>
 * 
 * Parse tree nodes are <i>not</i> stored in these
 * <code>ModuleEntry</code> objects or their children.
 * These should only be used to determine what symbols are
 * defined in the module.  More specific information can
 * be determined by reparsing the file and recreating its
 * symbol table.
 * 
 * FIXME-Jeff: Write this
 */
public class ModuleDB
{
	/**
	 * Open the module database to prepare for reading and writing
	 */
    public void open()
	{
		mapFilesToMaps = new HashMap();
	}
	
	/**
	 * Close the module database
	 */
    public void close()
	{
	}
	
    public long getTimeFileLastModifiedWhenRecordedInDB(String filePath)
	{
		return 0L;
	}

	// TEMPORARY
	private HashMap mapFilesToMaps = null;
	
    public ModuleEntry getModuleEntryForFile(String moduleName, String filePath)
	{
		HashMap mapNamesToModuleEntries = (HashMap)mapFilesToMaps.get(filePath);
		if (mapNamesToModuleEntries == null)
			return null;
		else
			return (ModuleEntry)mapNamesToModuleEntries.get(moduleName.toLowerCase());
	}
	
    public void storeModuleEntryForFile(ModuleEntry moduleEntry, File file)
	{
        String filePath;
        try { filePath = file.getCanonicalPath(); } catch (IOException e1) { throw new Error(e1); }

		String moduleName = moduleEntry.getIdentifier().getText().toLowerCase();

		HashMap mapNamesToModuleEntries = (HashMap)mapFilesToMaps.get(filePath);
		if (mapNamesToModuleEntries == null)
		{
			mapNamesToModuleEntries = new HashMap();
			mapFilesToMaps.put(filePath, mapNamesToModuleEntries);
		}
		
		mapNamesToModuleEntries.put(moduleName, moduleEntry);
	}

	public void deleteEntriesForFile(String filePath)
	{
		mapFilesToMaps.remove(filePath);
	}
}
