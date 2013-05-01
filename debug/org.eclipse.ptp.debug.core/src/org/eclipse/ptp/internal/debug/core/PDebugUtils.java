/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.w3c.dom.Document;

/**
 * @author Clement chu
 * 
 */
public class PDebugUtils {
	public static final boolean DEBUG = true;

	public static void println(String msg) {
		System.err.println(msg);
	}

	public static boolean question(IStatus status, Object source) {
		Boolean result = Boolean.FALSE;
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
		if (handler != null) {
			try {
				result = (Boolean) handler.handleStatus(status, source);
			} catch (CoreException e) {
			}
		}
		return result.booleanValue();
	}

	public static void error(IStatus status, Object source) {
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
		if (handler != null) {
			try {
				handler.handleStatus(status, source);
			} catch (CoreException e) {
			}
		}
	}

	public static void info(IStatus status, Object source) {
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
		if (handler != null) {
			try {
				handler.handleStatus(status, source);
			} catch (CoreException e) {
			}
		}
	}

	public static char[] getByteText(byte b) {
		return new char[] { charFromByte((byte) ((b >>> 4) & 0x0f)), charFromByte((byte) (b & 0x0f)) };
	}

	public static byte textToByte(char[] text) {
		byte result = 0;
		if (text.length == 2) {
			byte[] bytes = { charToByte(text[0]), charToByte(text[1]) };
			result = (byte) ((bytes[0] << 4) + bytes[1]);
		}
		return result;
	}

	public static char charFromByte(byte value) {
		if (value >= 0x0 && value <= 0x9)
			return (char) (value + '0');
		if (value >= 0xa && value <= 0xf)
			return (char) (value - 0xa + 'a');
		return '0';
	}

	public static byte charToByte(char ch) {
		if (Character.isDigit(ch)) {
			return (byte) (ch - '0');
		}
		if (ch >= 'a' && ch <= 'f') {
			return (byte) (0xa + ch - 'a');
		}
		if (ch >= 'A' && ch <= 'F') {
			return (byte) (0xa + ch - 'A');
		}
		return 0;
	}

	public static char bytesToChar(byte[] bytes) {
		try {
			return (char) Short.parseShort(new String(bytes), 16);
		} catch (RuntimeException e) {
		}
		return 0;
	}

	public static byte toByte(char[] bytes, boolean le) {
		if (bytes.length != 2)
			return 0;
		return (byte) Long.parseLong(bytesToString(bytes, le, true), 16);
	}

	public static short toUnsignedByte(char[] bytes, boolean le) {
		if (bytes.length != 2)
			return 0;
		return (short) Long.parseLong(bytesToString(bytes, le, false), 16);
	}

	public static short toShort(char[] bytes, boolean le) {
		if (bytes.length != 4)
			return 0;
		return (short) Long.parseLong(bytesToString(bytes, le, true), 16);
	}

	public static int toUnsignedShort(char[] bytes, boolean le) {
		if (bytes.length != 4)
			return 0;
		return (int) Long.parseLong(bytesToString(bytes, le, false), 16);
	}

	public static int toInt(char[] bytes, boolean le) {
		if (bytes.length != 8)
			return 0;
		return (int) Long.parseLong(bytesToString(bytes, le, true), 16);
	}

	public static long toUnsignedInt(char[] bytes, boolean le) {
		if (bytes.length != 8)
			return 0;
		return Long.parseLong(bytesToString(bytes, le, false), 16);
	}

	private static String bytesToString(char[] bytes, boolean le, boolean signed) {
		char[] copy = new char[bytes.length];
		if (le) {
			for (int i = 0; i < bytes.length / 2; ++i) {
				copy[2 * i] = bytes[bytes.length - 2 * i - 2];
				copy[2 * i + 1] = bytes[bytes.length - 2 * i - 1];
			}
		} else {
			System.arraycopy(bytes, 0, copy, 0, copy.length);
		}
		return new String(copy);
	}

	public static String prependString(String text, int length, char ch) {
		StringBuffer sb = new StringBuffer(length);
		if (text.length() > length) {
			sb.append(text.substring(0, length));
		} else {
			char[] prefix = new char[length - text.length()];
			Arrays.fill(prefix, ch);
			sb.append(prefix);
			sb.append(text);
		}
		return sb.toString();
	}

	public static List<IProject> getReferencedProjects(IProject project) {
		ArrayList<IProject> list = new ArrayList<IProject>(10);
		if (project != null && project.exists() && project.isOpen()) {
			IProject[] refs = new IProject[0];
			try {
				refs = project.getReferencedProjects();
			} catch (CoreException e) {
			}
			for (int i = 0; i < refs.length; ++i) {
				if (!project.equals(refs[i]) && refs[i] != null && refs[i].exists() && refs[i].isOpen()) {
					list.add(refs[i]);
					getReferencedProjects(project, refs[i], list);
				}
			}
		}
		return list;
	}

	private static void getReferencedProjects(IProject root, IProject project, List<IProject> list) {
		if (project != null && project.exists() && project.isOpen()) {
			IProject[] refs = new IProject[0];
			try {
				refs = project.getReferencedProjects();
			} catch (CoreException e) {
			}
			for (int i = 0; i < refs.length; ++i) {
				if (!list.contains(refs[i]) && refs[i] != null && !refs[i].equals(root) && refs[i].exists() && refs[i].isOpen()) {
					list.add(refs[i]);
					getReferencedProjects(root, refs[i], list);
				}
			}
		}
	}

	public static boolean isReferencedProject(IProject parent, IProject project) {
		if (parent != null && parent.exists()) {
			List<IProject> projects = PDebugUtils.getReferencedProjects(project);
			Iterator<IProject> it = projects.iterator();
			while (it.hasNext()) {
				IProject prj = (IProject) it.next();
				if (prj.exists() && (prj.equals(project)))
					return true;
			}
		}
		return false;
	}

	/**
	 * Serializes a XML document into a string - encoded in UTF8 format, with
	 * platform line separators.
	 * 
	 * @param doc
	 *            document to serialize
	 * @return the document as a string
	 */
	public static String serializeDocument(Document doc) throws IOException, TransformerException {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		DOMSource source = new DOMSource(doc);
		StreamResult outputTarget = new StreamResult(s);
		transformer.transform(source, outputTarget);
		return s.toString("UTF8"); //$NON-NLS-1$
	}

	public static int getAddressSize() {
		if (Platform.getOSArch().endsWith("64")) //$NON-NLS-1$
			return 8;
		return 4;
	}
}
