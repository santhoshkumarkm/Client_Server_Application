package server;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class HashMapObject implements Serializable {
	public static final long serialVersionUID = 10001L;
	LinkedHashMap<String, LinkedList<LinkedList<Integer>>> hashMap;
	LinkedHashMap<Integer, String> files;
	int lastFileId;

	LinkedHashMap<String, LinkedList<LinkedList<Integer>>> getHashMap() {
		return hashMap;
	}

	LinkedHashMap<Integer, String> getFiles() {
		return files;
	}

	int getLastFileId() {
		return lastFileId;
	}

	void increaseLastFileId() {
		++lastFileId;
	}

	public void setHashMap(LinkedHashMap<String, LinkedList<LinkedList<Integer>>> hashMap) {
		this.hashMap = hashMap;
	}

	public void setfilesMap(LinkedHashMap<Integer, String> files) {
		this.files = files;
	}
}
