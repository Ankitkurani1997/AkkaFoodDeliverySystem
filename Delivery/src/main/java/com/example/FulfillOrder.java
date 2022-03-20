package com.example;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.Delivery.Order;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import net.minidev.json.JSONObject;

public class FulfillOrder extends AbstractBehavior<FulfillOrder.FulFillOrderCommand>{
	
	private String status = "";
	
	private final String URI_WALLET_DEDUCTBALANCE = "http://localhost:8082/deductBalance";
	private final String URI_WALLET_ADDBALANCE = "http://localhost:8082/addBalance";
	private final String URI_RESTAURANT = "http://localhost:8081/acceptOrder";
	
	public FulfillOrder(ActorContext<FulFillOrderCommand> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	interface FulFillOrderCommand {}
	
	
	public static Behavior<FulFillOrderCommand> create() {
	    return Behaviors.setup(FulfillOrder::new);
	}
	
	public final static class AcceptOrder implements FulFillOrderCommand {
		public final Order order;
		//public final ActorRef<ActionPerformed> replyTo;
		public AcceptOrder(Order order) {
			// TODO Auto-generated constructor stub
			this.order = order;
			//this.replyTo = replyTo;
		}
	}
	
	public final static class GetOrderDetails implements FulFillOrderCommand {
		public final ActorRef<ActionPerformed> replyTo;
		public GetOrderDetails(ActorRef<ActionPerformed> replyTo) {
			// TODO Auto-generated constructor stub
			this.replyTo = replyTo;
		}
	}
	
	public final static class ActionPerformed implements FulFillOrderCommand {
	    public final String description;
	    public final String status;
	    public ActionPerformed(String description, String status){
	    	this.description = description;
	    	this.status = status;
	    }
	}
	
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public Receive<FulFillOrderCommand> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onMessage(AcceptOrder.class, this::onAcceptOrder)
				.onMessage(GetOrderDetails.class, this::onGetOrderDetails)
				.build();
	}	
	
	private Behavior<FulFillOrderCommand> onAcceptOrder(AcceptOrder acceptOrder){
		acceptOrder.order.status = "unassigned";
		setStatus("unassigned");
		int x = completeCommunication(acceptOrder.order);
		System.out.println(acceptOrder.order.custId);
		System.out.println(x);
		return this;
	}
	
	private Behavior<FulFillOrderCommand> onGetOrderDetails(GetOrderDetails getOrderDetails){
		/*
		 * Order ord = giveOrderDetails.order int x =
		 * completeCommunication(acceptOrder.order);
		 * System.out.println(acceptOrder.order.custId); System.out.println(x);
		 */
		getOrderDetails.replyTo.tell(new ActionPerformed("Details fetched:- ", status));
		return this;
	}
	
	public int computeTotalBill(int restId, int itemId, int qty) {
		int total = 0;
		List<Item> items = QuickstartApp.restaurants.get(restId);
		for(int i=0;i<items.size();i++)
		{
			if(items.get(i).getItemId() == itemId)
			{
				total = qty * items.get(i).getPrice();
				break;
			}
		}
		return total;
	}
	
	private int completeCommunication(Order ord) {
		
		int totalBill = computeTotalBill(ord.getRestId(), ord.getItemId(), ord.getQty());
		System.out.println(totalBill);
		System.out.println(ord.getOrderId());
		int flag = 0;
		JSONObject entityWallet = null;
		RestTemplate restTemplate = null;
		HttpEntity<Object> httpEntityWallet = null;
		HttpEntity<String> httpEntityRestaurant = null;
		HttpStatus responseRestaurant = null;
		
		try {
		
			restTemplate = new RestTemplate();
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			entityWallet = new JSONObject();
			entityWallet.appendField("custId", ord.getCustId());
			entityWallet.appendField("amount", totalBill);
			
			httpEntityWallet = new HttpEntity<Object>(entityWallet.toString(), headers);
			restTemplate.exchange(URI_WALLET_DEDUCTBALANCE, HttpMethod.POST, httpEntityWallet, Object.class);
			flag = 1;
			
			JSONObject entityRestaurant = new JSONObject();
			entityRestaurant.appendField("restId", ord.getRestId());
			entityRestaurant.appendField("itemId", ord.getItemId());
			entityRestaurant.appendField("qty", ord.getQty());
			
			httpEntityRestaurant = new HttpEntity<String>(entityRestaurant.toString(), headers);
			responseRestaurant = restTemplate.exchange(URI_RESTAURANT, HttpMethod.POST, httpEntityRestaurant, String.class).getStatusCode();
			
			int id = ord.getOrderId();
			
			if(responseRestaurant == HttpStatus.CREATED) {
				ord.setStatus("delivered");
				setStatus("delivered");
				return id;
			}
			else
			{
				return -1;
			}
		}
		catch (HttpClientErrorException e) {
			if(flag == 0) {
				return -1;
			}
			else {
				try {
					restTemplate.exchange(URI_WALLET_ADDBALANCE, HttpMethod.POST, httpEntityWallet, Object.class);
					return -1;
				}
				catch(HttpClientErrorException exception) {
					return -1;
				}
			}
		}
		catch (Exception e) {
			if(flag==0)
				System.out.println("Issue in deducting balance from wallet");
			else
				System.out.println("Issue in placing order");
			return -1;
		}
		
	}
	
	
}