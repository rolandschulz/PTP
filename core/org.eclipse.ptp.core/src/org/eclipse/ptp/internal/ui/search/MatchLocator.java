package org.eclipse.ptp.internal.ui.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.launch.core.ILaunchManager;

/**
 * @author Clement
 *
 */
public class MatchLocator implements IPSearchConstants {
    private PSearchPattern pattern = null;
    private PSearchResultCollector collector = null;
    private IProgressMonitor progressMonitor;
    private ILaunchManager launchManager = null;
    
    public MatchLocator(PSearchPattern pattern, PSearchResultCollector collector) {
        this.pattern = pattern;
        this.collector = collector;
        launchManager = ParallelPlugin.getDefault().getLaunchManager();
    }
    
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}
		
	public void locateMatches(PSearchPattern pattern) throws InterruptedException {
	    IPJob root = launchManager.getProcessRoot();
	    
	    if (pattern.getMode() == IPSearchConstants.EXACT_MATCH && pattern.getPLimiteTo() == IPSearchConstants.LIMIT_NUMBER)
    	    exactNumberMatches(root, pattern);
	    else
	        patternMatches(root, pattern);
	}
	
	private void patternMatches(IPJob root, PSearchPattern pattern) throws InterruptedException {
	    IPElement[] elements = getElements(root, pattern.getPSearchFor());
		if(progressMonitor != null)
			progressMonitor.beginTask("Start searching", elements.length);
		
		for(int i = 0; i <elements.length; i++){
			if(progressMonitor != null) {
				if(progressMonitor.isCanceled()){
					throw new InterruptedException();
				} else {
					progressMonitor.worked(1);
				}
			}
			
			IPElement element = elements[i];
			if (matchPattern(pattern.getPattern(), pattern.getPLimiteTo(), pattern.getMode(), element))
			    collector.addMatch(element, 0, 0);
		}
	}
	
	private void exactNumberMatches(IPJob root, PSearchPattern pattern) throws InterruptedException {
	    IPElement element = null;
	    switch (pattern.getPSearchFor()) {
	    	case IPSearchConstants.SEARCH_NODE:
	    	    element = root.findNode(pattern.getPattern());
	    	    break;
	    	case IPSearchConstants.SEARCH_PROCESS:
	    	    element = root.findProcess(pattern.getPattern());
	    	    break;
	    }
	    if (element != null)
	        collector.addMatch(element, 0, 0);
	}
	
	private boolean isMatch(String pattern, String target, int mode) {
		if (target == null)
			return false;
		
	    switch (mode) {
	        case PATTERN_MATCH:
	            return isMatch(pattern, target);
	        case EXACT_MATCH:
	            return target.equals(pattern);
	        default:
	            return false;
	    }
	}
	
	private boolean isMatch(String pattern, String target) {
        char[] patterns = pattern.toCharArray();	            
        int patternLen = pattern.length();
        int targetLen = target.length();
        int patPos = 0;
        int tarPos = 0;
        boolean hasStar = false;
        while (tarPos < targetLen) {
            if (patPos == patternLen)// other case return false;
                return false;

            char tChar = target.charAt(tarPos);
            char pChar = patterns[patPos];

            if (pChar == '*') {
                hasStar = true;
                patPos++;
                //eg.	1001, 10*
                if (patPos == patternLen)
                    return true;
                
                continue;
        	}
            
            if (hasStar) {	                    
                if (pChar == '?' || pChar == tChar) {
                    hasStar = false;
                    patPos++;

                    //eg.  1011, 1*1
	                if (patPos == patternLen) {
	                    if (pChar == '?' || pChar == target.charAt(targetLen-1))
		                    return true;
	                }
                }
                tarPos++;
                continue;
            }
            
            if (pChar == '?') {
                patPos++;
                if (patPos == patternLen) {
                    //eg.	100, 11?
                    if (tarPos == targetLen-1)
                        return true;
                    
                    return false;
                }
                tarPos++;
                continue;
            }
            
            if (pChar == tChar) {
                patPos++;
                if (patPos == patternLen) {
                    //eg.	100, ??1
                    if (tarPos == targetLen-1)
                        return true;
                    
                    return false;
                }
                tarPos++;
                continue;
            }
            
            return false;
        }
        //eg.	1, 100* or 1*1
        if (patternLen > targetLen) {
            //eg.	10, 10* or 100*
            if (pattern.endsWith("*") && patternLen - 1 == targetLen)
                return true;
            
            return false;
        }
        	            
        //eg.	100, 1* or 1? - 1010, 1*1 or 1?1
        if (patPos < patternLen || patternLen < targetLen)
            return false;
        
        //eg.	100, 10* or 10?
        return true;
	}
	
	private boolean matchPattern(String pattern, int limitTo, int mode, IPElement element) {
	    switch (limitTo) {
	        case LIMIT_NUMBER:
	            return isMatch(pattern, element.getKey(), mode);
	         case LIMIT_PID:
	             if (element instanceof IPProcess)
	                 return isMatch(pattern, ((IPProcess)element).getPid(), mode);
	             
	             return false;
	         case LIMIT_EXITCODE:
	             if (element instanceof IPProcess)
	                 return isMatch(pattern, ((IPProcess)element).getExitCode(), mode);
	             
	             return false;	             
	         default:
	             return false;
	    }	    
	}
	
	private IPElement[] getElements(IPJob root, int searchFor) {
	    switch (searchFor) {
	        case SEARCH_NODE:
	            return root.getSortedNodes(); 
	         case SEARCH_PROCESS:
	             return root.getSortedProcesses();
	         default:
	             return new IPElement[0];
	    }	    
	}
}
