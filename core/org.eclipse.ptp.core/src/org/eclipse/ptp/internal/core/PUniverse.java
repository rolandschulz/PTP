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
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPUniverse;

public class PUniverse extends Parent implements IPUniverse 
{
    protected String NAME_TAG = "universe ";
	public PUniverse() {
		/* '1' because this is the only universe */
		super(null, "TheUniverse", ""+1+"", P_UNIVERSE);
	}
	
	/* there is a single collection but in this collection we keep two different kinds
	 * of classes - they are the machines and the jobs.  So we have to go through the
	 * entire collection pulling out the right class and return an array of them
	 */
	public synchronized IPMachine[] getMachines() {
		System.out.println("Universe.getMachines()");
		Collection col = getCollection();
		Iterator it = col.iterator();
		Vector m = new Vector();
		
		while(it.hasNext()) {
			Object ob = it.next();
		
			System.out.println("\tchild is: "+ob);
			if(ob instanceof IPMachine)
				m.add((IPMachine)ob);
		}
		
		System.out.println("Final vector: "+m);
		/* this wouldnt work, oddly enough so have to do a brute force approach */
		//return (IPMachine[])(m.toArray());		
		
		Object[] o = m.toArray();
		IPMachine[] mac = new IPMachine[o.length];
		
		for(int i=0; i<o.length; i++) {
			mac[i] = (IPMachine)o[i];
		}
		
		return mac;
	}
	
	public synchronized IPMachine[] getSortedMachines() {
	    IPMachine[] macs = getMachines();
	    sort(macs);
	    for(int i=0; i<macs.length; i++) {
	    		System.out.println("SORTED MAC: "+macs[i]);
	    		System.out.println("\tKeyNumber = "+macs[i].getKeyNumber());
	    }
	    return macs;
	}
	
	public synchronized IPMachine findMachineByName(String mname) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while(it.hasNext()) {
			Object ob = it.next();
			if(ob instanceof IPMachine) {
				IPMachine mac = (IPMachine)ob;
				if(mac.getElementName().equals(mname)) return mac;
			}
		}
		return null;
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
		
		Object[] o = m.toArray();
		IPJob[] job = new IPJob[o.length];
		for(int i=0; i<o.length; i++) {
			job[i] = (IPJob)o[i];
		}
		
		return job;
	}	
	
	public synchronized IPJob[] getSortedJobs() {
		IPJob[] jobs = getJobs();
		sort(jobs);
		return jobs;
	}

	public synchronized IPJob findJobByName(String jname) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while(it.hasNext()) {
			Object ob = it.next();
			if(ob instanceof IPJob) {
				IPJob job = (IPJob)ob;
				if(job.getElementName().equals(jname)) return job;
			}
		}
		return null;
	}
}
