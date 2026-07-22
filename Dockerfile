FROM ubuntu:latest
LABEL authors="denny"

ENTRYPOINT ["top", "-b"]