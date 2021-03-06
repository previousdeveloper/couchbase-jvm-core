/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.core.message.kv.subdoc.simple;

import com.couchbase.client.core.annotations.InterfaceAudience;
import com.couchbase.client.core.annotations.InterfaceStability;
import com.couchbase.client.core.message.CouchbaseResponse;
import com.couchbase.client.core.message.kv.AbstractKeyValueRequest;
import com.couchbase.client.core.message.kv.subdoc.BinarySubdocRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import rx.subjects.AsyncSubject;
import rx.subjects.Subject;

/**
 * Base class for all {@link BinarySubdocRequest}.
 *
 * @author Simon Baslé
 * @since 1.2
 */
@InterfaceStability.Committed
@InterfaceAudience.Public
public abstract class AbstractSubdocRequest extends AbstractKeyValueRequest implements BinarySubdocRequest {

    /**
     * A {@link NullPointerException} that is thrown by constructors when a null path is provided.
     */
    public static final NullPointerException EXCEPTION_NULL_PATH = new NullPointerException("Path is mandatory");

    /**
     * An {@link IllegalArgumentException} that is thrown by constructors when a empty path is provided on an operation
     * that doesn't allow them.
     */
    public static final IllegalArgumentException EXCEPTION_EMPTY_PATH = new IllegalArgumentException("Path cannot be empty");

    private final String path;
    private final int pathLength;
    private final ByteBuf content;

    /**
     * Creates a new {@link AbstractSubdocRequest}.
     *
     * @param key           the key of the document.
     * @param path          the subdocument path to consider inside the document.
     * @param bucket        the bucket of the document.
     * @param restOfContent the optional remainder of the {@link #content()} of the final protocol message, or null if not applicable
     * @throws NullPointerException if the path is null (see {@link #EXCEPTION_NULL_PATH})
     */
    public AbstractSubdocRequest(String key, String path, String bucket, ByteBuf... restOfContent) {
        this(key, path, bucket, AsyncSubject.<CouchbaseResponse>create(), restOfContent);
    }

    /**
     * Creates a new {@link AbstractSubdocRequest}.
     *
     * @param key           the key of the document.
     * @param path          the subdocument path to consider inside the document.
     * @param bucket        the bucket of the document.
     * @param observable    the observable which receives responses.
     * @param restOfContent the optional remainder of the {@link #content()} of the final protocol message, or null if not applicable
     * @throws NullPointerException if the path is null (see {@link #EXCEPTION_NULL_PATH})
     */
    public AbstractSubdocRequest(String key, String path, String bucket,
                                 Subject<CouchbaseResponse, CouchbaseResponse> observable,
                                 ByteBuf... restOfContent) {
        super(key, bucket, null, null, observable);
        this.path = path;
        ByteBuf pathByteBuf;
        if (path == null || path.isEmpty()) {
            pathByteBuf = Unpooled.EMPTY_BUFFER;
        } else {
            pathByteBuf = Unpooled.wrappedBuffer(path.getBytes(CharsetUtil.UTF_8));
        }
        this.pathLength = pathByteBuf.readableBytes();
        this.content = createContent(pathByteBuf, restOfContent);

        //checking nullity here allows to release all of restOfContent through cleanUpAndThrow releasing content()
        if (this.path == null) {
            cleanUpAndThrow(EXCEPTION_NULL_PATH);
        }
    }

    /**
     * Utility method to ensure good cleanup when throwing an exception from a constructor.
     *
     * Cleans the content composite buffer by releasing it before throwing the exception.
     */
    protected void cleanUpAndThrow(RuntimeException e) {
        if (content != null && content.refCnt() > 0) {
            content.release();
        }
        throw e;
    }

    protected ByteBuf createContent(ByteBuf pathByteBuf, ByteBuf... restOfContent) {
        if (restOfContent == null || restOfContent.length == 0) {
            return pathByteBuf;
        } else {
            CompositeByteBuf composite = Unpooled.compositeBuffer(1 + restOfContent.length);
            composite.addComponent(pathByteBuf);
            composite.writerIndex(composite.writerIndex() + pathByteBuf.readableBytes());

            for (ByteBuf component : restOfContent) {
                composite.addComponent(component);
                composite.writerIndex(composite.writerIndex() + component.readableBytes());
            }

            return composite;
        }
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public int pathLength() {
        return this.pathLength;
    }

    @Override
    public ByteBuf content() {
        return this.content;
    }
}
