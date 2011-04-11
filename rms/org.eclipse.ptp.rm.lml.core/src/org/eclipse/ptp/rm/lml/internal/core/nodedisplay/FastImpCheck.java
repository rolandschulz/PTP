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

package org.eclipse.ptp.rm.lml.internal.core.nodedisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeType;

/**
 * This class is capable of checking implicit names given by refid-attributes
 * faster than by creating all implicit names and then look if refid-values were created
 * @author Carsten Karbach, FZ Juelich
 *
 */
public class FastImpCheck {
	
	
	private static ArrayList<ArrayList<Integer>> levels;
	
	public static ArrayList<ArrayList<Integer>> getAllImpNameLevels(String impname, Object scheme) {
		
		levels = new ArrayList<ArrayList<Integer>>();
		
		impnameToAllPossibleLevel(impname, scheme, new ArrayList<Integer>() );
		
		return levels;		
	}
	
	/**
	 * transform implicit name into level-nrs if possible,
	 * otherwise return null
	 * 
	 * only lower elements of scheme are interesting
	 * 
	 * @param impname relative name, which might be defined in the given scheme
	 * @param scheme any scheme-element on any level
	 * @param nrs Level-Numbers till now
	 * @return
	 */
	public static ArrayList<Integer> impnameToOneLevel(String impname, Object scheme, ArrayList<Integer> nrs) {
		
		List<SchemeElement> lower = LMLCheck.getLowerSchemeElements(scheme);
		for (SchemeElement low : lower) {
			
			Mask amask = new Mask(low);
			
			int i = 1;
			while (i <= impname.length() && ! amask.isOutputAllowed(impname.substring(0, i))) {
				i++;
			}
			
			if (i > impname.length()) {
				//No part-string is allowed for this mask
				continue;
			}
			
			//Otherwise impname.substring(0, i) is allowed
			while (i <= impname.length() && amask.isOutputAllowed(impname.substring(0, i))) {				
				
				int id = amask.getNumberOfLevelstring( impname.substring(0, i) );
				nrs.add(id);//add current number
				
				if (i == impname.length()) {//Solution found
					return nrs;
				}
				else {//Go deeper

					ArrayList<Integer> res = impnameToOneLevel( impname.substring(i) , low, nrs);
					if (res != null) {
						return res;
					}
				}
				
				nrs.remove(nrs.size() -1 );//Remove number added on this recursion-level
				i++;
			}
			
		}
		
		return null;		
	}
	
	/**
	 * transform implicit name into level-nrs if possible,
	 * generates all allowed solutions
	 * 
	 * only lower elements of scheme are interesting
	 * 
	 * @param impname relative name, which might be defined in the given scheme
	 * @param scheme any scheme-element on any level
	 * @param nrs Level-Numbers till now
	 * @return
	 */
	public static void impnameToAllPossibleLevel(String impname, Object scheme, ArrayList<Integer> nrs) {
		
		List<SchemeElement> lower = LMLCheck.getLowerSchemeElements(scheme);
		for(SchemeElement low: lower){
			
			Mask amask = new Mask(low);
			
			int i = 1;
			while (i <= impname.length() && ! amask.isOutputAllowed(impname.substring(0, i))) {
				i++;
			}
			
			if (i>impname.length()) {
				//No part-string is allowed for this mask
				continue;
			}
				
			
			//Otherwise impname.substring(0, i) is allowed
			while (i <= impname.length() && amask.isOutputAllowed(impname.substring(0, i))) {				
				int id = amask.getNumberOfLevelstring( impname.substring(0, i));
				nrs.add(id);//add current number
				
				if (i == impname.length() ) {//Solution found
					ArrayList<Integer> copy = LMLCheck.copyArrayList(nrs);
					levels.add(copy);
				}
				else {//Go deeper
					impnameToAllPossibleLevel(impname.substring(i), low, nrs);
				}
				
				nrs.remove(nrs.size() - 1);//Remove number added on this recursion-level
				i++;
			}
			
		}
		
	}
	
	
	
