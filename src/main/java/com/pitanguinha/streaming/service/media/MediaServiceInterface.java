package com.pitanguinha.streaming.service.media;

import com.pitanguinha.streaming.dto.media.*;
import com.pitanguinha.streaming.dto.media.response.*;
import com.pitanguinha.streaming.domain.media.Media;
import com.pitanguinha.streaming.enums.media.SearchType;

import reactor.core.publisher.*;

/**
 * Interface for media services.
 * 
 * @param <D> The type of media DTO.
 */
public interface MediaServiceInterface<E extends Media, D extends MediaSuccessDto> {
    Mono<String> getContentSignedUrl(String id);

    Mono<D> save(MediaPostDto postDto);

    Mono<D> update(MediaPutDto putDto);

    Mono<D> findById(String id);

    Flux<MediaResponseDto> findAllForOwner(String ownerId);

    Flux<D> findByAnyString(SearchType extendssearchType, String... anyString);

    Flux<D> findByAnyInt(SearchType searchType, int... anyInt);

    Flux<D> findByAnyEnum(SearchType searchType, Enum<?>... anyEnum);

    Mono<Void> delete(String id);

    @Deprecated(forRemoval = false, since = "1.0")
    Mono<Void> report(String id);
}
