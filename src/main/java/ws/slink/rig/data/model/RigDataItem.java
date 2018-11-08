package ws.slink.rig.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ws.slink.rig.data.RedisDAO;

public abstract class RigDataItem {
	
	private int updateTimeout = 30; // data invalidation period (seconds) 
	
	private String redisKeyTemplate = "";
	
	private Map<String, Long> updateTimestamps = new HashMap<>();
	private List<List<String>> tagsByIndex;
	private List<String> fields;
	
	public RigDataItem(String redisKeyTemplate) {
		this.redisKeyTemplate = redisKeyTemplate;
	}
	
	public List<String> fields() {
		// update from redis if needed
		if (fields == null || fields.isEmpty() || 
		    (updateTimestamps.get("fields") != null && 
		     System.currentTimeMillis() > updateTimestamps.get("fields") + updateTimeout * 1000)) {
			List<String> keys  = RedisDAO.instance().keys(redisKeyTemplate + ".*");
			Set<String> fields = new HashSet<>();
			for (String k : keys) {
				fields.add(k.split(":")[0].replace(redisKeyTemplate + ".", ""));
			}
			List<String> result = new ArrayList<>(fields);
			Collections.sort(result);
			updateTimestamps.put("fields", System.currentTimeMillis());
			this.fields = result;
		}
		// return locally stored values
		return this.fields;
	}
	
	private void updateTags() {
		// update from redis if needed
		if (tagsByIndex == null || tagsByIndex.isEmpty() || 
			(updateTimestamps.get("tags") != null && 
			 System.currentTimeMillis() > updateTimestamps.get("tags") + updateTimeout * 1000)) {

			List<String> allTags = RedisDAO.instance().keys(redisKeyTemplate + ".*", -1, true);

			Map<Integer, Set<String>> resultMap = new HashMap<>();
			
			for(String tagsStr : allTags) {
				String [] tagsArray = tagsStr.split("\\.");
				for (int i = 0; i < tagsArray.length; i++) {
					if (resultMap.get(i) == null) resultMap.put(i, new HashSet<>());
					resultMap.get(i).add(tagsArray[i].trim().toLowerCase());
				}
			}
			
			List<List<String>> result = new ArrayList<>();
			for (int k : resultMap.keySet()) {
				List<String> l = new ArrayList<>(resultMap.get(k));
				Collections.sort(l);
				result.add(l);
			}
			
			this.tagsByIndex = result;
		}
	}
	public List<String> tags(int idx) {
		// update from redis if needed
		this.updateTags();
		
		// throw error if index exceeds number of tags
		if (idx >= tagsByIndex.size())
			throw new IllegalArgumentException("index value exceeds number of tags");

		// return locally stored values
		return tagsByIndex.get(idx);
	}

	public List<List<String>> tags() {
		// update from redis if needed
		this.updateTags();
		// return locally stored values
		return tagsByIndex;
	}

	public Double value(String field, String tag1, String tag2, String tag3) {
		String key = redisKeyTemplate + "." + field + ":" +  tag1 + "." + tag2 + "." + tag3;
		return RedisDAO.instance().getDouble(key);
	}
	public Double value(String field, String tag1, String tag2) {
		String key = redisKeyTemplate + "." + field + ":" +  tag1 + "." + tag2;
		return RedisDAO.instance().getDouble(key);
	}
	public Double value(String field, String tags) {
		String key = redisKeyTemplate + "." + field + ":" +  tags;
		return RedisDAO.instance().getDouble(key);
	}
	
}
