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
package org.eclipse.ptp.internal.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPUniverse;

public class PUniverse extends Parent implements IPUniverse 
{
    protected String NAME_TAG = "universe ";
	public PUniverse() {
		super(null, "TheUniverse", P_UNIVERSE);
	}
	
	/* there is a single collection but in this collection we keep two different kinds
	 * of classes - they are the machines and the jobs.  So we have to go through the
	 * entire collection pulling out the right class and return an array of them
	 */
	public synchronized IPMachine[] getMachines() {
		Collection col = getCollection();
		Iterator it = col.iterator();
		Vector m = new Vector();
		
		while(it.hasNext()) {
			Object ob = it.next();
			
			if(ob instanceof IPMachine)
				m.add((IPMachine)ob);
		}
		
		return (IPMachine[])m.toArray();
	}
	
	/* there is a single collection but in this collection we keep two different kinds
	 * of classes - they are the machines and the jobs.  So we have to go through the
	 * entire collection pulling out the right class and return an array of them
	 */
	public synchronized IPJob[] getJobs() {
		Collection col = getCollection();
		Iterator it = col.iterator();
		Vector m = new Vector();
		
		while(it.hasNext()) {
			Object ob = it.next();
			
			if(ob instanceof IPJob)
				m.add((IPJob)ob);
		}
		
		return (IPJob[])m.toArray();
	}
}
