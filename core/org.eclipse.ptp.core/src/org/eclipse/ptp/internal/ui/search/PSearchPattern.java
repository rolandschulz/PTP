package org.eclipse.ptp.internal.ui.search;

/**
 * @author Clement
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
