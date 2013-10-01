// $Id: ValueSortedMap.java,v 1.6 2010/01/29 20:11:16 ruiliu Exp $

/*******************************************************************************
 * Copyright (c) 2009-2010 The Board of Trustees of the University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * 	   NCSA - initial API and implementation
 *******************************************************************************/

/**
 * Class to sort a given map by its values, not by its keys,
 * in ascending or descending order. Supports both regular maps
 * and nested maps (maps of maps). The type of the value in the
 * innermost map must be "Long".
 *
 * @author Rui Liu
 */

package org.eclipse.ptp.internal.etfw.feedback.perfsuite.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.Iterator;

public class ValueSortedMap {

    // definitions of the comparator classes
    private static class
        ValueComparator<K extends Comparable<K>, V extends Comparable<V>>
        implements Comparator<K> {

        private Map<K,V> map = null;
        private boolean desc = false;

        // indesc is a flag to request the descending order
        ValueComparator (Map<K,V> inmap, boolean indesc) {
            map = inmap;
            desc = indesc;
        }

        public int compare (K k1, K k2) {
            V v1 = map.get (k1);
            V v2 = map.get (k2);
            int ret = v1.compareTo (v2);
            // reverse the result for descending order
            if (desc) {
                ret = -ret;
            }
            if (ret != 0) {
                return ret;
            }
            // now, values are considered the same
            // we first compare the keys
            ret = k1.compareTo (k2);
            if (ret != 0) {
                return ret;
            }
            // now both values and keys are considered the same
            // we could possibly return any of the values -1, 0, and 1,
            // Let's just return 1, which means larger.
            // For an ascending sort, this means later.
            return 1;
        }
    }

    private static class
        NestedMapValueComparator<K extends Comparable<K>, V extends Map>
        implements Comparator<K> {

        private Map<K,V> map = null;
        private boolean desc = false;

        // indesc is a flag to request the descending order
        NestedMapValueComparator (Map<K,V> inmap, boolean indesc) {
            map = inmap;
            desc = indesc;
        }

        public int compare (K k1, K k2) {
            Map map1 = map.get (k1);
            Map map2 = map.get (k2);
            int ret = Long.signum (getMapSizeRecursively (map1)
                                   - getMapSizeRecursively (map2));
            // reverse the result for descending order
            if (desc) {
                ret = -ret;
            }
            if (ret != 0) {
                return ret;
            }
            ret = k1.compareTo (k2);
            if (ret != 0) {
                return ret;
            }
            return 1;
        }
    }

    // This method is mainly for a map whose leaf value object is a Long,
    // which is the case for the nestedMap used in PerfSuite profile processing.
    private static long getMapSizeRecursively (Map inmap) {
        long ret = 0;
        for (Iterator iter = inmap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (entry.getValue() instanceof Map) {
                long size = getMapSizeRecursively ((Map) entry.getValue());
                ret += size;
            } else {
                // Incorrectly wrote "ret++;" before, which was not the
                // desired behavior for sorting the sample map.
                Long val = (Long) entry.getValue();
                ret += val.longValue();
            }
        }
        return ret;
    }

    // get map methods
    public static <K extends Comparable<K>,V extends Comparable<V>>
        Map<K,V> getMap (Map<K,V> inmap) {

        Comparator<K> cmp = new ValueComparator<K,V> (inmap, false);
        Map<K,V> map = new TreeMap<K,V> (cmp);
        map.putAll (inmap);
        return map;
    }

    public static <K extends Comparable<K>,V extends Comparable<V>>
        Map<K,V> getMapDesc (Map<K,V> inmap) {

        Comparator<K> cmp = new ValueComparator<K,V> (inmap, true);
        Map<K,V> map = new TreeMap<K,V> (cmp);
        map.putAll (inmap);
        return map;
    }

    public static <K extends Comparable<K>,V extends Map>
        Map<K,V> getNestedMap (Map<K,V> inmap) {

        Comparator<K> cmp = new NestedMapValueComparator<K,V> (inmap, false);
        Map<K,V> map = new TreeMap<K,V> (cmp);
        map.putAll (inmap);
        return map;
    }

    public static <K extends Comparable<K>,V extends Map>
        Map<K,V> getNestedMapDesc (Map<K,V> inmap) {

        Comparator<K> cmp = new NestedMapValueComparator<K,V> (inmap, true);
        Map<K,V> map = new TreeMap<K,V> (cmp);
        map.putAll (inmap);
        return map;
    }

}
