package org.eclipse.ptp.internal.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.core.IOutputTextFileContants;

/**
 * @author clement
 *
 */
public class OutputTextFile implements IOutputTextFileContants {
    private String filename = "";
    private File file = null;
    private String outputPath = null;
    private int storeLine = 0;
    private int lineCounter = 0;
    
    public OutputTextFile(String processNumber, String outputPath, int storeLine) {
        this.filename = "process" + processNumber + ".tmp";
        this.outputPath = outputPath;
        this.storeLine = storeLine;
        init();
    }
    
    private void init() {
        file = getFilePath();
    }
    
    private File getFilePath() {
        IPath filePath = new Path(outputPath).append(filename);
        File tmpFile = filePath.toFile();
        try {
            tmpFile.createNewFile();
        } catch (IOException e) {
            System.out.println("OutputTextFile - getFilePath err: " + e.getMessage());
        }
        return tmpFile;
    }
    
    public void write(String text) {
   	    if (lineCounter == storeLine) {
   	        lineCounter = 0;
   	        write(text, false);
   	    }
   	    else
   	        write(text, true);
    }
    
    
    public void write(String text, boolean isAppend) {
        FileOutputStream fos = null;
       	try {
    	    fos = new FileOutputStream(file, isAppend);
       	    fos.write(text.getBytes());
       	    lineCounter++;
       	} catch (FileNotFoundException e) {
       	    System.out.println("OutputTextFile - append file err: " + e.getMessage());
       	} catch (IOException ioe) {    
       	    System.out.println("OutputTextFile - append io err: " + ioe.getMessage());
       	} finally {
       	    if (fos != null) {
       	        try {
       	            fos.close();
       	        } catch (IOException ioe) {
       	       	    System.out.println("OutputTextFile - append close err: " + ioe.getMessage());
       	        }
       	    }
       	    fos = null;        
       	}
    }

    public void delete() {
        if (file.exists())
            file.delete();
        
        file = null;
    }    
    
    public String getContents() {
        if (file == null)
            return null;

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            return readString(is, ResourcesPlugin.getEncoding());            
        } catch (FileNotFoundException e) {
            System.out.println("OutputTextFile - read file err: " + e.getMessage());            
        } finally {
            is = null;
        }
        return null;
    }

    private String readString(InputStream is, String encoding) {
        if (is == null)
            return null;
        
        BufferedReader reader = null;
        try {
            StringBuffer buffer = new StringBuffer();
            char[] part = new char[2048];
            int read = 0;
            reader = new BufferedReader(new InputStreamReader(is, encoding));
            while ((read = reader.read(part)) != -1)
                buffer.append(part, 0, read);
            
            return buffer.toString();
        } catch (IOException ex) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
        }
        return null;
    }
    
    /*
    private InputStream getInputStream(IResource resource) throws CoreException {
        if (resource instanceof IStorage) {
            InputStream is = null;
            IStorage storage = (IStorage)resource;
            try {                
                is = storage.getContents();
            } catch (CoreException e) {
                if (e.getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
                    resource.refreshLocal(IResource.DEPTH_INFINITE, null);
                    is = storage.getContents();
                } else
                    throw e;
            }
            if (is != null)
                return new BufferedInputStream(is);            
        }
        return null;
        
    }
    
    private String getCharset(IResource resource) {
        if (resource instanceof IEncodedStorage) {
            try {
                return ((IEncodedStorage)resource).getCharset();
            } catch (CoreException e) {
            }
        }
        return ResourcesPlugin.getEncoding();
    }
    
    private IContainer getContainer() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IPath destPath = root.getLocation().append(dirName);
        File destDir = destPath.toFile();        
        if (!destDir.exists()) {
            destDir.mkdir();
            try {
                return new ContainerGenerator(new Path(dirName)).generateContainer(null);
            } catch (CoreException e) {
    	        System.out.println("OuputTextFile - createDir core err: " + e.getMessage());
            }
        }
        
        return root.getFolder(new Path(dirName));
    }
    
    private IFile getFile(IContainer container) {
        try {            
            container.getLocation().append(filename).toFile().createNewFile();
        } catch (IOException e) {
            System.out.println("OuputTextFile - createFile io err: " + e.getMessage());
        }
        return container.getFile(new Path(filename));
    }
    
    public void delete() {
        if (file.exists()) {
	        try {
	            file.delete(false, null);
	        } catch (CoreException e) {
	            System.out.println("OuputTextStorage - delete err:" + e.getMessage());
	        }
        }
    }
    
    public void append(String text) {
        InputStream input = new BufferedInputStream(new ByteArrayInputStream(text.getBytes()));
        try {
            file.appendContents(input, true, false, null);
        } catch (CoreException e) {
            System.out.println("OuputTextStorage - append err:" + e.getMessage());
        }            
    }
    
    public IDocument createDocument() {
        if (file == null)
            return null;

        IDocument doc = null;
        try {
            InputStream is = getInputStream(file);
            String output = readString(is, getCharset(file));
            doc = new Document(output != null ? output : "");            
        } catch (CoreException e) {
            System.out.println("OuputTextViewer - createDocument err: " + e.getMessage());
        }
        return doc;
    }
    
    public IDocument getDocument(String filename) {
        IWorkbench wb = PlatformUI.getWorkbench();
        if (wb == null)
            return null;
        
        IWorkbenchWindow[] ws = wb.getWorkbenchWindows();
        if (ws == null)
            return null;
        
        FileEditorInput testEditor = new FileEditorInput(getFile(filename));
        
        for (int i=0; i<ws.length; i++) {
            IWorkbenchWindow w = ws[i];
            IWorkbenchPage[] wps = w.getPages();
            if (wps != null) {
                for (int j=0; j<wps.length; j++) {
                    IWorkbenchPage wp = wps[j];
                    IEditorPart ep = wp.findEditor(testEditor);
                    if (ep instanceof ITextEditor) {
                        ITextEditor te = (ITextEditor)ep;
                        IDocumentProvider dp = te.getDocumentProvider();
                        if (dp != null) {
                            IDocument doc = dp.getDocument(ep);
                            if (doc != null)
                                return doc;
                        }
                    }
                }
            }
        }
        return null;
    }
    */
}
