#/bin/sh
#send the tarball to gdv-archive
VERSION=$1
scp ../tmp_build/$VERSION/gdv.tgz java@sugar:/scratch/java/gdv-archive/$VERSION/.