package rainbownlp.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public class ExtensionFilter implements FilenameFilter, FileFilter {
	  private String extension;
	  public ExtensionFilter( String extension ) {
	    this.extension = extension;             
	  }
	  
	  @Override
	public boolean accept(File dir, String name) {
	    return (name.endsWith(extension));
	  }

	@Override
	public boolean accept(File file) {
		return file.toString().endsWith(extension);
	}
}