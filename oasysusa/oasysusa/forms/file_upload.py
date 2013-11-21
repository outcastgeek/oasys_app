__author__ = 'outcastgeek'

import json
import logging
import sys
import umsgpack

# from tempfile import NamedTemporaryFile

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
    existing_files_handles = list(request.db.fs.files.find(metadata))
    existing_files = map(lambda f: dict(fileid=str(f.get('_id')),
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
            file_data = dict(file_metadata.items() + dict(filename=raw_file_data.filename,
                                                          content_type=raw_file_data.type).items())
            with request.fs.new_file(**file_data) as fp:
                input_file.seek(0)
                while True:
                    data = input_file.read(2 << 16)
                    if not data:
                        break
                    fp.write(data)
                input_file.seek(0)
                pack_msg = umsgpack.packb(dict(file_data.items()
                                               + request.s3conf.items()
                                               + dict(file=input_file.read()).items()))
                request.s3socket.send(pack_msg)

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

