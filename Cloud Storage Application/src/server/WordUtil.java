package server;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class WordUtil implements Serializable {
	public static final long serialVersionUID = 10002L;
	private Map<Integer, LinkedList<Integer>> infoMap;

	public WordUtil() {
		infoMap = new LinkedHashMap<Integer, LinkedList<Integer>>();
	}

	public Map<Integer, LinkedList<Integer>> getInfoMap() {
		return infoMap;
	}

	public void setInfoMap(Map<Integer, LinkedList<Integer>> infoMap) {
		this.infoMap = infoMap;
	}
}
