package com.example;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.example.Delivery.Order;
import com.example.Delivery.RequestOrder;
import com.example.Delivery.GetOrder;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;

import static akka.http.javadsl.server.Directives.*;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.routing.GetRoutees;
import net.minidev.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import static akka.http.javadsl.server.PathMatchers.integerSegment;

/**
 * Routes can be defined in separated classes like shown in here
 */
//#user-routes-class
public class UserRoutes {
  //#user-routes-class
  private final static Logger log = LoggerFactory.getLogger(UserRoutes.class);
  private final ActorRef<Delivery.OrderCommand> deliveryActor;
  private final Duration askTimeout;
  private final Scheduler scheduler;

  public UserRoutes(ActorSystem<?> system, ActorRef<Delivery.OrderCommand> deliveryActor) {
    this.deliveryActor = deliveryActor;
    scheduler = system.scheduler();
    askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");

  }

  private CompletionStage<Delivery.ActionPerformed> createOrder(Order order) {
	    return AskPattern.ask(deliveryActor, ref -> new Delivery.RequestOrder(order, ref), askTimeout, scheduler);
  }
  
  private CompletionStage<FulfillOrder.ActionPerformed> getOrder(int order) {
	    return AskPattern.ask(deliveryActor, ref-> new Delivery.GetOrder(order, ref), askTimeout, scheduler);
  }
  
  private CompletionStage<Delivery.ActionPerformedReInitialize> reInitialize() {
	    return AskPattern.ask(deliveryActor, ref-> new Delivery.ReInitialize(ref), askTimeout, scheduler);
  }
  /**
   * This method creates one route (of possibly many more that will be part of your Web App)
   */
  //#all-routes
  public Route orderRoutes() {
    return concat(pathPrefix("requestOrder", () ->
        concat(
            pathEnd(() ->
                concat(
                    post(() ->
                        entity(
                            Jackson.unmarshaller(Order.class),
                            order ->
                            	onSuccess(createOrder(order), ord -> {
                            		log.info(Integer.toString(ord.order.getOrderId()));
                            		return complete(StatusCodes.CREATED, ord.order.orderId, Jackson.marshaller());
                            	})
                            )
                        )
                    )
                )
            )
            //#users-get-post
			),
    		get(() ->
    		pathPrefix("order", () ->
    		path(integerSegment(), (Integer orderId) -> onSuccess(getOrder(orderId), performed -> {
    		JSONObject entity = new JSONObject();
    		if(performed.status.equalsIgnoreCase("not")) {
    			return complete(StatusCodes.NOT_FOUND, entity, Jackson.marshaller());
    		}
    		entity.appendField("orderId", orderId );
    		entity.appendField("status", performed.status);	
    		String res = "{\"orderId\":\"" + orderId + "\", \"orderStatus\":\"" + performed.status + "\"}";
    		System.out.println(res);
    		return complete(StatusCodes.CREATED, entity, Jackson.marshaller());
    		})))),
    		post(() ->
    		pathPrefix("reInitialize", () ->
    		onSuccess(reInitialize(), performed -> {
    			JSONObject entity = new JSONObject();
    			return complete(StatusCodes.CREATED, entity,Jackson.marshaller());
    		}
    		)))
    );
    
     
  }
  //#all-routes

}
