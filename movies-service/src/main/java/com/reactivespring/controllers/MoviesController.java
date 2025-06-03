package com.reactivespring.controllers;

import com.reactivespring.client.MovieInfoRestClient;
import com.reactivespring.client.ReviewRestClient;
import com.reactivespring.domain.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
public class MoviesController {

    private final MovieInfoRestClient movieInfoRestClient;
    private final ReviewRestClient    reviewRestClient;

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable String id) {
        return movieInfoRestClient.retrieveMovieInfoById(id).flatMap(movieInfo -> {
            var review = reviewRestClient.retrieveReview(id).collectList();
            return review.map(reviews -> new Movie(movieInfo, reviews));
        });

    }

}
