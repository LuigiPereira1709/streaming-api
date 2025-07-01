package com.pitanguinha.streaming.util.test;

import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

public class SignedUrlMock implements SignedUrl {
    public static final String URL = "testSignedUrl"; 

    @Override
    public String protocol() {
        throw new UnsupportedOperationException("Unimplemented method 'protocol'");
    }

    @Override
    public String domain() {
        throw new UnsupportedOperationException("Unimplemented method 'domain'");
    }

    @Override
    public String encodedPath() {
        throw new UnsupportedOperationException("Unimplemented method 'encodedPath'");
    }

    @Override
    public String url() {
        return URL; // Mocked URL for testing purposes
    }

    @Override
    public SdkHttpRequest createHttpGetRequest() {
        throw new UnsupportedOperationException("Unimplemented method 'createHttpGetRequest'");
    }
}
