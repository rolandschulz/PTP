package org.eclipse.cldt.internal.ui.viewsupport;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.CoreModel;
import org.eclipse.cldt.core.model.IBinary;
import org.eclipse.cldt.core.model.IBinaryModule;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.core.model.ICProject;
import org.eclipse.cldt.core.model.IDeclaration;
import org.eclipse.cldt.core.model.IField;
import org.eclipse.cldt.core.model.IIncludeReference;
import org.eclipse.cldt.core.model.ILibraryReference;
import org.eclipse.cldt.core.model.IMethodDeclaration;
import org.eclipse.cldt.core.model.ISourceRoot;
import org.eclipse.cldt.core.model.ITemplate;
import org.eclipse.cldt.core.model.ITranslationUnit;
import org.eclipse.cldt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cldt.internal.ui.FortranPluginImages;
import org.eclipse.cldt.ui.CElementImageDescriptor;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;


/**
 * Default strategy of the C plugin for the construction of C element icons.
 */
public class FortranElementImageProvider {
	
	/**
	 * Flags for the CImageLabelProvider:
	 * Generate images with overlays.
	 */
	public final static int OVERLAY_ICONS= 0x1;

	/**
	 * Generate small sized images.
	 */
	public final static int SMALL_ICONS= 0x2;

	/**
	 * Use the 'light' style for rendering types.
	 */	
	public final static int LIGHT_TYPE_ICONS= 0x4;
	
	/**
	 * Show error overrlay. 
	 */	
	public final static int OVERLAY_ERROR= 0x8;

	/**
	 * Show warning overrlay
	 */	
	public final static int OVERLAY_WARNING= 0x10;
	
	/**
	 * Show override overrlay. 
	 */	
	public final static int OVERLAY_OVERRIDE= 0x20;

	/**
	 * Show implements overrlay. 
	 */	
	public final static int OVERLAY_IMPLEMENTS= 0x40;
	
	public static final Point SMALL_SIZE= new Point(16, 16);
	public static final Point BIG_SIZE= new Point(22, 16);

	private static ImageDescriptor DESC_OBJ_PROJECT_CLOSED;	
	private static ImageDescriptor DESC_OBJ_PROJECT;	
	//private static ImageDescriptor DESC_OBJ_FOLDER;
	{
		ISharedImages images= FortranUIPlugin.getDefault().getWorkbench().getSharedImages(); 
		DESC_OBJ_PROJECT_CLOSED= images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED);
		DESC_OBJ_PROJECT= 		 images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
		//DESC_OBJ_FOLDER= 		 images.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}
	
	public FortranElementImageProvider() {
	}	
		
	/**
	 * Returns the icon for a given element. The icon depends on the element type
	 * and element properties. If configured, overlay icons are constructed for
	 * <code>ISourceReference</code>s.
	 * @param flags Flags as defined by the JavaImageLabelProvider
	 */
	public Image getImageLabel(Object element, int flags) {
		ImageDescriptor descriptor= null;
		if (element instanceof ICElement) {
			descriptor= getCImageDescriptor((ICElement) element, flags);
		} else if (element instanceof IFile) {
			// Check for Non Translation Unit.
			IFile file = (IFile)element;
			if (CoreModel.isValidTranslationUnitName(file.getProject(), file.getName()) ||
					CoreModel.isValidTranslationUnitName(null, file.getName())) {
				descriptor = FortranPluginImages.DESC_OBJS_TUNIT_RESOURCE;
				Point size= useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
				descriptor = new CElementImageDescriptor(descriptor, 0, size);
			}
		}
		if (descriptor == null && element instanceof IAdaptable) {
			descriptor= getWorkbenchImageDescriptor((IAdaptable) element, flags);
		}
		if (descriptor != null) {
			return FortranUIPlugin.getImageDescriptorRegistry().get(descriptor);
		}
		return null;
	}

