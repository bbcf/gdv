# Modules #
import httplib2, urllib

# All parameters #
args = {'output_location' : '/tmp/gMiner',
        'callback_url'    : 'http://localhost:9999/',
        'job_id'          : '2',
        'data'            : '''{"operation_type":  "genomic_manip",
                                "manipulation":    "bool_and",
                                "compare_parents": [],
                                "per_chromosome":  ["per_chromosome"],
                                "ntracks":         [{"name":"S. cer refseq genes",
                                                     "path":"/scratch/genomic/tracks/hiv_bushman.sql"},
                                                    {"name":"RP genes",
                                                     "path":"/scratch/genomic/tracks/refseq_ucsc.sql"}]}'''
}

# Make the request #
connection = httplib2.Http()
body = urllib.urlencode(args)
headers = {'content-type':'application/x-www-form-urlencoded'}
address = "http://localhost:7522/"

# Send it #
response, content = connection.request(address, "POST", body=body, headers=headers)
print "Server status: ",  response.status
print "Server reason: ",  response.reason
print "Server content:", content

#-----------------------------------#
# This code was written by the BBCF #
# http://bbcf.epfl.ch/              #
# webmaster.bbcf@epfl.ch            #
#-----------------------------------#
