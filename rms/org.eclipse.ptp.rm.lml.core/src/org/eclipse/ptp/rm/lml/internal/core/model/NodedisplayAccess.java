/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ JUelich
 */

package org.eclipse.ptp.rm.lml.internal.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.internal.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.internal.core.nodedisplay.LMLCheck;
import org.eclipse.ptp.rm.lml.internal.core.nodedisplay.Mask;

/**
 * Fast access functions for getting tagnames of nodedisplays by their id.
 * Also fast access to mask-attributes
 */
public class NodedisplayAccess extends LguiHandler{
	
	private HashMap<String, HashMap<Integer, String>> oidToTagNames;
	private HashMap<String, HashMap<Integer, Mask>> oidToMasks;
	private HashMap<String, ArrayList<NodedisplaylayoutType>> oidToLayouts;//saves all layouts for nodedisplays, key is index of nodedisplay
	
	private static Nodedisplayelement defaultlayout;
	
	/**
	 * @param lguiItem LML-data-handler, which groups this handler and others to a set
	 * of LMLHandler. This instance is needed to notify all LMLHandler, if any data of
	 * the LguiType-instance was changed.
	 */
	public NodedisplayAccess(ILguiItem lguiItem, LguiType model) {
		super(lguiItem, model);
		updateData();
		
		this.lguiItem.addListener(new ILguiListener() {

			@Override
			public void handleEvent(ILguiUpdatedEvent e) {
				update(e.getLguiItem().getLguiType());
				updateData();
			}
		});
	}
	
	public int getNodedisplayNumbers() {
		return getNodedisplays().size();
	}
	
	public String toString() {
		return getNodedisplays().get(0).getTitle();
	}
	
	/**
	 * @param id identification of nodedisplay
	 * @return Hashmap of masks, keys are level-nrs or null if no nodedisplay found for this id
	 */
	public HashMap<Integer, Mask> getMasks(String id){
		return oidToMasks.get(id);
	}
	
	/**
	 * @param id identification of nodedisplay
	 * @return Hashmap of tagnames, keys are level-nrs or null if no nodedisplay found for this id
	 */
	public HashMap<Integer, String> getTagnames(String id){
		return oidToTagNames.get(id);
	}

	/**
	 * @param id identification of nodedisplay
	 * @param level level in tree, for which a tagname is needed
	 * @return tagname for nodedisplay in given level
	 */
	public String getTagname(String id, int level){
		HashMap<Integer, String> tagnames=getTagnames(id);
		
		if(tagnames==null) return null;
		
		return tagnames.get(level);
	}
	
	/**
	 * @param id identification of nodedisplay
	 * @param level level in tree, for which a mask is needed
	 * @return mask-object for nodedisplay in given level
	 */
	public Mask getMask(String id, int level){
		HashMap<Integer, Mask> masks=getMasks(id);
		
		if(masks==null) return null;
		
		return masks.get(level);
	}
	
	/**
	 * @param id identification of nodedisplay
	 * @return all defined nodedisplaylayouts for this object
	 */
	public ArrayList<NodedisplaylayoutType> getLayouts(String id){
		return oidToLayouts.get(id);
	}
	
	//Used to not create new masks every time a mask for a scheme is needed
	private static HashMap<SchemeElement, Mask> fastmasks;//is mainly used in getImplicitName
	
	/**
	 * Get implicit name of physical element with given ids per level
	 * Parameter ids is copied, so no changes will be made within this list through calling this function
	 * @param scheme scheme of a nodedisplay
	 * @param ids id of physical element on every level
	 * @return implicit name defined by masks and map-attributes
	 */
	public static String getImplicitName(SchemeType scheme, ArrayList<Integer> ids){
		if(ids==null || ids.size()==0) return "";
		
		ArrayList<Integer> copy=LMLCheck.copyArrayList(ids);
		return rekImplicitName(scheme, copy);
	}
	
	/**
	 * go through scheme-tree, find corresponding masks for ids and return whole implicit name
	 * @param scheme might be SchemeType or SchemeElement
	 * @param ids rest of ids in lower levels
	 * @return implicit name of rest of ids in lower levels
	 */
	private static String rekImplicitName(Object scheme, ArrayList<Integer> ids){
		
		if(ids==null || ids.size()==0) return "";
		
		int anum=ids.get(0);
		
		ids.remove(0);
		
		ArrayList<Integer> anumlist=new ArrayList<Integer>();
		anumlist.add(anum);
		//Find scheme for this level
		SchemeElement current=LMLCheck.getSchemeByLevels(anumlist, scheme);
		
		//Get Mask
		Mask mask=null;
		if(fastmasks.containsKey(current)){
			mask=fastmasks.get(current);
		}
		else{
			mask=new Mask(current);
			fastmasks.put(current, mask);
		}
		
		return mask.getImplicitLevelname(anum)+rekImplicitName(current, ids);
	}
	