	/**
 	 * 
	 * @param type
	 * @return
	 */
	public static ImageDescriptor getImageDescriptor(int type) {
		switch (type) {
			case ICElement.C_VCONTAINER:
				return FortranPluginImages.DESC_OBJS_CONTAINER;

			case ICElement.C_BINARY:
				return FortranPluginImages.DESC_OBJS_BINARY;
	
			case ICElement.C_ARCHIVE:
				return FortranPluginImages.DESC_OBJS_ARCHIVE;

			case ICElement.C_UNIT:
				return FortranPluginImages.DESC_OBJS_TUNIT;
				
			case ICElement.C_CCONTAINER:
				//return DESC_OBJ_FOLDER;
				return FortranPluginImages.DESC_OBJS_CFOLDER;
			
			case ICElement.C_PROJECT:
				return DESC_OBJ_PROJECT;
					
			case ICElement.C_STRUCT:
			case ICElement.C_TEMPLATE_STRUCT:
				return FortranPluginImages.DESC_OBJS_STRUCT;
				
			case ICElement.C_CLASS:
			case ICElement.C_TEMPLATE_CLASS:
				return FortranPluginImages.DESC_OBJS_CLASS;

			case ICElement.C_UNION:
			case ICElement.C_TEMPLATE_UNION:
				return FortranPluginImages.DESC_OBJS_UNION;

			case ICElement.C_TYPEDEF:
				return FortranPluginImages.DESC_OBJS_TYPEDEF;

			case ICElement.C_ENUMERATION:
				return FortranPluginImages.DESC_OBJS_ENUMERATION;

			case ICElement.C_ENUMERATOR:
				return FortranPluginImages.DESC_OBJS_ENUMERATOR;

			case ICElement.C_FIELD:
				return FortranPluginImages.DESC_OBJS_PUBLIC_FIELD;
			
			case ICElement.C_VARIABLE:
			case ICElement.C_TEMPLATE_VARIABLE:
				return FortranPluginImages.DESC_OBJS_VARIABLE;

			case ICElement.C_METHOD:  
			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_TEMPLATE_METHOD:
				return FortranPluginImages.DESC_OBJS_PUBLIC_METHOD;
				
			case ICElement.C_FUNCTION:
				return FortranPluginImages.DESC_OBJS_FUNCTION;

			case ICElement.C_STRUCT_DECLARATION:
			case ICElement.C_CLASS_DECLARATION:
			case ICElement.C_UNION_DECLARATION:
			case ICElement.C_VARIABLE_DECLARATION:
				return FortranPluginImages.DESC_OBJS_VAR_DECLARARION;
			
			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_TEMPLATE_FUNCTION:
				return FortranPluginImages.DESC_OBJS_DECLARARION;

			case ICElement.C_INCLUDE:
				return FortranPluginImages.DESC_OBJS_INCLUDE;

			case ICElement.C_MACRO:
				return FortranPluginImages.DESC_OBJS_MACRO;
				
			case ICElement.C_NAMESPACE:
				return FortranPluginImages.DESC_OBJS_NAMESPACE;

			case ICElement.C_USING:
				return FortranPluginImages.DESC_OBJS_USING;
		}
		return null;
	}

	private boolean showOverlayIcons(int flags) {
		return (flags & OVERLAY_ICONS) != 0;
	}
	
//	private boolean useLightIcons(int flags) {
//		return (flags & LIGHT_TYPE_ICONS) != 0;
//	}
	
	private boolean useSmallSize(int flags) {
		return (flags & SMALL_ICONS) != 0;
	}
	
	/**
	 * Returns an image descriptor for a C element. The descriptor includes overlays, if specified.
	 */
	public ImageDescriptor getCImageDescriptor(ICElement element, int flags) {
		int adornmentFlags= computeCAdornmentFlags(element, flags);
		Point size= useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
		ImageDescriptor desc = getBaseImageDescriptor(element, flags);
		if(desc != null) {
			return new CElementImageDescriptor(desc, adornmentFlags, size);
		}
		return null;
	}

	/**
	 * Returns an image descriptor for a IAdaptable. The descriptor includes overlays, if specified (only error ticks apply).
	 * Returns <code>null</code> if no image could be found.
	 */	
	public ImageDescriptor getWorkbenchImageDescriptor(IAdaptable adaptable, int flags) {
		IWorkbenchAdapter wbAdapter= (IWorkbenchAdapter) adaptable.getAdapter(IWorkbenchAdapter.class);
		if (wbAdapter == null) {
			return null;
		}
		ImageDescriptor descriptor= wbAdapter.getImageDescriptor(adaptable);
		if (descriptor == null) {
			return null;
		}
		int adornmentFlags= computeBasicAdornmentFlags(adaptable, flags);
		Point size= useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
		return new CElementImageDescriptor(descriptor, adornmentFlags, size);
	}
	
	// ---- Computation of base image key -------------------------------------------------
	
