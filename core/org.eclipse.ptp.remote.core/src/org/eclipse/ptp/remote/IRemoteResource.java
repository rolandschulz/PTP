/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IRemoteResource {
	/**
	 * A constant known to be zero (0), used in operations which
	 * take bit flags to indicate that "no bits are set".  This value is
	 * also used as a default value in cases where a file system attribute
	 * cannot be computed.
	 */
	public static final int NONE = 0;

	/**
	 * Option flag constant (value 1 &lt;&lt;0) indicating a file opened
	 * for appending data to the end.
	 */
	public static final int APPEND = 1 << 0;

	/**
	 * Option flag constant (value 1 &lt;&lt;1) indicating that existing
	 * files may be overwritten.
	 */
	public static final int OVERWRITE = 1 << 1;

	/**
	 * Option flag constant (value 1 &lt;&lt;2) indicating that an
	 * operation acts on a single file or directory, and not its parents
	 * or children.
	 */
	public static final int SHALLOW = 1 << 2;

	/**
	 * Option flag constant (value 1 &lt;&lt;10) indicating that a
	 * file's attributes should be updated.
	 */
	public static final int SET_ATTRIBUTES = 1 << 10;

	/**
	 * Option flag constant (value 1 &lt;&lt;11) indicating that a
	 * file's last modified time should be updated.
	 */
	public static final int SET_LAST_MODIFIED = 1 << 11;

	/**
	 * Option flag constant (value 1 &lt;&lt;12) indicating that
	 * a cached representation of a file should be returned.
	 */
	public static final int CACHE = 1 << 12;

	/**
	 * Attribute constant (value 1 &lt;&lt;1) indicating that a
	 * file is read only.
	 */
	public static final int ATTRIBUTE_READ_ONLY = 1 << 1;

	/**
	 * Attribute constant (value 1 &lt;&lt;2) indicating that a
	 * file is a executable.
	 */
	public static final int ATTRIBUTE_EXECUTABLE = 1 << 2;

	/**
	 * Attribute constant (value 1 &lt;&lt;3) indicating that a
	 * file is an archive.
	 */
	public static final int ATTRIBUTE_ARCHIVE = 1 << 3;

	/**
	 * Attribute constant (value 1 &lt;&lt;4) indicating that a
	 * file is hidden.
	 */
	public static final int ATTRIBUTE_HIDDEN = 1 << 4;

	/**
	 * Attribute constant (value 1 &lt;&lt;5) indicating that a
	 * file is a symbolic link.
	 */
	public static final int ATTRIBUTE_SYMLINK = 1 << 5;

	/**
	 * Attribute constant (value 1 &lt;&lt;6) for a string attribute indicating the
	 * target file name of a symbolic link.
	 */
	public static final int ATTRIBUTE_LINK_TARGET = 1 << 6;

	/**
	 * Returns an {@link IRemoteResourceInfo} instance for each file and directory contained 
	 * within this resource.
	 * 
	 * @param options bit-wise or of option flag constants (currently only {@link IRemoteResource#NONE}
	 * is applicable).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return An array of information about the children of this resource, or an empty 
	 * array if this resource has no children.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This resource does not exist.</li>
	 * </ul>
	 */
	public IRemoteResourceInfo[] childResourceInfos(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an {@link IRemoteResource} instance for each file and directory contained 
	 * within this resource.
	 * 
	 * @param options bit-wise or of option flag constants (currently only {@link IRemoteResource#NONE}
	 * is applicable).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return The children of this resource, or an empty array if this
	 * resource has no children.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This resource does not exist.</li>
	 * </ul>
	 */
	public IRemoteResource[] childResources(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Copies the file represented by this resource to the provided destination resource.
	 * Copying occurs with best-effort semantics; if some files cannot be copied,
	 * exceptions are recorded but other files will continue to be copied if possible.
	 * 
	 * <p>
	 * The {@link IRemoteResource#OVERWRITE} option flag indicates how
	 * this method deals with files that already exist at the copy destination. If
	 * the <code>IRemoteResource#OVERWRITE</code> flag is present, then existing files at the
	 * destination are overwritten with the corresponding files from the source
	 * of the copy operation.  When this flag is not present, existing files at
	 * the destination are not overwritten and an exception is thrown indicating
	 * what files could not be copied.
	 * </p>
	 * <p>
	 * The {@link IRemoteResource#SHALLOW} option flag indicates how
	 * this method deals with copying of directories. If the <code>SHALLOW</code> 
	 * flag is present, then a directory will be copied but the files and directories
	 * within it will not.  When this flag is not present, all child directories and files
	 * of a directory are copied recursively.
	 * </p>
	 * 
	 * @param destination The destination of the copy.
	 * @param options bit-wise or of option flag constants (
	 * {@link IRemoteResource#OVERWRITE} or {@link IRemoteResource#SHALLOW}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * <li> The parent of the destination file store does not exist.</li>
	 * <li> The <code>IRemoteResource#OVERWRITE</code> flag is not specified and a file of the
	 * same name already exists at the copy destination.</li>
	 * </ul>
	 */
	public void copy(IRemoteResource destination, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Deletes the files and directories represented by this resource. Deletion of a file
	 * that does not exist has no effect.
	 * <p>
	 * Deletion occurs with best-effort semantics; if some files cannot be deleted,
	 * exceptions are recorded but other files will continue to be deleted if possible.
	 * </p>
	 * <p>
	 * Deletion of a file with attribute {@link IRemoteResource#ATTRIBUTE_SYMLINK} will always 
	 * delete the link, rather than the target of the link.
	 * </p>
	 * 
	 * @param options bit-wise or of option flag constants (currently only {@link IRemoteResource#NONE}
	 * is applicable).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>Files or directories could not be deleted.
	 * </ul>
	 * @see IRemoteResource#ATTRIBUTE_SYMLINK
	 */
	public void delete(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Fetches and returns information about this resource from the file
	 * system.  Returns an object representing a non-existent resource if the 
	 * file system could not be contacted.
	 * <p>
	 * This is a convenience method, similar to: 
	 * <code>fetchInfo(IRemoteResource.NONE, null)</code>.
	 * This method is intended as a convenience when dealing with fast,
	 * highly available file systems such as the local file system.  Clients that
	 * require progress reporting and error handling, for example when dealing
	 * with remote file systems, should use {@link #fetchInfo(int, IProgressMonitor)}
	 * instead.
	 * </p>
	 * 
	 * @return A structure containing information about this file.
	 * @see #fetchInfo(int, IProgressMonitor)
	 */
	public IRemoteResourceInfo fetchInfo() throws CoreException;

	/**
	 * Fetches and returns information about this resource from the file
	 * system.
	 * <p>
	 * This method succeeds regardless of whether a corresponding
	 * resource currently exists. In the case of a non-existent
	 * resource, the returned info will include the resource's name and will return <code>false</code>
	 * when IRemoteResource#exists() is called, but all other information will assume default 
	 * values.
	 * 
	 * @param options bit-wise or of option flag constants (currently only {@link IRemoteResource#NONE}
	 * is applicable).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return A structure containing information about this resource.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>Problems occurred while contacting the file system.</li>
	 * </ul>
	 */
	public IRemoteResourceInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns a child resource with the provided name whose parent is
	 * this resource.  This is a handle-only method; a child is provided regardless
	 * of whether this resource or the child resource exists, or whether this resource
	 * represents a directory or not.
	 * 
	 * @param name The name of the child resource to return
	 * @return A child resource.
	 */
	public IRemoteResource getChild(String name, IProgressMonitor monitor);

	/**
	 * Returns the name of this resource.  This is a handle-only method; the name
	 * is returned regardless of whether this resource exists.
	 * <p>
	 * Note that when dealing with case-insensitive file systems, this name
	 * may differ in case from the name of the corresponding file in the file
	 * system.  To obtain the exact name used in the file system, use
	 * <code>fetchInfo().getName()</code>.
	 * </p>
	 * @return The name of this resource
	 */
	public String getName();

	/**
	 * Returns the parent of this resource.  This is a handle only method; the parent
	 * is returned regardless of whether this resource or the parent resource exists. This
	 * method returns <code>null</code> when this resource represents the root
	 * directory of a file system.
	 * 
	 * @return The parent resource, or <code>null</code> if this resource is the root
	 * of a file system.
	 */
	public IRemoteResource getParent();

	/**
	 * Returns whether this resource is a parent of the provided resource.  This
	 * is equivalent to, but typically more efficient than, the following:
	 * <code>
	 * while (true) {
	 * 	other = other.getParent();
	 * 	if (other == null)
	 * 		return false;
	 * 	if (this.equals(other))
	 * 		return true;
	 * }
	 * </code>
	 * <p>
	 * This is a handle only method; this test works regardless of whether
	 * this resource or the parameter resource exists.
	 * </p>
	 * 
	 * @param other The resource to test for parentage.
	 * @return <code>true</code> if this resource is a parent of the provided
	 * resource, and <code>false</code> otherwise.
	 */
	public boolean isParentOf(IRemoteResource other);

	/**
	 * Creates a directory, and optionally its parent directories.  If the directory 
	 * already exists, this method has no effect.
	 * <p>
	 * The {@link IRemoteResource#SHALLOW} option flag indicates how
	 * this method deals with creation when the parent directory does not exist.
	 * If the <code>SHALLOW</code> flag is present, this method will fail if
	 * the parent directory does not exist.  When the flag is not present, all
	 * necessary parent directories are also created.
	 * </p>
	 * 
	 * @param options bit-wise or of option flag constants ({@link IRemoteResource#SHALLOW}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return This directory
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>The directory could not be created</li>
	 * <li>A file already exists with this name that is not a directory</li>
	 * <li>The {@link IRemoteResource#SHALLOW} option flag was
	 * specified and the parent of this directory does not exist.</li>
	 * </ul>
	 */
	public IRemoteResource mkdir(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Moves the file represented by this resource to the provided destination resource.
	 * Moving occurs with best-effort semantics; if some files cannot be moved,
	 * exceptions are recorded but other files will continue to be moved if possible.
	 * 
	 * <p>
	 * The {@link IRemoteResource#OVERWRITE} option flag indicates how
	 * this method deals with files that already exist at the move destination. If
	 * the <code>OVERWRITE</code> flag is present, then existing files at the
	 * destination are overwritten with the corresponding files from the source
	 * of the move operation.  When this flag is not present, existing files at
	 * the destination are not overwritten and an exception is thrown indicating
	 * what files could not be moved.
	 * </p>
	 * 
	 * @param destination The destination of the move.
	 * @param options bit-wise or of option flag constants 
	 * ({@link IRemoteResource#OVERWRITE}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This resource does not exist.</li>
	 * <li> The parent of the destination resource does not exist.</li>
	 * <li> The {@link IRemoteResource#OVERWRITE} flag is not specified and a file of the
	 * same name already exists at the destination.</li>
	 * </ul>
	 */
	public void move(IRemoteResource destination, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an open input stream on the contents of this resource.  The caller
	 * is responsible for closing the provided stream when it is no longer
	 * needed.
	 * <p>
	 * The returned stream is not guaranteed to be buffered efficiently.  When reading
	 * large blocks of data from the stream, a <code>BufferedInputStream</code>
	 * wrapper should be used, or some other form of content buffering.
	 * </p>
	 * 
	 * @param options bit-wise or of option flag constants (currently only {@link EFS#NONE}
	 * is applicable).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return An input stream on the contents of this resource.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>This resource does not exist.</li>
	 * <li>This resource represents a directory.</li>
	 * </ul>
	 */
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an open output stream on the contents of this resource.  The caller
	 * is responsible for closing the provided stream when it is no longer
	 * needed.  This resource need not exist in the underlying file system at the
	 * time this method is called.
	 * <p>
	 * The returned stream is not guaranteed to be buffered efficiently.  When writing
	 * large blocks of data to the stream, a <code>BufferedOutputStream</code>
	 * wrapper should be used, or some other form of content buffering.
	 * </p>
	 * <p>
	 * The {@link IRemoteResource#APPEND} update flag controls where
	 * output is written to the resource.  If this flag is specified, content written
	 * to the stream will be appended to the end of the resource.  If this flag is
	 * not specified, the contents of the existing resource, if any, is truncated to zero
	 * and the new output will be written from the start of the resource.
	 * </p>
	 * 
	 * @param options bit-wise or of option flag constants (
	 * {@link IRemoteResource#APPEND}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return An output stream on the contents of this resource.
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>This resource represents a directory.</li>
	 * <li>The parent of this resource does not exist.</li>
	 * </ul>
	 */
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Writes information about this resource to the underlying file system. Only 
	 * certain parts of the resource information structure can be written using this
	 * method, as specified by the option flags.  Other changed information
	 * in the provided info will be ignored.  This method has no effect when no 
	 * option flags are provided.  
	 * <p>
	 * The {@link IRemoteResource#SET_ATTRIBUTES} update flag controls 
	 * whether the resource's attributes are changed.  When this flag is specified,
	 * the <code>IRemoteResource#ATTRIBUTE_*</code> values, with
	 * the exception of <code>IRemoteResource#ATTRIBUTE_DIRECTORY</code>,
	 * <code>IRemoteResource#ATTRIBUTE_SYMLINK</code> and
	 * <code>IRemoteResource#ATTRIBUTE_LINK_TARGET</code>,
	 * are set for this resource. When this flag is not specified, changed attributes
	 * on the provided resource info are ignored.
	 * </p>
	 * <p>
	 * The {@link IRemoteResource#SET_LAST_MODIFIED} update flag controls 
	 * whether the resource's last modified time is changed.  When this flag is specified,
	 * the last modified time for the resource in the underlying file system is updated
	 * to the value in the provided info object.  Due to the different granularities
	 * of file systems, the time that is set might not exact match the provided
	 * time.
	 * </p>
	 * 
	 * @param info The resource information instance containing the values to set.
	 * @param options bit-wise or of option flag constants (
	 * {@link IRemoteResource#SET_ATTRIBUTES} or {@link IRemoteResource#SET_LAST_MODIFIED}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This resource does not exist.</li>
	 * </ul>
	 */
	public void putInfo(IRemoteResourceInfo info, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns a string representation of this resource.  The string will be translated
	 * if applicable, and suitable for displaying in error messages to an end-user.
	 * The actual format of the string is unspecified.
	 * 
	 * @return A string representation of this resource.
	 */
	public String toString();

	/**
	 * Returns a URI instance corresponding to this resource.
	 * 
	 * @return A URI corresponding to this resource.
	 */
	public URI toURI();
}
