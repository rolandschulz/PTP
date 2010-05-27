/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.preprocessor.c;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * A class which uses reflection to obtain the name of an IToken
 * type, given the integer type from IToken.getType(). This class
 * should only be used for debugging.
 * 
 * @author Matthew Michelotti
 */
public class TokenTypeTranslator
{
    /**mapping of integer codes to names*/
    private static HashMap<Integer, String> typeMap = null;
    
    /**
     * @param type - integer type obtained by IToken.getType()
     * @return the field name as found when TokenTypeTranslator was
     *         constructed, or "unknown" if not found
     */
    public static String typeToString(int type) {
    	if(typeMap == null) initialize();
        String str = typeMap.get(type);
        if(str == null) return "unknown"; //$NON-NLS-1$
        else return str;
    }
    
    /**
     * Uses reflection to obtain public static
     * integer values beginning with a lower-case 't' from the classes
     * IToken, Lexer, and CPreprocessor. Creates a mapping from the
     * values of these fields to their names.
     */
    private static void initialize() {
    	HashMap<Integer, String> newTypeMap = new HashMap<Integer, String>();
    	mapTypesFromClass(newTypeMap, IToken.class);
        mapTypesFromClass(newTypeMap, Lexer.class);
        mapTypesFromClass(newTypeMap, CPreprocessor.class);
        typeMap = newTypeMap;
    }
    
    /**
     * Create a mapping (as described in the constructor)
     * for a single class. Values are placed in typeMap.
     * @param myClass - class to look in for token type fields
     *        (any public static int beginning with a 't')
     */
    private static void mapTypesFromClass(HashMap<Integer, String> typeMap,
    										Class<?> myClass)
    {
        Field[] fields = myClass.getFields();
        for(int i = 0; i < fields.length; i++) {
            try {
                
            Field field = fields[i];
            int modifiers = field.getModifiers();
            if(!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers))
                    continue;
            
            String name = field.getName();
            if(name.length() == 0 || name.charAt(0) != 't') continue;
            
            typeMap.put(field.getInt(null), name);
            
            }catch(IllegalArgumentException e) {}
            catch(IllegalAccessException e) {}
        }
    }
}