	/**
	 * Returns an image descriptor for a C element. This is the base image, no overlays.
	 */
	public ImageDescriptor getBaseImageDescriptor(ICElement celement, int renderFlags) {
		int type = celement.getElementType();
		switch (type) {
			case ICElement.C_VCONTAINER:
				if (celement instanceof IBinaryModule) {
					return FortranPluginImages.DESC_OBJS_BINARY;
				} else if (celement instanceof ILibraryReference) {
					return FortranPluginImages.DESC_OBJS_UNKNOWN;
				} else if (celement instanceof IIncludeReference) {
					return FortranPluginImages.DESC_OBJS_INCLUDES_FOLDER;
				}
				return FortranPluginImages.DESC_OBJS_CONTAINER;

			case ICElement.C_BINARY: {
				IBinary bin = (IBinary)celement;
				if (bin.isExecutable()) {
					if (bin.hasDebug())
						return FortranPluginImages.DESC_OBJS_CEXEC_DEBUG;
					return FortranPluginImages.DESC_OBJS_CEXEC;
				} else if (bin.isSharedLib()) {
					return FortranPluginImages.DESC_OBJS_SHLIB;
				} else if (bin.isCore()) {
					return FortranPluginImages.DESC_OBJS_CORE;
				}
				return FortranPluginImages.DESC_OBJS_BINARY;
			}
	
			case ICElement.C_ARCHIVE:
				return FortranPluginImages.DESC_OBJS_ARCHIVE;

			case ICElement.C_UNIT: {
				ITranslationUnit unit = (ITranslationUnit)celement;
				if (unit.isHeaderUnit()) {
					return FortranPluginImages.DESC_OBJS_TUNIT_HEADER;
				} else if (unit.isSourceUnit()) {
					if (unit.isASMLanguage()) {
						return FortranPluginImages.DESC_OBJS_TUNIT_ASM;
					}
				}
				return FortranPluginImages.DESC_OBJS_TUNIT;
			}
				
			case ICElement.C_CCONTAINER:
				if (celement instanceof ISourceRoot) {
					return FortranPluginImages.DESC_OBJS_SOURCE_ROOT;
				}
				//return DESC_OBJ_FOLDER;
				return FortranPluginImages.DESC_OBJS_CFOLDER;
			
			case ICElement.C_PROJECT:
				ICProject cp= (ICProject)celement;
				if (cp.getProject().isOpen()) {
					IProject project= cp.getProject();
					IWorkbenchAdapter adapter= (IWorkbenchAdapter)project.getAdapter(IWorkbenchAdapter.class);
					if (adapter != null) {
						ImageDescriptor result= adapter.getImageDescriptor(project);
						if (result != null)
							return result;
					}
					return DESC_OBJ_PROJECT;
				}
				return DESC_OBJ_PROJECT_CLOSED;

			case ICElement.C_STRUCT:
			case ICElement.C_TEMPLATE_STRUCT:
				return getStructImageDescriptor();
				
			case ICElement.C_CLASS:
			case ICElement.C_TEMPLATE_CLASS:
				return getClassImageDescriptor();
				
			case ICElement.C_UNION:
			case ICElement.C_TEMPLATE_UNION:
				return getUnionImageDescriptor();

			case ICElement.C_TYPEDEF:
				return getTypedefImageDescriptor();

			case ICElement.C_ENUMERATION:
				return getEnumerationImageDescriptor();

			case ICElement.C_ENUMERATOR:
				return getEnumeratorImageDescriptor();

			case ICElement.C_FIELD:
				try {
					IField  field = (IField)celement;
					ASTAccessVisibility visibility = field.getVisibility();
					return getFieldImageDescriptor(visibility);
				} catch (CModelException e) {
					return null;
				}
			
			case ICElement.C_METHOD:  
			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_TEMPLATE_METHOD:
				try {
					
					IMethodDeclaration  md= (IMethodDeclaration)celement;
					ASTAccessVisibility visibility =md.getVisibility();
					return getMethodImageDescriptor(visibility); 
				} catch (CModelException e) {
					return null;
				}
			case ICElement.C_VARIABLE:
			case ICElement.C_TEMPLATE_VARIABLE:
				return getVariableImageDescriptor();
				
			case ICElement.C_FUNCTION:
				return getFunctionImageDescriptor();

			case ICElement.C_STRUCT_DECLARATION:
			case ICElement.C_CLASS_DECLARATION:
			case ICElement.C_UNION_DECLARATION:
			case ICElement.C_VARIABLE_DECLARATION:
				return getVariableDeclarationImageDescriptor();
			
			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_TEMPLATE_FUNCTION:
				return getFunctionDeclarationImageDescriptor();

			case ICElement.C_INCLUDE:
				return getIncludeImageDescriptor();

			case ICElement.C_MACRO:
				return getMacroImageDescriptor();
				
			case ICElement.C_NAMESPACE:
				return getNamespaceImageDescriptor();

			case ICElement.C_USING:
				return getUsingImageDescriptor();

		}
		return null;
	}	


	// ---- Methods to compute the adornments flags ---------------------------------
	
