
PROJ_PATH = $(shell pwd)

all: build_proj all_static

all_static: cljs_build #minify_static

build_proj:
	lein compile

cljs_build:
	lein cljsbuild once

minify_static:
	~/ENV/bin/python $(PROJ_PATH)/static_assets.py

local_provision:
	~/ENV/bin/fab -f $(PROJ_PATH)/kitchen/cuisiniere.py -R local bootstrap get_oasys -p vagrant

remote_provistion:
	~/ENV/bin/fab -f $(PROJ_PATH)/kitchen/cuisiniere.py -R polyglot bootstrap refresh_oasys -p YKoiv6d7Hyu5

clean:
	lein clean
