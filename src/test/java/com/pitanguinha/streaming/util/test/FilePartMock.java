package com.pitanguinha.streaming.util.test;

import java.nio.file.Path;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FilePartMock implements FilePart {
    @Override
    public String name() {
        throw new UnsupportedOperationException("Unimplemented method 'name'");
    }

    @Override
    public HttpHeaders headers() {
        throw new UnsupportedOperationException("Unimplemented method 'headers'");
    }

    @Override
    public Flux<DataBuffer> content() {
        throw new UnsupportedOperationException("Unimplemented method 'content'");
    }

    @Override
    public String filename() {
        throw new UnsupportedOperationException("Unimplemented method 'filename'");
    }

    @Override
    public Mono<Void> transferTo(Path dest) {
        throw new UnsupportedOperationException("Unimplemented method 'transferTo'");
    }
}
