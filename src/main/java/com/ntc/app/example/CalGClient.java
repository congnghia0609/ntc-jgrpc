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

package com.ntc.app.example;

import com.ntc.grpc.GClient;
import com.ntc.ngrpc.AverageRequest;
import com.ntc.ngrpc.AverageResponse;
import com.ntc.ngrpc.CalculatorServiceGrpc;
import com.ntc.ngrpc.FindMaxRequest;
import com.ntc.ngrpc.FindMaxResponse;
import com.ntc.ngrpc.PNDRequest;
import com.ntc.ngrpc.PNDResponse;
import com.ntc.ngrpc.SquareRequest;
import com.ntc.ngrpc.SquareResponse;
import com.ntc.ngrpc.SumRequest;
import com.ntc.ngrpc.SumResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nghiatc
 * @since Jun 20, 2020
 */
public class CalGClient {
    private static final Logger log = LoggerFactory.getLogger(CalGClient.class);
    
    private String name;
    private GClient gc;

    public String getName() {
        return name;
    }

    public GClient getGClient() {
        return gc;
    }

    public CalGClient(String name) throws SSLException {
        this.name = name;
        gc = GClient.getInstance(name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CalGClient cgc = new CalGClient("tutorial");
            
            cgc.callSum();

            cgc.callSumWithDeadline(1000);
            cgc.callSumWithDeadline(5000);
            
            cgc.callPND();
            
            cgc.callPNDBlock();
            
            cgc.callAverage();
            
            cgc.callFindMax();
            
            cgc.callSquare(9);
            
            cgc.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void shutdown() {
        if (gc != null) {
            gc.shutdown();
        }
    }

    public void callSum() {
        try {
            System.out.println("Call sum...");
            gc = GClient.getInstance(name);
            CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(gc.getChannel());
            SumRequest req = SumRequest.newBuilder().setNum1(3).setNum2(5).build();
            SumResponse resp = stub.sum(req);
            log.info("sum api response " + resp.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callSumWithDeadline(long time) {
        try {
            System.out.println("Call sumWithDeadline...");
            gc = GClient.getInstance(name);
            CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(gc.getChannel());
            SumRequest req = SumRequest.newBuilder().setNum1(3).setNum2(5).build();
            SumResponse resp = stub.withDeadlineAfter(time, TimeUnit.MILLISECONDS).sumWithDeadline(req);
            log.info("sum api response " + resp.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callPND() {
        try {
            System.out.println("Call PND...");
            gc = GClient.getInstance(name);
            CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(gc.getChannel());
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

            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callPNDBlock() {
        try {
            System.out.println("Call callPNDBlock...");
            gc = GClient.getInstance(name);
            CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(gc.getChannel());
            PNDRequest req = PNDRequest.newBuilder().setNumber(120).build();
            stub.primeNumberDecomposition(req).forEachRemaining((PNDResponse pndResp) -> {
                System.out.println("prime number: " + pndResp.getResult());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callAverage() {
        try {
            System.out.println("Call Average...");
            gc = GClient.getInstance(name);
            CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(gc.getChannel());
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

            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callFindMax() {
        try {
            System.out.println("Call FindMax...");
            gc = GClient.getInstance(name);
            CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(gc.getChannel());
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

            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callSquare(int num) {
        try {
            System.out.println("Call Square...");
            gc = GClient.getInstance(name);
            CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(gc.getChannel());
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
