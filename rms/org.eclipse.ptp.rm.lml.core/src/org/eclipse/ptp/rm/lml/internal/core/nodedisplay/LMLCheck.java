package org.eclipse.ptp.rm.lml.internal.core.nodedisplay;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement1;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement2;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement3;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement4;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement5;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement6;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement7;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement8;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement9;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataType;
import org.eclipse.ptp.rm.lml.internal.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement0;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement1;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement2;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement3;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement4;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement5;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement6;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement7;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement8;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement9;
import org.eclipse.ptp.rm.lml.internal.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectsType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement1;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement2;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement3;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement4;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement5;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement6;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement7;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement8;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement9;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeType;
import org.xml.sax.SAXException;

/**
 * This class is a copy of LMLCheck out of a lml-checking-tool
 * only function checkImplicitNames is missing
 * 
 * This class supports several view-classes by navigating through the nodedisplays
 * @author karbach
 *
 */
public class LMLCheck {
	
	
	/**
	 * @return Object of LguiType parsed out of an lml-File
	 * @throws JAXBException
	 */
	public static LguiType parseLML(URL xml, URL xsd) throws JAXBException {
		//Causes errors while used in applet
		
		JAXBContext jc = JAXBContext.newInstance("lml");
		
		Unmarshaller unmar = jc.createUnmarshaller();

		
		if(xsd!=null){
			Schema mySchema;
			SchemaFactory sf =
				SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
			try {
				mySchema = sf.newSchema(xsd);
			} catch( SAXException saxe ){
				// ...(error handling)
				mySchema = null;
			}

			//Connect schema to unmarshaller
			unmar.setSchema(mySchema);
		}
		
		//Validate lml-file and unmarshall in one step
		JAXBElement<LguiType> doc = (JAXBElement<LguiType>) unmar.unmarshal( xml);
		//Get root-element
		LguiType lml = doc.getValue();
		
		return lml;
	}
	
	/**
	 * get all objects-Tags
	 * @param model
	 * @return
	 */
	public static List<ObjectsType> getObjects(LguiType model) {
		
		List<JAXBElement<?>> all = model.getObjectsAndRelationsAndInformation();
		
		List<ObjectsType> objects = new ArrayList<ObjectsType>();
		
		for (JAXBElement<?> aobj: all) {
			if (aobj.getValue() instanceof ObjectsType) {
				objects.add((ObjectsType) aobj.getValue());
			}
		}
		
		return objects;	
		
	}
	
	/**
	 * Get all Layout-Definitions out of the lml-file
	 * @param pmodell
	 * @return
	 */
	public static List<LayoutType> getLayouts(LguiType pmodell) {
		
		List<JAXBElement<?>> all = pmodell.getObjectsAndRelationsAndInformation();
		
		List<LayoutType> layouts = new ArrayList<LayoutType>();
		
		for (JAXBElement<?> aobj: all) {
			if (aobj.getValue() instanceof LayoutType) {
				layouts.add((LayoutType) aobj.getValue());
			}
		}
		
		return layouts;		
	}
	
	/**
	 * Get all graphical objects out of the lml-file
	 * @param pmodell
	 * @return
	 */
	public static List<GobjectType> getGraphicalObjects(LguiType pmodell)  {
		
		List<JAXBElement<?>> all = pmodell.getObjectsAndRelationsAndInformation();
		
		List<GobjectType> gobj = new ArrayList<GobjectType>();
		
		for (JAXBElement<?> aobj: all) {
			if (aobj.getValue() instanceof GobjectType) {
				gobj.add((GobjectType) aobj.getValue());
			}
		}
		
		return gobj;	
	}
	
	/**
	 * @param el
	 * @return in which level this dataelement is placed, 0 for DataType, 10 for DataElement (unspecific)
	 */
	public static int getDataLevel(Object el) {
		if (el instanceof DataType) {
			return 0;
		}		
		if (el instanceof DataElement1) {
			return 1;
		}
		if (el instanceof DataElement2) {
			return 2;
		}
		if (el instanceof DataElement3) {
			return 3;
		}
		if (el instanceof DataElement4) {
			return 4;
		}
		if (el instanceof DataElement5) {
			return 5;
		}
		if (el instanceof DataElement6) {
			return 6;
		}
		if (el instanceof DataElement7) {
			return 7;
		}
		if (el instanceof DataElement8) {
			return 8;
		}
		if (el instanceof DataElement9) {
			return 9;
		}
		if (el instanceof DataElement) {
			return 10;
		}
		
		return -1;
	}
	
