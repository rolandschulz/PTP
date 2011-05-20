package org.eclipse.ptp.rm.lml.ui.providers;

import java.util.ArrayList;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.internal.core.model.FastImpCheck;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLCheck;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLCheck.SchemeAndData;

/**
 * A node which is input for the nodedisplaytreepanel
 * 
 * This DisplayNode can be implicitly or explicitly defined
 * (means there could be a data-element for this node, but must not) 
 * 
 * Collects information for one node in the NodedisplayTreePanel
 * @author karbach
 *
 */
public class DisplayNode implements Comparable<DisplayNode>{

	private String tagname;//What type of Node is this
	
	private DataElement data;//Is there an explicit data-element for this node?
	//otherwise upper-level data-element is saved here
	
	private SchemeElement scheme;//corresponding scheme-element
	
	private ArrayList<Integer> level;//contains for every level the id-number of this node
	
	private SchemeAndData referencedData;//null if this element is placed in a base nodedisplay
	//contains referenced scheme and data-elements given by refid
	
	private Nodedisplay nodedisplay;//model for surrounding nodedisplay, needed for full implicit name
	
	private ILguiItem lml;//LML-data-manager
	
	/**
	 * Generate a DisplayNode just from its implicit name
	 * @param plml wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param impname implicit name identifying a node within the tree
	 * @param model Nodedisplay as data-root
	 * @return DisplayNode for this implicit name
	 */
	public static DisplayNode getDisplayNodeFromImpName(ILguiItem plml, String impname, Nodedisplay model) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ids = FastImpCheck.impnameToOneLevel(impname, model.getScheme(), ids);
		
		if (ids == null) {
			return null;//Name could not be converted into ids
		}
		
		//Goes as far as data and scheme are available
		SchemeAndData schemedata = LMLCheck.getSchemeAndDataByLevels(LMLCheck.copyArrayList(ids), model.getData(), model.getScheme());
		//Goes down to exactly the scheme where this node is defined by
		SchemeElement scheme = LMLCheck.getSchemeByLevels(LMLCheck.copyArrayList(ids), model.getScheme());
		
		if (schemedata == null || scheme == null) {
			return null;//No scheme and data found for these ids => impname not allowed in this nodedisplay
		}
		
		return new DisplayNode(plml, scheme.getTagname(), schemedata.data, scheme, ids, model);
	}
	
	/**
	 * @param plml wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param ptag tagname-attribute for this DisplayNode
	 * @param pdata data-tag-reference, which gives information for this DisplayNode
	 * @param pscheme scheme-tag-reference, which gives information for this DisplayNode
	 * @param plevel list of ids, which identify this node on every level of the lml-tree
	 * @param pnodedisplay surrounding lml-nodedisplay-instance. This DisplayNode is a physical part of the nodedisplay.
	 */
	public DisplayNode(ILguiItem plml, String ptag, DataElement pdata, SchemeElement pscheme, ArrayList<Integer> plevel, Nodedisplay pnodedisplay) {
		lml = plml;
		
		data = pdata;
		
		tagname = ptag;
		
		scheme = pscheme;
		//Deep copy of plevel-numbers
		level = new ArrayList<Integer>();
		if (plevel != null) {
			for (int i = 0; i < plevel.size(); i++){
				level.add(plevel.get(i));
			}
		}
		
		nodedisplay = pnodedisplay;
	}
	
	//Defined by refid
	/**
	 * Set referenced data of this DisplayNode. This is only needed
	 * for Nodedisplay-References
	 * @param pref collects scheme- and data in one instance
	 */
	public void setReferencedData(SchemeAndData pref) {
		referencedData = pref;
	}
	
	/**
	 * Call this method only for Nodedisplay-references
	 * @return combined scheme- and data-reference
	 */
	public SchemeAndData getReferencedData() {
		return referencedData;
	}
	
	/**
	 * @return corresponding color for this data-Element
	 */
	public LMLColor getObjectColor() {
		
		ObjectType refob = getConnectedObject();
		
		if (refob == null)  {
			return lml.getOIDToObject().getColorById(null);
		}
		
		return lml.getOIDToObject().getColorById(refob.getId());
	}
	
	/**
	 * @return Depth in the data-tree where to find this node
	 */
	public int getLevel() {
		return level.size();
	}
	
	/**
	 * @return Object which is connected with this node through oid or refid
	 */
	public ObjectType getConnectedObject() {
		//Return referenced object
		if (referencedData != null) {
			if (referencedData.data != null) {
				return lml.getOIDToObject().getObjectById(referencedData.data.getOid());
			}
		}
		
		if (data == null) {
			return null;
		}
		return lml.getOIDToObject().getObjectById(data.getOid());
	}
	
	//Create a nice output for a displaynode
	public String toString() {
		if (scheme != null && level.size() > 0) {
			String impname = String.format( scheme.getMask(), level.get(level.size()-1));
			
			String connection = "";
			String refid = "";
			
			if (data != null) {
				if (data.getOid() != null) {
					connection = data.getOid() + " ";
				}
				if (data.getRefid() != null) {
					refid = data.getRefid();
				}
			}
			
			if (referencedData != null) {
				connection = referencedData.data.getOid() + " ";
			}
			
			return impname + " " + connection + refid;
		}
		else {
			return tagname;
		}
	}
	
	/**
	 * @return relative name of referenced data-element just for last level
	 */
	public String getImplicitName() {
		if (scheme == null) {
			return "";
		}
		return LMLCheck.getLevelName(scheme, level.get(level.size()-1));
	}
	
	/**
	 * Generates a name, which identifies the referenced physical element
	 * in the whole lml-tree.
	 * @return absolute name of the element within the tree
	 */
	public String getFullImplicitName() {
		if (nodedisplay == null ) {
			return getImplicitName();
		}
		return LMLCheck.getImplicitName( LMLCheck.copyArrayList(level) , nodedisplay.getScheme());
	}
	
	/**
	 * @return referenced scheme-element, by which this DisplayNode is defined in nodedisplay
	 */
	public SchemeElement getScheme() {
		return scheme;
	}
	
	/**
	 * @return referenced data-element, by which this DisplayNode's data is defined in nodedisplay
	 */
	public DataElement getData() {
		return data;
	}
	
	/**
	 * @return list of ids, which identify this DisplayNode on every level
	 */
	public ArrayList<Integer> getLevelNrs() {
		return level;
	}

	//Make this DisplayNode comparable to other DisplayNodes
	//This is mainly used to identify equality
	public int compareTo(DisplayNode o) {
		
		if (o.level == null) {
			if (level == null) {
				return 0;
			}
			else {
				return 1;
			}
		}
		
		if (level == null) {
			return -1;
		}
		
		if (level.size() != o.level.size() ) {
			return level.size() - o.level.size();
		}
		
		for (int i = 0; i < level.size(); i++){
			if(level.get(i) != o.level.get(i)){
				return level.get(i) - o.level.get(i);
			}
		}
		
		return 0;
	}
	

}

