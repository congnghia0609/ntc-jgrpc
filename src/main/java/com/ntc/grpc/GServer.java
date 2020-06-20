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
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since Jun 20, 2020
 */
public class GServer {
    private static final Logger log = LoggerFactory.getLogger(GServer.class);
    
    private final String GSPrefix = ".gserver.";
    private String name;
    private int port;
    private boolean isSSL = false;
    private Server server;

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public boolean isIsSSL() {
        return isSSL;
    }

    public Server getServer() {
        return server;
    }

    public GServer(String name, BindableService bindableService) {
        if (name == null || name.isEmpty()) {
            throw new ExceptionInInitializerError("Name is NULL or empty");
        }
        this.name = name;
        this.port = NConfig.getConfig().getInt(name + GSPrefix + "port", -1);
        if (port < 0) {
            throw new ExceptionInInitializerError("Not found config PORT server");
        }
        this.isSSL = NConfig.getConfig().getBoolean(name + GSPrefix + "is_ssl", false);
        log.info("GServer[" + name + "] init isSSL: " + isSSL);
        if (isSSL) {
            File certFile = new File(NConfig.getConfig().getString(name + GSPrefix + "cert_file", "ssl/server.crt"));
            File keyFile = new File(NConfig.getConfig().getString(name + GSPrefix + "key_file", "ssl/server.pem"));
            server = ServerBuilder.forPort(port).useTransportSecurity(certFile, keyFile).addService(bindableService).build();
        } else {
            server = ServerBuilder.forPort(port).addService(bindableService).build();
        }
    }
    
    public void start() throws InterruptedException, IOException {
        server.start();
        // Server threads are running in the background.
        log.info("GServer started, listening on: " + port);
        // Await termination on the main thread since the grpc library uses daemon threads.
        server.awaitTermination();
    }
    
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(2, TimeUnit.SECONDS);
        }
    }
}
