package org.eclipse.ptp.rm.lml.internal.core.nodedisplay;

import java.math.BigInteger;

import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;

/**
 * Saves a mask for creating implicit names
 * calculates the length of the output, which this mask will print out 
 * @author karbach
 *
 */
public class Mask {

	private static final String number = "\\s*(-|\\+)?(0x|0X)?([a-fA-F\\d]+)";//Regular expression for a printed integer
	
	private String maskstring;
	
	private String regularMask;//regular expression which printouts must match for this mask
	
	private int outputlength;
	
	private String pre, post;//text-pattern before and behind the printed digit
	
	private String[] names;//null, if no map-attribute specified, otherwise contains explicitly allowed names
	
	private SchemeElement scheme;//scheme-element which contains mask-definition
	
	private int[] numbers = null;//contains numbers of elements defined by scheme (list=1,17,2  => numbers={1,17,2})
	
	/**
	 * @param pscheme scheme-element which contains the mask-definition
	 */
	public Mask(SchemeElement pscheme) {
		
		scheme = pscheme;
		
		if (pscheme.getMap() != null) {
			names = pscheme.getMap().split(",");
			
			outputlength = -1;//variable, because name lengths can be variable
			
			regularMask = pscheme.getMap().replace(',', '|');
			pre = "";
			post = "";
			maskstring = "";
			
		}
		else {
			names = null;
			
			maskstring = pscheme.getMask();
			//regular expression in xsd (([^%])*%(\-|\+|\s|\#)*0(\-|\+|\s|\#)*(\d)+d([^%])*)|(([^%])*%(\-|\+|\s|\#)*d([^%])+)
			if (maskstring.matches("([^%])*%(\\-|\\+|\\s|\\#)*d([^%])+")) {//length is unknown but there is a separator specified
				outputlength = -1;
			}
			else {//there must be given a length within the mask
				//Find length by printing out the number 1 with that mask
				outputlength = String.format(maskstring, 1).length();
			}

			//create regular expression for this mask
			int percent = maskstring.indexOf('%');
			int dpos = maskstring.indexOf('d');
			pre = maskstring.substring(0, percent);
			post = maskstring.substring(dpos+1, maskstring.length());
			regularMask = pre + number + post;
		}
		
	}
	
	/**
	 * Tests output against regular expression given by the mask
	 * Moreover outputlength must match the length of this output
	 * @param output number-output probably created through this mask
	 * @return true if output is allowed, false otherwise
	 */
	public boolean isOutputAllowed(String output) {
		if (names == null) {
			return output.matches(regularMask) && (outputlength == -1 || outputlength == output.length());
		}
		//Just test if output is equal to one of the names
		for (String name : names) {
			if (name.equals(output)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Input is the part of the implicit name printed by this mask
	 * @param levelstring output part of implicit name
	 * @return id-nr for current level or -1, if id could not be parsed
	 */
	public int getNumberOfLevelstring(String levelstring) {
		
		if (names == null) {

			if (!isOutputAllowed(levelstring)) {
				return -1;//For robustness of this function
			}

			levelstring = levelstring.substring(pre.length()); //cut pre-text from inception
			levelstring = levelstring.substring(0, levelstring.length()-post.length()); //cut post-text from end of levelstring

			char[] chars = levelstring.toCharArray();

			int i = 0;
			while (i < chars.length && chars[i] < '0' || chars[i] > '9') {
				i++;
			}

			//No digit at all?
			if (i == chars.length) {
				return -1;
			}

			String number = String.valueOf(chars[i]);
			i++;
			while (i < chars.length && chars[i] >= '0' && chars[i] <= '9') {
				number += chars[i];
				i++;
			}

			return Integer.parseInt(number);

		}
		else{//special names defined, search position of levelstring within names, map position to id
			int pos = 0;
			for (String name : names) {
				if (name.equals(levelstring)) {
					
					if (scheme.getList() != null) {

						if (numbers == null) {
							numbers = LMLCheck.getNumbersFromNumberlist(scheme.getList());
						}
						
						return numbers[pos];
					}
					else {
						int min = scheme.getMin().intValue();
						int max = min;

						if (scheme.getMax() != null) {
							max = scheme.getMax().intValue();
						}

						int step = scheme.getStep().intValue();
						
						return min + step * pos;
					}
				}
				pos++;
			}
		}
		//No matches
		return -1;
	}
	
	/**
	 * Inverse function of getNumberOfLevelstring, id is given and searched is the implicit name
	 * of the component with that id on this level.
	 * @param id
	 * @return implicit name of element with given id
	 */
	public String getImplicitLevelname(int id) {
		return LMLCheck.getLevelName(scheme, id);
	}
	
	public int getOutputLength() {
		return outputlength;
	}
	
	public String getMask() {
		return maskstring;
	}
	
	public static void main(String[] args) {
		
		ObjectFactory objf = new ObjectFactory();
		
		SchemeElement scheme = objf.createSchemeElement();
		scheme.setMin(BigInteger.valueOf(5));
		scheme.setMax(BigInteger.valueOf(10));
		scheme.setStep(BigInteger.valueOf(1));
		scheme.setMap("hans,peter,jupp");
		
		Mask m = new Mask(scheme);
		System.out.println(m.getNumberOfLevelstring("peter"));
	}
	
}

