package com.pitanguinha.streaming.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.pitanguinha.streaming.dto.music.*;
import com.pitanguinha.streaming.enums.media.SearchType;
import com.pitanguinha.streaming.enums.media.music.Mood;
import com.pitanguinha.streaming.exceptions.domain.NotFoundException;
import com.pitanguinha.streaming.service.media.MusicService;
import static com.pitanguinha.streaming.util.test.creator.media.music.MusicDtoCreator.*;

import reactor.core.publisher.*;

@ExtendWith(SpringExtension.class)
@WebFluxTest(MusicController.class)
@TestPropertySource(properties = "spring.application.services.music.enabled=true")
public class MusicControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    MusicService musicService;

    MusicSuccessDto successDto = createSuccessDto();

    @Test
    @DisplayName("When generating a signed URL for content (valid ID), then it should return mono of Stringo URL")
    void getContentSignedUrl_ReturnsMonoString() {
        String id = "42";
        String expectedUrl = "https://example.com/signed-url";

        when(musicService.getContentSignedUrl(id)).thenReturn(Mono.just(expectedUrl));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/music/content")
                        .queryParam("id", id)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("When trying to generate a signed URL for content (invalid ID), then it should return bad request")
    void getContentSignedUrl_ReturnsBadRequest() {
        String invalidId = "invalid-id";

        when(musicService.getContentSignedUrl(invalidId))
                .thenReturn(Mono.error(
                        new NotFoundException("Podcast not found", "Podcast with ID " + invalidId + " not found.")));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/music/content")
                        .queryParam("id", invalidId)
                        .build())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").exists()
                .jsonPath("$.details.errorMessage").exists();
    }

    @Test
    @DisplayName("When posting a new music (valid), then it should return mono of success response")
    void save_ReturnsMonoSuccessResponse() {
        when(musicService.save(any(MusicPostDto.class)))
                .thenReturn(Mono.just(successDto));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("title", "title");
        builder.part("artist", "artist");
        builder.part("album", "album");
        List.of("feat1", "feat2").forEach(feat -> builder.part("feats", feat));
        builder.part("genre", "jazz");
        List.of("happy", "sad").forEach(mood -> builder.part("moods", mood));
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
                .uri("/music")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(MusicSuccessDto.class);
    }

    @Test
    @DisplayName("When updating a music (valid), then it should return mono of success response")
    void update_ReturnsMonoSuccessResponse() {
        when(musicService.update(any(MusicPutDto.class)))
                .thenReturn(Mono.just(successDto));

        var builder = new MultipartBodyBuilder();
        builder.part("id", "42");

        webTestClient.put()
                .uri("/music")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("When deleting a music (valid ID), then it should return no content")
    void delete_ReturnsNoContent() {
        String id = "42";
        when(musicService.delete(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/music/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("When finding a music by ID (valid), then it should return mono of success response")
    void findById_ReturnsMonoSuccessResponse() {
        String id = "42";
        when(musicService.findById(id)).thenReturn(Mono.just(successDto));

        webTestClient.get()
                .uri("/music/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MusicSuccessDto.class);
    }

    @Test
    @DisplayName("When searching music by feats (valid), then it should return flux of success response")
    void findByFeat_ReturnsFluxSuccessResponse() {
        String[] feats = { "feat1", "feat2" };
        when(musicService.findByAnyString(eq(SearchType.FEAT_IN), any(String[].class)))
                .thenReturn(Flux.just(successDto, successDto, successDto));

        webTestClient.post()
                .uri("/music/search/by-feats")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(feats)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MusicSuccessDto.class).hasSize(3);
    }

    @Test
    @DisplayName("When searching a music by moods (valid), then it should return flux of success response")
    void findByMoods_ReturnsFluxSuccessResponse() {
        String[] moods = { "happy", "sad" };
        when(musicService.findByAnyEnum(eq(SearchType.MOODS_IN), any(Mood[].class)))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.post()
                .uri("/music/search/by-moods")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(moods)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MusicSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for music title, should return flux of success response")
    void smartSearch_Title_ReturnsFluxSuccessResponse() {
        when(musicService.findByAnyString(eq(SearchType.TITLE), anyString()))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/music/search")
                        .queryParam("title", "foo bar")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MusicSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for music artist, should return flux of success response")
    void smartSearch_Artist_ReturnsFluxSuccessResponse() {
        when(musicService.findByAnyString(eq(SearchType.ARTIST), anyString()))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/music/search")
                        .queryParam("artist", "foo bar")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MusicSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for music album, should return flux of success response")
    void smartSearch_Album_ReturnsFluxSuccessResponse() {
        when(musicService.findByAnyString(eq(SearchType.ALBUM), anyString()))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/music/search")
                        .queryParam("album", "foo bar")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MusicSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for music feat, should return flux of success response")
    void smartSearch_Feat_ReturnsFluxSuccessResponse() {
        when(musicService.findByAnyString(eq(SearchType.FEAT_CONTAINS), anyString()))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/music/search")
                        .queryParam("feat", "foo bar")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MusicSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for music genre, should return flux of success response")
    void smartSearch_Genre_ReturnsFluxSuccessResponse() {
        when(musicService.findByAnyEnum(eq(SearchType.GENRE), any()))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/music/search")
                        .queryParam("genre", "jazz")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MusicSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for music year, should return flux of success response")
    void smartSearch_Year_ReturnsFluxSuccessResponse() {
        when(musicService.findByAnyInt(eq(SearchType.YEAR), anyInt()))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/music/search")
                        .queryParam("year", 2023)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MusicSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for music year range, should return flux of success response")
    void smartSearch_YearRange_ReturnsFluxSuccessResponse() {
        when(musicService.findByAnyInt(eq(SearchType.YEAR_BETWEEN), anyInt(), anyInt()))
                .thenReturn(Flux.just(successDto, successDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/music/search")
                        .queryParam("startYear", 2000)
                        .queryParam("endYear", 2023)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MusicSuccessDto.class).hasSize(2);
    }

    @Test
    @DisplayName("When searching for nothing, should throw SearchTypeArgumentsException")
    void smartSearch_Nothing_ThrowsSearchTypeArgumentsException() {
        webTestClient.get()
                .uri("/music/search")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists()
                .jsonPath("$.details").exists();
    }
}
