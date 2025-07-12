package com.pitanguinha.streaming.service.media;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.*;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.pitanguinha.streaming.dto.media.*;
import com.pitanguinha.streaming.dto.media.response.*;
import com.pitanguinha.streaming.domain.media.Media;

import com.pitanguinha.streaming.enums.media.*;

import com.pitanguinha.streaming.exceptions.domain.*;
import com.pitanguinha.streaming.exceptions.aws.s3.S3Exception;

import com.pitanguinha.streaming.service.aws.*;
import com.pitanguinha.streaming.service.TempDirService;
import com.pitanguinha.streaming.service.media.operation.MediaOperator;

import com.pitanguinha.streaming.util.test.*;
import com.pitanguinha.streaming.utils.FileUtils;

import reactor.core.publisher.*;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AbstractMediaServiceTest {
    @Mock
    AwsS3Service s3Service;
    @Mock
    AwsCloudFrontService cloudFrontService;
    @Mock
    TempDirService tempDirService;
    @Mock
    MediaOperator<Media> mediaOperator;
    @Mock
    ReactiveCrudRepository<Media, String> repository;
    @InjectMocks
    MediaServiceTest mediaService;

    @BeforeEach
    void setMocks() {
        // CloudFrontService
        when(cloudFrontService.getSignedUrl(anyString()))
                .thenReturn(new SignedUrlMock());
    }

    @Test
    @DisplayName("Should return a signed URL for content")
    void getContentSignedUrl_ReturnsMonoString() {
        var media = new Media();
        media.setContentKey("testContentKey");
        media.setConversionStatus(ConversionStatus.SUCCESS);

        // Mocks the repository
        when(repository.findById(anyString()))
                .thenReturn(Mono.just(media));

        mediaService.getContentSignedUrl("testId")
                .as(StepVerifier::create)
                .expectNextMatches(signedUrl -> signedUrl.equals(SignedUrlMock.URL))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should to save media in S3 and in repository, returning a SuccessDto")
    void saveInS3AndRepository_ReturnsMediaSuccessDto() {
        try (var mockedFileUtils = mockStatic(FileUtils.class)) {
            var media = new Media();
            media.setId("testId");

            // Mocks the isFileSizeSupported method
            mockedFileUtils.when(() -> FileUtils.isFileSizeSupported(any(FilePart.class), anyInt()))
                    .thenReturn(Mono.empty());

            // Mocks the mediaOperator's uploadOrUpdateToS3 method to return a Mono of Media
            when(mediaOperator.uploadOrUpdateToS3(any(Media.class), any(FilePart.class), any(FilePart.class)))
                    .thenReturn(Mono.just(media));

            // Mock the repository's save method to return the Media object
            when(repository.save(any(Media.class))).thenReturn(Mono.just(media));

            mediaService.saveInS3AndRepository(media, mock(FilePart.class), mock(FilePart.class))
                    .as(StepVerifier::create)
                    .expectNextMatches(dto -> dto != null && dto.getThumbnailUrl().equals("testSignedUrl"))
                    .verifyComplete();

            // Verify that the methods were called the expected number of times
            verify(repository, times(1)).save(eq(media));
            verify(mediaOperator, times(1)).uploadOrUpdateToS3(eq(media), any(FilePart.class), any(FilePart.class));
            verify(cloudFrontService, times(1)).getSignedUrl(eq(media.getId() + "/" + media.getThumbnailSuffix()));
        }
    }

    @Test
    @DisplayName("When has an error during upload to S3, should throw an S3Exception")
    void saveInS3AndRepository_ThrowsS3Exception() {
        try (var mockedFileUtils = mockStatic(FileUtils.class)) {
            mockedFileUtils.when(() -> FileUtils.isFileSizeSupported(any(FilePart.class), anyInt()))
                    .thenReturn(Mono.empty());

            when(repository.save(any(Media.class)))
                    .thenReturn(Mono.just(new Media()));

            when(mediaOperator.uploadOrUpdateToS3(any(Media.class), any(FilePart.class), any(FilePart.class)))
                    .thenReturn(Mono.error(new S3Exception("Error", null, null, null)));

            // When: call the saveInS3AndRepository method
            mediaService.saveInS3AndRepository(mock(Media.class), mock(FilePart.class), mock(FilePart.class))
                    // Then: it should throw an S3Exception
                    .as(StepVerifier::create)
                    .expectError(S3Exception.class)
                    .verify();
        }
    }

    @Test
    @DisplayName("Should update media in S3 and in repository, returning a SuccessDto")
    void updateInS3AndRepository_ReturnsMediaSuccessDto() {
        try (var mockedFileUtils = mockStatic(FileUtils.class)) {
            var media = new Media();
            media.setId("testId");
            media.setConversionStatus(ConversionStatus.SUCCESS);

            // Mocks the isFileSizeSupported method
            mockedFileUtils.when(() -> FileUtils.isFileSizeSupported(any(FilePart.class), anyInt()))
                    .thenReturn(Mono.empty());

            // Mocks the mediaOperator's uploadOrUpdateToS3 method to return a Mono of Media
            when(mediaOperator.uploadOrUpdateToS3(any(Media.class), any(FilePart.class), any(FilePart.class)))
                    .thenReturn(Mono.just(media));

            // Mock the repository's save method to return the Media object
            when(repository.save(any(Media.class))).thenReturn(Mono.just(media));
            when(repository.findById(anyString())).thenReturn(Mono.just(media));

            // Givem: create a MediaPutDto
            var putDto = new MediaPutDto();
            putDto.setId(media.getId());
            putDto.setContentFile(new FilePartMock());
            putDto.setThumbnailFile(new FilePartMock());

            // When: call the updateInS3AndRepository method
            mediaService.updateInS3AndRepository(putDto, (entity, dto) -> {
            })
                    // Then: it should return a Mono containing a MediaSuccessDto
                    .as(StepVerifier::create)
                    .expectNextMatches(dto -> dto != null && dto.getThumbnailUrl().equals("testSignedUrl"))
                    .verifyComplete();

            // Verify that the methods were called the expected number of times
            verify(repository, times(1)).save(eq(media));
            verify(repository, times(1)).findById(eq(media.getId()));
            verify(mediaOperator, times(1)).uploadOrUpdateToS3(eq(media), any(FilePart.class), any(FilePart.class));
            verify(cloudFrontService, times(1)).getSignedUrl(eq(media.getId() + "/" + media.getThumbnailSuffix()));
        }
    }

    @Test
    @DisplayName("When trying to update an entity that does not exist, should throw an NotFoundException")
    void updateInS3AndRepository_ThrowsNotFoundException() {
        // Mock the repository's findById method to return an empty Mono
        when(repository.findById(anyString())).thenReturn(Mono.empty());

        var putDto = new MediaPutDto();
        putDto.setId("nonExistentId");
        // When: call the updateInS3AndRepository method
        mediaService.updateInS3AndRepository(putDto, (entity, dto) -> {
        })
                // Then: it should throw a NotFoundException
                .as(StepVerifier::create)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("When trying to update an entity with status PENDING, should throw a DomainStateException")
    void updateInS3AndRepository_ThrowsDomainStateException() {
        var media = new Media();
        media.setId("testId");
        media.setConversionStatus(ConversionStatus.PENDING);

        // Mock the repository's findById method to return the Media object
        when(repository.findById(anyString())).thenReturn(Mono.just(media));

        var putDto = new MediaPutDto();
        putDto.setId(media.getId());
        // When: call the updateInS3AndRepository method
        mediaService.updateInS3AndRepository(putDto, (entity, dto) -> {
        })
                // Then: it should throw a DomainStateException
                .as(StepVerifier::create)
                .expectError(DomainStateException.class)
                .verify();
    }

    @Test
    @DisplayName("When finding media by id, should return a MediaSuccessDto")
    void findById_ReturnsMediaSuccessDto() {
        var media = new Media();
        media.setId("testId");
        media.setConversionStatus(ConversionStatus.SUCCESS);

        // Mock the repository's findById method to return the Media object
        when(repository.findById(anyString())).thenReturn(Mono.just(media));

        mediaService.findById("testId")
                .as(StepVerifier::create)
                .expectNextMatches(dto -> dto != null && dto.getThumbnailUrl().equals("testSignedUrl"))
                .verifyComplete();

        // Verify that the methods were called the expected number of times
        verify(repository, times(1)).findById(eq("testId"));
        verify(cloudFrontService, times(1)).getSignedUrl(eq(media.getId() + "/" + media.getThumbnailSuffix()));
    }

    @Test
    @DisplayName("When trying to find media by id that does not exist, should throw a NotFoundException")
    void findById_ThrowsNotFoundException() {
        // Mock the repository's findById method to return an empty Mono
        when(repository.findById(anyString())).thenReturn(Mono.empty());

        // When: call the findById method
        mediaService.findById("nonExistentId")
                // Then: it should throw a NotFoundException
                .as(StepVerifier::create)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("When finding entity by id with status not SUCCESS, should throw a DomainStateException")
    void findById_ThrowsDomainStateException() {
        var media = new Media();
        media.setId("testId");
        media.setConversionStatus(ConversionStatus.PENDING);

        // Mock the repository's findById method to return the Media object
        when(repository.findById(anyString())).thenReturn(Mono.just(media));

        // When: call the findById method
        mediaService.findById("testId")
                // Then: it should throw a DomainStateException
                .as(StepVerifier::create)
                .expectError(DomainStateException.class)
                .verify();
    }

    @Test
    @DisplayName("Should delete media from S3 and repository")
    void delete_ReturnsMonoVoid() {
        var media = new Media();
        media.setId("testId");
        media.setConversionStatus(ConversionStatus.SUCCESS);

        // Mock the repository's findById method to return the Media object
        when(repository.findById(anyString())).thenReturn(Mono.just(media));
        when(repository.delete(any(Media.class))).thenReturn(Mono.empty());

        // Mock the s3Service's delete method to return a Mono<Void>
        when(s3Service.deleteArtifact(anyString())).thenReturn(Mono.just(true));

        // When: call the delete method
        mediaService.delete("testId")
                // Then: it should complete without error
                .as(StepVerifier::create)
                .verifyComplete();

        // Verify that the methods were called the expected number of times
        verify(repository, times(1)).findById(eq("testId"));
        verify(s3Service, times(1)).deleteArtifact(eq(media.getId()));
    }

    @Test
    @DisplayName("When trying to delete media that does not exist, should throw a NotFoundException")
    void delete_ThrowsNotFoundException() {
        // Mock the repository's findById method to return an empty Mono
        when(repository.findById(anyString())).thenReturn(Mono.empty());

        // When: call the delete method
        mediaService.delete("nonExistentId")
                // Then: it should throw a NotFoundException
                .as(StepVerifier::create)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("When trying to delete media with status PENDING, should throw a DomainStateException")
    void delete_ThrowsDomainStateException() {
        var media = new Media();
        media.setConversionStatus(ConversionStatus.PENDING);

        // Mock the repository's findById method to return the Media object
        when(repository.findById(anyString())).thenReturn(Mono.just(media));

        // When: call the delete method
        mediaService.delete("testId")
                // Then: it should throw a DomainStateException
                .as(StepVerifier::create)
                .expectError(DomainStateException.class)
                .verify();
    }

    @Test
    @DisplayName("When conversion status is SUCCESS return entity, otherwise throw an exception")
    void errorIfConversionStatusIsNotSuccess() {
        var media = new Media();
        media.setConversionStatus(ConversionStatus.SUCCESS);

        // When: the status is SUCCESS
        Media result = mediaService.errorIfConversionNotSuccessful(media);
        // Then: it should return the media entity
        Assertions.assertEquals(media, result);

        // When: the status is not SUCCESS
        media.setConversionStatus(ConversionStatus.ERROR);
        media.setConversionStatus(ConversionStatus.PENDING);
        // Then: it should throw an exception
        assertThrows(DomainStateException.class, () -> mediaService.errorIfConversionNotSuccessful(media));
        assertThrows(DomainStateException.class, () -> mediaService.errorIfConversionNotSuccessful(media));
    }

    @Test
    @DisplayName("When map or wrap error, should return a successDto or wrap in an errorDto")
    void mapOWrapErrorDto_ReturnsFluxMediaResponseDto() {
        var media1 = new Media();
        media1.setId("id1");
        media1.setConversionStatus(ConversionStatus.SUCCESS);

        var media2 = new Media();
        media2.setId("id2");
        media2.setConversionStatus(ConversionStatus.ERROR);

        Flux<Media> mediaFlux = Flux.just(media1, media2);

        // When: mapOrWrapErrorDto is called
        StepVerifier.create(mediaService.mapOrWrapErrorDto(mediaFlux))
                // Then: it should wrap the entities without Success status
                .expectNextMatches(dto -> dto instanceof MediaSuccessDto)
                .expectNextMatches(dto -> dto instanceof MediaErrorDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should map only entities with SUCCESS status")
    void mapOnlySuccessfulConversions_ReturnsFluxMediaSuccessDto() {
        var media1 = new Media();
        media1.setId("id1");
        media1.setConversionStatus(ConversionStatus.SUCCESS);

        var media2 = new Media();
        media2.setId("id2");
        media2.setConversionStatus(ConversionStatus.ERROR);

        Flux<Media> mediaFlux = Flux.just(media1, media2);

        // When: mapOnlySuccessfulConversions is called
        StepVerifier.create(mediaService.mapOnlySuccessfulConversions(mediaFlux))
                // Then: it should return only entities with SUCCESS status
                .expectNextMatches(
                        dto -> dto instanceof MediaSuccessDto && ((MediaSuccessDto) dto).getId().equals("id1"))
                .verifyComplete();
    }
}

class MediaServiceTest extends AbstractMediaService<Media, MediaSuccessDto> {
    MediaServiceTest(AwsS3Service s3Service, AwsCloudFrontService cloudFrontService,
            TempDirService tempDirService, MediaOperator<Media> mediaOperator,
            ReactiveCrudRepository<Media, String> repository) {
        super(s3Service, cloudFrontService, tempDirService, repository, mediaOperator);
    }

    @Override
    public Mono<MediaSuccessDto> save(MediaPostDto postDto) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public Mono<MediaSuccessDto> update(MediaPutDto putDto) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public Flux<MediaResponseDto> findAllForOwner(String ownerId) {
        throw new UnsupportedOperationException("Unimplemented method 'findAllForOwner'");
    }

    @Override
    public Flux<MediaSuccessDto> findByAnyString(SearchType extendssearchType, String... anyString) {
        throw new UnsupportedOperationException("Unimplemented method 'findByAnyString'");
    }

    @Override
    public Flux<MediaSuccessDto> findByAnyInt(SearchType searchType, int... anyInt) {
        throw new UnsupportedOperationException("Unimplemented method 'findByAnyInt'");
    }

    @Override
    public Flux<MediaSuccessDto> findByAnyEnum(SearchType searchType, Enum<?>... anyEnum) {
        throw new UnsupportedOperationException("Unimplemented method 'findByAnyEnum'");
    }

    @Override
    public Mono<Void> report(String id) {
        throw new UnsupportedOperationException("Unimplemented method 'report'");
    }

    protected MediaSuccessDto toDto(Media entity) {
        var dto = new MediaSuccessDto();
        dto.setId(entity.getId());
        return dto;
    }
}
