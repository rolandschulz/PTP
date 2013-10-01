/* $Id: InstructionTraceCache.java,v 1.13 2009/01/23 04:29:52 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2008-2009 The Board of Trustees of the University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * 	   NCSA - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.etfw.feedback.perfsuite.xml;

/**
 * Class containing information describing an instruction trace cache.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class InstructionTraceCache extends Cache {
    public String toString() {
        return "  [Instruction trace: " + super.toString() + "]\n";
    }

}
