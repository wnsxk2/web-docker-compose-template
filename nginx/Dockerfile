ARG NGINX_VERSION=stable-alpine
FROM nginx:${NGINX_VERSION}

RUN apk add --no-cache tzdata

RUN rm /etc/nginx/conf.d/default.conf
COPY ./conf.d/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
