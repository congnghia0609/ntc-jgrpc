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

import com.ntc.ngrpc.*;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since May 29, 2020
 */
public class CalClient {

    private static final Logger log = LoggerFactory.getLogger(CalClient.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Access a service running on the local machine on port 3333
            String target = "localhost:3330"; // grpc-haproxy
            //String target = "localhost:3334"; // grpc-java

            // Create a communication channel to the server, known as a Channel. Channels are thread-safe
            // and reusable. It is common to create channels at the beginning of your application and reuse
            // them until the application shuts down.
            
            // 1. No SSL
            //ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            
            // 2. With server authentication SSL/TLS
            //ManagedChannel channel = ManagedChannelBuilder.forTarget(target).useTransportSecurity().build();
            
            // 3. With server authentication SSL/TLS; custom CA root certificates; not on Android.
            // NettyChannelBuilder or OkHttpChannelBuilder
//            File certFile = new File("ssl/client.crt");
            File certFile = new File("/home/nghiatc/go-projects/src/ntc-ggrpc/ssl/client.crt");
            ManagedChannel channel = NettyChannelBuilder.forTarget(target).sslContext(GrpcSslContexts.forClient().trustManager(certFile).build()).build();

            
            callSum(channel);

            //callSumWithDeadline(channel, 1000);
            //callSumWithDeadline(channel, 5000);
            
            //callPND(channel);
            
            //callPNDBlock(channel);
            
            //callAverage(channel);
            
            callFindMax(channel);
            
            //callSquare(channel, -2);
            
            
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callSum(Channel channel) {
        try {
            System.out.println("Call sum...");
            CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
            SumRequest req = SumRequest.newBuilder().setNum1(3).setNum2(5).build();
            SumResponse resp = stub.sum(req);
            log.info("sum api response " + resp.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callSumWithDeadline(Channel channel, long time) {
        try {
            System.out.println("Call sumWithDeadline...");
            CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
            SumRequest req = SumRequest.newBuilder().setNum1(3).setNum2(5).build();
            SumResponse resp = stub.withDeadlineAfter(time, TimeUnit.MILLISECONDS).sumWithDeadline(req);
            log.info("sum api response " + resp.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callPND(Channel channel) {
        try {
            System.out.println("Call PND...");
            CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
            PNDRequest req = PNDRequest.newBuilder().setNumber(120).build();

            StreamObserver<PNDResponse> toServer = new StreamObserver<PNDResponse>() {
                @Override
                public void onNext(PNDResponse pndResp) {
                    System.out.println("prime number: " + pndResp.getResult());
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("callPND Error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("server finish streaming");
                }
            };
            stub.primeNumberDecomposition(req, toServer);

            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callPNDBlock(Channel channel) {
        try {
            System.out.println("Call callPNDBlock...");
            CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
            PNDRequest req = PNDRequest.newBuilder().setNumber(120).build();
            stub.primeNumberDecomposition(req).forEachRemaining((PNDResponse pndResp) -> {
                System.out.println("prime number: " + pndResp.getResult());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callAverage(Channel channel) {
        try {
            System.out.println("Call Average...");
            CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
            List<AverageRequest> listAR = new ArrayList<>();
            listAR.add(AverageRequest.newBuilder().setNum(5).build());
            listAR.add(AverageRequest.newBuilder().setNum(10).build());
            listAR.add(AverageRequest.newBuilder().setNum(15).build());
            listAR.add(AverageRequest.newBuilder().setNum(20).build());
            listAR.add(AverageRequest.newBuilder().setNum(25).build());

            StreamObserver<AverageResponse> avgResp = new StreamObserver<AverageResponse>() {
                @Override
                public void onNext(AverageResponse value) {
                    System.out.println("Average response onNext: " + value.getResult());
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("receive average response error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Average response Completed");
                }
            };
            StreamObserver<AverageRequest> avgReq = stub.average(avgResp);
            for (AverageRequest ar : listAR) {
                avgReq.onNext(ar);
                Thread.sleep(1000);
            }

            avgReq.onCompleted();

            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callFindMax(Channel channel) {
        try {
            System.out.println("Call FindMax...");
            CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
            List<FindMaxRequest> listFM = new ArrayList<>();
            listFM.add(FindMaxRequest.newBuilder().setNum(5).build());
            listFM.add(FindMaxRequest.newBuilder().setNum(10).build());
            listFM.add(FindMaxRequest.newBuilder().setNum(4).build());
            listFM.add(FindMaxRequest.newBuilder().setNum(6).build());
            listFM.add(FindMaxRequest.newBuilder().setNum(7).build());

            StreamObserver<FindMaxResponse> fmResp = new StreamObserver<FindMaxResponse>() {
                int max = 0;

                @Override
                public void onNext(FindMaxResponse value) {
                    System.out.println("FindMax response onNext: " + value.getMax());
                    max = value.getMax();
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("receive FindMax response error: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("FindMax response Completed: " + max);
                }
            };
            StreamObserver<FindMaxRequest> fmReq = stub.findMax(fmResp);
            for (FindMaxRequest fm : listFM) {
                fmReq.onNext(fm);
                Thread.sleep(1000);
            }

            fmReq.onCompleted();

            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callSquare(Channel channel, int num) {
        try {
            System.out.println("Call Square...");
            CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
            SquareRequest req = SquareRequest.newBuilder().setNum(num).build();
            SquareResponse resp = stub.square(req);

            log.info("Square api response " + resp.getSquareRoot());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                log.error("Error code:" + e.getStatus().getCode());
                log.error("Error msg:" + e.getStatus().getDescription());
                log.error("InvalidArgument num=" + num);
            } else {
                log.error("Error code:" + e.getStatus().getCode());
                log.error("Error msg:" + e.getStatus().getDescription());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
