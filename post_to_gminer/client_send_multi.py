# Modules #
from multiprocessing import Process
import httplib2, urllib

def send_request(job_id=1, characteristic="number_of_features"):
    # Set up request #
    args = {
            'data'            : '''{"operation_type":"desc_stat","characteristic":"''' + characteristic + '''","compare_parents":[],"per_chromosome":["per_chromosome"],"ntracks":[{"name":"S. cer refseq genes","path":"/scratch/genomic/tracks/all_yeast_genes.sql"},{"name":"RP genes","path":"/scratch/genomic/tracks/ribosome_proteins.sql"}]}''',
            'output_location' : '/tmp/gMiner',
            'callback_url'    : 'http://localhost:9999/',
            'job_id'          : str(job_id),
    }
    # Make the request #
    connection = httplib2.Http()
    body = urllib.urlencode(args)
    headers = {'content-type':'application/x-www-form-urlencoded'}
    address = "http://localhost:7522/"
    # Send it #
    response, content = connection.request(address, "POST", body=body, headers=headers)
    print "[" + str(job_id) + "] Server status: ",  response.status
    print "[" + str(job_id) + "] Server reason: ",  response.reason
    print "[" + str(job_id) + "] Server content:",  content

if __name__ == '__main__':
    chara_dict = {
        0: 'number_of_features',
        1: 'base_coverage',
        2: 'length',
        3: 'score',
    }
    procs = [Process(target=send_request, args=(i,chara_dict[i%4])) for i in range(12)]
    for p in procs: p.start()

#-----------------------------------------#
# This code was written by Lucas Sinclair #
# lucas.sinclair@epfl.ch                  #
#-----------------------------------------#
