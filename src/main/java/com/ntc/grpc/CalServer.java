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
 * @since May 29, 2020
 */
public class CalServer {
    private static final Logger log = LoggerFactory.getLogger(CalServer.class);
    
    private Server server;
    
    public void start() throws IOException {
        int port = 3334;
        
        //====== Begin Mode SSL ======//
        File certFile = new File("ssl/server.crt");
        File keyFile = new File("ssl/server.pem");
        // Create a new server to listen on port
        server = ServerBuilder.forPort(port).useTransportSecurity(certFile, keyFile).addService(new CalculatorImpl()).build();
        //====== End Mode SSL ======//
        
        // Create a new server to listen on port
        //server = ServerBuilder.forPort(port).addService(new CalculatorImpl()).build();
        
        // Start the server
        server.start();
        
        // Server threads are running in the background.
        log.info("Server started, listening on " + port);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    CalServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }
    
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
    
    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

}
