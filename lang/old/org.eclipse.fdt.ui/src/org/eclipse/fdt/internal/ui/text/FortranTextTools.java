package org.eclipse.fdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.fdt.internal.ui.text.IFortranColorConstants;
import org.eclipse.fdt.internal.ui.text.IFortranPartitions;
import org.eclipse.fdt.ui.FortranUIPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * This type shares all scanners and the color manager between
 * its clients.
 */
public class FortranTextTools extends CTextTools {
	
	private class PreferenceListener implements IPropertyChangeListener, Preferences.IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			adaptToPreferenceChange(event);
		}
        public void propertyChange(Preferences.PropertyChangeEvent event) {
            adaptToPreferenceChange(new PropertyChangeEvent(event.getSource(), event.getProperty(), event.getOldValue(), event.getNewValue()));
        }
	}
	
	/** The color manager */
	private CColorManager fColorManager;
	/** The Fortran source code scanner */
	private FortranCodeScanner fCodeScanner;
	/** The Fortran partitions scanner */
	private FastFortranPartitionScanner fPartitionScanner;
	/** The Fortran singleline comment scanner */
	private FortranCommentScanner fSinglelineCommentScanner;
	/** The Fortran string scanner */
	private SingleTokenFortranScanner fStringScanner;

	/** The preference store */
	private IPreferenceStore fPreferenceStore;
    /** The core preference store */
    private Preferences fCorePreferenceStore;	
	/** The preference change listener */
	private PreferenceListener fPreferenceListener= new PreferenceListener();
	
	
	/**
	 * Creates a new C text tools collection and eagerly creates 
	 * and initializes all members of this collection.
	 */
    public FortranTextTools(IPreferenceStore store) {
        this(store, null);
    }
    
    /**
     * Creates a new C text tools collection and eagerly creates 
     * and initializes all members of this collection.
     */
	public FortranTextTools(IPreferenceStore store, Preferences coreStore) {
		if(store == null) {
			store = FortranUIPlugin.getDefault().getPreferenceStore();
		}
		fPreferenceStore = store;
		fPreferenceStore.addPropertyChangeListener(fPreferenceListener);
        
        fCorePreferenceStore= coreStore;
        if (fCorePreferenceStore != null) {
            fCorePreferenceStore.addPropertyChangeListener(fPreferenceListener);
        }
		
		fColorManager= new CColorManager();
		fCodeScanner= new FortranCodeScanner(fColorManager, store);
		fPartitionScanner= new FastFortranPartitionScanner();
		
		fSinglelineCommentScanner= new FortranCommentScanner(fColorManager, store, coreStore, IFortranColorConstants.FORTRAN_SINGLE_LINE_COMMENT);
		fStringScanner= new SingleTokenFortranScanner(fColorManager, store, IFortranColorConstants.FORTRAN_STRING);
	}
	
	/**
	 * Creates a new C text tools collection and eagerly creates 
	 * and initializes all members of this collection.
	 */
	public FortranTextTools() {
		this((IPreferenceStore)null);
	}
	
	/**
	 * Disposes all members of this tools collection.
	 */
	public void dispose() {
		
		fCodeScanner= null;
		fPartitionScanner= null;
		
		
		fSinglelineCommentScanner= null;
		fStringScanner= null;
		
		if (fColorManager != null) {
			fColorManager.dispose();
			fColorManager= null;
		}
		
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fPreferenceListener);
			fPreferenceStore= null;
            
            if (fCorePreferenceStore != null) {
                fCorePreferenceStore.removePropertyChangeListener(fPreferenceListener);
                fCorePreferenceStore= null;
            }
            
			fPreferenceListener= null;
		}
	}
	
	/**
	 * Gets the color manager.
	 */
	public CColorManager getColorManager() {
		return fColorManager;
	}
	
	/**
	 * Gets the code scanner used.
	 */
	public RuleBasedScanner getFortranCodeScanner() {
		return fCodeScanner;
	}
	
	/**
	 * Returns a scanner which is configured to scan 
	 * Fortran-specific partitions.
	 *
	 * @return a C partition scanner
	 */
	public IPartitionTokenScanner getPartitionScanner() {
		return fPartitionScanner;
	}
	
	/**
	 * Gets the document provider used.
	 */
	public IDocumentPartitioner createDocumentPartitioner() {
		
		String[] types= new String[] {
			IFortranPartitions.FORTRAN_SINGLE_LINE_COMMENT,
			IFortranPartitions.FORTRAN_STRING
		};
		
		return new FastPartitioner(getPartitionScanner(), types);
	}

	/**
	 * Returns a scanner which is configured to scan C singleline comments.
	 *
	 * @return a Fortran singleline comment scanner
	 */
	public RuleBasedScanner getSinglelineCommentScanner() {
		return fSinglelineCommentScanner;
	}
	
	/**
	 * Returns a scanner which is configured to scan Fortran strings.
	 *
	 * @return a Java string scanner
	 */
	public RuleBasedScanner getStringScanner() {
		return fStringScanner;
	}

	
	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one its contained components.
	 * 
	 * @param event the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return  fCodeScanner.affectsBehavior(event) ||
					fSinglelineCommentScanner.affectsBehavior(event) ||
					fStringScanner.affectsBehavior(event);
	}
	
	/**
	 * Adapts the behavior of the contained components to the change
	 * encoded in the given event.
	 * 
	 * @param event the event to whch to adapt
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (fCodeScanner.affectsBehavior(event))
			fCodeScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		if (fStringScanner.affectsBehavior(event))
			fStringScanner.adaptToPreferenceChange(event);
	}

}