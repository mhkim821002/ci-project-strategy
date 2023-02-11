FROM alpine:3.17

COPY entrypoint.sh /entrypoint.sh

RUN apk add curl
RUN chmod +x /entrypoint.sh

ENTRYPOINT [ "/entrypoint.sh" ]
