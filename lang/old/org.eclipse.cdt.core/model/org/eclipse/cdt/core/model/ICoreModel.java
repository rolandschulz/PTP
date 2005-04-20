
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;


public interface ICoreModel {

	/**
	 * Creates an ICElement form and IPath. Returns null if not found.
	 */
	public abstract ICElement create(IPath path);

	/**
	 * Creates a translation form and IPath. Returns null if not found.
	 */
	public abstract ITranslationUnit createTranslationUnitFrom(ICProject cproject, IPath path);

	/**
	 * Creates an ICElement form and IFile. Returns null if not found.
	 */
	public abstract ICElement create(IFile file);

	/**
	 * Creates an ICElement form and IFolder. Returns null if not found.
	 */
	public abstract ICContainer create(IFolder folder);

	/**
	 * Creates an ICElement form and IProject. Returns null if not found.
	 */
	public abstract ICProject create(IProject project);

	/**
	 * Creates an ICElement form and IResource. Returns null if not found.
	 */
	public abstract ICElement create(IResource resource);

	/**
	 * Returns the default ICModel.
	 */
	public abstract ICModel getCModel();

	/**
	 * Return true if IFile is a shared library, i.e. libxx.so
	 */
	public abstract boolean isSharedLib(IFile file);

	/**
	 * Return true if IFile is a an object(ELF), i.e. *.o
	 */
	public abstract boolean isObject(IFile file);

	/**
	 * Return true if IFile is an ELF executable
	 */
	public abstract boolean isExecutable(IFile file);

	/**
	 * Return true if IFile is an ELF.
	 */
	public abstract boolean isBinary(IFile file);

	/**
	 * Return true if IFile is an Achive, *.a
	 */
	public abstract boolean isArchive(IFile file);

	public abstract void addElementChangedListener(IElementChangedListener listener);

	/**
	 * Removes the given element changed listener. Has no affect if an
	 * identical listener is not registered.
	 * 
	 * @param listener
	 *            the listener
	 */
	public abstract void removeElementChangedListener(IElementChangedListener listener);

	public abstract void startIndexing();

	public abstract IndexManager getIndexManager();

}