	/**
	 * if no layout is found for a level, it needs default values anyhow
	 * just creates a default nodedsiplayelement, default values are given by default values from lml-scheme
	 * @return default Nodedisplayelement for layout definitions
	 */
	public static Nodedisplayelement getDefaultLayout(){
		return defaultlayout;
	}
	
	/**
	 * Searches for tagnames and masks within scheme-tag and saves them in tagnames-object
	 * 
	 * @param schemeelement current scheme-model
	 * @param level current level in tree for putting mask and tagname at the right index into hashmap
	 * @param tagnames hashmap for tagnames key=level of tree, value=tagname
	 * @param masks hashmap for masks key=level of tree, value=mask-object
	 */
	private void findtagNamesAndMasks(Object schemeelement, int level, HashMap<Integer, String> tagnames, HashMap<Integer, Mask> masks){
		
		List els=LMLCheck.getLowerSchemeElements(schemeelement);
		
		for(Object el:els){
			
			SchemeElement asel=(SchemeElement) el;
			
			if( ! tagnames.containsKey(level) && asel.getTagname()!=null){
				tagnames.put(level, asel.getTagname());
			}
			
			if(! masks.containsKey(level) && asel.getMask()!=null){
				masks.put(level, new Mask(asel) );
			}
			
			findtagNamesAndMasks(asel, level+1, tagnames, masks);
		}
		
	}
	
	

	
	/**
	 * Getting a list of all elements of type Nodedisplay from LguiType.
	 * @return list of elements(Nodedisplay)
	 */
	public List<Nodedisplay> getNodedisplays() {
		List<Nodedisplay> nodedisplays = new ArrayList<Nodedisplay>();
		for (GobjectType tag : lguiItem.getOverviewAccess().getGraphicalObjects()) {
			if (tag instanceof Nodedisplay) {
				nodedisplays.add((Nodedisplay) tag);
			}
		}
		return nodedisplays;
	}

	/**
	 * Call this method, if lml-model changed. The new model is passed
	 * to this handler. All getter-functions accessing the handler will
	 * then return data, which is collected from this new model
	 * @param model new lml-data-model
	 */
	public void updateData() {
		
		oidToTagNames=new HashMap<String, HashMap<Integer,String>>();
		oidToMasks=new HashMap<String, HashMap<Integer,Mask>>();
		oidToLayouts=new HashMap<String, ArrayList<NodedisplaylayoutType>>();

		List<Nodedisplay> nodedisplays = getNodedisplays();
		for(Nodedisplay nodedisplay: nodedisplays){
			HashMap<Integer, String> atagnames=new HashMap<Integer, String>();
			HashMap<Integer, Mask> amasks=new HashMap<Integer, Mask>();
				
			findtagNamesAndMasks(nodedisplay.getScheme(), 1, atagnames, amasks);
			//Save them for every id
			oidToTagNames.put(nodedisplay.getId(), atagnames);
			oidToMasks.put(nodedisplay.getId(), amasks);
		}
		List<NodedisplaylayoutType> nodedisplayLayouts = lguiItem.getLayoutAccess().getNodedisplayLayouts();
		for (NodedisplaylayoutType nodedisplayLayout : nodedisplayLayouts) {
			if (oidToLayouts.containsKey(nodedisplayLayout.getGid())) {//Already layout found for referenced nodedisplay
				ArrayList<NodedisplaylayoutType> old = oidToLayouts.get(nodedisplayLayout.getGid());
				old.add(nodedisplayLayout);
			}
			else{//Create new layout-list
				ArrayList<NodedisplaylayoutType> layouts=new ArrayList<NodedisplaylayoutType>();
				layouts.add(nodedisplayLayout);
				oidToLayouts.put(nodedisplayLayout.getGid(), layouts);
			}
			
		}
		
		ObjectFactory objf=new ObjectFactory();
		defaultlayout=objf.createNodedisplayelement();
		
		fastmasks=new HashMap<SchemeElement, Mask>();
		
	}
	
	public String getNodedisplayTitel(int i) {
		return getNodedisplays().get(i).getTitle();
	}
	
	public Object getNodedisplayData(int i) {
		return getNodedisplays().get(i).getData();
	}
	
	public Object getNodedisplayScheme(int i) {
		return getNodedisplays().get(i).getScheme();
	}
	
}
