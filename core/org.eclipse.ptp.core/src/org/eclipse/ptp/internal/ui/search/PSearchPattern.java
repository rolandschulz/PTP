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
package org.eclipse.ptp.internal.ui.search;

/**
 *
 */
public class PSearchPattern implements IPSearchConstants {
    private String pattern = null;
    private int pSearchFor;
    private int pLimiteTo;
    private int mode;
    
    public PSearchPattern(String pattern, int pSearchFor, int pLimiteTo, int mode) {
        this.pattern = pattern;
        this.pSearchFor = pSearchFor;
        this.pLimiteTo = pLimiteTo;
        this.mode = mode;
    }
    /**
     * @return Returns the mode.
     */
    public int getMode() {
        return mode;
    }
    /**
     * @param mode The mode to set.
     */
    public void setMode(int mode) {
        this.mode = mode;
    }
    /**
     * @return Returns the pattern.
     */
    public String getPattern() {
        return pattern;
    }
    /**
     * @param pattern The pattern to set.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    /**
     * @return Returns the pLimiteTo.
     */
    public int getPLimiteTo() {
        return pLimiteTo;
    }
    /**
     * @param limiteTo The pLimiteTo to set.
     */
    public void setPLimiteTo(int limiteTo) {
        pLimiteTo = limiteTo;
    }
    /**
     * @return Returns the pSearchFor.
     */
    public int getPSearchFor() {
        return pSearchFor;
    }
    /**
     * @param searchFor The pSearchFor to set.
     */
    public void setPSearchFor(int searchFor) {
        pSearchFor = searchFor;
    }
    
    public int markCount(char mark) {
        int counter = 0;
        if (mode == PATTERN_MATCH) {
            for (int i=0; i<pattern.length(); i++) {
                char aChar = pattern.charAt(i);
                if (aChar == mark)
                    counter++;
            }
        }
        return counter;
    }
}
