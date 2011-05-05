"""
=================
Script: gm_server
=================

Implementation a web server around the functinoality of gFeatMiner. 

The servers listens for requests. Formats them. Sends them off to the gMiner library. Formats the result obtained and sends that off in a HTTP POST to an URL specified in the original request. This can be usefull if GDV wants to send requests to an instance of gm_server and recieve answers.

A typical request arriving to the server would be:

{'form':'''{"compare_parents":["compare_parents"],"per_chromosome":[],"selected_regions":"M:7455 .. 7720;M:8315 .. 8792","tracks":"{1:{'name':'Rip genes','path':'/tpm/asddasd.sql'},2:{'name':'rp genes','path':'/tmp/adsd.sql}}"}''','callback_url':'http://svitsrv25.epfl.ch/gdv_dev/GFeatMiner','from':'1','job_id':'1'}

A typical answer from the server would be:

{'from':1,'job_id':'1','results':{'files':['/tmp/asd.sql','/tmp/qq.sql'],'type':'sql'}}

"""

# General modules #
import cherrypy, httplib2, urllib, json

# Other modules #
import gMiner

# Specific variables #
from gMiner.gm_constants import gm_project_name, gm_project_version

###########################################################################
class gmServer(object):
    def __init__(self, port=7522):
        self.port = port
         
    def serve(self):
        # Change the server name #
        serverTag = gm_project_name + "/" + str(gm_project_version)
        cherrypy.config.update({'tools.response_headers.on':      True,
                                'tools.response_headers.headers': [('Server', serverTag)]})
        # Change the port #
        cherrypy.server.socket_port = self.port
        # Add post processing #
        cherrypy.tools.post_process = cherrypy.Tool('on_end_request', post_process)
        # Start Server #
        cherrypy.quickstart(CherryRoot(), config={'/':
            {'request.dispatch': cherrypy.dispatch.MethodDispatcher(),
             'tools.post_process.on': True}})

class CherryRoot(object):
    exposed = True
    def GET(self, **kwargs):
        return pre_process(**kwargs)
    def POST(self, **kwargs):
        return pre_process(**kwargs)

def pre_process(**kwargs):
    # Create a job # 
    global job
    job = kwargs
    return 'Job added to queue'

#-------------------------------------------------------------------------#
def post_process(**kwargs):
    # Format the request #
    global job
    request = {}
    # Run the request #
    try:
        result = gMiner.run(request)
    except Exception as err:
        answer = str(err)
    else:
        type = result[0].split('.')[-1]
        answer = {'files': result, 'type': type}
    # Make an HTTP POST #
    connection = httplib2.Http()
    body = urllib.urlencode({'from': job['from'],'job_id': job['job_id'], 'result': answer})
    headers = {'content-type': 'application/x-www-form-urlencoded'}
    address = job['callback_url']
    response, content = connection.request(address, "POST", body=body, headers=headers)

###########################################################################
if __name__ == '__main__': gmServer().serve()
