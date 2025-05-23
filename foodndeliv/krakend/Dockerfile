FROM devopsfaith/krakend:latest
COPY krakend.json /etc/krakend/krakend.json

# Create a directory for SSL certificates inside the container
RUN mkdir -p /etc/krakend/ssl/

# Copy your TLS server certificate and private key into the image
COPY krakend-api.crt /etc/krakend/ssl/krakend-api.crt
COPY krakend-api.key /etc/krakend/ssl/krakend-api.key
# COPY foodndeliv-ca.crt /etc/krakend/ssl/foodndeliv-ca.crt # Only if needed for outbound connections

# The krakend user (default in official images) needs to read these.
# The official images usually handle permissions correctly, but if you face issues:
# USER root
# RUN chown -R krakend:krakend /etc/krakend/ssl && chmod -R 640 /etc/krakend/ssl/*
# USER krakend

# ENTRYPOINT and CMD are usually inherited from the base krakendframework/krakend image.
# It will look for krakend.json in /etc/krakend/ by default.
