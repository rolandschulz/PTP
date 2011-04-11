/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.internal.core.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectName;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectsType;


/**
 * This class provides an index for fast access
 * to objects within the objects tag. You can pass
 * the id of the objects. This class then returns
 * the corresponding objects.
 */
public class OIDToObject extends LguiHandler{

	/*
	 * Color which is returned, if an id as parameter is not connected
	 */
	private static final LMLColor notConnectedColor = LMLColor.lightGray;

	private HashMap<String, ObjectType> oidToObject;
	private HashMap<String, LMLColor> oidToColor;

	private String systemId=null;//Id of an object with type system, there should only be one

	/**
	 * @param lguiItem LML-data-handler, which groups this handler and others to a set
	 * of LMLHandler. This instance is needed to notify all LMLHandler, if any data of
	 * the LguiType-instance was changed.
	 */
	public OIDToObject(ILguiItem lguiItem, LguiType lgui){
		super(lguiItem, lgui);		
		
		updateData();	
		
		this.lguiItem.addListener(new ILguiListener() {
			
			@Override
			public void handleEvent(ILguiUpdatedEvent e) {
				update(e.getLguiItem().getLguiType());
				updateData();
			}
		});
	}

	/**
	 * Call this method, if lml-model changed. 
	 * All getter-functions accessing the handler will
	 * then return data, which is collected from this new model
	 */
	public void updateData(){

		List<ObjectsType> allobjs = lguiItem.getOverviewAccess().getObjects();

		for(ObjectsType frame: allobjs){

			List<ObjectType> objects=frame.getObject();

			oidToObject=new HashMap<String, ObjectType>();

			oidToColor=new HashMap<String, LMLColor>();

			for(ObjectType obj: objects){
				oidToObject.put(obj.getId(), obj);
				oidToColor.put(obj.getId(), LMLColor.stringToColor(obj.getColor()));

				if(obj.getType()==ObjectName.SYSTEM){
					systemId=obj.getId();
				}
			}

		}
	}

	/**
	 * get an object by an Id of this object
	 * @param id
	 * @return
	 */
	public ObjectType getObjectById(String id){
		return oidToObject.get(id);		
	}

	/**
	 * Get a color-object for a objects-id
	 * @param id
	 * @return
	 */
	public LMLColor getColorById(String id){		
		if(id==null) 
			return notConnectedColor;
		LMLColor res=oidToColor.get(id);

		if(res==null) return notConnectedColor;
		else return res;
	}
	
	/**
	 * Convert a colorstring into an Color-Object
	 * 
	 * Allowed strings: #FFF #0000FF ffeeff cef ...
	 * 
	 * @param colorstring
	 * @return
	 */
	public static Color stringToColor(String colorstring){

		if(colorstring==null || colorstring.length()==0){
			return Color.white;
		}

		if(colorstring.charAt(0)=='#'){
			colorstring=colorstring.substring(1);
		}

		int red=0;
		int green=0;
		int blue=0;


		if(colorstring.length()==3){
			red=Integer.parseInt(colorstring.substring(0,1), 16);
			green=Integer.parseInt(colorstring.substring(1,2), 16);
			blue=Integer.parseInt(colorstring.substring(2,3), 16);

			red=red+16*red;
			green=green+16*green;
			blue=blue+16*blue;
		}
		else
			if(colorstring.length()==6){
				red=Integer.parseInt(colorstring.substring(0,2), 16);
				green=Integer.parseInt(colorstring.substring(2,4), 16);
				blue=Integer.parseInt(colorstring.substring(4,6), 16);
			}
			else{
				System.err.println("Not allowed color specified: "+colorstring);
			}

		return new Color(red, green, blue);	
	}

	/**
	 * search for an object of type "system", return its id
	 * Searching will only be done once for every model
	 * @return id of system-object
	 */
	public String getSystemObjectId(){

		return systemId;	
	}

}