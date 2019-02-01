package server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class HashMapObject implements Serializable {
	public static final long serialVersionUID = 10001L;
	HashMap<String, LinkedHashMap<Integer, Integer>> hashMap;
	HashMap<Integer, String> files;
	int lastFileId;

	HashMap<String, LinkedHashMap<Integer, Integer>> getHashMap() {
		return hashMap;
	}

	HashMap<Integer, String> getFiles() {
		return files;
	}

	int getLastFileId() {
		return lastFileId;
	}

	void increaseLastFileId() {
		++lastFileId;
	}

	public void setHashMap(HashMap<String, LinkedHashMap<Integer, Integer>> hashMap) {
		this.hashMap = hashMap;
	}

	public void setfilesMap(HashMap<Integer, String> files) {
		this.files = files;
	}
}
