#/bin/bash 
Hostname="localhost"
Port=9092

# Get the options
while getopts ":h:p:" option; do
   case $option in
      h)       # Re-assign the default hostname.
         Hostname=$OPTARG;;
      p)       # Re-assign the default port.
         Port=$OPTARG;;
     \?) # Invalid option
         echo "Error: Invalid option"
         exit;;
   esac
done

cd kafka
./bin/zookeeper-server-start.sh config/zookeeper.properties &
./bin/kafka-server-start.sh config/server.properties        &
./bin/kafka-topics.sh --create --topic loss-events --bootstrap-server $Hostname:$Port &
./bin/kafka-topics.sh --create --topic big-losses --bootstrap-server $Hostname:$Port