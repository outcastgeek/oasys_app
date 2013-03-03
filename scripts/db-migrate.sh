#!/bin/bash

psql -U zbpsaqvvhlpmue -d d46duve5di23qn -h ec2-54-243-224-162.compute-1.amazonaws.com -f OasysSchema/db/structure.sql #-W
