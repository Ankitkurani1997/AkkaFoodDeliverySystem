package com.iisc.pods.wallet.pojo;


/**
 * 
 * @author sawrush
 * A Java class that corresponds to a customer. Each customer has the following information :
 * 		1. custId : An unique ID assigned to every Customer
 * 		2. balance : The amount currently available in the customer's wallet.
 */
public class Customer {
	
	int custId, balance;

	public int getCustId() {
		return custId;
	}

	public void setCustId(int custId) {
		this.custId = custId;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}


}
