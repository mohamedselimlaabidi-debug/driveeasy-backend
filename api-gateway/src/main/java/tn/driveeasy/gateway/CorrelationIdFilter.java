package tn.driveeasy.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Filtre personnalisé : attribue un identifiant de corrélation à chaque requête
 * (ou réutilise celui du client) et journalise le statut et la durée.
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    static final String HEADER = "X-Correlation-Id";
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String recu = exchange.getRequest().getHeaders().getFirst(HEADER);
        String id = (recu == null || recu.isBlank()) ? UUID.randomUUID().toString() : recu;
        long debut = System.currentTimeMillis();

        // La requête étant immuable, on en construit une copie avec l'en-tête
        ServerWebExchange modifie = exchange.mutate()
                .request(r -> r.header(HEADER, id))
                .build();
        modifie.getResponse().getHeaders().set(HEADER, id);

        return chain.filter(modifie).doFinally(signal -> log.info("[{}] {} {} -> {} en {} ms",
                id,
                modifie.getRequest().getMethod(),
                modifie.getRequest().getPath(),
                modifie.getResponse().getStatusCode(),
                System.currentTimeMillis() - debut));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