	/**
	 * Returns a List of lower data-elements for a data-element or a dataType-instance
	 * within a data-section of a nodedisplay
	 * 
	 * @param el
	 * @return
	 */
	public static List getLowerDataElements(Object el) {
		
		if (el instanceof DataType) {
			return ((DataType) el).getEl1();
		}		
		if (el instanceof DataElement1) {
			return ((DataElement1)el).getEl2();
		}
		if (el instanceof DataElement2) {
			return ((DataElement2)el).getEl3();
		}
		if (el instanceof DataElement3) {
			return ((DataElement3)el).getEl4();
		}
		if (el instanceof DataElement4) {
			return ((DataElement4)el).getEl5();
		}
		if (el instanceof DataElement5) {
			return ((DataElement5)el).getEl6();
		}
		if (el instanceof DataElement6) {
			return ((DataElement6)el).getEl7();
		}
		if (el instanceof DataElement7) {
			return ((DataElement7)el).getEl8();
		}
		if (el instanceof DataElement8) {
			return ((DataElement8)el).getEl9();
		}
		if (el instanceof DataElement9) {
			return ((DataElement9)el).getEl10();
		}		
		
		return new ArrayList();
	}
	
	/**
	 * @param el
	 * @return in which level this schemeelement is placed, 0 for SchemeType, 10 for SchemeElement (unspecific)
	 */
	public static int getSchemeLevel(Object el){
		
		if (el instanceof SchemeType) {
			return 0;
		}		
		if (el instanceof SchemeElement1) {
			return 1;
		}
		if (el instanceof SchemeElement2) {
			return 2;
		}
		if (el instanceof SchemeElement3) {
			return 3;
		}
		if (el instanceof SchemeElement4) {
			return 4;
		}
		if (el instanceof SchemeElement5) {
			return 5;
		}
		if (el instanceof SchemeElement6) {
			return 6;
		}
		if (el instanceof SchemeElement7) {
			return 7;
		}
		if (el instanceof SchemeElement8) {
			return 8;
		}
		if (el instanceof SchemeElement9) {
			return 9;
		}
		if (el instanceof SchemeElement) {
			return 10;
		}
		
		return -1;
	}
	
	/**
	 * Returns a List of lower scheme elements for a scheme-element or a SchemeType-instance
	 * 
	 * @param el
	 * @return
	 */
	public static List getLowerSchemeElements(Object el) {
		
		if (el instanceof SchemeType) {
			return ((SchemeType) el).getEl1();
		}		
		if (el instanceof SchemeElement1) {
			return ((SchemeElement1)el).getEl2();
		}
		if (el instanceof SchemeElement2) {
			return ((SchemeElement2)el).getEl3();
		}
		if (el instanceof SchemeElement3) {
			return ((SchemeElement3)el).getEl4();
		}
		if (el instanceof SchemeElement4) {
			return ((SchemeElement4)el).getEl5();
		}
		if (el instanceof SchemeElement5) {
			return ((SchemeElement5)el).getEl6();
		}
		if (el instanceof SchemeElement6) {
			return ((SchemeElement6)el).getEl7();
		}
		if (el instanceof SchemeElement7) {
			return ((SchemeElement7)el).getEl8();
		}
		if (el instanceof SchemeElement8) {
			return ((SchemeElement8)el).getEl9();
		}
		if (el instanceof SchemeElement9) {
			return ((SchemeElement9)el).getEl10();
		}		
		
		return new ArrayList();
	}
	
