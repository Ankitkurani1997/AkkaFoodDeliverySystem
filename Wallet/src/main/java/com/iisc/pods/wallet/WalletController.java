package com.iisc.pods.wallet;

import java.io.IOException;
import java.util.HashMap;

import com.iisc.pods.wallet.pojo.Customer;
import com.iisc.pods.wallet.service.TransactService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController {
	
	@Autowired
	TransactService transactService;
	private final Object lock = new Object();
	/**
	 * @param requestData (JSON payload of the form {"custId":num, "amount":z})
	 * Increase the balance of custId num by z.
	 * @return HTTP status code 201
	 */
	@PostMapping("/addBalance")
	synchronized public ResponseEntity<Object> addMoney(@RequestBody HashMap<String, Integer> requestData) {
		synchronized (lock) {
			if(transactService.addBal(requestData.get("custId"), requestData.get("amount")))
				return ResponseEntity.status(HttpStatus.CREATED).body(null);
			else
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}
	
	
	/**
	 * @param requestData (JSON payload of the form {"custId":num, "amount":z})
	 * If current balance of custId num is less than z, 
	 * return HTTP status code 410, 
	 * else reduce custId num's balance by z and 
	 * return HTTP status code 201.
	 * @return HTTP status code
	 */
	@PostMapping("/deductBalance")
	synchronized public ResponseEntity<Object> deductMoney(@RequestBody HashMap<String, Integer> requestData) {
		synchronized (lock) {
			int status = transactService.deductBal(requestData.get("custId"), requestData.get("amount"));
			if(status == 1) {
				return ResponseEntity.status(HttpStatus.CREATED).body(null);
			}
			else if(status == 0)
				return ResponseEntity.status(HttpStatus.GONE).body(null);
			else
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}
	
	/**
	 * @param num (An integer ID for the customer whose balance is to be fetched)
	 * Returns the balance of the requested customer and a HTTP status code of 200 if the entered customer ID is valid.
	 * @return JSON : {"custId":num, "balance":z} and HTTP status code of 200
	 */
	@GetMapping("/balance/{num}")
	public ResponseEntity<Customer> getBalance(@PathVariable("num") int num) {
		synchronized (lock) {
			Customer cust = new Customer();
			cust.setCustId(num);
			cust.setBalance(transactService.getBal(num));
			return new ResponseEntity<Customer>(cust, HttpStatus.OK);
		}
	}
	
	
	/**
	 * Set balance of all customers to the initial value as given in the /initialData.txt file.
	 * @return HTTP status code 201
	 * @throws IOException
	 */
	@PostMapping("/reInitialize")
	synchronized public ResponseEntity<Object> reInit() throws IOException {
		synchronized (lock) {
			transactService.freshInitWallet();
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		}
	}

}
