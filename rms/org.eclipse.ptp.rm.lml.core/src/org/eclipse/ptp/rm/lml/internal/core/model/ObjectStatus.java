/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.internal.core.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;


/**
 * Saves which object has to be highlighted
 */
public class ObjectStatus extends LguiHandler{

	/**
	 * 
	 * All graphical components of the applet have to implement this interface.
	 * It contains a callback-function, which is called for every registered
	 * component. So every component is informed, if the status of highlighted
	 * objects has changed. 
	 * 
	 * @author karbach
	 *
	 */
	public static interface Updatable{
		/**
		 * @param j job, which was changed
		 * @param mouseover true= mouse is over this object
		 * @param mousedown true= mouse is pressed on this object
		 */
		public void updateStatus(ObjectType j, boolean mouseover, boolean mousedown);
	}
	
	
	/**
	 * Saves all state-attributes, which can be changed by the user via
	 * mouse-interaction.
	 * 
	 * @author karbach
	 */
	private static class State{
		
		public boolean mousedown, mouseover;
		
		public State(){
			mousedown=false;
			mouseover=false;
		}	
	}
	
	//lastmouseover and lastmousedown might be null at sturtup
	private ObjectType lastmouseover;//saves the last object, which was touched by the mouse-cursor
	private ObjectType lastmousedown;//saves the last object, on which the mouse clicked 
	
	private HashMap<ObjectType, State> mapping;
	
	private ArrayList<Updatable> components;//registered components, whose call-back-functions are called
	
	
	/**
	 * Initialise object-status attributes.
	 * @param psuperHandler LML-data-handler, which groups this handler and others to a set
	 * of LMLHandler. This instance is needed to notify all LMLHandler, if any data of
	 * the LguiType-instance was changed.
	 */
	public ObjectStatus(ILguiItem psuperHandler, LguiType model){
		super(psuperHandler, model);
		
		mapping=new HashMap<ObjectType, State>();
		components=new ArrayList<Updatable>();
		
		lguiItem.addListener(new ILguiListener() {
			
			@Override
			public void handleEvent(ILguiUpdatedEvent e) {
				updateData(e.getLguiItem().getLguiType());
			}
		});
	}
	
	/**
	 * @return last and current object, which was touched by the mouse-cursor or null
	 */
	public ObjectType getLastMouseOver(){
		return lastmouseover;
	}
	
	/**
	 * @return last and current object, on which the user is pressing the mouse
	 */
	public ObjectType getLastMouseDown(){
		return lastmousedown;
	}
	
	/**
	 * Reset all object states.
	 */
	private void reset(){
		mapping=new HashMap<ObjectType, State>();
		lastmouseover=null;
		lastmousedown=null;
	}
	
	/**
	 * 
	 * Add component to internal memory. The component is then registered for call-back.
	 * @param up
	 */
	public void addComponent(Updatable up){
		if(!components.contains(up))
			components.add(up);
	}
	
	/**
	 * Remove a component from updater-list.
	 * This component's update-function wont be called after calling this function.
	 * @param up updatable Component, which should not be updated any more
	 */
	public void removeComponent(Updatable up){
		if(components.contains(up)){
			components.remove(up);
		}
	}
	
	
	/**
	 * Inform all components when an object-state updates
	 */
	private void informAll(ObjectType obj, boolean mouseover, boolean mousedown){
		for(int i=0; i<components.size(); i++){
			components.get(i).updateStatus(obj, mouseover, mousedown);
		}
	}
	
	/**
	 * @param obj 
	 * @return true, if the connected object is currently touched by the cursor
	 */
	public boolean isMouseover(ObjectType obj){
		if(obj==null) return false;
		
		if(mapping.containsKey(obj)){
			return mapping.get(obj).mouseover;
		}
		else{//Otherwise insert new pair into mapping-instance
			mapping.put(obj, new State());
			return false;
		}
	}
	
