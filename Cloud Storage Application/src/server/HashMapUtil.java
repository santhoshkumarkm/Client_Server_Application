package server;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class HashMapUtil {
	HashMapObject hashMapObject;
	LinkedHashMap<Integer, String> files;
	LinkedHashMap<String, WordUtil> hashMap;
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
			hashMap = new LinkedHashMap<String, WordUtil>();
			files = new LinkedHashMap<Integer, String>();
		}
	}
//
//	public void setFileInfo(String filePath, String fileContent) {
//		this.filePath = filePath;
//		this.fileContent = fileContent;
//	}

	public void addWords(String filePath, String fileContent) {
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
			addHashMapEntry(i, words[i], fileId);
		}
	}

	private void addHashMapEntry(int position, String word, int fileId) {
		WordUtil wordUtil = new WordUtil();
		if (hashMap.containsKey(word)) {
			wordUtil = hashMap.get(word);
			for (int i = 0; i < wordUtil.getInfoMap().size(); i++) {
				if (wordUtil.getInfoMap().containsKey(fileId)) {
					if (!wordUtil.getInfoMap().get(fileId).contains(position)) {
						wordUtil.getInfoMap().get(fileId).add(position);
					}
				} else {
					LinkedList<Integer> list = new LinkedList<Integer>();
					list.add(position);
					wordUtil.getInfoMap().put(fileId, list);
				}
			}
		} else {
			LinkedList<Integer> list = new LinkedList<Integer>();
			list.add(position);
			wordUtil.getInfoMap().put(fileId, list);
		}
		hashMap.put(word, wordUtil);
		saveHashMap();
	}

	LinkedHashMap<Integer, ArrayList<Integer>> fileAndPosition = new LinkedHashMap<Integer, ArrayList<Integer>>();

	public LinkedHashMap<Integer, ArrayList<Integer>> findWord(String[] words) {
		if (findMultiWordsImpl(false, words, 0, new ArrayList<Integer>(), new ArrayList<Integer>())) {
			return fileAndPosition;
		}
		return null;
	}

	private boolean findMultiWordsImpl(boolean flag, String[] words, int index, ArrayList<Integer> tempPositions,
			ArrayList<Integer> tempFiles) {
		WordUtil wordUtil = hashMap.get(words[index]);
		if (wordUtil == null) {
			return false;
		}
		if (index == 0) {
			for (Map.Entry<Integer, LinkedList<Integer>> entry : wordUtil.getInfoMap().entrySet()) {
				ArrayList<Integer> tempList = new ArrayList<Integer>();
				tempList.addAll(entry.getValue());
				fileAndPosition.put(entry.getKey(), tempList);
			}
		} else {
			LinkedHashMap<Integer, ArrayList<Integer>> fileAndPositionCopy = fileAndPosition;
			for (Map.Entry<Integer, ArrayList<Integer>> entry : fileAndPositionCopy.entrySet()) {
				tempPositions.clear();
				tempFiles.clear();
				fileAndPosition.clear();
				if (wordUtil.getInfoMap().get(entry.getKey()) != null) {
					ArrayList<Integer> tempPosListConfirm = new ArrayList<Integer>();
					ArrayList<Integer> tempPosList = entry.getValue();
					for (int i = 0; i < tempPosList.size(); i++) {
						int val = tempPosList.get(i);
						if (wordUtil.getInfoMap().get(entry.getKey()).contains(val + 1)) {
							tempPosListConfirm.add(val + 1);
						}
					}
					if (tempPosListConfirm.size() > 0) {
						fileAndPosition.put(entry.getKey(), tempPosListConfirm);
					}
				}
			}
		}
		if (fileAndPosition.size() > 0 && index + 1 != words.length) {
			flag = findMultiWordsImpl(flag, words, index + 1, tempPositions, tempFiles);
		} else if (fileAndPosition.size() > 0 && index + 1 == words.length)
			return true;
		return flag;
	}

	private void saveHashMap() {
		hashMapObject.setHashMap(hashMap);
		hashMapObject.setfilesMap(files);
		Utilities.writeFile(hashMapFile, hashMapObject);
	}

}
