package com.reactivespring.client;

import com.reactivespring.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;


@Configuration
@RequiredArgsConstructor
public class ReviewRestClient {

    @Value("${restClients.movies.reviews.url}")
    private String reviewsUrl;
    
    private final WebClient webClient;
    
    public Flux<Review> retrieveReview(String movieId){
        String url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();
        return webClient.get().uri(url).retrieve().bodyToFlux(Review.class);
    }
    
    
    
    
}
