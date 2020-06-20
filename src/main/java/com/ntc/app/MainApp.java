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

package com.ntc.app;

//import com.ntc.app.example.CalServer;
//import com.ntc.app.example.CalculatorImpl;
//import com.ntc.grpc.GServer;

/**
 *
 * @author nghiatc
 * @since May 29, 2020
 */
public class MainApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // 1. CalServer
//            CalServer calServer = new CalServer();
//            calServer.start();
//            calServer.blockUntilShutdown();
            
            // 2. GServer
//            GServer gs = new GServer("tutorial", new CalculatorImpl());
//            gs.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
