package com.pitanguinha.streaming.service.media;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;

import com.pitanguinha.streaming.domain.media.Podcast;

import com.pitanguinha.streaming.enums.media.*;
import com.pitanguinha.streaming.enums.media.podcast.Category;

import com.pitanguinha.streaming.exceptions.search.*;

import com.pitanguinha.streaming.mapper.media.PodcastMapper;

import com.pitanguinha.streaming.repository.media.PodcastRepository;
import com.pitanguinha.streaming.service.aws.*;
import com.pitanguinha.streaming.service.media.operation.MediaOperator;

import com.pitanguinha.streaming.util.test.SignedUrlMock;

import reactor.core.publisher.*;
import reactor.test.StepVerifier;

import static com.pitanguinha.streaming.util.test.creator.media.podcast.PodcastDtoCreator.*;
import static com.pitanguinha.streaming.util.test.creator.media.podcast.PodcastEntityCreator.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PodcastServiceTest {
    @InjectMocks
    PodcastService podcastService;
    @Mock
    PodcastRepository repository;
    @Mock
    PodcastMapper mapper;
    @Mock
    AwsS3Service s3Service;
    @Mock
    AwsCloudFrontService cloudFrontService;
    @Mock
    MediaOperator<Podcast> mediaOperator;

    static Flux<Podcast> entities;

    @BeforeAll
    static void init() {
        var entitySuccess = createEntity();
        entitySuccess.setConversionStatus(ConversionStatus.SUCCESS);
        var entityPending = createEntity();
        entityPending.setConversionStatus(ConversionStatus.PENDING);
        var entityError = createEntity();
        entityError.setConversionStatus(ConversionStatus.ERROR);

        entities = Flux.just(entitySuccess, entityPending, entityError);
    }

    @BeforeEach
    void setMocks() {
        // CloudFrontService
        when(cloudFrontService.getSignedUrl(anyString()))
                .thenReturn(new SignedUrlMock());

        // Mapper
        when(mapper.toDto(any(Podcast.class))).thenReturn(createSuccessDto());
    }

    @Test
    @DisplayName("Should findByAnyString and return a Flux of Dto")
    void findByAnyString_ReturnsFluxSuccessDto() {
        // Repository
        when(repository.findByTitleRegexCaseInsensitive(anyString())).thenReturn(entities);
        when(repository.findByPresenterRegexCaseInsensitive(anyString())).thenReturn(entities);
        when(repository.findByGuestContainsCaseInsensitive(anyString())).thenReturn(entities);
        when(repository.findByGuestsIn(anyList())).thenReturn(entities);

        // When: The findByAnyString method is called for different search types
        // Then: Any search type should called for once
        // 1. TITLE
        StepVerifier.create(podcastService.findByAnyString(SearchType.TITLE, anyString()))
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByTitleRegexCaseInsensitive(anyString());

        // 2. PRESENTER
        StepVerifier.create(podcastService.findByAnyString(SearchType.PRESENTER, anyString()))
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByPresenterRegexCaseInsensitive(anyString());

        // 3. GUEST_CONTAINS
        StepVerifier.create(podcastService.findByAnyString(SearchType.GUEST_CONTAINS, anyString()))
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByGuestContainsCaseInsensitive(anyString());

        // 4. GUESTS_IN
        String[] guests = { "guest1", "guest2" };
        StepVerifier.create(podcastService.findByAnyString(SearchType.GUESTS_IN, guests))
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByGuestsIn(eq(Arrays.asList(guests)));
    }

    @Test
    @DisplayName("Should findByAnyInt and return a Flux of Dto")
    void findByInt_ReturnsFluxDto() {
        // Repository
        when(repository.findByYear(anyInt())).thenReturn(entities);
        when(repository.findByYearBetween(anyInt(), anyInt())).thenReturn(entities);

        // When: The findByAnyInt method is called for differente search types
        // Then: Any search type shopuld called for once
        // 1. YEAR_BETWEEN
        StepVerifier.create(podcastService.findByAnyInt(SearchType.YEAR_BETWEEN, 2000, 2020))
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByYearBetween(anyInt(), anyInt());

        // 2. YEAR
        StepVerifier.create(podcastService.findByAnyInt(SearchType.YEAR, 2003))
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByYear(anyInt());
    }

    @Test
    @DisplayName("Should findByAnyEnum and return a Flux of Dto")
    void findByEnum_ReturnsFluxDto() {
        // Repository
        when(repository.findByCategoriesIn(anyList())).thenReturn(entities);

        // When: The findByAnyEnum method is called for different search types
        // Then: Any search type should called for once
        // 1. CATEGORIES_IN
        Category[] categories = { Category.ARTS, Category.BUSINESS };
        StepVerifier.create(podcastService.findByAnyEnum(SearchType.CATEGORIES_IN, categories))
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByCategoriesIn(eq(Arrays.asList(categories)));
    }

    @Test
    @DisplayName("When searching by any invalid type, it should throw an InvalidSearchTypeException")
    void findByAny_ThrowsInvalidSearchTypeException() {
        // Don't use the block because it will not called the repository method,
        // will be called the default case and throw an exception
        assertThrows(InvalidSearchTypeException.class,
                () -> podcastService.findByAnyString(SearchType.YEAR, ""),
                "The search type is not valid");

        assertThrows(InvalidSearchTypeException.class,
                () -> podcastService.findByAnyInt(SearchType.TITLE, 2000),
                "The search type is not valid");

        assertThrows(InvalidSearchTypeException.class,
                () -> podcastService.findByAnyEnum(SearchType.TITLE, ConversionStatus.SUCCESS),
                "The search type is not valid");
    }

    // @Test
    // @DisplayName("Should find all entities for an owner and return a Flux of
    // ResponseDto")
    // void findAllForOwner_ReturnsFluxResponseDto() {
    // // Mock setup:
    // var entitySuccess = createEntity();
    // entitySuccess.setConversionStatus(ConversionStatus.SUCCESS);
    // var entityPending = createEntity();
    // entityPending.setConversionStatus(ConversionStatus.PENDING);
    // var entityError = createEntity();
    // entityError.setConversionStatus(ConversionStatus.ERROR);

    // Flux<Podcast> entityFlux = Flux.just(entitySuccess, entityPending,
    // entityError);

    // when(repository.findByPresenterRegexCaseInsensitive(anyString())).thenReturn(entityFlux);
    // when(mapper.toDto(any(Podcast.class))).thenReturn(dto);

    // // When: The findAllForOwner method is called
    // StepVerifier.create(podcastService.findAllForOwner(entity.getPresenter()))
    // // Then: The returned Flux ResponseDto is valid
    // .expectNextMatches(dto -> {
    // assertTrue(dto instanceof PodcastSuccessDto, "Expected PodcastSuccessDto");
    // return true;
    // })
    // .expectNextMatches(dtoError -> {
    // assertTrue(dtoError instanceof MediaErrorDto, "Expected MediaErrorDto");
    // assertEquals(MediaErrorType.CONVERSION_PENDING, ((MediaErrorDto)
    // dtoError).getErrorType(),
    // "Expected conversion status to be PENDING");
    // return true;
    // })
    // .expectNextMatches(dtoError -> {
    // assertTrue(dtoError instanceof MediaErrorDto, "Expected MediaErrorDto");
    // assertEquals(MediaErrorType.CONVERSION_FAILED, ((MediaErrorDto)
    // dtoError).getErrorType(),
    // "Expected conversion status to be ERROR");
    // return true;
    // }).verifyComplete();
    // }
}