	private int computeCAdornmentFlags(ICElement element, int renderFlags) {
		
		int flags= computeBasicAdornmentFlags(element, renderFlags);

		try {
			if (showOverlayIcons(renderFlags) && element instanceof IDeclaration) {
				IDeclaration decl = (IDeclaration) element;
				if(decl.isStatic()){
					flags |= CElementImageDescriptor.STATIC;
				}
				if(decl.isConst()){
					flags |= CElementImageDescriptor.CONSTANT;
				}
				if(decl.isVolatile()){
					flags |= CElementImageDescriptor.VOLATILE;
				}
				if(element instanceof ITemplate){
					flags |= CElementImageDescriptor.TEMPLATE;
				}
			}
		} catch (CModelException e) {
		}
		return flags;
	}
	
	private int computeBasicAdornmentFlags(Object element, int renderFlags) {
		int flags= 0;
		if ((renderFlags & OVERLAY_ERROR) !=0) {
			flags |= CElementImageDescriptor.ERROR;
		}
		if ((renderFlags & OVERLAY_WARNING) !=0) {
			flags |= CElementImageDescriptor.WARNING;
		}		
		if ((renderFlags & OVERLAY_OVERRIDE) !=0) {
			flags |= CElementImageDescriptor.OVERRIDES;
		}
		if ((renderFlags & OVERLAY_IMPLEMENTS) !=0) {
			flags |= CElementImageDescriptor.IMPLEMENTS;
		}
		return flags;			
	}	
	
	public void dispose() {
	}
	
	public static ImageDescriptor getStructImageDescriptor(){
		return FortranPluginImages.DESC_OBJS_STRUCT;	
	}
	
	public static ImageDescriptor getClassImageDescriptor(){
		return FortranPluginImages.DESC_OBJS_CLASS;	
	}
	
	public static ImageDescriptor getUnionImageDescriptor(){
		return FortranPluginImages.DESC_OBJS_UNION;	
	}
	
	public static ImageDescriptor getTypedefImageDescriptor(){
		return FortranPluginImages.DESC_OBJS_TYPEDEF;	
	}
	
	public static ImageDescriptor getEnumerationImageDescriptor(){
		return FortranPluginImages.DESC_OBJS_ENUMERATION;	
	}
	
	public static ImageDescriptor getEnumeratorImageDescriptor(){
		return FortranPluginImages.DESC_OBJS_ENUMERATOR;	
	}

	public static ImageDescriptor getFieldImageDescriptor(ASTAccessVisibility visibility) {
		if (visibility == ASTAccessVisibility.PUBLIC)
			return FortranPluginImages.DESC_OBJS_PUBLIC_FIELD;
		if( visibility == ASTAccessVisibility.PROTECTED)
			return FortranPluginImages.DESC_OBJS_PROTECTED_FIELD;
		
		return FortranPluginImages.DESC_OBJS_PRIVATE_FIELD;			
	}

	public static ImageDescriptor getMethodImageDescriptor(ASTAccessVisibility visibility) {
		if( visibility == ASTAccessVisibility.PUBLIC)
			return FortranPluginImages.DESC_OBJS_PUBLIC_METHOD;
		if( visibility == ASTAccessVisibility.PROTECTED)
			return FortranPluginImages.DESC_OBJS_PROTECTED_METHOD;
		
		return FortranPluginImages.DESC_OBJS_PRIVATE_METHOD;				
	}

	public static ImageDescriptor getVariableImageDescriptor(){
		return getImageDescriptor(ICElement.C_VARIABLE);
	}

	public static ImageDescriptor getLocalVariableImageDescriptor(){
		return FortranPluginImages.DESC_OBJS_LOCAL_VARIABLE;	
	}
	
	public static ImageDescriptor getFunctionImageDescriptor(){
		return getImageDescriptor(ICElement.C_FUNCTION);
	}

	public static ImageDescriptor getVariableDeclarationImageDescriptor(){
		return getImageDescriptor(ICElement.C_VARIABLE_DECLARATION);
	}

	public static ImageDescriptor getFunctionDeclarationImageDescriptor(){
		return getImageDescriptor(ICElement.C_FUNCTION_DECLARATION);
	}

	public static ImageDescriptor getIncludeImageDescriptor(){
		return getImageDescriptor(ICElement.C_INCLUDE);
	}

	public static ImageDescriptor getMacroImageDescriptor(){
		return getImageDescriptor(ICElement.C_MACRO);
	}

	public static ImageDescriptor getNamespaceImageDescriptor(){
		return getImageDescriptor(ICElement.C_NAMESPACE);
	}

	public static ImageDescriptor getUsingImageDescriptor(){
		return getImageDescriptor(ICElement.C_USING);
	}

	public static ImageDescriptor getKeywordImageDescriptor(){
		return FortranPluginImages.DESC_OBJS_KEYWORD;
	}

}