	/**
	 * Returns a List of lower nodedisplayelements for a NodedisplayelementX or a NodedisplaylayoutType
	 * 
	 * @param el
	 * @return
	 */
	public static List getLowerNodedisplayElements(Object el) {
		
		if (el instanceof NodedisplaylayoutType) {
			ArrayList<Nodedisplayelement0> justonelement = new ArrayList<Nodedisplayelement0>();
			justonelement.add( ((NodedisplaylayoutType) el).getEl0() );
			return justonelement;
		}		
		if (el instanceof Nodedisplayelement0) {
			return ((Nodedisplayelement0)el).getEl1();
		}
		if (el instanceof Nodedisplayelement1) {
			return ((Nodedisplayelement1)el).getEl2();
		}
		if (el instanceof Nodedisplayelement2) {
			return ((Nodedisplayelement2)el).getEl3();
		}
		if (el instanceof Nodedisplayelement3) {
			return ((Nodedisplayelement3)el).getEl4();
		}
		if (el instanceof Nodedisplayelement4) {
			return ((Nodedisplayelement4)el).getEl5();
		}
		if (el instanceof Nodedisplayelement5) {
			return ((Nodedisplayelement5)el).getEl6();
		}
		if (el instanceof Nodedisplayelement6) {
			return ((Nodedisplayelement6)el).getEl7();
		}
		if (el instanceof Nodedisplayelement7) {
			return ((Nodedisplayelement7)el).getEl8();
		}
		if (el instanceof Nodedisplayelement8) {
			return ((Nodedisplayelement8)el).getEl9();
		}
		if (el instanceof Nodedisplayelement9) {
			return ((Nodedisplayelement9)el).getEl10();
		}
		
		return new ArrayList();
	}
	
	/**
	 * Returns the height of the scheme-tree given by this schemeEl
	 * @param schemeEl scheme elements or a SchemeType-instance
	 * @return
	 */
	public static int getDeepestSchemeLevel(Object schemeEl) {
		
		if (schemeEl == null) {
			return 0;
		}
		
		List els = getLowerSchemeElements(schemeEl);
		if (els == null) {
			return 1;
		}
		
		int max = 0;
		
		for (int i = 0; i < els.size(); i++) {
			int aheight = getDeepestSchemeLevel(els.get(i));
			
			if (aheight > max) {
				max = aheight;
			}
		}
		
		return max + (schemeEl instanceof SchemeType ? 0: 1);
		
	}
	
	/**
	 * Parses numberlist (for example 1,2,16,2,100) and returns a list of integers like new int[]{1,2,16,2,100}
	 * @param numberlist list of numbers separated by commas
	 * @return integer list
	 */
	public static int[] getNumbersFromNumberlist(String numberlist) {
		
		String[] parts = numberlist.split(",");
		
		int[] res = new int[parts.length];
		
		int i = 0;
		for (String part:parts) {
			res[i++] = Integer.parseInt(part.trim());
		}
		
		return res;		
	}
	
	
	/**
	 * A class which combines a data-element and the defining scheme-element
	 * @author karbach
	 *
	 */
	public static class SchemeAndData {
		
		public DataElement data;
		public SchemeElement scheme;
		
		public SchemeAndData(DataElement pdata, SchemeElement pscheme) {
			data = pdata;
			scheme = pscheme;
		}
		
	}
	
	/**
	 * Searches within the direct childs of dataEl for a tag
	 * which includes the idnr number.
	 * 
	 * @param number number of this element in current level
	 * @param dataEl data-object of the corresponding nodedisplay or a lower-level data-element
	 * @return the DataElement in which this scheme-node is described, null if there is no explicitly defined data
	 */
	public static DataElement isSchemenodeInThisData(int number, Object dataEl) {
		List els = getLowerDataElements(dataEl);
		
		if (els == null) {
			return null;
		}
		
		for (int i = 0; i < els.size(); i++) {
			
			DataElement ael = (DataElement) els.get(i);
			
			if(ael.getMin()!=null){ //ranges

				int min = ael.getMin().intValue();
				int max = min;

				if (ael.getMax() != null) {
					max = ael.getMax().intValue();
				}

				if (number >= min && number <= max) {
					return ael;
				}
			}
			else if (ael.getList() != null) {//list-attribute
				
				int[] listels = getNumbersFromNumberlist(ael.getList());//for example 1,2,16,23

				for (int anum:listels) {
					if (anum == number) {
						return ael;
					}
				}
				
			}
		}
		
		return null;
	}
	
