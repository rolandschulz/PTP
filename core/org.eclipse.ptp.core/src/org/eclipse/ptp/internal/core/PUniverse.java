package org.eclipse.ptp.internal.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPUniverse;

/**
 * @author Nathan DeBardeleben
 *
 */
public class PUniverse extends Parent implements IPUniverse 
{
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
