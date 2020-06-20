/*
 * Copyright 2020 nghiatc.
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

package com.ntc.grpc;

import com.ntc.configer.NConfig;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since Jun 20, 2020
 */
public class GClient {
    private static final Logger log = LoggerFactory.getLogger(GClient.class);
    
    private static Map<String, GClient> mapInstanceGClient = new ConcurrentHashMap<String, GClient>();
    private static Lock lockInstance = new ReentrantLock();
    
    private final String GCPrefix = ".gclient.";
    private String name;
    private String target;
    private boolean isSSL = false;
    private ManagedChannel channel;

    public String getName() {
        return name;
    }

    public String getTarget() {
        return target;
    }

    public boolean isIsSSL() {
        return isSSL;
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public GClient(String name) throws SSLException {
        if (name == null || name.isEmpty()) {
            throw new ExceptionInInitializerError("Name is NULL or empty");
        }
        this.name = name;
        this.target = NConfig.getConfig().getString(name + GCPrefix + "target", "");
        if (target.isEmpty()) {
            throw new ExceptionInInitializerError("Not found config Target client");
        }
        this.isSSL = NConfig.getConfig().getBoolean(name + GCPrefix + "is_ssl", false);
        log.info("GClient[" + name + "] init isSSL: " + isSSL);
        if (isSSL) {
            File certFile = new File(NConfig.getConfig().getString(name + GCPrefix + "cert_file", "ssl/client.crt"));
            log.info("GClient[" + name + "] init certFile.exists: " + certFile.exists());
            if (certFile.exists()) {
                // With server authentication SSL/TLS; custom CA root certificates; not on Android.
                channel = NettyChannelBuilder.forTarget(target).sslContext(GrpcSslContexts.forClient().trustManager(certFile).build()).build();
            } else {
                // With server authentication SSL/TLS
                channel = ManagedChannelBuilder.forTarget(target).useTransportSecurity().build();
            }
        } else {
            // No SSL
            channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        }
    }
    
    public static GClient getInstance(String name) throws SSLException {
        if (name == null || name.isEmpty()) {
            return null;
        }
        GClient instance = mapInstanceGClient.containsKey(name) ? mapInstanceGClient.get(name) : null;
        if (instance == null || instance.channel.isShutdown()) {
            lockInstance.lock();
            try {
                instance = mapInstanceGClient.containsKey(name) ? mapInstanceGClient.get(name) : null;
                if (instance == null || instance.channel.isShutdown()) {
                    instance = new GClient(name);
                    mapInstanceGClient.put(name, instance);
                }
            } finally {
                lockInstance.unlock();
            }
        }
        return instance;
    }
    
    public boolean isShutdown() {
        return channel.isShutdown();
    }
    
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}
