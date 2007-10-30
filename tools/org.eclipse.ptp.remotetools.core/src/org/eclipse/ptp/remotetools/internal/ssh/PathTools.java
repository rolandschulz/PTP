/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.ssh;

import java.awt.geom.IllegalPathStateException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.eclipse.ptp.remotetools.core.IRemotePathTools;


public class PathTools implements IRemotePathTools {
	private static final String rootPath = "/"; //$NON-NLS-1$
	ExecutionManager executionManager;
	
	public PathTools(ExecutionManager manager) {
		this.executionManager = manager;
	}

	public String rootPath() {
		return new String(rootPath);
	}

	public boolean isRoot(String path) {
		if (path == null) throw new IllegalPathStateException();
		return path.equals(rootPath);
	}

	public boolean isAbsolute(String path) {
		if (path == null) throw new IllegalPathStateException();
		return path.startsWith("/"); //$NON-NLS-1$
	}

	public boolean isRelative(String path) {
		if (path == null) throw new IllegalPathStateException();
		return ! path.startsWith("/"); //$NON-NLS-1$
	}
	
	public boolean isLeave(String path) {
		if (path == null) throw new IllegalPathStateException();
		return ! (isRoot(path) || path.endsWith("/")); //$NON-NLS-1$
	}
	
	public String leave(String path) {
		if (path == null) throw new IllegalPathStateException();
		if (isRoot(path)) return rootPath();
		if (isLeave(path)) {
			return path.substring(0, path.length()-1);
		} else {
			return new String(path);
		}
	}

	public String canonicalize(String path) {
		/*
		 * Remove "." (current directory)
		 * Remove "a/.." (parent directory)
		 * Remove "//" (empty directory)
		 */
		if (path == null) throw new IllegalPathStateException();
		if (isRoot(path)) return rootPath();
		
		/*
		 * Split the path an remeber properties.
		 */
		String segments[] = split(path);
		boolean absolute = isAbsolute(path);
		boolean leave = isLeave(path);
		
		return canonicalize(segments, absolute, leave);
	}
		
	private String canonicalize(String segments[], boolean absolute, boolean leave) {
		int lastPosition = 0;
		int currentPosition = 0;
		int currentSize = segments.length;
		while (currentPosition < segments.length) {
			String segment = segments[currentPosition];
			if (segment.equals(".") || segment.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				// Skip segment
				currentPosition++;
				continue;
			} else if (segment.equals("..")) { //$NON-NLS-1$
				if (lastPosition == 0) {
					if (absolute) {
						// Ignore, the parent of the root is the root itself.
						currentPosition++;				
					} else {
						// Keep, since the path is relative.
						if (currentPosition > lastPosition) {
							segments[lastPosition] = segments[currentPosition];
						}
						lastPosition++;
						currentPosition++;				
					}
				} else {
					// Remove the segment and the parent, if the parent is not already ".."
					String segment2 = segments[lastPosition-1];
					if (segment2.equals("..")) { //$NON-NLS-1$
						// Keep
						if (currentPosition > lastPosition) {
							segments[lastPosition] = segments[currentPosition];
						}
						lastPosition++;
						currentPosition++;				
					} else {
						// Remove parent
						lastPosition--;
						currentPosition++;
					}
				}
			} else {
				// Keep
				if (currentPosition > lastPosition) {
					segments[lastPosition] = segments[currentPosition];
				}
				lastPosition++;
				currentPosition++;
			}
		}
		currentSize = lastPosition;
				
		/*
		 * Rebuild the path as a string.
		 */
		return unsplit(segments, 0, currentSize-1, absolute, leave);
	}
	
	public String join(String base, String path) {
		if (base == null) throw new IllegalPathStateException();
		if (path == null) throw new IllegalPathStateException();
		if (isAbsolute(path)) return canonicalize(path);
		String s1[] = split(base);
		String s2[] = split(path);
		String s[] = new String[s1.length + s2.length];
		System.arraycopy(s1, 0, s, 0, s1.length);
		System.arraycopy(s2, 0, s, s1.length, s2.length);
		boolean absolute = isAbsolute(base);
		boolean leave = isLeave(path);
		return canonicalize(s,absolute,leave);
	}
	
	public String parent(String path) {
		if (path == null) throw new IllegalPathStateException();
		if (isRoot(path)) return rootPath();
		return join(path, ".."); //$NON-NLS-1$
	}
	
	private String [] split (String path) {
		if (isRoot(path)) {
			return new String[0];
		}
		int start = 0;
		int end = path.length();
		if (isAbsolute(path)) {
			// Ignore trailing "/"
			start++;
		}
		if (! isLeave(path)) {
			// Ignore ending "/"
			end--;
		}
		return path.substring(start, end).split("/"); //$NON-NLS-1$
	}

	private String unsplit(String[] segments, int start, int end, boolean absolute, boolean leave) {
		String result = null;
		int segmentCount = end-start+1;
		if (segmentCount <= 0) {
			if (absolute) {
				return rootPath;
			} else {
				return ""; //$NON-NLS-1$
			}
		}
		if (absolute) {
			result = "/"; //$NON-NLS-1$
		} else {
			result = ""; //$NON-NLS-1$
		}
		for (int i = start; i <= end; i++) {
			if (i != start) {
				result += "/"; //$NON-NLS-1$
			}
			result += segments[i];
		}
		if (! leave) {
			if (segmentCount > 0) {
				result += "/"; //$NON-NLS-1$
			}
		}
		return result;
	}
	
