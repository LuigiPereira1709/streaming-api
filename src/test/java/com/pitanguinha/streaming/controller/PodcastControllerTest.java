package com.pitanguinha.streaming.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.pitanguinha.streaming.dto.podcast.*;
import com.pitanguinha.streaming.enums.media.SearchType;
import com.pitanguinha.streaming.enums.media.podcast.Category;
import com.pitanguinha.streaming.exceptions.domain.NotFoundException;
import com.pitanguinha.streaming.service.media.PodcastService;

import reactor.core.publisher.*;

import static com.pitanguinha.streaming.util.test.creator.media.podcast.PodcastDtoCreator.createSuccessDto;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(PodcastController.class)
@TestPropertySource(properties = "spring.application.services.podcast.enabled=true")
public class PodcastControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    PodcastService podcastService;

    PodcastSuccessDto successDto = createSuccessDto();

    @Test
    @DisplayName("When generating a signed URL for podcast content, then it should return mono of string URL")
    void getContentSignedUrl_ReturnsMonoStringUrl() {
        String id = "42";
        String expectedUrl = "https://example.com/signed-url";
        when(podcastService.getContentSignedUrl(id)).thenReturn(Mono.just(expectedUrl));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/podcast/content")
                        .queryParam("id", id)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("When trying to generate a signed URL for podcast content with invalid ID, then it should return bad request")
    void getContentSignedUrl_InvalidId_ReturnsBadRequest() {
        String invalidId = "invalid-id";
        when(podcastService.getContentSignedUrl(invalidId))
                .thenReturn(Mono.error(
                        new NotFoundException("Podcast not found", "Podcast with ID 'invalid-id' does not exist.")));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/podcast/content")
                        .queryParam("id", invalidId)
                        .build())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").exists()
                .jsonPath("$.details.errorMessage").exists();
    }

    @Test
    @DisplayName("When posting a new podcast (valid), then it should return mono of success response")
    void save_ReturnsMonoSuccessResponse() {
        when(podcastService.save(any(PodcastPostDto.class)))
                .thenReturn(Mono.just(successDto));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("title", "title");
        builder.part("presenter", "presenter");
        List.of("guest_0", "guest_1").forEach(guest -> builder.part("guests", guest));
        builder.part("description", "description");
        List.of("Education", "Technology").forEach(category -> builder.part("categories", category));
        builder.part("episodeNumber", "1");
        builder.part("seasonNumber", "1");
        builder.part("year", "2003");
        builder.part("explicit", "true");

        // Mock files to upload
        builder.part("thumbnailFile", new ByteArrayResource("fake-image".getBytes()) {
            @Override
            public String getFilename() {
                return "thumbnail.png";
            }
        });

        builder.part("contentFile", new ByteArrayResource("fake-content".getBytes()) {
            @Override
            public String getFilename() {
                return "music.mp3";
            }
        });

        webTestClient.post()
                .uri("/podcast")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PodcastSuccessDto.class);
    }

    @Test
    @DisplayName("When updating a podcast (valid), then it should return mono of success response")
    void update_ReturnsMonoSuccessResponse() {
        when(podcastService.update(any(PodcastPutDto.class)))
                .thenReturn(Mono.just(successDto));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("id", "42");

        webTestClient.put()
                .uri("/podcast")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PodcastSuccessDto.class);
    }

    @Test
    @DisplayName("When deleting a podcast (valid), then it should return no content response")
    void delete_ReturnsNoContentResponse() {
        String id = "42";
        when(podcastService.delete(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/podcast/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("When finding a podcast by ID (valid), then it should return mono of success response")
    void findById_ReturnsMonoSuccessResponse() {
        String id = "42";
        when(podcastService.findById(id)).thenReturn(Mono.just(successDto));

        webTestClient.get()
                .uri("/podcast/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PodcastSuccessDto.class);
    }

    @Test
    @DisplayName("When searching podcasts by guests (valid), then it should return flux of success responses")
    void findByGuestsIn_ReturnsFluxSuccessResponses() {
        String[] guests = { "guest_0", "guest_1" };
        when(podcastService.findByAnyString(eq(SearchType.GUESTS_IN), eq(guests)))
                .thenReturn(Flux.just(successDto, successDto, successDto));

        webTestClient.post()
                .uri("/podcast/search/by-guests")
                .bodyValue(guests)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PodcastSuccessDto.class).hasSize(3);
    }

    @Test
    @DisplayName("When searching podcasts by categories (valid), then it should return flux of success responses")
    void findByCategoriesIn_ReturnsFluxSuccessResponses() {
        String[] categories = { "education", "comedy" };
        when(podcastService.findByAnyEnum(eq(SearchType.CATEGORIES_IN), any(Category[].class)))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.post()
                .uri("/podcast/search/by-categories")
                .bodyValue(categories)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PodcastSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for podcast title, then it should return flux of success responses")
    void smartSearch_Title_ReturnsFluxSuccessResponses() {
        String title = "Hello World";
        when(podcastService.findByAnyString(eq(SearchType.TITLE), eq(title)))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/podcast/search")
                        .queryParam("title", title)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PodcastSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for podcast presenter, then it should return flux of success responses")
    void smartSearch_Presenter_ReturnsFluxSuccessResponses() {
        String presenter = "John Doe";
        when(podcastService.findByAnyString(eq(SearchType.PRESENTER), eq(presenter)))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/podcast/search")
                        .queryParam("presenter", presenter)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PodcastSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for podcast guest, then it should return flux of success responses")
    void smartSearch_Guest_ReturnsFluxSuccessResponses() {
        String guest = "Jane Doe";
        when(podcastService.findByAnyString(eq(SearchType.GUEST_CONTAINS), eq(guest)))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/podcast/search")
                        .queryParam("guest", guest)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PodcastSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for podcast year, then it should return flux of success responses")
    void smartSearch_Year_ReturnsFluxSuccessResponses() {
        Integer year = 2023;
        when(podcastService.findByAnyInt(eq(SearchType.YEAR), eq(year)))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/podcast/search")
                        .queryParam("year", year)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PodcastSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for podcast year range, then it should return flux of success responses")
    void smartSearch_YearRange_ReturnsFluxSuccessResponses() {
        Integer startYear = 2020;
        Integer endYear = 2023;
        when(podcastService.findByAnyInt(eq(SearchType.YEAR_BETWEEN), eq(startYear), eq(endYear)))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/podcast/search")
                        .queryParam("startYear", startYear)
                        .queryParam("endYear", endYear)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PodcastSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for nothing, should throw SearchTypeArgumentsException")
    void smartSearch_Nothing_ThrowsSearchTypeArgumentsException() {
        webTestClient.get()
                .uri("/podcast/search")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists()
                .jsonPath("$.details").exists();
    }
}
