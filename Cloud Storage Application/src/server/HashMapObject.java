package server;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class HashMapObject implements Serializable {
	public static final long serialVersionUID = 10001L;
	LinkedHashMap<String, WordUtil> hashMap;
	LinkedHashMap<Integer, String> files;
	int lastFileId;

	LinkedHashMap<String, WordUtil> getHashMap() {
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

	public void setHashMap(LinkedHashMap<String, WordUtil> hashMap) {
		this.hashMap = hashMap;
	}

	public void setfilesMap(LinkedHashMap<Integer, String> files) {
		this.files = files;
	}
}
