"""
=================
Script: gm_server
=================

Implementation of a web server around the functionality of gFeatMiner. 

The servers listens for requests. When it gets one, it formats them. Then, sends the request off to the gMiner library. Formats the result obtained and sends that off in a HTTP POST to an URL specified in the original request. This can be useful if GDV wants to send requests to an instance of gm_server and receive answers.

################################################################
#########################   REQUEST   ##########################
################################################################
A typical request arriving to the server would be (HTML FORM ENCODED):
 - output_location = /tmp/ (path where you want the output)
 - callback_url = http://server.org/gfeatminer (URL to post back the response)
 - data= {} (the data from the web form, it is JSON formatted)

 # detailled description of 'data' parameter :

 - operation_type : 'desc_stat' (self-explanatory)
 - characteristic : 'base_coverage' (self-explanatory)
 - compare_parents :[] (a boolean)
 - per_chromosome : ['per_chromosome'] (idem)
 - filter:[{path:'/data/gdv_dev/files/115d5da7db0c588ae95bb91a5710ba2147be3df0.db',name:'foo'}] (path to a file that take the role of a filter, a selection)
 - ntracks:[{path:'/data/gdv_dev/files/d08aa4569c17aa79abc57c3b44da6abab927fa2.db',name:'bar'},{...},...] (path to the file(s) to process. It can be ordered)
 - job_id : 1 (the identifier of the job)



E.G. : 
{'output_location': u'/data/gdv_dev/gFeatMiner/208', 'data': u'{"compare_parents":["compare_parents"],"per_chromosome":["per_chromosome"],"characteristic":"base_coverage","operation_type":"desc_stat","ntracks":[{"name":"RNAPol_fwd.sql","path":"/data/gdv_dev/files/6cb8a2f895e2cf829912bdbd244a6092289fe82f.db"}],"filter":[{"name":"chr(2839500,2909000)_chr(3023000,3070000)_.db","path":"/data/gdv_dev/files/952f788e0e0c5b43ec854515c9ebbad6a5c5e771.db"}]}', 'callback_url': u'http://svitsrv25.epfl.ch/gdv_dev/post', 'output_name': u'gfeatminer_output', 'job_id': u'208'}



################################################################
#########################  RESPONSE  ###########################
################################################################

A typical answer from the server would be (HTML FORM ENCODED):
 
 - id = 'job' (static)
 - action = 'gfeatresponse' (static)
 - job_id = the job identifier
 - data = {} (the result, JSON formatted)
 
# detailled description of 'data' parameter :
 - files : [] a list of result files in JSON
 -  ...  path : path to the file
 -  ...  type : type of the file
 
E.G. :

{'id':'job','action':'gfeatresponse','job_id':'1','result':'''{"files":[{"path":"/tmp/asd.sql","type":"sql"},{"path":"/tmp/qq.png","type":"png"}]}'''}

If an error is found, the answer could be:

{'id':'job','action':'gfeatresponse','job_id':'1','result':'''{"type":"error","msg":"Text about error","html":"<html>Displayable version for user</html>"'''}
"""

# General modules #
import cherrypy, httplib2, urllib, json, sys, cgitb, warnings

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

#-------------------------------------------------------------------------#
def pre_process(**kwargs):
    # Create a job # 
    global jobs
    jobs.append(kwargs)
    # Return result #
    return 'Job added to queue'

def post_process(**kwargs):
    global jobs
    job = jobs.pop(0)
    print "#######################"
    print job
    print '#######################'
    try:
        # Format the input #
        request = json.loads(job['data'])
        if request.has_key('compare_parents' ): request['compare_parents' ] = bool(request['compare_parents' ])
        if request.has_key('per_chromosome'  ): request['per_chromosome'  ] = bool(request['per_chromosome'  ])
        # Filter #
        
        # Special parameters #
        # if request.has_key('tracks'):
        #     request.update(parse_tracks(request['tracks']))
        #     request.pop('tracks')
        # Extra parameters #
        request['output_location'] = job['output_location']
        # Unicode filtering #
        request = dict([(k.encode('ascii'),v) for k,v in request.items()])
        # To remove in production version # 
        print "Request:", request
        # Run the request # 
        files = gMiner.run(**request)
        # Format the output #
        result = {'files': [dict([('path',p),('type',p.split('.')[-1])]) for p in files]}
    except Exception as err:
        print "The job raised an error: ", str(err)
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            result = {'type':'error', 'html':cgitb.html(sys.exc_info()), 'msg': str(err)}
            # To remove in production version # 
            print cgitb.text(sys.exc_info()) 
    finally:
        connection = httplib2.Http()
        body       = urllib.urlencode({'from': job['from'],'job_id': job['job_id'], 'result': json.dumps(result)})
        headers    = {'content-type': 'application/x-www-form-urlencoded'}
        address    = job['callback_url']
        response, content = connection.request(address, "POST", body=body, headers=headers)

#-------------------------------------------------------------------------#
def parse_tracks(input):
    output      = dict([('track' + str(k),v['path'])                          for k,v in input.items()])
    output.update(dict([('track' + str(k) + '_name', v.get('name', 'Unamed')) for k,v in input.items()]))
    return output

def parse_regions(input):
    return ';'.join([':'.join([x.split(':')[0], x.split(':')[1].split(' .. ')[0], x.split(':')[1].split(' .. ')[1]]) for x in input.split(';')])

###########################################################################
if __name__ == '__main__': gmServer().serve()
