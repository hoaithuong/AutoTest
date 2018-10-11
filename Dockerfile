FROM harbor.intgdc.com/base/gdc-base-centos:latest

ARG GIT_COMMIT=unspecified
LABEL image_name="Checklist xvfb image based on CentOS-7"
LABEL maintainer="ATT Scrum <lhv-auto@gooddata.com>"
LABEL git_repository_url="https://github.com/gooddata/graphene-tests"
LABEL parent_image="harbor.intgdc.com/base/gdc-base-centos:latest"
LABEL git_commit=$GIT_COMMIT

ARG CHROME_VERSION=67.0.3396.79-1
ARG CHROMEDRIVER_VERSION=2.39
ARG FIREFOX_VERSION=60.0.1
ARG GECKODRIVER_VERSION=0.20.1

# Commands are chained to squeeze the image size (~950 MB).

# Install latest Firefox and Chrome browsers.
# The procedure is taken from official Selenium Dockerfiles.
# Note: Firefox is installed with yum to resolve dependencies, but it is ESR version,
# so then it is replaced with specific version.
RUN set -x && \
    yum clean all && \
    yum install --setopt=tsflags=nodocs -y xorg-x11-server-Xvfb xorg-x11-xinit \
                dejavu-sans-fonts dejavu-sans-mono-fonts dejavu-serif-fonts \
                phantomjs maven-bin which curl unzip bzip2 firefox && \
    echo "checklist_image" | md5sum |cut -f1 -d\ > /etc/machine-id && \
    curl https://dl.google.com/linux/linux_signing_key.pub > /tmp/linux_signing_key.pub && \
    rpm --import /tmp/linux_signing_key.pub && \
    curl http://orion.lcg.ufrj.br/RPMS/myrpms/google/google-chrome-stable-$CHROME_VERSION.x86_64.rpm > /tmp/google-chrome-stable-$CHROME_VERSION.x86_64.rpm && \
    rpm --checksig /tmp/google-chrome-stable-$CHROME_VERSION.x86_64.rpm > /etc/google-sign-check && \
    yum localinstall --setopt=tsflags=nodocs -y /tmp/google-chrome-stable-$CHROME_VERSION.x86_64.rpm && \
    rm -f /tmp/linux_signing_key.pub && \
    rm -f /tmp/google-chrome-stable-$CHROME_VERSION.x86_64.rpm && \
    curl https://download-installer.cdn.mozilla.net/pub/firefox/releases/$FIREFOX_VERSION/linux-x86_64/en-US/firefox-$FIREFOX_VERSION.tar.bz2 > /tmp/firefox.tar.bz2 && \
    yum remove -y firefox && \
    rm -rf /opt/firefox && \
    tar -C /opt -xjf /tmp/firefox.tar.bz2 && \
    rm /tmp/firefox.tar.bz2 && \
    mv /opt/firefox /opt/firefox-$FIREFOX_VERSION && \
    ln -fs /opt/firefox-$FIREFOX_VERSION/firefox /usr/bin/firefox && \
    yum remove -y bzip2 && yum clean all && rm -rf /var/cache/yum

# Install Chromedriver and Geckodriver.
# The procedure is taken from official Selenium Dockerfiles.
RUN curl https://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_linux64.zip > /tmp/chromedriver_linux64.zip && \
    unzip /tmp/chromedriver_linux64.zip -d /opt/selenium && \
    rm /tmp/chromedriver_linux64.zip && \
    mv /opt/selenium/chromedriver /opt/selenium/chromedriver-$CHROMEDRIVER_VERSION && \
    chmod 755 /opt/selenium/chromedriver-$CHROMEDRIVER_VERSION && \
    ln -fs /opt/selenium/chromedriver-$CHROMEDRIVER_VERSION /usr/bin/chromedriver && \
    ln -fs /opt/google/chrome/chrome /usr/bin/chrome && \
    curl -L https://github.com/mozilla/geckodriver/releases/download/v$GECKODRIVER_VERSION/geckodriver-v$GECKODRIVER_VERSION-linux64.tar.gz > /tmp/geckodriver.tar.gz && \
    tar -C /opt -zxf /tmp/geckodriver.tar.gz && \
    rm /tmp/geckodriver.tar.gz && \
    mv /opt/geckodriver /opt/geckodriver-$GECKODRIVER_VERSION && \
    chmod 755 /opt/geckodriver-$GECKODRIVER_VERSION && \
    ln -fs /opt/geckodriver-$GECKODRIVER_VERSION /usr/bin/geckodriver

