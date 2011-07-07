import sys
sys.path.insert(0, "./gMiner")
sys.path.insert(0, "./bbcflib")
sys.path.insert(0, "./bein")
import bbcflib
print 'Using this bbcflib:', bbcflib, 'v', bbcflib.__version__
import gMiner
print 'Using this gMiner:', gMiner, 'v', gMiner.__version__
execfile("gm_server.py")