	public String quote(String path, boolean full) {
		StringBuffer buffer = new StringBuffer();
		StringCharacterIterator iter = new StringCharacterIterator(path);
		for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			switch (c) {
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '|':
			case '\\':
			case '*':
			case '&':
			case '^':
			case '%':
			case '$':
			case '#':
			case '@':
			case '!':
			case '~':
			case '`':
			case '\'':
			case '"':
			case ':':
			case ';':
			case '?':
			case '<':
			case '>':
			case ',':
			case '\n':
				if (full) {
					buffer.append('\\');
				}
				buffer.append(c);
				continue;
			case ' ':
				buffer.append('\\');
				buffer.append(c);
				continue;
			default:
				buffer.append(c);
				continue;
			}
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		IRemotePathTools tools = new PathTools(null);
		System.out.println(tools.rootPath());
		System.out.println();
		System.out.println(tools.isRoot("/")); //$NON-NLS-1$
		System.out.println(tools.isRoot("/a")); //$NON-NLS-1$
		System.out.println(tools.isRoot("/a/")); //$NON-NLS-1$
		System.out.println(tools.isRoot("a")); //$NON-NLS-1$
		System.out.println();
		System.out.println(tools.isAbsolute("/")); //$NON-NLS-1$
		System.out.println(tools.isAbsolute("/a")); //$NON-NLS-1$
		System.out.println(tools.isAbsolute("/a/")); //$NON-NLS-1$
		System.out.println(tools.isAbsolute("a")); //$NON-NLS-1$
		System.out.println();
		System.out.println(tools.isRelative("/")); //$NON-NLS-1$
		System.out.println(tools.isRelative("/a")); //$NON-NLS-1$
		System.out.println(tools.isRelative("/a/")); //$NON-NLS-1$
		System.out.println(tools.isRelative("a")); //$NON-NLS-1$
		System.out.println();
		System.out.println(tools.isLeave("/")); //$NON-NLS-1$
		System.out.println(tools.isLeave("/a")); //$NON-NLS-1$
		System.out.println(tools.isLeave("/a/")); //$NON-NLS-1$
		System.out.println(tools.isLeave("a")); //$NON-NLS-1$
		System.out.println();
		System.out.println(tools.parent("/")); //$NON-NLS-1$
		System.out.println(tools.parent("/a")); //$NON-NLS-1$
		System.out.println(tools.parent("/a/b")); //$NON-NLS-1$
		System.out.println(tools.parent("/a/")); //$NON-NLS-1$
		System.out.println(tools.parent("/a/.")); //$NON-NLS-1$
		System.out.println(tools.parent("/a/..")); //$NON-NLS-1$
		System.out.println(tools.parent("/a//")); //$NON-NLS-1$
		System.out.println(tools.parent("/a/b/")); //$NON-NLS-1$
		System.out.println(tools.parent("/a/b/.")); //$NON-NLS-1$
		System.out.println(tools.parent("/a/b/..")); //$NON-NLS-1$
		System.out.println(tools.parent("/a/b//")); //$NON-NLS-1$
		System.out.println(tools.parent("/..")); //$NON-NLS-1$
		System.out.println(tools.parent("/.")); //$NON-NLS-1$
		System.out.println();
		System.out.println(tools.parent("")); //$NON-NLS-1$
		System.out.println(tools.parent("a")); //$NON-NLS-1$
		System.out.println(tools.parent("a/b")); //$NON-NLS-1$
		System.out.println(tools.parent(Messages.PathTools_47));
		System.out.println(tools.parent("a/.")); //$NON-NLS-1$
		System.out.println(tools.parent("a/..")); //$NON-NLS-1$
		System.out.println(tools.parent("a//")); //$NON-NLS-1$
		System.out.println(tools.parent("a/b/")); //$NON-NLS-1$
		System.out.println(tools.parent("a/b/.")); //$NON-NLS-1$
		System.out.println(tools.parent("a/b/..")); //$NON-NLS-1$
		System.out.println(tools.parent("a/b//")); //$NON-NLS-1$
		System.out.println(tools.parent("..")); //$NON-NLS-1$
		System.out.println(tools.parent(".")); //$NON-NLS-1$
		System.out.println();
		System.out.println(tools.join("b", "a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b", "/a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b", "..")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b", ".")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b", "")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b", "/")); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println(tools.join("b/", "a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b/", "/a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b/", "..")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b/", ".")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b/", "")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("b/", "/")); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println(tools.join(".", "a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join(".", "/a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join(".", "..")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join(".", ".")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join(".", "")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join(".", "/")); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println(tools.join("..", "a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("..", "/a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("..", "..")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("..", ".")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("..", "")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("..", "/")); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println(tools.join("", "a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("", "/a")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("", "..")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("", ".")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("", "")); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(tools.join("", "/")); //$NON-NLS-1$ //$NON-NLS-2$

	}
	
}
