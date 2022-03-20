package com.example;

import java.util.Map;
import java.util.TreeMap;

import com.example.FulfillOrder.AcceptOrder;
import com.example.FulfillOrder.GetOrderDetails;
import com.example.FulfillOrder.FulFillOrderCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Delivery extends AbstractBehavior<Delivery.OrderCommand>{
	
	interface OrderCommand {}
	
	int globalOrderId=1000;
	
	public Delivery(ActorContext<OrderCommand> context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public final static class RequestOrder implements OrderCommand {
		public final Order order;
		//public Object replyTo;
		public final ActorRef<ActionPerformed> replyTo;
		//public final ActorRef<AcceptOrder> replyTo;
		/*
		 * public RequestOrder(Order order, ActorRef<ActionPerformed> replyTo) { // TODO
		 * Auto-generated constructor stub this.order = order; this.replyTo = replyTo; }
		 */
		public RequestOrder(Order order, ActorRef<ActionPerformed> replyTo) {
			// TODO Auto-generated constructor stub
			this.order = order;
			this.replyTo = replyTo;
		}
	}
	
	public final static class GetOrder implements OrderCommand {
		public final int orderId;
		public final ActorRef<com.example.FulfillOrder.ActionPerformed> replyTo;
		public GetOrder(int orderId, ActorRef<com.example.FulfillOrder.ActionPerformed> replyTo) {
			// TODO Auto-generated constructor stub
			this.orderId = orderId;
			this.replyTo = replyTo;
		}
	}
	
	public final static class ReInitialize implements OrderCommand {
		public final ActorRef<ActionPerformedReInitialize> replyTo;
		public ReInitialize(ActorRef<ActionPerformedReInitialize> replyTo) {
			this.replyTo = replyTo;
		}
	}
	
	public final static class ActionPerformed implements OrderCommand {
	    public final String description;
	    public final Order order;
	    public ActionPerformed(String description,Order order){
	    	this.description = description;
	    	this.order = order;
	    }
	}
	
	public final static class ActionPerformedReInitialize implements OrderCommand {
	    public final String description;
	    public ActionPerformedReInitialize(String description){
	    	this.description = description;
	    }
	}
	
	public final static class Order{
		int restId;
		int itemId;
		int qty;
		int custId;
		int orderId;
		String status;
		
		
		
		public int getRestId() {
			return restId;
		}



		public void setRestId(int restId) {
			this.restId = restId;
		}



		public int getItemId() {
			return itemId;
		}



		public void setItemId(int itemId) {
			this.itemId = itemId;
		}



		public int getQty() {
			return qty;
		}



		public void setQty(int qty) {
			this.qty = qty;
		}



		public int getCustId() {
			return custId;
		}



		public void setCustId(int custId) {
			this.custId = custId;
		}



		public int getOrderId() {
			return orderId;
		}



		public void setOrderId(int orderId) {
			this.orderId = orderId;
		}



		public String getStatus() {
			return status;
		}



		public void setStatus(String status) {
			this.status = status;
		}



		@JsonCreator
		public Order(@JsonProperty("custId") int custId, @JsonProperty("restId") int restId, 
				@JsonProperty("itemId") int itemId, @JsonProperty("qty") int qty 
			) {
			// TODO Auto-generated constructor stub
			
			this.custId = custId;
			this.restId = restId;
			this.itemId = itemId;
			this.qty = qty;
		}



		@Override
		public String toString() {
			return "Order [restId=" + restId + ", itemId=" + itemId + ", qty=" + qty + ", custId=" + custId
					+ ", orderId=" + orderId + ", status=" + status + "]";
		}
		
	}
	
	@Override
	public String toString() {
		return "Delivery [orders=" + orders + "]";
	}

	public final static class Orders{
		Map<Integer, Order> orders = new TreeMap<>();
		public Orders(Map<Integer, Order> orders) {
			this.orders = orders;
		}
	}
	
	Map<Integer, ActorRef<FulFillOrderCommand>> orders = new TreeMap<>();
	
	
	public static Behavior<OrderCommand> create() {
	    return Behaviors.setup(Delivery::new);
	}
	

	@Override
	public Receive<OrderCommand> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onMessage(RequestOrder.class, this::onRequestOrder)
				.onMessage(GetOrder.class, this::onGetOrder)
				.onMessage(ReInitialize.class, this::onReInitialize)
				.build();
	}
	
	private Behavior<OrderCommand> onRequestOrder(RequestOrder requestOrder) {
		//requestOrder.order.orderId = globalOrderId;
		requestOrder.order.setOrderId(globalOrderId);
		
	    
	    ActorRef<FulFillOrderCommand> act =  getContext().spawn(FulfillOrder.create(), "fulFill" + Integer.toString(globalOrderId, globalOrderId));
	    orders.put(globalOrderId, act);
	    globalOrderId++;
	    act.tell(new AcceptOrder(requestOrder.order));
	    requestOrder.replyTo.tell(new ActionPerformed("Order created", requestOrder.order));
	    return this;
	  }
	
	private Behavior<OrderCommand> onGetOrder(GetOrder getOrder) {
		System.out.println(orders.size());
		if(orders.containsKey(getOrder.orderId)) {
			ActorRef<FulFillOrderCommand> act= orders.get(getOrder.orderId);
			act.tell(new GetOrderDetails(getOrder.replyTo));
		}
		else {
			getOrder.replyTo.tell(new com.example.FulfillOrder.ActionPerformed("Details fetched:- ", "not"));
		}
	    //getOrder.replyTo.tell(new ActionPerformed("Order created", ord));
	    return this;
	  }
	 
	private Behavior<OrderCommand> onReInitialize(ReInitialize reInitialize) {
		//orders.clear();
		globalOrderId = 1000;
		for (Map.Entry<Integer,ActorRef<FulFillOrderCommand> > entry : orders.entrySet()) {
			getContext().stop(entry.getValue());
		}
		orders.clear();
		reInitialize.replyTo.tell(new ActionPerformedReInitialize("reInitialization successfull"));
		return this;
	}

}
