package server;

import java.util.LinkedList;
import java.util.List;

public class FileList {
	List<String> fileNames = new LinkedList<String>();

	public void addFileName(String name) {
		fileNames.add(name);
	}

	public List<String> getFileNames() {
		return fileNames;
	}
	
}
