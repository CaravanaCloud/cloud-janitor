FROM gitpod/workspace-full

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && sdk install java 22.3.r19-grl && sdk default java 22.3.r19-grl && sdk install quarkus"
RUN bash -c "curl 'https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip' -o 'awscliv2.zip' && unzip awscliv2.zip && sudo ./aws/install"
RUN bash -c "npm install -g aws-cdk"
RUN bash -c "sudo install-packages cowsay fortune"
