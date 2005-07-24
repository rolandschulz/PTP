package org.eclipse.cdt.core.addl_langs;

import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Iterates through all of the <code>IAdditionalLanguage</code>s supplied via the
 * <code>AdditionalLanguage</code> extension point.
 * 
 * @author Jeff Overbey
*/
public final class AdditionalLanguagesIterator implements Iterator/* <IAdditionalLanguage> */
{
	private abstract class State {
		private State() {
			;
		}

		public abstract IAdditionalLanguage getNextLanguage();
	}

	public final State INITIAL = new State() {
		public IAdditionalLanguage getNextLanguage() {
			IExtensionRegistry extensionRegistry = Platform
					.getExtensionRegistry();
			IExtensionPoint extensionPoint = extensionRegistry
					.getExtensionPoint(CCorePlugin.PLUGIN_ID,
							AdditionalLanguagesExtension.EXTENSION_POINT_ID);
			stateInfo.extensions = extensionPoint.getExtensions();

			stateInfo.currentExtensionNum = -1;
			currentState = GOTO_NEXT_EXTENSION;
			return currentState.getNextLanguage();
		}
	};

	public final State GOTO_NEXT_EXTENSION = new State() {
		public IAdditionalLanguage getNextLanguage() {
			stateInfo.currentExtensionNum++;
			if (stateInfo.currentExtensionNum == stateInfo.extensions.length) {
				currentState = DONE;
				return null;
			}
			stateInfo.configElements = stateInfo.extensions[stateInfo.currentExtensionNum]
					.getConfigurationElements();
			if (stateInfo.configElements.length == 0)
				return getNextLanguage();
			else {
				stateInfo.currentConfigElementNum = -1;
				currentState = GOTO_NEXT_CONFIG_ELEMENT;
				return currentState.getNextLanguage();
			}
		}
	};

	public final State GOTO_NEXT_CONFIG_ELEMENT = new State() {
		public IAdditionalLanguage getNextLanguage() {
			stateInfo.currentConfigElementNum++;
			if (stateInfo.currentConfigElementNum == stateInfo.configElements.length) {
				stateInfo.currentConfigElementNum = -1;
				currentState = GOTO_NEXT_EXTENSION;
				return currentState.getNextLanguage();
			} else {
				try {
					IAdditionalLanguage currentLanguage = (IAdditionalLanguage) stateInfo.configElements[stateInfo.currentConfigElementNum]
							.createExecutableExtension("class");
					if (currentLanguage == null) // Only return null when
													// we're finished (DONE
													// state)!
						return getNextLanguage();
					else
						return currentLanguage;
				} catch (Exception e) {
					CCorePlugin.log(e);
					return getNextLanguage(); // Ignore the problematic one
				}
			}
		}
	};

	public final State DONE = new State() {
		public IAdditionalLanguage getNextLanguage() {
			return null;
		}
	};

	private class StateInfo {
		private IExtension[] extensions;

		private IConfigurationElement[] configElements;

		private int currentExtensionNum;

		private int currentConfigElementNum;
	}

	private final StateInfo stateInfo = new StateInfo();
	
	private State currentState = null;
	
	private IAdditionalLanguage nextLanguage = null;

	AdditionalLanguagesIterator() {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(
				CCorePlugin.PLUGIN_ID,
				AdditionalLanguagesExtension.EXTENSION_POINT_ID);
		IExtension[] extensions = extensionPoint.getExtensions();

		currentState = INITIAL;
		nextLanguage = currentState.getNextLanguage();
	}

	public boolean hasNext() {
		return nextLanguage != null;
	}

	public Object next() {
		IAdditionalLanguage currentLanguage = nextLanguage;
		nextLanguage = currentState.getNextLanguage();
		return currentLanguage;
	}

	public void remove() {
		throw new Error("AdditionalLanguagesIterator#remove is not a valid operation.");
	}
}
