package com.iisc.pods.restaurant.pojo;
import java.util.HashMap;


/**
 * 
 * @author sawrush
 * A Java class that corresponds to a restaurant. Each restaurant has the following information :
 * 		1. restId : An unique ID assigned to every restaurant
 * 		2. items : A list of key value pairs. Each key value pair corresponds to one item and its quantity in the restaurant's inventory.
 */

public class Restaurant {
	
	int restId;
	
	HashMap<Integer, Integer> items;

	public int getRestId() {
		return restId;
	}

	public void setRestId(int restId) {
		this.restId = restId;
	}

	public HashMap<Integer, Integer> getItems() {
		return items;
	}

	public void setItems(HashMap<Integer, Integer> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "Restaurant [restId=" + restId + ", items=" + items + "]";
	}

}