	/**
	 * Traverses elements-tree of layout-section for a nodedisplay till all ids given by numbers are found
	 * 
	 * @param numbers contains level-ids
	 * @param nodeel upper-level-el-tag
	 * @return nodedisplayelement describing the object`s layout given by element-ids through numbers
	 */
	public static Nodedisplayelement getNodedisplayElementByLevels(ArrayList<Integer> numbers, Nodedisplayelement nodeel) {
		
		if (numbers == null || numbers.size() == 0) {
			return null;
		}

		if (nodeel == null) {
			return null;
		}

		int anum = numbers.get(0);

		numbers.remove((int)0);//Remove the first element of the arraylist

		List schemeEls = getLowerNodedisplayElements(nodeel);

		Nodedisplayelement scheme = null;

		//Find scheme-tag which contains element with id anum
		for (int i = 0; i < schemeEls.size(); i++) {

			Nodedisplayelement ascheme = (Nodedisplayelement)schemeEls.get(i);

			if (ascheme.getMin() != null) {//Scheme-elements defined by range through min- max-attributes
				int amin = ascheme.getMin().intValue();
				int amax = amin;

				if (ascheme.getMax() != null) {
					amax = ascheme.getMax().intValue();
				}

				if (anum >= amin && anum <= amax) {
					scheme = ascheme;

					break;
				}

			}
			else if (ascheme.getList() != null) {//Scheme defines list of elements

				int[] listels = getNumbersFromNumberlist(ascheme.getList());//for example 1,2,16,23

				for (int number:listels) {
					if (number == anum) {
						scheme = ascheme;		
						break;
					}
				}
				//if scheme found stop searching
				if (scheme != null) {
					break;
				}

			}
		}

		//No scheme found for anum
		if (scheme == null) {
			return null;
		}

		//Real solutions are always found in this part
		
		if (numbers.size() == 0) {
			return scheme;//All numbers processed?
		}
		else{
			//are the lower levels of the scheme-node allowed in the data-tag?
			Nodedisplayelement res = getNodedisplayElementByLevels(numbers, scheme);
			return res;
		}

	}
	
	
	/**
	 * Traverses scheme-tree till all ids given by numbers are found
	 * 
	 * @param numbers contains level-ids
	 * @param schemeEl upper-level-scheme-tag
	 * @return schemeelement describing the object given by element-ids through numbers
	 */
	public static SchemeElement getSchemeByLevels(ArrayList<Integer> numbers, Object schemeEl) {
		
		if (numbers == null || numbers.size() == 0) {
			return null;
		}

		if (schemeEl == null) {
			return null;
		}

		int anum = numbers.get(0);

		numbers.remove((int)0);//Remove the first element of the arraylist

		List schemeEls = getLowerSchemeElements(schemeEl);

		SchemeElement scheme = null;

		//Find scheme-tag which contains element with id anum
		for (int i = 0; i < schemeEls.size(); i++) {

			SchemeElement ascheme = (SchemeElement)schemeEls.get(i);

			if (ascheme.getMin() != null) {//Scheme-elements defined by range through min- max-attributes
				int amin = ascheme.getMin().intValue();
				int amax = amin;

				if (ascheme.getMax() != null) {
					amax = ascheme.getMax().intValue();
				}

				int astep = ascheme.getStep().intValue();

				if (anum >= amin && anum <= amax && (anum-amin) % astep == 0 ) {
					scheme = ascheme;

					break;
				}

			}
			else if (ascheme.getList() != null) {//Scheme defines list of elements

				int[] listels = getNumbersFromNumberlist(ascheme.getList());//for example 1,2,16,23

				for (int number:listels) {
					if (number == anum) {
						scheme = ascheme;		
						break;
					}
				}
				//if scheme found stop searching
				if (scheme != null) {
					break;
				}

			}
		}

		//No scheme found for anum
		if (scheme == null) {
			return null;
		}

		//Real solutions are always found in this part
		
		if (numbers.size() == 0) {
			return scheme;//All numbers processed?
		}
		else{
			//are the lower levels of the scheme-node allowed in the data-tag?
			SchemeElement res = getSchemeByLevels(numbers, scheme);
			return res;
		}

	}
	
	
	/**
	 * Nice call for just one number, just creates an arraylist with one number and calls overloaded function
	 * @param number one integer value with id for the element to be checked
	 * @param dataEl upper-level data-element
	 * @param schemeEl scheme-element on the same level as dataEl
	 * @return Data- and Scheme which are connected to each other
	 */
	public static SchemeAndData getSchemeAndDataByLevels(int number, Object dataEl, Object schemeEl) {
		
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		numbers.add(number);
		
		return getSchemeAndDataByLevels(numbers, dataEl, schemeEl);
		
	}
	
