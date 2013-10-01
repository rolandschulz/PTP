/* $Id: DataCache.java,v 1.17 2009/03/25 15:17:46 ruiliu Exp $ */

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
 * Class containing information describing a data cache.
 *
 * @author Rick Kufrin
 * @author Rui Liu
 */
public class DataCache extends Cache {

    public String toString() {
        return "  [Data: " + super.toString() +
            ", linesize: " + getLineSize() + " (B)]\n";
    }

    /**
     * Returns the cache line size in bytes.
     */
    public int getLineSize() { return super.getLineSize(); }

}
