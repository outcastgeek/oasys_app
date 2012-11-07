#!/bin/bash

virtualenv ENV
ENV/bin/pip install -r requirements.txt || ENV/Scripts/pip install -r requirements.txt
