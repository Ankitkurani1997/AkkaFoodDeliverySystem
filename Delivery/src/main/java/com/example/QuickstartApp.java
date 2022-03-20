package com.example;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.ActorSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletionStage;

//#main-class
public class QuickstartApp {
    // #start-http-server
	
	static HashMap<Integer, List<Item> > restaurants = new HashMap<>(); 
	
    static void startHttpServer(Route route, ActorSystem<?> system) {
        CompletionStage<ServerBinding> futureBinding =
            Http.get(system).newServerAt("localhost", 8080).bind(route);

        futureBinding.whenComplete((binding, exception) -> {
            if (binding != null) {
                InetSocketAddress address = binding.localAddress();
                system.log().info("Server online at http://{}:{}/",
                    address.getHostString(),
                    address.getPort());
            } else {
                system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
                system.terminate();
            }
        });
    }
    // #start-http-server
	
	public static void initializeData() throws IOException
	{
		File file;
		try {
			file = new File("initialData.txt");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String pattern = "****";
			String line;
			int counter = 0;
			int restId = 0;
			while((line = bufferedReader.readLine())!=null) {
				line = line.strip();
				if(line.equalsIgnoreCase(pattern)) {
					counter++;
				}
				else
				{
					if(counter == 0)
					{
						String[] str = line.split(" ");
						if(str.length == 2)
						{
							restId = Integer.parseInt(str[0]);
						}
						else
						{
							int itemId = Integer.parseInt(str[0]), price = Integer.parseInt(str[1]);
							addRestaurant(restId, itemId, price);
						}
					}
				}
			}
			
		bufferedReader.close();
		fileReader.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("Initialialization file not found!");
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void addRestaurant(int restId, int itemId, int price)
	{
		Item item  = new Item();
		item.setItemId(itemId);
		item.setPrice(price);
		if(restaurants.containsKey(restId))
		{
			List<Item> lst = restaurants.get(restId);
			lst.add(item);
			restaurants.put(restId, lst);
		}
		else
		{
			List<Item> lst = new ArrayList<>();
			lst.add(item);
			restaurants.put(restId, lst);
		}
	}

    public static void main(String[] args) throws Exception {
        //#server-bootstrapping
    	initializeData();
        Behavior<NotUsed> rootBehavior = Behaviors.setup(context -> {
            ActorRef<Delivery.OrderCommand> deliveryActor =
                context.spawn(Delivery.create(), "Order");

            
            UserRoutes userRoutes = new UserRoutes(context.getSystem(), deliveryActor);
            startHttpServer(userRoutes.orderRoutes(), context.getSystem());
            
            return Behaviors.empty();
        });

        // boot up server using the route as defined below
        ActorSystem.create(rootBehavior, "HelloAkkaHttpServer");
        //#server-bootstrapping
    }

}
//#main-class


