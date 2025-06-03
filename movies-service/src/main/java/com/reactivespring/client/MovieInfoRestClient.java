package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;


@Component
@RequiredArgsConstructor
public class MovieInfoRestClient {

    @Value("${restClients.movies.info.url}")
    private       String    moviesInfoUrl;
    private final WebClient webClient;

    public Mono<MovieInfo> retrieveMovieInfoById(String movieInfoId) {
        var retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(1)).onRetryExhaustedThrow(
                    (retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));
        String url = moviesInfoUrl.concat("/{id}");
        return webClient.get().uri(url, movieInfoId).retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                        if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            return Mono.error(new MoviesInfoClientException("MovieInfo Not Found",
                                        clientResponse.statusCode().value()));
                        }
                        return clientResponse.bodyToMono(String.class).flatMap(errorBody -> Mono.error(
                                    new MoviesInfoClientException(errorBody, clientResponse.statusCode().value())));
                    }).bodyToMono(MovieInfo.class).retryWhen(retrySpec);
    }
}
