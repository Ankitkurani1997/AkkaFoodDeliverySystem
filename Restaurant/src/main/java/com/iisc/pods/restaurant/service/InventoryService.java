package com.iisc.pods.restaurant.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.iisc.pods.restaurant.pojo.Restaurant;

@Service
public class InventoryService {
	
	//A set of key value pairs where the key is a restaurant ID and he value is set of key value pairs which store the inventory details of the restaurant.
	HashMap<Integer, HashMap<Integer, Integer> > restaurants = new HashMap<>();
	
	/**
	 * @param restId
	 * @param itemId
	 * @param qty
	 * Updates the inventory if the required quantity is available.
	 * Decrease the quantity of the item by the required quantity.
	 * If the required quantity is not available, return a fail status.
	 * @return Integer that describes the status of the operation.
	 */
	public int acceptOrder(int restId, int itemId, int qty) {
		if(restaurants.containsKey(restId) && restaurants.get(restId).containsKey(itemId)) {
			if(qty <= restaurants.get(restId).get(itemId))
			{
				restaurants.get(restId).put(itemId, restaurants.get(restId).get(itemId) - qty);
				return 1;
			}
			else
				return 0;
		}
		return -1;
	}
	
	
	/**
	 * Inserts key value pairs in the HashMap from the data found in initialData.txt
	 * @throws IOException
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void initRestaurants() throws IOException {
		
		try {
			File file = new File("initialData.txt");
		
			Scanner sc = new Scanner(file);
			
			int count = -1, restId = -1;
			HashMap<Integer, Integer> items = new HashMap<>();
			while(sc.hasNextLine())
			{
				String line = sc.nextLine();
				line = line.strip();
				if(line.contains("****"))
					break;
				if(count==-1) {
					restId = Integer.parseInt(line.split(" ")[0]);
					count = Integer.parseInt(line.split(" ")[1]);
					if(count==0)
						count=-1;
				}
				else if(count>0) {
					items.put(Integer.parseInt(line.split(" ")[0]), Integer.parseInt(line.split(" ")[2]));
					count--;
					if(count==0) {
						restaurants.put(restId, items);
						restId = -1;
						items = new HashMap<>();
						count = -1;
					}		
				}
			}
			sc.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("Initialialization file not found!");
		}
		catch(NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Clear out the key value pairs in the HashMap.
	 * Fill out the key value pairs from initialData.txt
	 * @throws IOException
	 */
	public void freshInitRestaurants() throws IOException {
		restaurants = new HashMap<>();
		this.initRestaurants();
	}

	/**
	 * @param restId
	 * @param itemId
	 * @param qty
	 * Update the quantity of the particular itemId provided as paramter
	 * Add the givwn value in qty to the value of restId and the value of itemId
	 * @return Integer that describes the status of the operation.
	 */
	public int refill(int restId, int itemId, int qty) {
		if(restaurants.containsKey(restId) && restaurants.get(restId).containsKey(itemId)) {
			restaurants.get(restId).put(itemId, restaurants.get(restId).get(itemId) + qty);
			return 1;
		}
		return -1;
	}
	
}
