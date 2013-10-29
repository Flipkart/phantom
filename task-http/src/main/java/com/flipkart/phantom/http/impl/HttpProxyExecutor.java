/*
 * Copyright 2012-2015, the original author or authors.
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

package com.flipkart.phantom.http.impl;

import com.flipkart.phantom.task.spi.TaskContext;
import com.netflix.hystrix.*;
import org.apache.http.HttpResponse;

/**
 * Implements the HystrixCommand class for executing HTTP proxy requests
 *
 * @author kartikbu
 * @version 1.0
 * @created 16/7/13 1:54 AM
 */
public class HttpProxyExecutor extends HystrixCommand<HttpResponse> {

    /** method */
    String method;

    /** uri */
    String uri;

    /** data */
    byte[] data;

    /** the proxy client */
    private HttpProxy proxy;

    /** current task context */
    private TaskContext taskContext;

    /** only constructor uses the proxy client, task context and the http request */
    public HttpProxyExecutor(HttpProxy proxy, TaskContext taskContext, String method, String uri, byte[] data) {
        super(
            Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(proxy.getGroupKey()))
            .andCommandKey(HystrixCommandKey.Factory.asKey(proxy.getCommandKey()))
            .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(proxy.getThreadPoolKey()))
			.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(proxy.getThreadPoolSize()))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionIsolationThreadTimeoutInMilliseconds(proxy.getPool().getOperationTimeout()))
        );
        this.proxy = proxy;
        this.taskContext = taskContext;
        this.method = method;
        this.uri = uri;
        this.data = data;
    }

    /**
     * Interface method implementation
     * @return response HttpResponse for the give request
     * @throws Exception
     */
    @Override
    protected HttpResponse run() throws Exception {
        return proxy.doRequest(method,uri,data);
    }

    /**
     * Interface method implementation
     * @return response HttpResponse from the fallback
     * @throws Exception
     */
    @Override
    protected HttpResponse getFallback() {
        return proxy.fallbackRequest(method,uri,data);
    }

    /** Getter/Setter methods */
    public HttpProxy getProxy() {
        return proxy;
    }
    /** End Getter/Setter methods */
}
