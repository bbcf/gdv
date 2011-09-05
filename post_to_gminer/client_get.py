# Modules #
import cherrypy, json

# Cherrypy server #
class CherryRoot(object):
    exposed = True
    def POST(self, **kwargs):
        print "Answer:", kwargs
        #print "Result:", json.loads(kwargs['result'])

# Prepare to recieve the response #
cherrypy.server.socket_port = 9999
cherrypy.quickstart(CherryRoot(), config={'/':
    {'request.dispatch': cherrypy.dispatch.MethodDispatcher()}})

#-----------------------------------#
# This code was written by the BBCF #
# http://bbcf.epfl.ch/              #
# webmaster.bbcf@epfl.ch            #
#-----------------------------------#
