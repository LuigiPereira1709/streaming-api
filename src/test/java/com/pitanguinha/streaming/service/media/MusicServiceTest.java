package com.pitanguinha.streaming.service.media;

import org.junit.jupiter.api.*;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.extension.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.*;
import org.mockito.junit.jupiter.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import com.pitanguinha.streaming.enums.media.*;
import com.pitanguinha.streaming.enums.media.music.*;
import com.pitanguinha.streaming.exceptions.search.InvalidSearchTypeException;

import com.pitanguinha.streaming.domain.media.Music;
import com.pitanguinha.streaming.mapper.media.MusicMapper;
import com.pitanguinha.streaming.repository.media.MusicRepository;

import com.pitanguinha.streaming.service.aws.*;
import com.pitanguinha.streaming.service.media.operation.MediaOperator;

import com.pitanguinha.streaming.util.test.SignedUrlMock;

import reactor.test.StepVerifier;
import reactor.core.publisher.Flux;

import static com.pitanguinha.streaming.util.test.creator.media.music.MusicDtoCreator.*;
import static com.pitanguinha.streaming.util.test.creator.media.music.MusicEntityCreator.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MusicServiceTest {
    @InjectMocks
    MusicService musicService;
    @Mock
    MusicRepository repository;
    @Mock
    MusicMapper mapper;
    @Mock
    AwsS3Service s3Service;
    @Mock
    AwsCloudFrontService cloudFrontService;
    @Mock
    MediaOperator<Music> mediaOperator;

    static Flux<Music> entities;

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
        when(mapper.toDto(any())).thenReturn(createSuccessDto());
    }

    @Test
    @DisplayName("Should findByAnyString and return a Flux of Dto")
    void findByAnyString_ReturnsFluxSuccessDto() {
        // Mock setup:
        var entitySuccess = createEntity();
        entitySuccess.setConversionStatus(ConversionStatus.SUCCESS);
        var entityPending = createEntity();
        entityPending.setConversionStatus(ConversionStatus.PENDING);
        var entityError = createEntity();
        entityError.setConversionStatus(ConversionStatus.ERROR);

        Flux<Music> entityFlux = Flux.just(entitySuccess, entityPending, entityError);

        // Repository mock
        when(repository.findByTitleRegexCaseInsensitive(anyString())).thenReturn(entityFlux);
        when(repository.findByArtistRegexCaseInsensitive(anyString())).thenReturn(entityFlux);
        when(repository.findByAlbumRegexCaseInsensitive(anyString())).thenReturn(entityFlux);
        when(repository.findByFeatsContains(anyString())).thenReturn(entityFlux);
        when(repository.findByFeatsIn(anyList())).thenReturn(entityFlux);

        // When: The findByAnyString method is called for different search types
        // Then: Any search type should called for once
        // 1. TITLE
        musicService.findByAnyString(SearchType.TITLE, anyString())
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByTitleRegexCaseInsensitive(anyString());

        // 2. ARTIST
        musicService.findByAnyString(SearchType.ARTIST, anyString())
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByArtistRegexCaseInsensitive(anyString());

        // 3. ALBUM
        musicService.findByAnyString(SearchType.ALBUM, anyString())
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByAlbumRegexCaseInsensitive(anyString());

        // 4. FEAT_IN
        String[] feats = { "feat1", "feat2" };
        musicService.findByAnyString(SearchType.FEAT_IN, feats)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByFeatsIn(eq(Arrays.asList(feats)));

        // 5. FEAT_CONTAINS
        musicService.findByAnyString(SearchType.FEAT_CONTAINS, anyString())
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByFeatsContains(anyString());
    }

    @Test
    @DisplayName("Should findByAnyEnum and return a Flux of Dto")
    void findByAnyEnum_ReturnsFluxSuccessDto() {
        // Repository mock
        when(repository.findByGenre(any(Genre.class))).thenReturn(entities);
        when(repository.findByMoodsIn(anyList())).thenReturn(entities);

        // When: The findByAnyEnum method is called for different search types
        // Then: Any search type should called for once
        // 1. GENRE
        musicService.findByAnyEnum(SearchType.GENRE, Genre.ROCK)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByGenre(eq(Genre.ROCK));

        // 2. MOODS_IN
        Mood[] moods = { Mood.HAPPY, Mood.SAD };
        musicService.findByAnyEnum(SearchType.MOODS_IN, moods)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByMoodsIn(eq(Arrays.asList(moods)));
    }

    @Test
    @DisplayName("Should findByAnyInt and return a Flux of Dto")
    void findByAnyInt_ReturnsFluxSuccessDto() {
        // Repository mock
        when(repository.findByYear(anyInt())).thenReturn(entities);
        when(repository.findByYearBetween(anyInt(), anyInt())).thenReturn(entities);

        // When: The findByAnyInt method is called for different search types
        // Then: Any search type should called for once
        // 1. YEAR_BETWEEN
        musicService.findByAnyInt(SearchType.YEAR_BETWEEN, 2000, 2020)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByYearBetween(anyInt(), anyInt());

        // 2. YEAR
        musicService.findByAnyInt(SearchType.YEAR, 2003)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verify(repository, times(1)).findByYear(anyInt());
    }

    @Test
    @DisplayName("When searching by any invalid type, it should throw an InvalidSearchTypeException")
    void findByAny_ThrowsInvalidSearchTypeException() {
        // Don't use the block because it will not called the repository method,
        // will be called the default case and throw an exception
        assertThrows(InvalidSearchTypeException.class,
                () -> musicService.findByAnyString(SearchType.YEAR, ""),
                "The search type is not valid");

        assertThrows(InvalidSearchTypeException.class,
                () -> musicService.findByAnyInt(SearchType.TITLE, 2000),
                "The search type is not valid");

        assertThrows(InvalidSearchTypeException.class,
                () -> musicService.findByAnyEnum(SearchType.TITLE, ConversionStatus.SUCCESS),
                "The search type is not valid");
    }
}
