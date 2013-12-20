#!/bin/bash

#service: ./run service
#web: ./run webapp $PORT

service: cd oasysusa && run_oasysusa_services production.ini
web: cd oasysusa && pserve production.ini http_port=$PORT
