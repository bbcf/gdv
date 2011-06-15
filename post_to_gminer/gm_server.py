"""
=================
Script: gm_server
=================

Implementation of a web server around the functionality of gFeatMiner. 

The servers listens for requests. When it gets one, it formats them. Then, sends the request off to the gMiner library. Formats the result obtained and sends that off in a HTTP POST to an URL specified in the original request. This can be useful if GDV wants to send requests to an instance of gm_server and receive answers.

#########################   REQUEST   ##########################
A typical job arriving to the server would be (HTML FORM ENCODED):
 - output_location = /tmp/ (path where you want the output)
 - callback_url = http://server.org/gfeatminer (URL to post back the response)
 - data = u'' (the data from the web form specifying the request, it is JSON formatted)

This is the detailled description of 'data' parameter:
 - operation_type : 'desc_stat' (self-explanatory)
 - characteristic : 'base_coverage' (self-explanatory)
 - compare_parents :[] (a boolean)
 - per_chromosome : ['per_chromosome'] (idem)
 - filter:[{path:'/data/gdv_dev/files/115d5da7db0c588ae95bb91a5710ba2147be3df0.db',name:'foo'}] (path to a file that take the role of a filter, a selection)
 - ntracks:[{path:'/data/gdv_dev/files/d08aa4569c17aa79abc57c3b44da6abab927fa2.db',name:'bar'},{...},...] (path to the file(s) to process. It can be ordered)
 - job_id : 1 (the identifier of the job)

Example:
    {'output_location': u'/data/gdv_dev/gFeatMiner/208', 'data': u'{"compare_parents":["compare_parents"],"per_chromosome":["per_chromosome"],"characteristic":"base_coverage","operation_type":"desc_stat","ntracks":[{"name":"RNAPol_fwd.sql","path":"/data/gdv_dev/files/6cb8a2f895e2cf829912bdbd244a6092289fe82f.db"}],"filter":[{"name":"chr(2839500,2909000)_chr(3023000,3070000)_.db","path":"/data/gdv_dev/files/952f788e0e0c5b43ec854515c9ebbad6a5c5e771.db"}]}', 'callback_url': u'http://svitsrv25.epfl.ch/gdv_dev/post', 'output_name': u'gfeatminer_output', 'job_id': u'208'}

#########################  RESPONSE  ###########################
A typical answer from the server would be (HTML FORM ENCODED):
 - id = 'job' (static)
 - action = 'gfeatresponse' (static)
 - job_id = the job identifier
 - data = u'' (the result, JSON formatted)
 
This is the detailled description of 'data' parameter:
 - files : [] a list of result files in JSON
 -  ...  path : path to the file
 -  ...  type : type of the file
 
Example:
    {'id':'job','action':'gfeatresponse','job_id':'1','data':'''{"files":[{"path":"/tmp/asd.sql","type":"sql"},{"path":"/tmp/qq.png","type":"png"}]}'''}

If an error is found, the answer could be:
    {'id':'job','action':'gfeatresponse','job_id':'1','data':'''{"type":"error","msg":"Text about error","html":"<html>Displayable version for user</html>"'''}
"""

# General modules #
import cherrypy, httplib2, urllib, json, sys, cgitb, traceback, warnings, time

# Other modules #
import gMiner

# Specific variables #
from gMiner.constants import gm_project_name, gm_project_version

# Job list #
global jobs
jobs = []

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
            {'request.dispatch':      cherrypy.dispatch.MethodDispatcher(),
             'tools.post_process.on': True}})

class CherryRoot(object):
    exposed = True
    def GET(self, **kwargs):  return pre_process(**kwargs)
    def POST(self, **kwargs): return pre_process(**kwargs)

###########################################################################
def pre_process(**kwargs):
    global jobs
    jobs.append(kwargs)
    return 'Job added to queue'

def post_process(**kwargs):
    try:
        # Get a job from the list #
        global jobs
        job = jobs.pop(0)
        # Load the form #
        request = json.loads(job['data'])
        # Get the job id #
        job['job_id'] = request.pop('job_id') 
        # Get the output location #
        request['output_location'] = job.pop('output_location')
        # Format the request #
        if request.has_key('compare_parents' ): request['compare_parents' ] = bool(request['compare_parents'  ])
        if request.has_key('per_chromosome'  ): request['per_chromosome'  ] = bool(request['per_chromosome'   ])
        if request.has_key('filter'):
            request['selected_regions'] = request['filter'][0]['path']
            request.pop('filter')
        if request.has_key('ntracks'):
            request.update(dict([('track' + str(i+1), v['path'])                         for i,v in enumerate(request['ntracks'])]))
            request.update(dict([('track' + str(i+1) + '_name', v.get('name', 'Unamed')) for i,v in enumerate(request['ntracks'])]))
            request.pop('ntracks')
        # Unicode filtering #
        request = dict([(k.encode('ascii'),v) for k,v in request.items()])
        # Run the request #
        files = gMiner.run(**request)
        # Format the output #
        result = {'files': [dict([('path',p),('type',p.split('.')[-1])]) for p in files]}
        # Report success #
        print '\033[1;33m[' + time.asctime() + ']\033[0m \033[42m' + files[0] + '\033[0m'
    except Exception as err:
        traceback.print_exc()
        print '\033[1;33m[' + time.asctime() + ']\033[0m \033[41m' + str(err) + '\033[0m'
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            result = {'type':'error', 'html':cgitb.html(sys.exc_info()), 'msg': str(err)}
    finally:
        result     = locals().get('result', '')
        connection = httplib2.Http()
        body       = urllib.urlencode({'id':'job', 'action':'gfeatresponse', 'job_id':job.get('job_id', -1), 'data':json.dumps(result)})
        headers    = {'content-type': 'application/x-www-form-urlencoded'}
        address    = job['callback_url']
        response, content = connection.request(address, "POST", body=body, headers=headers)

#-------------------------------------------------------------------------#
if __name__ == '__main__': gmServer().serve()
