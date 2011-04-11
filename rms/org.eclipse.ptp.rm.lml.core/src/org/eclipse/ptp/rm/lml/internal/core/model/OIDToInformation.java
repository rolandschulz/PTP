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
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InformationType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;

/**
 * This class provides access to information-objects within a lml-model.
 * It can be used for fast and simple access to information filtered by
 * the information-type or the object-id.  
 * 
 * @author karbach
 *
 */
public class OIDToInformation extends LguiHandler{

	//Hashmap, keys are oid-references, values are lists of InfoType
	private HashMap<String, List<InfoType>> oidtoinfo;
	
	/**
	 * @param psuperHandler LML-data-handler, which groups this handler and others to a set
	 * of LMLHandler. This instance is needed to notify all LMLHandler, if any data of
	 * the LguiType-instance was changed.
	 */
	public OIDToInformation(ILguiItem psuperHandler, LguiType model){
		super(psuperHandler, model);
		
		updateData(model);
		
		lguiItem.addListener(new ILguiListener() {
			
			@Override
			public void handleEvent(ILguiUpdatedEvent e) {
				updateData(e.getLguiItem().getLguiType());
			}
		});
	}
	
	/**
	 * Extracts all info-tags from lml-model
	 */
	public void getInformationFromModel(){
		
		oidtoinfo=new HashMap<String, List<InfoType>>();
		
		List<JAXBElement<?>> alltags=lgui.getObjectsAndRelationsAndInformation();
		
		for( JAXBElement<?> jaxbtag: alltags ){//over all information-tags
			
			Object tag=jaxbtag.getValue();
			
			if(! (tag instanceof InformationType) ) continue;
			
			InformationType ainfos=(InformationType) tag;
			
			List<InfoType> realinfos=ainfos.getInfo();
			
			for(InfoType ainfo: realinfos){ // over all info-tags (information/info)
				
				String oid=ainfo.getOid();
				
				if(oidtoinfo.containsKey(oid)){//Already list existent
					List<InfoType> oldlist=oidtoinfo.get(oid);
					oldlist.add(ainfo);
				}
				else{//new list for oid
					ArrayList<InfoType> newlist=new ArrayList<InfoType>();
					newlist.add(ainfo);
					oidtoinfo.put(oid, newlist);
				}
			}
			
		}
		
	}
	
	
	/**
	 * @param id identification for an object 
	 * @param type type of information
	 * @return all infos of a type for object with given id, null if no infos there, empty list if no infos with this type exist
	 */
	public List<InfoType> getInfosByType(String id, String type){
		
		List<InfoType> allinfos=getInfosById(id);
		if(allinfos==null) return null;
		
		List<InfoType> res=new ArrayList<InfoType>();
		//Get only infos with specific type
		for(InfoType ainfo: allinfos){
			if(ainfo.getType().equals(type)){
				res.add(ainfo);
			}
		}
		
		return res;
	}
	
	/**
	 * @param id ID-name of an object defined in objects-tag
	 * @return all info-elements for this object, null if no info exists for this id
	 */
	public List<InfoType> getInfosById(String id){
		return oidtoinfo.get(id);
	}
	
	public static void main(String[] args) {
		for(int r=0; r<=8; r++){
			for(int ra=0; ra<=7; ra++){
				for(int m=0; m<=1; m++){
					System.out.println("<object id=\"R"+String.valueOf(r)+String.valueOf(ra)+"-M"+m+"\" type=\"node\"/>") ;
				}
			}
		}
	}
	
	/**
	 * Call this method, if lml-model changed. The new model is passed
	 * to this handler. All getter-functions accessing the handler will
	 * then return data, which is collected from this new model
	 * @param model new lml-data-model
	 */
	public void updateData(LguiType pmodel) {
		lgui=pmodel;
		
		getInformationFromModel();
	}
}