	/**
	 * 
	 * Searches within the dataEl-tree for a data-element which describes the treenode
	 * with the level-ids given by numbers.
	 * 
	 * Returns corresponding scheme-element if available
	 * 
	 * @param numbers level-ids, which has to be searched in lower elements, content might be changed by this function
	 * @param dataEl root of the data-tags, which will be processed, dataEl itself is just used to get lower elements
	 * @param schemeEl root of the scheme-tags, which will be processed, schemeEl itself is just used to get lower elements
	 * @return data-element and corresponding scheme
	 */
	public static SchemeAndData getSchemeAndDataByLevels(ArrayList<Integer> numbers, Object dataEl, Object schemeEl) {
		
//		numbers=copyArrayList(numbers);
		
		if (numbers == null || numbers.size() == 0) {
			return null;
		}
		
		if (schemeEl == null) {
			return null;
		}
		
		int anum = numbers.get(0);
		
		numbers.remove((int)0);//Remove the first element of the arraylist
		
		List els = getLowerDataElements(dataEl);
		
		List schemeEls = getLowerSchemeElements(schemeEl);
		
		for (int i = 0; i < els.size(); i++) {
			
			DataElement ael = (DataElement) els.get(i);
			
			if (ael.getMin() != null) {//data-Element with ranges

				int min = ael.getMin().intValue();
				int max = min;

				if (ael.getMax() != null) {
					max = ael.getMax().intValue();
				}

				if (min>anum || max < anum) {
					continue;
				}
				//else data-Element for anum found
			}
			else if (ael.getList() != null) {//check if anum in list
				
				boolean found = false;
				
				int[] listels = getNumbersFromNumberlist(ael.getList());//for example 1,2,16,23

				for (int number : listels) {
					if (number == anum) {
						found = true;
						break;
					}
				}
				
				//if anum not in list, search for another data-element
				if (!found) {
					continue;				
				}
			}

			//Find corresponding scheme-element
			SchemeElement scheme = null;
			for (int j = 0; j < schemeEls.size(); j++) {

				SchemeElement ascheme = (SchemeElement) schemeEls.get(j);

				if (ascheme.getMin() != null) {//Scheme-elements defined by range through min- max-attributes
					int amin = ascheme.getMin().intValue();
					int amax = amin;

					if (ascheme.getMax() != null) {
						amax = ascheme.getMax().intValue();
					}

					int astep = ascheme.getStep().intValue();

					if (anum >= amin && anum <= amax && (anum-amin) % astep == 0 ) {
						scheme = ascheme;

						break;
					}

				}
				else if (ascheme.getList() != null) {//Scheme defines list of elements

					int[] listels = getNumbersFromNumberlist(ascheme.getList());//for example 1,2,16,23

					for (int number : listels) {
						if (number == anum) {
							scheme = ascheme;		
							break;
						}
					}
					//if scheme found stop searching
					if (scheme != null) {
						break;
					}

				}
			}

			//No scheme found for the current data-element
			if (scheme == null) {
				continue;
			}

			//Real solutions are always found in this part

			if (numbers.size() == 0) {
				return new SchemeAndData(ael, scheme);//All numbers processed?
			}
			else{
				//are the lower levels of the scheme-node allowed in the data-tag?
				SchemeAndData res = getSchemeAndDataByLevels(numbers, ael, scheme);
				if (res != null) {
					return res;
				}
			}
			
		}
		
		if(dataEl instanceof DataType){
			return null;
		}
		else
			return new SchemeAndData((DataElement) dataEl, (SchemeElement) schemeEl);
		
	}
	
	/**
	 * @param levels
	 * @return a string describing the levels of a node within a tree
	 */
	public static String getLevelString(ArrayList<Integer> levels) {
		
		String levelstring = "";
		
		for (int j = 1; j <= levels.size(); j++) {
			levelstring += "el" + j + "=" + levels.get(j-1) + " ";
		}
		
		return levelstring;		
	}
	
	/**
	 * Copy an Integer-ArrayList
	 * @param orig
	 * @return
	 */
	public static ArrayList<Integer> copyArrayList(ArrayList<Integer> orig) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		
		for (int i = 0; i < orig.size(); i++) {
			res.add(orig.get(i));
		}
		
