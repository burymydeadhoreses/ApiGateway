package io.github.burymydeadhoreses.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class BasicAuthGatewayFilter extends AbstractGatewayFilterFactory<BasicAuthGatewayFilter.Config> {
    private static final String EMPTY_STR = "";
    private static final String COLON_STR = ":";
    private static final String BASIC_STR = "Basic ";

    public BasicAuthGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpMethod method = exchange.getRequest().getMethod();
            String path = exchange.getRequest().getPath().toString();
            if ((HttpMethod.POST.equals(method) || HttpMethod.DELETE.equals(method)) && path.startsWith("/products")) {
                if (exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    String basicAuthValue = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                    basicAuthValue = basicAuthValue != null ? basicAuthValue.replace(BASIC_STR, EMPTY_STR) : EMPTY_STR;
                    basicAuthValue = new String(Base64.getDecoder().decode(basicAuthValue.getBytes(StandardCharsets.UTF_8)));
                    String[] credentials = basicAuthValue.split(COLON_STR);
                    if (credentials.length == 2) {
                        String userName = credentials[0];
                        String password = credentials[1];
                        if (userName.equals("burymydeadhoreses") && password.equals("qwerty")) {
                            return chain.filter(exchange);
                        }
                    }
                }
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
