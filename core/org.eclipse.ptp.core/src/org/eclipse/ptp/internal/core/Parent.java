/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.ptp.core.IPElement;

/**
 *  
 */
public abstract class Parent extends PElement {
    public Parent(IPElement parent, String name, int type) {
        super(parent, name, type);
    }

    public void addChild(IPElement member) {
        getElementInfo().addChild(member);
    }

    public void removeChild(IPElement member) {
        getElementInfo().removeChild(member);
    }

    public IPElement findChild(String key) {
        return getElementInfo().findChild(key);
    }

    public void removeChildren() {
        getElementInfo().removeChildren();
    }
    
    public Collection getCollection() {
        PElementInfo info = getElementInfo();
        if (info != null)
            return info.getCollection();

        return null;
    }

    public IPElement[] getChildren() {
        PElementInfo info = getElementInfo();
        if (info != null)
            return info.getChildren();

        return new IPElement[]{};
    }

    public IPElement[] getSortedChildren() {
        IPElement[] elements = getChildren();
        sort(elements);
        return elements;
    }

    public List getChildrenOfType(int type) {
        IPElement[] children = getChildren();
        int size = children.length;
        ArrayList list = new ArrayList(size);
        for (int i = 0; i < size; ++i) {
            PElement elt = (PElement) children[i];
            if (elt.getElementType() == type) {
                list.add(elt);
            }
        }
        return list;
    }

    public boolean hasChildren() {
        return getElementInfo().hasChildren();
    }

    public boolean isAllStop() {
        IPElement[] elements = getChildren();
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].isAllStop())
                return false;
        }
        return true;
    }

    private void quickSort(IPElement element[], int low, int high) {
        int lo = low;
        int hi = high;
        int mid;
        if (high > low) {
            mid = element[(low + high) / 2].getKeyNumber();
            while (lo <= hi) {
                while ((lo < high) && (element[lo].getKeyNumber() < mid))
                    ++lo;
                while ((hi > low) && (element[hi].getKeyNumber() > mid))
                    --hi;
                if (lo <= hi) {
                    swap(element, lo, hi);
                    ++lo;
                    --hi;
                }
            }
            if (low < hi)
                quickSort(element, low, hi);
            if (lo < high)
                quickSort(element, lo, high);
        }
    }

    private void swap(IPElement element[], int i, int j) {
        IPElement tempElement;
        tempElement = element[i];
        element[i] = element[j];
        element[j] = tempElement;
    }

    public void sort(IPElement element[]) {
        quickSort(element, 0, element.length - 1);
    }
}