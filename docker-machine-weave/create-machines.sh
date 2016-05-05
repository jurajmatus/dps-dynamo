
if [ -z $(docker-machine inspect master | grep 'Host does not exist') ] ; then
   docker-machine rm master
fi

if [ -z $(docker-machine inspect slave | grep 'Host does not exist') ] ; then
   docker-machine rm slave
fi

# Machine options are in (master|slave).json - set them there

docker-machine create\
 -d virtualbox\
 master
 
docker-machine create\
 -d virtualbox\
 slave

