package com.nextlabs.bulkprotect.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.vfs2.FileObject;

public class FileExtensionCriteria implements Predicate<FileObject> {
	
	private Set<String> exts;
	private boolean incl;
	private static final String[] supportedFileExts = new String[] {"doc", "docx", "ppt", "pptx" ,"xls", "xlsx", "vsd", "vsdx", "csv", "docm", "potm", "xlsm" ,"xltm", "dotx", "potx" ,"rtf",
	"xlsb", "xlt" ,"xltx" ,"dot", "ppsx", "mp3", "mp4", "pdf", "txt", "log", "png", "jpg", "bmp", "tif", "tiff", "gif", "jt", "sldprt", "sldasm", "igs", "iges", "stp", "stl", "step", "hsf",
	"par", "psm", "cgr", "catpart", "catshape", "3dxml", "model", "prt", "neu", "x_t", "x_b", "xml_txt", "ipt", "dxf", "dwg", "c", "cpp", "err", "h", "java", "js", "m", "md", "vb", "py", "sql",
	"swift", "xml", "htm", "json", "rh", "vds", "nxl","all"};
	
	public FileExtensionCriteria(Collection<String> extensions, boolean includes) {
		exts = new HashSet<String>(extensions);
		incl = includes;
	}

	public FileExtensionCriteria(String[] fileExtCriteria, boolean incl) { 
		this(Arrays.asList(fileExtCriteria), incl);
	}

	@Override
	public boolean test(FileObject f) {
		boolean allCondition = exts.stream().anyMatch("all"::equalsIgnoreCase);
		if(allCondition || exts.size() == 1 && exts.contains("nxl"))
			return true;
		String[] sp = f.toString().split("\\.");
		String ext = sp[sp.length - 1].toLowerCase();
		boolean match = exts.stream().anyMatch(ext::equalsIgnoreCase);
		return (incl == match) && Arrays.stream(supportedFileExts).anyMatch(ext::equalsIgnoreCase);
	}
}
