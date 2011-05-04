# General Modules #
import cherrypy, httplib2, urllib

# gFeatMiner #
from gMiner import gmJob
from gMiner.gm_constants import *

###########################################################################
class gmServer(object):
    def __init__(self, port=7522):
        self.HTTP_PORT = port
        cherrypy.log("init of the server")
         
    def serve(self):
        cherrypy.log("serve")
        # Change the server name #
        serverTag = gm_project_name + "/" + str(gm_project_version)
        cherrypy.config.update({'tools.response_headers.on':      True,
                                'tools.response_headers.headers': [('Server', serverTag)]})
        # Change the port #
        cherrypy.server.socket_port = self.HTTP_PORT
        # Add post processing #
        cherrypy.tools.post_process = cherrypy.Tool('on_end_request', post_process)
        # Start Server #
        cherrypy.quickstart(CherryRoot(), config={'/':
            {'request.dispatch': cherrypy.dispatch.MethodDispatcher(),
             'tools.post_process.on': True}})

#-------------------------------------------------------------------------#
class CherryRoot(object):
    exposed = True
    def GET(self, **kwargs):
        return self.POST(**kwargs)

    def POST(self, **kwargs):
        nargs = format_request(**kwargs)
        # Run the request #
        global job
        job = gmJob(nargs)
        error, result, type = job.prepare_catch_errors()
        # Check if callback or errors #
        if error != 200:
            del job
        else:
            if not job.request.has_key('callback_url'):
                error, result, type = job.run_catch_errors()
                del job
        # Return result #
        cherrypy.response.headers['Content-Type'] = type
        cherrypy.response.status = error
        return result

    # def GET(self):
    #     cherrypy.response.status = 301
    #     cherrypy.response.headers['Location'] = gm_doc_url
    #     return 'Redirecting to documentation'

#-------------------------------------------------------------------------#
def post_process(**kwargs):
    # Check for a job #
    cherrypy.log("post process :"+str(kwargs))
    global job
    try:
        if not job:
            cherrypy.log("no job")
            return
    except NameError:
        return
    if not job.request.has_key('callback_url'): return
    # Run the job #
    error, result, type = job.run_catch_errors()
    # Get the id #
    if job.request.has_key('id'):
        id = job.request['id']
        body = urllib.urlencode({'id':     id,
                                 'status': error, 
                                 'result': str(result),
                                 'type':   type})
    else:
        body = urllib.urlencode({'status': error, 
                                 'result': str(result),
                                 'type':   type})
    # Make an HTTP POST #
    connection = httplib2.Http()
    headers = {'content-type': 'application/x-www-form-urlencoded'}
    address = job.request['callback_url']
    # Send it #
    response, content = connection.request(address, "POST",
                                           body   = body,
                                           headers= headers)

###########################################################################


TEST_REQUEST = {'form':"""{"compare_parents":["compare_parents"],"per_chromosome":["per_chromosome"],"selected_regions":"M:7455 .. 7720;M:8315 .. 8792","tracks":"Rip140_day0_May_treat_afterfiting_chr6.wig;Rip140_day0_May_treat_afterfiting_chr7.wig"}""",'callback_url':'http://svitsrv25.epfl.ch/gdv_dev/GFeatMiner','from':'1','job_id':'1','version':'1.0.0'}
import json

"""

Format the request to process a GFM job

"""

def format_request(**kwargs):
    nargs={}
    kwargs=TEST_REQUEST
    cherrypy.log("format request :"+str(kwargs))
    params="";
    if(kwargs['form']):
        params=json.loads(kwargs['form'])
        for param in params :
            if(param=="selected_regions"):
                handle_selected_regions(nargs,params[param],param)
            elif(param=="tracks"):
                handle_tracks(nargs,params[param])
            else :
                nargs[param]=params[param]

    ##TODO put versionning
    ## and the other parameters
    cherrypy.log(str(nargs))
    return nargs




def handle_selected_regions(dic,params,param):
    dic[param]=params
def handle_tracks(dic,tracks):
    l=tracks.split(";")
    cherrypy.log(str(len(l)))
    for i in range(1,len(l)+1):
        dic["track_"+str(i)]=l[i-1]
    



if __name__ == '__main__': gmServer().serve()
