package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.handler.ExceptionHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, ExceptionHandler.class})
public class ReviewsUnitTest {

    @MockBean
    private ReviewReactiveRepository repository;

    @Autowired
    private WebTestClient webTestClient;
    static String REVIEWS_URL = "/v1/reviews";


    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        when(repository.save(isA(Review.class))).thenReturn(Mono.just(new Review("1", 1L, "Awesome Movie", 9.0)));

        //when

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });
    }

    @Test
    void addReview_validationError() {
        var review = new Review(null, null, "Awesome Movie", -9.0);
        when(repository.save(isA(Review.class))).thenReturn(Mono.just(new Review("1", 1L, "Awesome Movie", 9.0)));

        //when

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class);
    }


}
