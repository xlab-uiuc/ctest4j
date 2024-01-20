FROM ubuntu:bionic

LABEL Shuai Wang<shuaiwang516@gmail.com>

WORKDIR /root

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

#####
# Disable suggests/recommends
#####
RUN echo APT::Install-Recommends "0"\; > /etc/apt/apt.conf.d/10disableextras
RUN echo APT::Install-Suggests "0"\; >>  /etc/apt/apt.conf.d/10disableextras

ENV DEBIAN_FRONTEND noninteractive
ENV DEBCONF_TERSE true
# This requires the machine needs to have at least have 7g memory
# ENV _JAVA_OPTIONS="-Xmx7g"
######
# Install common dependencies from packages. Versions here are either
# sufficient or irrelevant.
#
# WARNING: DO NOT PUT JAVA APPS HERE! Otherwise they will install default
# Ubuntu Java.  See Java section below!
######
# hadolint ignore=DL3008

RUN apt-get -q update \
    && apt-get -q install -y --no-install-recommends \
        apt-utils \
        bats \
        build-essential \
        bzip2 \
        clang \
        cmake \
        curl \
        doxygen \
        fuse \
        g++ \
        gcc \
        git \
        gnupg-agent \
        libbz2-dev \
        libcurl4-openssl-dev \
        libfuse-dev \
        libprotobuf-dev \
        libprotoc-dev \
        libsasl2-dev \
        libsnappy-dev \
        libssl-dev \
        libsnappy-dev \
        libtool \
        libzstd1-dev \
        locales \
        make \
        pinentry-curses \
        pkg-config \
        python3 \
        python3-pip \
        python3-pkg-resources \
        python3-setuptools \
        python3-wheel \
        rsync \
        shellcheck \
        software-properties-common \
        sudo \
        valgrind \
        emacs \
        wget \
        zlib1g-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN locale-gen en_US.UTF-8
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
ENV PYTHONIOENCODING=utf-8


#######
# Install SpotBugs 4.2.2
#######
RUN mkdir -p /opt/spotbugs \
    && curl -L -s -S https://github.com/spotbugs/spotbugs/releases/download/4.2.2/spotbugs-4.2.2.tgz \
      -o /opt/spotbugs.tgz \
    && tar xzf /opt/spotbugs.tgz --strip-components 1 -C /opt/spotbugs \
    && chmod +x /opt/spotbugs/bin/*
ENV SPOTBUGS_HOME /opt/spotbugs

#######
# OpenJDK 8 and 11
#######
# hadolint ignore=DL3008
RUN apt-get -q update \
    && apt-get -q install -y --no-install-recommends openjdk-8-jdk libbcprov-java \
    && apt-get -q install -y --no-install-recommends openjdk-11-jdk libbcprov-java \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

######
# Install Google Protobuf 3.7.1 (3.0.0 ships with Bionic)
######
# hadolint ignore=DL3003
RUN mkdir -p /opt/protobuf-src \
    && curl -L -s -S \
      https://github.com/protocolbuffers/protobuf/releases/download/v3.7.1/protobuf-java-3.7.1.tar.gz \
      -o /opt/protobuf.tar.gz \
    && tar xzf /opt/protobuf.tar.gz --strip-components 1 -C /opt/protobuf-src \
    && cd /opt/protobuf-src \
    && ./configure --prefix=/opt/protobuf \
    && make "-j$(nproc)" \
    && make install \
    && cd /root \
    && rm -rf /opt/protobuf-src
ENV PROTOBUF_HOME /opt/protobuf
ENV PATH "${PATH}:/opt/protobuf/bin"

######
# Install Apache Maven 3.6.0 (3.6.0 ships with Bionic)
######
ENV MAVEN_HOME /usr/share/maven
ARG MAVEN_VERSION=3.8.6

RUN wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && echo "Unziping maven" \
    && mkdir /usr/share/maven \
    && tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /usr/share/maven --strip-components=1 \
    && echo "Cleaning and setting links" \
    && rm -f apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

COPY settings.xml /usr/share/maven/conf/settings.xml

# Install Gralde 8.5
RUN wget https://services.gradle.org/distributions/gradle-8.5-bin.zip &&
    sudo mkdir /opt/gradle
    sudo unzip -d /opt/gradle gradle-8.5-bin.zip &&
    echo "export PATH=\$PATH:/opt/gradle/gradle-8.5/bin" >> ~/.profile &&
    source ~/.profile &&

# Install pylint and python-dateutil
RUN pip3 install pylint==2.6.0 python-dateutil==2.8.1

###
# Install hadolint
####
RUN curl -L -s -S \
        https://github.com/hadolint/hadolint/releases/download/v1.11.1/hadolint-Linux-x86_64 \
        -o /bin/hadolint \
   && chmod a+rx /bin/hadolint \
   && shasum -a 512 /bin/hadolint | \
        awk '$1!="734e37c1f6619cbbd86b9b249e69c9af8ee1ea87a2b1ff71dccda412e9dac35e63425225a95d71572091a3f0a11e9a04c2fc25d9e91b840530c26af32b9891ca" {exit(1)}'

######
# Intel ISA-L 2.29.0
######
# hadolint ignore=DL3003,DL3008
RUN mkdir -p /opt/isa-l-src \
    && apt-get -q update \
    && apt-get install -y --no-install-recommends automake yasm \
    && apt-get clean \
    && curl -L -s -S \
      https://github.com/intel/isa-l/archive/v2.29.0.tar.gz \
      -o /opt/isa-l.tar.gz \
    && tar xzf /opt/isa-l.tar.gz --strip-components 1 -C /opt/isa-l-src \
    && cd /opt/isa-l-src \
    && ./autogen.sh \
    && ./configure \
    && make "-j$(nproc)" \
    && make install \
    && cd /root \
    && rm -rf /opt/isa-l-src

###
# Avoid out of memory errors in builds
###
ENV MAVEN_OPTS -Xms256m -Xmx1536m

# Skip gpg verification when downloading Yetus via yetus-wrapper
ENV HADOOP_SKIP_YETUS_VERIFICATION true

# Prepare for user mode
RUN rm -f /var/log/faillog /var/log/lastlog \
    && groupadd --non-unique -g 1 ctestrunner \
    && useradd -g ctestrunner -u 11 -k /root -m ctestrunner -d "/home/ctestrunner" \
    &&  echo "ctestrunner ALL=NOPASSWD: ALL" > "/etc/sudoers.d/hadoop-build"
ENV HOME "/home/ctestrunner"

ENV MAVEN_CONFIG "/home/ctestrunner/.m2"

WORKDIR /home/ctestrunner

# copy evaluation projects and ctest-runner repo to home dir
ARG RUNNER_APP_DIR
COPY ${RUNNER_APP_DIR}/ /home/ctestrunner/

# use java11 for the install next
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64

ARG RUNNER_BRANCH
RUN cd /home/ctestrunner/ctest-runner \
    && git checkout ${RUNNER_BRANCH} \
    && mvn install -DskipTests


WORKDIR /home/ctestrunner
RUN pwd && ls -l

# Build projects
RUN python3 /home/ctestrunner/build.py 1> /home/ctestrunner/build-docker.log 2>build-docker.err
RUN grep -ir "BUILD FAILURE" /home/ctestrunner/build-docker.log -A10 -B10

#RUN cp -r /root/.m2 /home/ctestrunner && chmod -R a+rwX /home/ctestrunner

CMD [""]
