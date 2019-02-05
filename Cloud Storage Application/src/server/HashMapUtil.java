package server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HashMapUtil extends Thread {
	HashMapObject hashMapObject;
	LinkedHashMap<Integer, String> files;
	LinkedHashMap<String, LinkedList<LinkedList<Integer>>> hashMap;
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
			hashMap = new LinkedHashMap<String, LinkedList<LinkedList<Integer>>>();
			files = new LinkedHashMap<Integer, String>();
		}
	}

	public void setFileInfo(String filePath, String fileContent) {
		this.filePath = filePath;
		this.fileContent = fileContent;
	}

	synchronized public void run() {
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
		for (int i = 0; i < words.length; i++) {
			addHashMapEntry(i, words[i], fileId, true);
		}
	}

	synchronized private void addHashMapEntry(int position, String word, int fileId, boolean add) {
		LinkedList<LinkedList<Integer>> valueList = new LinkedList<LinkedList<Integer>>();
		int prevId = 0;
		if (add) {
			if (hashMap.containsKey(word)) {
				for (int i = 0; i < hashMap.get(word).size(); i++) {
					LinkedList<Integer> tempList = hashMap.get(word).get(i);
					prevId = tempList.get(0);
					if (prevId == fileId) {
						tempList.set(1, (tempList.get(1) + 1));
						int positionIndex = isPresent(tempList, position);
						if (positionIndex != 0) {
							tempList.add(positionIndex, position);
						}
						valueList.add(i, tempList);
					} else {
						valueList.add(new LinkedList<Integer>(Arrays.asList(fileId, 1, position)));
					}
				}
			} else {
				valueList.add(new LinkedList<Integer>(Arrays.asList(fileId, 1, position)));
			}
			hashMap.put(word, valueList);
			saveHashMap();
		}
	}

	private int isPresent(LinkedList<Integer> tempList, int position) {
		boolean loopEntry = true;
		int returnVal = 0;
		for (int i = 2; i < tempList.size(); i++) {
			if (position == tempList.get(i)) {
				return 0;
			}
			if (loopEntry && position < tempList.get(i)) {
				returnVal = i;
				loopEntry = false;
			}
		}
		return returnVal;
	}

	public String getMap() {
		return hashMap.toString();
	}

	public String findWord(String word) {
		List<Integer> fileList = new ArrayList<Integer>();
		List<Integer> timesList = new ArrayList<Integer>();
		String values = "Present in:";
		int count = 0, fileId = 0;
		for (Entry<String, LinkedList<LinkedList<Integer>>> entry : hashMap.entrySet()) {
			if (((String) entry.getKey()).matches(".*" + word + ".*")) {
				for (int i = 0; i <= entry.getKey().length() - word.length(); i++) {
					if (entry.getKey().substring(i, i + word.length()).equalsIgnoreCase(word)) {
						count++;
					}
				}

				for (int i = 0; i < entry.getValue().size(); i++) {
					LinkedList<Integer> temp = new LinkedList<Integer>();
					temp.addAll(entry.getValue().get(i));
					fileId = temp.get(0);
					if (fileList.contains(fileId)) {
						int index = fileList.indexOf(fileId);
						timesList.set(index, timesList.get(index) + 1);
					} else {
						fileList.add(fileId);
						timesList.add(count);
					}
				}
			}
		}

		for (int i = 0; i < fileList.size(); i++) {
			values += "\n" + (String) files.get(fileList.get(i)) + "\tNo. of times = " + timesList.get(i);
		}
		return values.length() > "Present in:".length() ? values : "Not found";

	}

	public String findMultiWords(String[] words) {
		String values = "Present in:";
		int fileId = 0;
		LinkedList<LinkedList<Integer>> temp = null, prev = new LinkedList<LinkedList<Integer>>();
		int h = 0;
//		outer: for (; h < words.length; h++) {
		outer: for (Entry<String, LinkedList<LinkedList<Integer>>> entry : hashMap.entrySet()) {
			if (h == words.length) {
				break;
			}

			if (((String) entry.getKey()).equalsIgnoreCase(words[h])) {
				h++;
				temp = entry.getValue();

				if (prev.size() == 0) {
					prev.addAll(temp);
					continue outer;

				}
				for (int i = 0; i < temp.size(); ++i) {
					for (int j = 0; j < prev.size(); j++) {
						if (temp.get(i).get(0) == prev.get(j).get(0)) {
							for (int k = 2; k < temp.get(i).size(); k++) {
								for (int l = 2; l < prev.get(j).size(); ++l) {
									if (temp.get(i).get(k) == prev.get(j).get(l) + 1
											|| temp.get(i).get(k) == prev.get(j).get(l) - 1) {
										prev.clear();
										prev.addAll(temp);
										fileId = temp.get(i).get(0);
										continue outer;
									}
								}
							}
						} else {
							break outer;
						}
					}
				}
			}

		}

		if (h == words.length) {
			return values + "\n" + (String) files.get(fileId);
		}
		return "Not found";
	}

	private void saveHashMap() {
		hashMapObject.setHashMap(hashMap);
		hashMapObject.setfilesMap(files);
		Utilities.writeFile(hashMapFile, hashMapObject);
	}

}