		return res;
	}
		
		
	
	/**
	 * Check if all created data-nodes are allowed as described in the corresponding scheme
	 * returns list of Errors containing messages about not-allowed nodes in Errorlist
	 */
	public static void checkDataNodesinScheme(ErrorList errl, Object datanode, ArrayList<Integer> upperlevelnumbers, Object scheme ) {
		
		if (datanode == null) {
			return;
		}
		
		List els = getLowerDataElements(datanode);
		if (els == null) {
			return;
		}
		
		for (int i = 0; i < els.size(); i++) {
			
			DataElement dat = (DataElement) els.get(i);
			
			if (dat.getMin() != null) {//data-tag given by range => check every element in range
				int min = dat.getMin().intValue();
				int max = min;

				if (dat.getMax() != null) {
					max = dat.getMax().intValue();
				}

				upperlevelnumbers.add(min);

				SchemeAndData first = getSchemeAndDataByLevels(min, datanode, scheme);
//				SchemeElement first=isDatanodeInScheme(upperlevelnumbers, scheme);

				if (first == null) {//Minimum of given data-node is not in scheme
					String levelstring = getLevelString(upperlevelnumbers);

					errl.addError("\tDatanode with tree-path " + levelstring + " is not allowed in scheme");
				}
				else{

					//Check Lower-Level Data-Nodes
					checkDataNodesinScheme(errl, dat, upperlevelnumbers, first.scheme);

					int step = first.scheme.getStep().intValue();//default of step is one, even if scheme does not define min- max- attributes

					for (int j = min + step; j <= max; j += step) {

						upperlevelnumbers.set(upperlevelnumbers.size()-1, j);
						
						//get current schemeanddata
						SchemeAndData asad = getSchemeAndDataByLevels(j, datanode, scheme);
//						SchemeElement ascheme=isDatanodeInScheme(upperlevelnumbers, scheme);

						if (asad == null) {//No scheme found for id j

							String levelstring = getLevelString(upperlevelnumbers);

							errl.addError("\tDatanode with tree-path " + levelstring + " is not allowed in scheme");
						}
						else{
							//Check inner Nodes
							checkDataNodesinScheme(errl, dat, upperlevelnumbers, asad.scheme);
						}

					}

				}

				upperlevelnumbers.remove((int)(upperlevelnumbers.size()-1));

			} //end of range-checks
			else if (dat.getList() != null) {//elements in data-tag defined with a list
				
				upperlevelnumbers.add(0);
				
				int[] elements = getNumbersFromNumberlist(dat.getList());
				for (int number : elements) {
					
					upperlevelnumbers.set(upperlevelnumbers.size()-1, number);
					
					SchemeAndData asad = getSchemeAndDataByLevels(number, datanode, scheme);
					
					if (asad == null) {
						String levelstring = getLevelString(upperlevelnumbers);

						errl.addError("\tDatanode with tree-path " + levelstring + " is not allowed in scheme");
					}
					else{//current number is allowed, check lower elements
						
						checkDataNodesinScheme(errl, dat, upperlevelnumbers, asad.scheme);						
					}
					
				}
				
				upperlevelnumbers.remove((int)(upperlevelnumbers.size()-1));
				
			}
			
		}//for-loop over subelements of datanode
		
	}
	
	/**
	 * @param ids identifying numbers on every level
	 * @param scheme upper-level scheme object
	 * @return implicit name for this object by using masks and maps
	 */
	public static String getImplicitName(ArrayList<Integer> ids, Object scheme) {
		
		if (ids == null || ids.size() == 0 ) {
			return "";
		}
		
		ArrayList<Integer> id = new ArrayList<Integer>();
		id.add(ids.get(0));
		
		SchemeElement subscheme = getSchemeByLevels(copyArrayList(id), scheme);
		
		if (subscheme == null) {
			return "";
		}
		
		String subname = getLevelName(subscheme, id.get(0));
		
		ids.remove(0);
		
		return subname + getImplicitName(ids, subscheme);
	}
	
	/**
	 * Uses mask-attribute to return formatted output of levelid
	 * Exception is the definition of map-attribute, then names are explicitly given by map-attribute
	 * @param scheme corresponding scheme-element
	 * @param levelid id of the element, the name is searched for
	 * @return implicit name of the element for this level
	 */
	public static String getLevelName(SchemeElement scheme, int levelid) {
		
		if (scheme.getMap() == null) {
			return String.format(scheme.getMask(), levelid);
		}
		
		String[] names = scheme.getMap().split(",");
		if (scheme.getMin() != null) {//min-max-attributes
			int min = scheme.getMin().intValue();
			int max = min;

			if (scheme.getMax() != null) {
				max = scheme.getMax().intValue();
			}

			int step = scheme.getStep().intValue();
			
			int namespos = (levelid - min) / step;
			
			if (namespos < names.length) {
				return names[namespos];
			}
		}
		else {//List-attribute, map list-ids to map-names, in other words: find position of levelid in list-attribute and return corresponding positioned map-name
			int[] nrs = getNumbersFromNumberlist(scheme.getList());
			
			for (int i = 0; i < nrs.length; i++) {
				if (nrs[i] == levelid) {
					return names[i];
				}
			}
		}
		
		return "";
	}
	
	/**
	 * Searches for allowed implicitly defined names from a name schema.
	 * Adds all names of elements, which are directly children of the schemeElement.
	 * Then calls this method again for all children. 
	 * 
	 * @param errlist list of errors occuring while parsing
	 * @param names allowed names
	 * @param schemeElement actual node in scheme-tree
	 * @param aname name of this element, can be extended by the names of children
	 */
	public static void addAllowedNames(ErrorList errlist, HashSet<String> names, Object schemeElement, String aname) {
		
		List els = getLowerSchemeElements(schemeElement);
		if (els == null) {
			return;//no sub-elements found
		}
		for (int i = 0; i < els.size(); i++) {
			
			SchemeElement el = (SchemeElement) els.get(i);
			
			if (el.getMin() != null) {//Lower elements through ranges
				int min = el.getMin().intValue();
				int max = min;

				if (el.getMax() != null) {
					max = el.getMax().intValue();
				}

				int step = el.getStep().intValue();

				if (min > max) {
					errlist.addError("A min-value is bigger then the appropriate max-value." + aname + " min= " + min + " max= " + max);
				}

				for (int j = min; j <= max; j += step) {

					String thisname = aname + getLevelName(el, j);
					names.add(thisname);

					addAllowedNames(errlist, names, el, thisname);				
				}
			}
			else if (el.getList() != null) {//Lower elements through list
				
				int[] numbers = getNumbersFromNumberlist(el.getList());
				
				for (int number : numbers) {
					String thisname = aname+getLevelName(el, number);
					names.add(thisname);

					addAllowedNames(errlist, names, el, thisname);
				}
				
			}
			
		}
		
	}
	
	/**
	 * Traverses the data-tag-tree and searches for refid-Attributes.
	 * Then checks whether these attributes have allowed values according to
	 * name scheme of the corresponding nodedisplay.
	 * Allowed names are passed as a set of names
	 * 
	 * Traverses all nodedisplay-references
	 * 
	 * @param errlist
	 * @param names
	 * @param schemeElement
	 */
	public static void checkNamesAllowed(ErrorList errlist, HashSet<String> names, Object datarefelement) {
		
		List els = getLowerDataElements(datarefelement);
		if (els == null) {
			return;//Keine Unterelemente gefunden
		}
		for (int i = 0; i < els.size(); i++) {
			
			DataElement el = (DataElement)els.get(i);
			
			String refname = el.getRefid();
			
			if (refname != null) {
				if (!names.contains(refname)) {
					errlist.addError("\t\tImplicit name " + refname + " is not allowed");
				}
			}
			
			checkNamesAllowed(errlist, names, el);
		}
		
	}
	
	/**
	 * Checks validity of nodedisplays.
	 * Checks whether only allowed attributes where used for
	 * base nodedisplays and nodedisplayrefs.
	 * 
	 * Also checks whether the defined data-nodes are allowed against the scheme of the corresponding nodedisplay
	 * 
	 * @param lgui
	 * @return found errors
	 */
	public static ErrorList checkNodedisplays(LguiType lgui) {
		
		ErrorList res = new ErrorList();
		
		List<GobjectType> gobjects = getGraphicalObjects(lgui);
		
		//Search all nodedisplays and check their attributes
		for (GobjectType gobj : gobjects) {
			if (gobj instanceof Nodedisplay) {
				
				Nodedisplay dis = (Nodedisplay)gobj;
				
				res.addMessage("Errors for Nodedisplay " + dis.getId());
				//This part is now done by xsl-file
//				checkAttributesAllowedForNodedisplay(dis.getRefto()!=null, res, dis.getData(), new ArrayList<Integer>());
				
				checkDataNodesinScheme(res, dis.getData(), new ArrayList<Integer>(), dis.getScheme());
				
			}			
		}
		
		return res;
	}
	
	private static final String line="-----------------------------------";
	
}

