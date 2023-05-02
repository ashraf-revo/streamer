# `livePoll`

##`calling`
1. `cd livepoll`
1. `mvn spring-boot:run`
2. `ffmpeg -re -i input.mp4  -c:a aac -c:v h264  -rtsp_transport tcp -threads 2 -quality realtime -preset ultrafast -deadline .001 -tune zerolatency   -f rtsp rtsp://localhost:8081/stream/123/123`
3.  open `http://localhost:8080/index.html` and see your live-streaming



####`inspired from this projects`
- https://github.com/fecloud/H264Parse
- https://github.com/fyhertz/libstreaming
- https://github.com/wangdxh/netty-stream
- https://github.com/variflight/feeyo-hlsserver
