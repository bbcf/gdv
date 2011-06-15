# Modules #
import httplib2, urllib

# All parameters #
args = {
        'data'        : '''{"operation_type":"desc_stat","characteristic":"number_of_features","compare_parents":[],"per_chromosome":["per_chromosome"],"filter":[{"name":"Ribi genes","path":"/scratch/genomic/tracks/ribosome_genesis.sql"}],"ntracks":[{"name":"S. cer refseq genes","path":"/scratch/genomic/tracks/all_yeast_genes.sql"},{"name":"RP genes","path":"/scratch/genomic/tracks/ribosome_proteins.sql"}]}''',
        'output_location' : '/tmp/gMiner',
        'callback_url'    : 'http://localhost:9999/',
        'job_id'          : '1'
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
