package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReviewHandler {

    private final ReviewReactiveRepository reviewReactiveRepository;
    private final Validator validator;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository, Validator validator) {
        this.reviewReactiveRepository = reviewReactiveRepository;
        this.validator = validator;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);

    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        if (!violations.isEmpty()) {
            String message = violations.stream().map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(message);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var movieRequestId = request.queryParam("movieInfoId").orElse(null);
        if(Objects.nonNull(movieRequestId)) {
            return buildResponseFromFlux(reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieRequestId)));
        }
        return buildResponseFromFlux(reviewReactiveRepository.findAll());
    }

    private Mono<ServerResponse> buildResponseFromFlux(Flux<Review> data){
        return ServerResponse.ok().body(data,Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {

        var reviewId = request.pathVariable("id");

        var existingReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException(reviewId)));

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                ).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {

        var reviewId = request.pathVariable("id");

        var existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId)
                        .then(Mono.defer(() -> ServerResponse.noContent().build())));

    }
}
