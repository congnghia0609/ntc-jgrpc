# ntc-jgrpc
ntc-jgrpc is an example java gRPC

## Maven
```Xml
<dependency>
    <groupId>com.streetcodevn</groupId>
    <artifactId>ntc-jgrpc</artifactId>
    <version>1.0.0</version>
</dependency>
```

## gRPC Server
Example CalculatorService Server:  
```java
public class MainApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // 2. GServer
            GServer gs = new GServer("tutorial", new CalculatorImpl());
            gs.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## gRPC Client
Example CalculatorService Client:  
```java
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
}
```

## HAProxy Config Load Balancer for gRPC Server
```bash
frontend grpc_fe
	bind *:3330 ssl crt /ssl/haproxy.pem alpn h2
	option http-use-htx
    option logasap
	default_backend grpc_be

backend grpc_be
	balance roundrobin
	server grpc-go 127.0.0.1:3333 check ssl alpn h2 ca-file /ssl/server.crt
	server grpc-java 127.0.0.1:3334 check ssl alpn h2 ca-file /ssl/server.crt
```

## NginX Config Load Balancer for gRPC Server, mode Non-SSL
Reference document at [NginX blog](https://www.nginx.com/blog/nginx-1-13-10-grpc/) and [Module ngx_http_grpc_module](http://nginx.org/en/docs/http/ngx_http_grpc_module.html)  
```bash
http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  1800;

    map $http_upgrade $connection_upgrade {
         default upgrade;
         ''      close;
    }

    upstream grpcservers {
        server 127.0.0.1:3333;
        server 127.0.0.1:3334;
    }

    server {
        listen 3330 http2;

        # Can use location detail with a path of gRPC Service. Ex: /helloworld.Greeter
        location /ngrpc.CalculatorService {
            # The 'grpc://' ==> Non-SSL gRPC
            # The 'grpcs://' ==> SSL gRPC
            grpc_pass grpc://grpcservers;
        }
    }
}
```

## NginX Config Load Balancer for gRPC Server, mode SSL
```bash
http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  1800;

    map $http_upgrade $connection_upgrade {
         default upgrade;
         ''      close;
    }

    upstream grpcservers {
        server 127.0.0.1:3333;
        server 127.0.0.1:3334;
    }

    server {
        listen 3330 ssl http2;

        ## Create a certificate that points to the hostname.
        ## $ openssl req -nodes -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -subj '/CN=nginx'
        ssl_certificate /ssl/server.crt;
        ssl_certificate_key /ssl/server.pem;

        # Can use location detail with a path of gRPC Service. Ex: /helloworld.Greeter
        location /ngrpc.CalculatorService {
            # The 'grpc://' ==> Non-SSL gRPC
            # The 'grpcs://' ==> SSL gRPC
            grpc_pass grpcs://grpcservers;
        }
    }
}
```

## License
This code is under the [Apache License v2](https://www.apache.org/licenses/LICENSE-2.0).  
