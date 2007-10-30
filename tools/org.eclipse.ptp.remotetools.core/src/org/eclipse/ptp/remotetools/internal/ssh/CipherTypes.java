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
package org.eclipse.ptp.remotetools.internal.ssh;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.util.NLS;

/**
 * Available cipher types and associated information. 
 * To add another cipher type, it's necessary that two attributes of the String type are created: first one
 * returns the id of the cipher type and must have its name in the format CIPHER_<cipher attributes' name>. Second one
 * must return the return the cipher's name and must have its name in the format NAME_CIPHER_<cipher attributes' name>
 * 
 * @author Richard Maciel
 *
 */
public class CipherTypes {
//	Available JSch cipher types: blowfish-cbc, 3des-cbc, aes128-cbc,aes192-cbc,aes256-cbc
	public static final String CIPHER_BLOWFISH = CipherTypesMessages.getString("KEY_BLOWFISH"); //$NON-NLS-1$
	public static final String CIPHER_3DES = CipherTypesMessages.getString("KEY_3DES"); //$NON-NLS-1$
	public static final String CIPHER_AES128 = CipherTypesMessages.getString("KEY_AES128"); //$NON-NLS-1$
	public static final String CIPHER_AES192 = CipherTypesMessages.getString("KEY_AES192"); //$NON-NLS-1$
	public static final String CIPHER_AES256 = CipherTypesMessages.getString("KEY_AES256"); //$NON-NLS-1$
//	 Use the default JSch cipher
	public static final String CIPHER_DEFAULT = CipherTypesMessages.getString("KEY_DEFAULT"); //$NON-NLS-1$
	
	public static final String NAME_CIPHER_BLOWFISH = CipherTypesMessages.getString("NAME_BLOWFISH"); //$NON-NLS-1$
	public static final String NAME_CIPHER_3DES = CipherTypesMessages.getString("NAME_3DES"); //$NON-NLS-1$
	public static final String NAME_CIPHER_AES128 = CipherTypesMessages.getString("NAME_AES128"); //$NON-NLS-1$
	public static final String NAME_CIPHER_AES192 = CipherTypesMessages.getString("NAME_AES192"); //$NON-NLS-1$
	public static final String NAME_CIPHER_AES256 = CipherTypesMessages.getString("NAME_AES256"); //$NON-NLS-1$
	public static final String NAME_CIPHER_DEFAULT = CipherTypesMessages.getString("NAME_DEFAULT"); // Use the default JSch cipher //$NON-NLS-1$
	
	
	private static Map cipherTypesMap = null;
	
	/**
	 * Creates a map containing tuples CipherTypeId and CipherTypeName
	 * from the declared attributes.
	 * @return Map created
	 */
	private static Map createCipherTypesMap() {
		Field [] fields = CipherTypes.class.getDeclaredFields();
		List fieldList = new ArrayList(Arrays.asList(fields));
		
		Map cipherMap = new HashMap();
		
//		 Get the tuples of attributes from the fieldList and put them in the cipherMap
		for(Iterator it = fieldList.iterator(); it.hasNext(); ) {
//			 Look for a attribute whose prefix is CIPHER and fetch its value
			Field f = (Field)it.next();
			if(f.getName().startsWith("CIPHER_")) { //$NON-NLS-1$
				Field ciphField = f;
				// Found one. Now concat the NAME_ prefix into the choosen name string and
				// look for an attribute with that name.
				try {
					Field nameCiphField = (Field)CipherTypes.class.getField("NAME_" + ciphField.getName()); //$NON-NLS-1$
					try {
						// Add field values to map.
						cipherMap.put(ciphField.get(null), nameCiphField.get(null));
					} catch (IllegalArgumentException e) {
						// Will not occur, since we use null as argument
						e.printStackTrace();
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						// Will not occur since outside try already treat this case.
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				} catch (SecurityException e) {
					// Ignore protected and private fields
				} catch (NoSuchFieldException e) {
					// No equivalent name field. Throw a RuntimeException
					e.printStackTrace();
					//"Did not find " + "NAME_" + ciphField.getName() + " to match"
					throw new RuntimeException(
							NLS.bind(CipherTypesMessages.getString("CipherTypes.AttributeNameExceptionMsg2"), 
									 ciphField.getName())); //$NON-NLS-1$
				}
				
			}
		}
		
		return cipherMap;
	}
	
	/**
	 * Get the map containing tuples CipherTypeId and CipherTypeName
	 *  
	 * @return Map
	 */
	static public Map getCipherTypesMap() {
		// Return the value of the singleton
		if(cipherTypesMap == null)
			cipherTypesMap = createCipherTypesMap();
		
		return cipherTypesMap;
	}
	
	/**
	 * Class should not be created.
	 */
	protected CipherTypes() {
	}
}	
