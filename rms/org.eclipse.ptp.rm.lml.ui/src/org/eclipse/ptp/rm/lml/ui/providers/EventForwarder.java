package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus.Updatable;

/**
 * This class forwards events created in ObjectStatus to 
 * the LMLManager. This is used in the nodedisplay to forward
 * mouse-events to tables.
 * 
 * @author karbach
 *
 */
public class EventForwarder implements Updatable{

	private ILMLManager lmlmanager;//Default LMLManager attribute
	
	private ObjectType lastchanged;//Contains the ObjectType-instance, which was changed the last time
	private boolean wasmouseover, wasmousedown;//Corresponding state of lastchanged
	
	public EventForwarder(){
		lmlmanager=LMLCorePlugin.getDefault().getLMLManager();
	}
	
	
	
	public void updateStatus(ObjectType j, boolean mouseover, boolean mousedown){
		//Translate ObjectStatus-Event to LML-Event
		if(j==null || j.getId()==null){
			return;
		}
				
		String oid=j.getId();
		
		if(mouseover){
			lmlmanager.selectObject(oid);
		}
		
		if(mousedown){
			lmlmanager.markObject(oid);
		}
		
		if(lastchanged==j){
			if(wasmouseover && ! mouseover){
				lmlmanager.unselectObject(oid);
			}
			
			if(wasmouseover && mouseover && wasmousedown && !mousedown){
				lmlmanager.unmarkObject(oid);
			}
		}
		//Save current state and object
		lastchanged=j;
		wasmouseover=mouseover;
		wasmousedown=mousedown;
	}
	
}
