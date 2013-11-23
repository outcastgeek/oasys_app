__author__ = 'outcastgeek'

import json
import logging
import sys
import umsgpack

from tempfile import NamedTemporaryFile

from bson.objectid import ObjectId

from pyramid.httpexceptions import HTTPFound
from pyramid.view import view_config
from pyramid.renderers import render
from pyramid.response import Response
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from formencode import Schema
from formencode.validators import FileUploadKeeper

logging.basicConfig()
log = logging.getLogger(__file__)


def form(request, metadata):
    form = Form(request,
                schema=FileUploadSchema())
    existing_files_handles = list(request.client_timesheets.find(metadata))
    existing_files = map(lambda f: dict(file_url=f.get('file_url'),
                                        filename=f.get('filename'),
                                        content_type=f.get('content_type')), existing_files_handles)
    response_data = dict(metadata.items() + dict(renderer=FormRenderer(form), request=request,
                                                 existing_files=existing_files).items(),
                         file_metadata=json.dumps(metadata))
    response = render('templates/partials/file_upload_partial.jinja2', response_data)
    return response


@view_config(route_name='file-download',
             permission='employee')
def file_download(request):
    fileid = request.matchdict.get('fileid')
    with request.fs.get(ObjectId(fileid)) as fp_read:
        return Response(body=fp_read.read(), content_type=str(fp_read.content_type))


@view_config(route_name='file-upload',
             renderer='templates/partials/file_upload_partial.jinja2',
             permission='employee')
def file_upload(request):
    form = Form(request,
                schema=FileUploadSchema())
    if 'submit' in request.POST:
        return_to = request.POST.get('return_to')
        raw_file_data = request.POST.get('file_info')
        input_file = raw_file_data.file
        file_metadata_json_str = request.POST.get('file_metadata')
        file_metadata = json.loads(file_metadata_json_str)
        log.info("Persisting file somewhere...")
        try:
            filename = raw_file_data.filename.replace(' ', '_')
            file_url = "//%s.s3.amazonaws.com/%s" % (request.s3conf.get('s3_bucket_name'), filename)
            file_data = dict(file_metadata.items() + dict(filename=filename,
                                                          file_url=file_url,
                                                          content_type=raw_file_data.type).items())
            input_file.seek(0)
            with NamedTemporaryFile(delete=True) as tmp_file:
                while True:
                    data = input_file.read(2 << 16)
                    if not data:
                        break
                    tmp_file.write(data)
                tmp_file.seek(0)
                pack_msg = umsgpack.packb(dict(file_data.items()
                                               + request.s3conf.items()
                                               + dict(file=tmp_file.read()).items()))
                request.s3socket.send(pack_msg)
                request.loop.add_callback(check_upload_later, request, file_url)
            request.client_timesheets.insert(file_data)
            request.session.flash("You successfully uploaded file %s" % raw_file_data.filename)
        except: # catch *all* exceptions
            e = sys.exc_info()[0]
            request.session.flash("Error: %s" % e)
        return HTTPFound(location=return_to)
    return dict(renderer=FormRenderer(form))


class FileUploadSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True

    file_info = FileUploadKeeper


import time
from tornado.gen import Task, Return, coroutine
from tornado.httpclient import AsyncHTTPClient, HTTPRequest, HTTPError

gen_log = logging.getLogger("tornado.general")


@coroutine
def check_upload_later(request, file_url):
    yield Task(request.loop.add_timeout, time.time() + 5)
    status = yield Task(check_upload_success, request, file_url)
    gen_log.info("\n\n%s\n\n", status)


@coroutine
def check_upload_success(request, file_url):
    yield Task(request.loop.add_timeout, time.time() + 5)
    try:
        url = "http:%s" % file_url
        client = AsyncHTTPClient()
        response = yield client.fetch(url,
                                      method="HEAD",
                                      connect_timeout=10,
                                      request_timeout=30)
        raise Return(response)
    except HTTPError, e:
        gen_log.error("\n\nOops, something went wrong...\n\n")
        raise Return(None)