	/**
	 * @return true <=> at least one job has mouseover=true
	 */
	public boolean isAnyMouseover(){
		return lastmouseover!=null;
	}
	
	/**
	 * @param obj
	 * @return true, if mouse is pressed on this object 
	 */
	public boolean isMousedown(ObjectType obj){
		if(obj==null) return false;
		
		if(mapping.containsKey(obj)){
			return mapping.get(obj).mousedown;
		}
		else{//Otherwise insert new pair into mapping-instance
			mapping.put(obj, new State());
			return false;
		}
	}
	
	/**
	 * @return true <=> at least one job has mousedown=true
	 */
	public boolean isAnyMousedown(){
		return lastmousedown!=null;
	}
	
	/**
	 * Set the mouseover-property for object obj to true
	 * Use this method, if the mouse moves over an object.
	 * @param obj
	 */
	private void justMouseover(ObjectType obj){
		
		if(obj==null) return ;
		
		if(! mapping.containsKey(obj)){
			mapping.put(obj, new State());
		}
		
		State az=mapping.get(obj);
		
		if(! az.mouseover){
			az.mouseover=true;
			informAll(obj, true, az.mousedown);
		}
	}
	
	/**
	 * Call this function, if the object obj is focussed by mouse-over.
	 * Does Mouseexit for jobs, which were focussed before, does normal
	 * mouse-over-action. Finally calls a mouseup for the last pressed job,
	 * except the last job is equal to obj.
	 * 
	 * @param obj
	 */
	public void mouseover(ObjectType obj){
		if(obj==null) return ;
		
		if(obj==lastmouseover) return;
		
		if(obj!=lastmouseover)
			mouseExitLast();		
		if(obj!=lastmousedown){
			mouseup(lastmousedown);		
		}
		
		lastmouseover=obj;
		justMouseover(obj);		
	}
	
	/**
	 * Sets mousedown-property for object obj.
	 * Should be called, if mouse is pressed on the
	 * obj object.
	 * @param obj
	 */
	public void mousedown(ObjectType obj){
		if(obj==null) return ;
		
		if(! mapping.containsKey(obj)){
			mapping.put(obj, new State());
		}
		
		State az=mapping.get(obj);
		
		if(! az.mousedown){
			az.mousedown=true;
			lastmousedown=obj;
			informAll(obj, az.mouseover, true);			
		}
	}
	
	/**
	 * Sets mouse-over-property for obj to false.
	 * This method is used, if the mouse exits the object obj.
	 * @param obj
	 */
	public void mouseexit(ObjectType obj){
		if(obj==null) return ;
		
		if(! mapping.containsKey(obj)){
			mapping.put(obj, new State());
		}
		
		State az=mapping.get(obj);
		
		if(az.mouseover){
			az.mouseover=false;
			lastmouseover=null;
			informAll(obj, false, az.mousedown);			
		}
	}
	
	/**
	 * If it is not known, which was the last focused job, call this function.
	 * Last focused objects are set to unfocused.
	 */
	public void mouseExitLast(){
		mouseexit(lastmouseover);
		mouseup(lastmousedown);
	}
	
	/**
	 * Call this function, if mouse releases on this object.
	 * 
	 * @param obj
	 */
	public void mouseup(ObjectType obj){
		if(obj==null) return ;
		
		//Release the last object, on which the mouse pressed 
		if(lastmousedown!=obj){
			mouseup(lastmousedown);
			mouseExitLast();
			mouseover(obj);
		}
		
		if(! mapping.containsKey(obj)){
			mapping.put(obj, new State());
		}
		State az=mapping.get(obj);
		
		if(az.mousedown){
			az.mousedown=false;
			lastmousedown=null;
			informAll(obj, az.mouseover, false);			
		}
	}

	/**
	 * Call this method, if lml-model changed. The new model is passed
	 * to this handler. All getter-functions accessing the handler will
	 * then return data, which is collected from this new model
	 * @param model new lml-data-model
	 */
	public void updateData(LguiType model) {
		reset();
	}
	
}
