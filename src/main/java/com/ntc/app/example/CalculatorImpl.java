///*
// * Copyright 2020 nghiatc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.ntc.app.example;
//
//import com.ntc.ngrpc.*;
//import io.grpc.Context;
//import io.grpc.Status;
//import io.grpc.stub.*;
//
///**
// *
// * @author nghiatc
// * @since May 29, 2020
// */
//public class CalculatorImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
//    @Override
//    public void sum(SumRequest req, StreamObserver<SumResponse> resp) {
//        System.out.println("CalculatorImpl sum...");
//        // You must use a builder to construct a new Protobuffer object
//        SumResponse sumresp = SumResponse.newBuilder().setResult(req.getNum1() + req.getNum2()).build();
//        // Use responseObserver to send a single response back
//        resp.onNext(sumresp);
//        // When you are done, you must call onCompleted.
//        resp.onCompleted();
//    }
//    
//    @Override
//    public void sumWithDeadline(SumRequest req, StreamObserver<SumResponse> resp) {
//        System.out.println("CalculatorImpl sumWithDeadline...");
//        
//        for (int i=0; i < 3; i++) {
//            if (Context.current().isCancelled()) {
//                resp.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
//                return;
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//               ex.printStackTrace();
//            }
//        }
//        
//        // You must use a builder to construct a new Protobuffer object
//        SumResponse sumresp = SumResponse.newBuilder().setResult(req.getNum1() + req.getNum2()).build();
//        // Use responseObserver to send a single response back
//        resp.onNext(sumresp);
//        // When you are done, you must call onCompleted.
//        resp.onCompleted();
//    }
//    
//    @Override
//    public void primeNumberDecomposition(PNDRequest req, StreamObserver<PNDResponse> resp) {
//        System.out.println("CalculatorImpl primeNumberDecomposition...");
//        int k = 2;
//        int N = req.getNumber();
//        while (N > 1) {
//            if (N % k == 0) {
//                N = N / k;
//                // send to client
//                PNDResponse pndResp = PNDResponse.newBuilder().setResult(k).build();
//                resp.onNext(pndResp);
//                System.out.println("server response k = " + k);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                }
//            } else {
//                k++;
//            }
//        }
//        resp.onCompleted();
//    }
//    
//    @Override
//    public StreamObserver<AverageRequest> average(StreamObserver<AverageResponse> resp) {
//        System.out.println("CalculatorImpl average...");
//        StreamObserver<AverageRequest> rs = new StreamObserver<AverageRequest>() {
//            private float total = 0F;
//            private int count  = 0;
//            
//            @Override
//            public void onNext(AverageRequest req) {
//                System.out.println("receive req: " + req.getNum());
//                total += req.getNum();
//                count++;
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                System.out.println("average error: " + t.getMessage());
//                resp.onError(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                resp.onNext(AverageResponse.newBuilder().setResult(total/count).build());
//                resp.onCompleted();
//            }
//        };
//        return rs;
//    }
//    
//    @Override
//    public StreamObserver<FindMaxRequest> findMax(StreamObserver<FindMaxResponse> resp) {
//        System.out.println("CalculatorImpl findMax...");
//        
//        return new StreamObserver<FindMaxRequest>() {
//            private int max = 0;
//            
//            @Override
//            public void onNext(FindMaxRequest fmReq) {
//                System.out.println("receive req: " + fmReq.getNum());
//                int num = fmReq.getNum();
//                if (num > max) {
//                    max = num;
//                }
//                resp.onNext(FindMaxResponse.newBuilder().setMax(max).build());
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                System.out.println("FindMax recv error: " + t.getMessage());
//                resp.onError(t);
//            }
//
//            @Override
//            public void onCompleted() {
//                resp.onCompleted();
//            }
//        };
//    }
//    
//    @Override
//    public void square(SquareRequest req, StreamObserver<SquareResponse> resp) {
//        System.out.println("CalculatorImpl square...");
//        int num = req.getNum();
//        System.out.println("receive req: " + num);
//        if (num <= 0) {
//            System.out.println("square INVALID_ARGUMENT num <= 0, num = " + num);
//            resp.onError(Status.INVALID_ARGUMENT.withDescription("Expect num > 0, req num was: " + num).asRuntimeException());
//            return;
//        }
//        resp.onNext(SquareResponse.newBuilder().setSquareRoot(Math.sqrt(Double.valueOf(num))).build());
//        resp.onCompleted();
//    }
//    
//}
