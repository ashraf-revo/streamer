# livePoll

##calling

`ffmpeg -re -i input.mp4  -c:a aac -c:v h264  -rtsp_transport tcp -threads 2 -quality realtime -preset ultrafast -deadline .001 -tune zerolatency   -f rtsp rtsp://localhost:8081/stream/123/123`