package com.iisc.pods.wallet.service;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import com.iisc.pods.wallet.pojo.Customer;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TransactService {
	
	//A set of key value pairs where the key is an customer ID and value is amount.
	HashMap<Integer, Integer> customers = new HashMap<>();
	
	/**
	 * @param custId
	 * @param amount
	 * Updates the key value pair where key is the given customer ID and the value is increased by the parameter amount.
	 * @return true or false
	 */
	public boolean addBal(int custId, int amount) {
		if(customers.containsKey(custId)) {
			customers.put(custId, customers.get(custId)+amount);
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param custId
	 * @param amount
	 * Updates the key value pair where key is the given customer ID and the value is decreased by the parameter amount.
	 * @return Integer based on the update status
	 */
	public int deductBal(int custId, int amount) {
		if(customers.containsKey(custId) && customers.get(custId) >= amount) {
			customers.put(custId, customers.get(custId)-amount);
			return 1;
		}
		else if(customers.get(custId) < amount)
			return 0;
		return -1;
	}
	
	
	/**
	 * @param custId
	 * Fetch the balance of the customer with the given customer ID.
	 * @return Integer based on the fetch status
	 */
	public int getBal(int custId) {
		if(customers.containsKey(custId))
			return customers.get(custId);
		return -1;
	}
	
	
	/**
	 * Inserts key value pairs in the HashMap from the data found in initialData.txt
	 * @throws IOException
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void initWallet() throws IOException {
		
		try {
			File file = new File("initialData.txt");
		
			Scanner sc = new Scanner(file);
			
			int skip_count = 3, wallet_bal = 0;
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				line = line.strip();
				if(line.contains("****") && skip_count>0)
					skip_count--;
				else if(skip_count==0)
					wallet_bal = Integer.parseInt(line);
			}
			System.out.println(wallet_bal);
			
			sc.close();
			sc = new Scanner(file);
			skip_count = 2;
			
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				if(line.contains("****") && skip_count>0)
					skip_count--;
				else if(skip_count==0 && line.contains("****"))
					break;
				else if(skip_count==0) {
					int custId = Integer.parseInt(line);
					customers.put(custId, wallet_bal);
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
	public void freshInitWallet() throws IOException {
		customers = new HashMap<>();
		this.initWallet();
	}
	
}
