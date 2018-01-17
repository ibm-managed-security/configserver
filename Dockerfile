FROM anapsix/alpine-java:8_jdk_unlimited
MAINTAINER Tiago Lopo<tlopo@ibm.com>

RUN apk update \
 && apk upgrade \
 && apk add --no-cache \
        ca-certificates \
        ruby ruby-irb bash curl\
        ruby-dev wget gnupg git\
        python2\
 && apk add --no-cache --virtual .build-deps \
        build-base \
        ruby-dev wget gnupg \
 && update-ca-certificates \
 && echo 'gem: --no-document' >> /etc/gemrc \
 && gem install oj \
 && gem install json \
 && ( /usr/glibc-compat/bin/localedef --force --inputfile POSIX --charmap UTF-8 C.UTF-8 || true )\
 && echo "export LANG=C.UTF-8" > /etc/profile.d/locale.sh\
 && /usr/glibc-compat/sbin/ldconfig /lib /usr/glibc-compat/lib\
 && apk del .build-deps \
 && (cd /opt && git clone https://github.com/elasticdog/transcrypt.git && ln -s /opt/transcrypt/transcrypt /sbin/transcrypt)\
 && rm -rf /var/cache/apk/* \
 && rm -rf /tmp/* /var/tmp/* /usr/lib/ruby/gems/*/cache/*.gem


