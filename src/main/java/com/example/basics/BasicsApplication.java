package com.example.basics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.SetPathGatewayFilterFactory;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class BasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicsApplication.class, args);
    }

    @Bean
    RouteLocator gateway(SetPathGatewayFilterFactory ff) {
        var singleRoute = Route.async()
                .id("test-route")
                .filter(new OrderedGatewayFilter(ff.apply(config -> config.setTemplate("/customers")), 1))
                .uri("lb://customers")
                .asyncPredicate(serverWebExchange -> {
                    var uri = serverWebExchange.getRequest().getURI();
                    var path = uri.getPath();
                    var match = path.contains("/customers");
                    return Mono.just(match);
                })
                .build();

        return () -> Flux.just(singleRoute);
    }

    @Bean
    ApplicationListener<RefreshRoutesResultEvent> routeRefreshed() {
        return rre -> {
            System.out.println("route updated");

            var crl = (CachingRouteLocator) rre.getSource();
            Flux<Route> routes = crl.getRoutes();
            routes.subscribe(System.out::println);
        };
    }

//    // web socket - using refresh scope
//
//    private final AtomicBoolean webSocket = new AtomicBoolean(false);
//
//    @Bean
//    @RefreshScope
//    RouteLocator gateway(RouteLocatorBuilder rlb) {
//        var id = "customers";
//
//        if (!this.webSocket.get()) {
//            this.webSocket.set(true);
//            return rlb.routes()
//                    .route(id, routeSpec -> routeSpec
//                            .path("/customers")
//                            .uri("lb://customers"))
//                    .build();
//        } else {
//            return rlb.routes()
//                    .route(id, routeSpec -> routeSpec
//                            .path("/customers")
//                            .filters(fs -> fs.setPath("/ws/customers"))
//                            .uri("lb://customers"))
//                    .build();
//        }
//    }

//    @Bean
//    RouteLocator gateway(RouteLocatorBuilder rlb) {
//        return rlb
//                .routes()
//                .route(routeSpec -> routeSpec
//                        .path("/customers")
//                        .uri("lb://customers/"))
//                .build();
//    }
//
//    @Bean
//    RouteLocator gateway(SetPathGatewayFilterFactory ff) {
//        var singleRoute = Route.async()
//                .id("test-route")
//                .filter(new OrderedGatewayFilter(ff.apply(config -> config.setTemplate("/customers")), 1))
//                .uri("lb://customers")
//                .asyncPredicate(serverWebExchange -> {
//                    var uri = serverWebExchange.getRequest().getURI();
//                    var path = uri.getPath();
//                    var match = path.contains("/customers");
//
//                    return Mono.just(match);
//                })
//                .build();
//
//        return () -> Flux.just(singleRoute);
//    }
//
//    @Bean
//    RouteLocator gateway(RouteLocatorBuilder rlb) {
//        return rlb
//                .routes()
//                .route(routeSpec -> routeSpec
//                        .path("/hello").and().host("*.spring.io")
//                        .filters(gatewayFilterSpec ->
//                                gatewayFilterSpec
//                                        .setPath("/guides"))
//                        .uri("https://spring.io/"))
//                .route("twitter", routeSpec ->
//                        routeSpec
//                                .path("/twitter/**")
//                                .filters(fs ->
//                                        fs.rewritePath(
//                                                "/twitter/(?<handle>.*)",
//                                                "/${handle}"))
//                                .uri("http://twitter.com/@"))
//                .build();
//    }
}
