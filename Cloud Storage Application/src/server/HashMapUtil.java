package server;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HashMapUtil extends Thread {
	HashMapObject hashMapObject;
	HashMap<Integer, String> files;
	HashMap<String, LinkedHashMap<Integer, Integer>> hashMap;
	File hashMapFile;
	String filePath, fileContent;

	public HashMapUtil() {
		hashMapFile = new File("/Users/santhosh-pt2425/Documents/Cloud_Storage_Application/Clients/HashMap.txt");
		if (hashMapFile.exists()) {
			try {
				hashMapObject = (HashMapObject) Utilities.readFile(hashMapFile);
				hashMap = hashMapObject.getHashMap();
				files = hashMapObject.getFiles();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			hashMapObject = new HashMapObject();
			hashMap = new HashMap<String, LinkedHashMap<Integer, Integer>>();
			files = new HashMap<Integer, String>();
		}
	}

	public void setFileInfo(String filePath, String fileContent) {
		this.filePath = filePath;
		this.fileContent = fileContent;
	}

	public void run() {
		int fileId = 0;
		if (hashMapObject.getLastFileId() != 0 && files.containsValue(filePath)) {
			for (Map.Entry<Integer, String> entry : files.entrySet()) {
				if (entry.getValue().equals(filePath)) {
					fileId = entry.getKey();
					break;
				}
			}
		}
		if (fileId == 0) {
			fileId = hashMapObject.getLastFileId() + 1;
			files.put(fileId, filePath);
			hashMapObject.increaseLastFileId();
		}
		String[] words = fileContent.split(" ");
		for (String word : words) {
			addHashMapEntry(word, fileId, true);
		}
	}

	private void addHashMapEntry(String word, int fileId, boolean add) {
		LinkedHashMap<Integer, Integer> valueMap;
		int count = 0;
		if (add) {
			if (hashMap.containsKey(word)) {
				valueMap = (LinkedHashMap<Integer, Integer>) hashMap.get(word);
				count = valueMap.get(fileId);
			} else {
				valueMap = new LinkedHashMap<Integer, Integer>();
			}
			valueMap.put(fileId, count + 1);
			hashMap.put(word, valueMap);
		}
		saveHashMap();
	}

	public String getMap() {
		return hashMap.toString();
	}

	public String findWord(String word) {
		LinkedHashMap<Integer, Integer> valueMap;
		String values = "Present in:";
		valueMap = (LinkedHashMap<Integer, Integer>) hashMap.get(word);
		if (valueMap != null) {
			for (Map.Entry<Integer, Integer> entry : valueMap.entrySet()) {
				values += "\n" + (String) files.get(entry.getKey()) + "\tNo. of times = " + entry.getValue();
			}
		} else {
			values = "Not found";
		}
		return values;
	}

	private void saveHashMap() {
		hashMapObject.setHashMap(hashMap);
		hashMapObject.setfilesMap(files);
		Utilities.writeFile(hashMapFile, hashMapObject);
	}

}
