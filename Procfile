#!/bin/bash

#service: ./run service
#web: ./run webapp $PORT

service: cd oasysusa && /app/.heroku/python/bin/run_oasysusa_services production.ini
web: cd oasysusa && /app/.heroku/python/bin/pserve production.ini http_port=$PORT
