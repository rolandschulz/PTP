/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.parser;

/**
 * OpenMP keywords
 * 
 * @author pazel
 * 
 */
public class OpenMPKeywords
{
	public static final char[] ATOMIC = "atomic".toCharArray(); //$NON-NLS-1$
	public static final char[] BARRIER = "barrier".toCharArray(); //$NON-NLS-1$
	public static final char[] COPYIN = "copyin".toCharArray(); //$NON-NLS-1$
	public static final char[] COPYPRIVATE = "copyprivate".toCharArray(); //$NON-NLS-1$
	public static final char[] CRITICAL = "critical".toCharArray(); //$NON-NLS-1$
	public static final char[] DEFAULT = "default".toCharArray(); //$NON-NLS-1$
	public static final char[] FIRSTPRIVATE = "firstprivate".toCharArray(); //$NON-NLS-1$
	public static final char[] FLUSH = "flush".toCharArray(); //$NON-NLS-1$
	public static final char[] FOR = "for".toCharArray(); //$NON-NLS-1$
	public static final char[] IF = "if".toCharArray(); //$NON-NLS-1$
	public static final char[] LASTPRIVATE = "lastprivate".toCharArray(); //$NON-NLS-1$
	public static final char[] MASTER = "master".toCharArray(); //$NON-NLS-1$
	public static final char[] NONE = "none".toCharArray(); //$NON-NLS-1$
	public static final char[] NOWAIT = "nowait".toCharArray(); //$NON-NLS-1$
	public static final char[] NUMTHREADS = "num_threads".toCharArray(); //$NON-NLS-1$
	public static final char[] OMP = "omp".toCharArray(); //$NON-NLS-1$
	public static final char[] ORDERED = "ordered".toCharArray(); //$NON-NLS-1$
	public static final char[] PARALLEL = "parallel".toCharArray(); //$NON-NLS-1$
	public static final char[] PRIVATE = "private".toCharArray(); //$NON-NLS-1$
	public static final char[] REDUCTION = "reduction".toCharArray(); //$NON-NLS-1$
	public static final char[] SCHEDULE = "schedule".toCharArray(); //$NON-NLS-1$
	public static final char[] SECTION = "section".toCharArray(); //$NON-NLS-1$
	public static final char[] SECTIONS = "sections".toCharArray(); //$NON-NLS-1$
	public static final char[] SHARED = "shared".toCharArray(); //$NON-NLS-1$
	public static final char[] SINGLE = "single".toCharArray(); //$NON-NLS-1$
	public static final char[] THREADPRIVATE = "threadprivate".toCharArray(); //$NON-NLS-1$
	public static final char[] DYNAMIC = "dynamic".toCharArray(); //$NON-NLS-1$
	public static final char[] STATIC = "static".toCharArray(); //$NON-NLS-1$

	public static final char[] POUND = "#".toCharArray(); //$NON-NLS-1$
	public static final char[] PRAGMA = "pragma".toCharArray(); //$NON-NLS-1$
}