	/**
	 * parses all coherent digits and puts them into a an arraylist
	 * 
	 * p100-13,  0009-0-012 is transformed into 100,13,9,0,12 in an arraylist
	 * 
	 * @param impname implicit name of a component of a parallel computer
	 * @param scheme where the implicit object is defined by for masks-access
	 * @return arraylist of level-numbers
	 */
	private static ArrayList<Integer> impnameToLevel(String impname, HashMap<Integer, Mask> pmasks) {
		
		ArrayList<Integer> res = new ArrayList<Integer>();
		
		int length = impname.length();
		
		int sumlength = 0;
		
		int level = 1;
		
		while (sumlength < length) {
			
			Mask amask = pmasks.get(level++);
			
			if (amask == null) {//No mask for this level => error in parsing
//				System.err.println("No mask for level "+level);
				return null;
			}
			
			int alength = 0;//Length of the current mask-output
			
			if (amask.getOutputLength() >= 0) {//fixed length
				alength = amask.getOutputLength();
				
				if (sumlength + alength > impname.length() ||
						! amask.isOutputAllowed(impname.substring(sumlength, sumlength+alength))) {
					return null;
				}
			}
			else {
				//Variable-length-mask
				String rest = impname.substring(sumlength);
				
				int i = 1;
				//Find first part that is allowed
				while (i <= rest.length() && ! amask.isOutputAllowed(rest.substring(0, i)) ) {
					i++;
				}
				
				if (i <= rest.length()) {
					//Find last part that is allowed
					int j = i;
					while (j <= rest.length() && amask.isOutputAllowed(rest.substring(0, j))){
						j++;
					}
					
					alength = j - 1;
				}
				else {
					return null;//Given output is not allowed
				}
			}
			
			int nr = amask.getNumberOfLevelstring(impname.substring(sumlength, sumlength + alength));
			res.add(nr);
			
			sumlength += alength;
		}
		
		return res;
	}
	
	/**
	 * Has to be called for nodedisplayviews
	 * begin level with 1
	 * Searches all masks within the scheme-tag of the nodedisplay
	 * @param sel schemeelement
	 * @param level
	 */
	private static HashMap<Integer, Mask> findMasks(Object sel, int level, HashMap<Integer, Mask> masks){
		
		List els=LMLCheck.getLowerSchemeElements(sel);
		
		for(Object el:els){
			
			SchemeElement asel=(SchemeElement) el;
			
			if(! masks.containsKey(level) && asel.getMask()!=null){
				masks.put(level, new Mask(asel) );
			}
			
			findMasks(asel, level+1, masks);
		}
		
		return masks;
		
	}
	
	/**
	 * Runs through data-tree and searches for refid-attributes
	 * Converts every refid into level-ids and searches given object
	 * within the scheme-definition of base nodedisplay
	 * adds errors to res, if refid-object does not exist within scheme
	 * @param data Data-Element of referencing nodedisplay
	 * @param masks mask-attributes of base nodedisplay 
	 * @param scheme scheme-element of base nodedisplay
	 * @param res errors
	 * @return errorlist which occurred during checks
	 */
	private static ErrorList rekCheckRefids(Object data, SchemeType scheme, ErrorList res) {
		
		List els = LMLCheck.getLowerDataElements(data);
		if (els == null) {
			return res;//Keine Unterelemente gefunden
		}
		for (int i = 0; i < els.size(); i++){
			
			DataElement el = (DataElement) els.get(i);
			
			String refname = el.getRefid();
			
			if (refname != null) {
//				ArrayList<Integer> level=impnameToLevel(refname, masks);
				ArrayList<Integer> level = impnameToOneLevel(refname, scheme, new ArrayList<Integer>());
				if (level == null) {
					res.addError("\tError refid " + refname + " is not allowed corresponding to the scheme-tag");
				}
				else {
					if (LMLCheck.getSchemeByLevels(level, scheme) == null) {

						res.addError("\tError refid " + refname + " is not allowed corresponding to the scheme-tag");
					}
				}
			}
			
			rekCheckRefids(el, scheme, res);
		}
		
		return res;
		
	}
	
	/**
	 * searches for refid within referencing nodedisplay and return errors if these
	 * refids are not allowed concerning the scheme given by the base nodedisplay
	 * @param base
	 * @param ref
	 * @return
	 */
	public static ErrorList checkRefids(Nodedisplay base, Nodedisplay ref, ErrorList pres) {
		
		ErrorList res;
		if (pres == null) {
			res = new ErrorList();
		}
		else {
			res = pres;
		}
		
		rekCheckRefids(ref.getData(), base.getScheme(), res);
		
		return res;
	}

}

