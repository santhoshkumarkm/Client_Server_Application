package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.TimerTask;

public class AddWordsTask extends TimerTask {

	static FileList fileList = new FileList();
	File file = new File("/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/NewFiles.txt");
	HashMapUtil hashMapUtil = new HashMapUtil();

	AddWordsTask() {
		if (file.exists()) {
			fileList = (FileList) Utilities.readFile(file);
		}
	}

	public static FileList getFileList() {
		return fileList;
	}

	@Override
	public void run() {
		String filePath = null, fileContent = null;
		if (fileList.getFileNames().size() != 0) {
			try {
				filePath = fileList.getFileNames().iterator().next();
				fileContent = Utilities.stringBuilder(
						new BufferedReader(new FileReader(new File(HTTPServer.defaultLocation + filePath))));
				hashMapUtil.addWords(filePath, fileContent);
				fileList.getFileNames().remove(filePath);
				Utilities.writeFile(file, fileList);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

}
