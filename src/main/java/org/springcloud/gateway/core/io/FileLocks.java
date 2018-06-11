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
package org.springcloud.gateway.core.io;

import org.springcloud.gateway.core.function.ProcessFunction;

import static org.springcloud.gateway.core.lang.Assert2.notNullOf;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import javax.validation.constraints.NotNull;

/**
 * {@link FileLocks}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @author vjay
 * @date 2020-03-24
 * @sine v1.0.0
 * @see
 */
public abstract class FileLocks {

    /**
     * Try file lock.
     * 
     * @param file
     * @param processor
     * @param <R>
     * @return
     */
    public static <R> R doTryLock(@NotNull File file, @NotNull ProcessFunction<FileLock, R> processor) {
        notNullOf(file, "file");
        notNullOf(processor, "processor");
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
                FileChannel fileChannel = raf.getChannel();
                FileLock lock = fileChannel.tryLock();) {
            return processor.process(lock);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}