/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springcloud.gateway.core.remoting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.springcloud.gateway.core.remoting.standard.HttpHeaders;

/**
 * Base implementation of {@link ClientHttpRequest} that buffers output in a
 * byte array before sending it over the wire.
 */
abstract class AbstractBufferingClientHttpRequest extends AbstractClientHttpRequest {

    private ByteArrayOutputStream bufferedOutput = new ByteArrayOutputStream(1024);

    @Override
    protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
        return this.bufferedOutput;
    }

    @Override
    protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
        byte[] bytes = this.bufferedOutput.toByteArray();
        if (headers.getContentLength() < 0) {
            headers.setContentLength(bytes.length);
        }
        ClientHttpResponse result = executeInternal(headers, bytes);
        this.bufferedOutput = null;
        return result;
    }

    /**
     * Abstract template method that writes the given headers and content to the
     * HTTP request.
     * 
     * @param headers
     *            the HTTP headers
     * @param bufferedOutput
     *            the body content
     * @return the response object for the executed request
     */
    protected abstract ClientHttpResponse executeInternal(HttpHeaders headers, byte[] bufferedOutput) throws IOException;

}