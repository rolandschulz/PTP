/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.model;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.core.runtime.IPath;

/**
 * A serializable wrapper for an IPath.
 */
public class Path implements IPath, Serializable {
	private static final long serialVersionUID = 1L;
	
	String fPath;
	transient IPath fDelegate;
	
	public Path(String path) {
		fPath = path;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		// We can't serialize IPaths in general so just before
		// serialization, convert it to its serializable String form.
		if (fDelegate != null) {
			fPath = fDelegate.toPortableString();
		}
		out.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
	
	void checkDelegate() {
		if (fDelegate == null) {
			fDelegate = new org.eclipse.core.runtime.Path(fPath); 
		}
	}
	
	@Override
	public Object clone() {
		checkDelegate();
		return new Path(fDelegate.toPortableString());
	}
	
	public IPath addFileExtension(String extension) {
		checkDelegate();
		return fDelegate.addFileExtension(extension);
	}

	public IPath addTrailingSeparator() {
		checkDelegate();
		return fDelegate.addTrailingSeparator();
	}

	public IPath append(String path) {
		checkDelegate();
		return fDelegate.append(path);
	}

	public IPath append(IPath path) {
		checkDelegate();
		return fDelegate.append(path);
	}

	public String getDevice() {
		checkDelegate();
		return fDelegate.getDevice();
	}

	public String getFileExtension() {
		checkDelegate();
		return fDelegate.getFileExtension();
	}

	public boolean hasTrailingSeparator() {
		checkDelegate();
		return fDelegate.hasTrailingSeparator();
	}

	public boolean isAbsolute() {
		checkDelegate();
		return fDelegate.isAbsolute();
	}

	public boolean isEmpty() {
		checkDelegate();
		return fDelegate.isEmpty();
	}

	public boolean isPrefixOf(IPath anotherPath) {
		checkDelegate();
		return fDelegate.isPrefixOf(anotherPath);
	}

	public boolean isRoot() {
		checkDelegate();
		return fDelegate.isRoot();
	}

	public boolean isUNC() {
		checkDelegate();
		return fDelegate.isUNC();
	}

	public boolean isValidPath(String path) {
		checkDelegate();
		return fDelegate.isValidPath(path);
	}

	public boolean isValidSegment(String segment) {
		checkDelegate();
		return fDelegate.isValidSegment(segment);
	}

	public String lastSegment() {
		checkDelegate();
		return fDelegate.lastSegment();
	}

	public IPath makeAbsolute() {
		checkDelegate();
		return fDelegate.makeAbsolute();
	}

	public IPath makeRelative() {
		checkDelegate();
		return fDelegate.makeRelative();
	}

	public IPath makeUNC(boolean toUNC) {
		checkDelegate();
		return fDelegate.makeUNC(toUNC);
	}

	public int matchingFirstSegments(IPath anotherPath) {
		checkDelegate();
		return fDelegate.matchingFirstSegments(anotherPath);
	}

	public IPath removeFileExtension() {
		checkDelegate();
		return fDelegate.removeFileExtension();
	}

	public IPath removeFirstSegments(int count) {
		checkDelegate();
		return fDelegate.removeFirstSegments(count);
	}

	public IPath removeLastSegments(int count) {
		checkDelegate();
		return fDelegate.removeLastSegments(count);
	}

	public IPath removeTrailingSeparator() {
		checkDelegate();
		return fDelegate.removeTrailingSeparator();
	}

	public String segment(int index) {
		checkDelegate();
		return fDelegate.segment(index);
	}

	public int segmentCount() {
		checkDelegate();
		return fDelegate.segmentCount();
	}

	public String[] segments() {
		checkDelegate();
		return fDelegate.segments();
	}

	public IPath setDevice(String device) {
		checkDelegate();
		return fDelegate.setDevice(device);
	}

	public File toFile() {
		checkDelegate();
		return fDelegate.toFile();
	}

	public String toOSString() {
		checkDelegate();
		return fDelegate.toOSString();
	}

	public String toPortableString() {
		checkDelegate();
		return fDelegate.toPortableString();
	}

	public IPath uptoSegment(int count) {
		checkDelegate();
		return fDelegate.uptoSegment(count);
	}
	
	@Override
	public String toString() {
		checkDelegate();
		return fDelegate.toString();
	}

	public IPath makeRelativeTo(IPath base) {
		checkDelegate();
		return fDelegate.makeRelativeTo(base);
	}
}
