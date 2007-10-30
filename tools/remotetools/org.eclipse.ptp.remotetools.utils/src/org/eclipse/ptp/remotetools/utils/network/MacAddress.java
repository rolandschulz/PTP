/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.utils.network;

import java.math.BigInteger;

/**
 * Class that represents a 
 * 
 * @author Richard Maciel
 * @since 3.0
 */
public class MacAddress {
	public static int MAX_BITS = 48;
	public static int HEXDIGITS = MAX_BITS/4;
	public static int HEXBASE = 16;
	
	BigInteger mac;
	
	private MacAddress() {
	}
	
	/**
	 * Creatre a MacAddress from a {@link String}.
	 * 
	 * If the string is not in the XX:XX:XX:XX:XX:XX format, where XX is two hexadecimal digits, then
	 * a MacAddressFormatException is raised.
	 * 
	 * @param macString
	 * @return
	 * @throws MacAddressFormatException
	 */
	public static MacAddress createMacAddress(String macString) throws MacAddressFormatException {
		MacAddress maddr = new MacAddress();
		maddr.mac = convertStringToInternalRepresentation(macString);
		
		return maddr;
	}
	
	/**
	 * Create a Mac Address from a {@link BigInteger}.
	 * 
	 * If the number is negative, it will be converted to a positive one.
	 * If the number cannot be represented with MAX_BITS, then get the MAX_BITS least significant bits
	 *  
	 * @param macNumber
	 */
	public static MacAddress createMacAddress(BigInteger macNumber) {
		MacAddress maddr = new MacAddress();
		maddr.mac  = convertBigIntegerToInternalRepresentation(macNumber);
		
		return maddr;
	}

	private static BigInteger convertBigIntegerToInternalRepresentation(BigInteger macNumber) {
		if(macNumber.signum() < 0) {
//			 macNumber cannot be negative. Set it to a positive number
			macNumber = macNumber.negate();
		} 
		
		if(macNumber.bitLength() > MAX_BITS) { 
//			 Mac number cannot be greater than 2 ^48, then convert to a 48 bit number
			String bigMacStr = macNumber.toString(HEXBASE); 
			String macStr = bigMacStr.substring(bigMacStr.length()-1 - HEXDIGITS, bigMacStr.length() - 1);
			macNumber = new BigInteger(macStr);
		}
		return macNumber;
	}

	private static BigInteger convertStringToInternalRepresentation(String macString) throws MacAddressFormatException {
		String trimmedMacString = macString.trim(); 
		boolean isValidMac = trimmedMacString.matches("^(\\p{XDigit}{2}:){5}\\p{XDigit}{2}$");
		
		if(!isValidMac) {
			throw new MacAddressFormatException("Invalid Mac Address format!");
		}
		// Remove all ':' from the String
		String validMacString = trimmedMacString.replaceAll(":", ""); //replace(':', '\0');
		
		// Convert to the internal representation
		return new BigInteger(validMacString, HEXBASE);
		
	}
	
	public void setValue(BigInteger macNumber) {
		mac = MacAddress.convertBigIntegerToInternalRepresentation(macNumber);
	}
	
	public void setValue(String macString) throws MacAddressFormatException {
		mac = convertStringToInternalRepresentation(macString);
	}
	
	public BigInteger getBigIntegerRepresentation() {
		return mac;
	}
	
	/**
	 * Return a string representation of the mac address
	 * 
	 * @return String containing the mac address representation
	 */
	public String getStringRepresentation() {
		String strRep = mac.toString(HEXBASE);
		
//		 Add the 0 at the left to complete the string.
		if(strRep.length() < HEXDIGITS) {
			
			StringBuffer zeroStr =  new StringBuffer();
			
			for(int i=0; i < (HEXDIGITS - strRep.length()); i++) {
				zeroStr.append("0");
			}
			strRep = new String(zeroStr + strRep);
		}
		
		// For each pair of character, add a : character, except for the last one
		StringBuffer hexRep = new StringBuffer();
		for(int i=0; i < HEXDIGITS - 2; i = i + 2) {
			hexRep.append(strRep.substring(i, i+2) + ":");
		}
		hexRep.append(strRep.substring(HEXDIGITS-2, HEXDIGITS));
		
		return hexRep.toString();
	}
}
