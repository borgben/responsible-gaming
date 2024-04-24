#!/bin/bash
############################################################
# Help                                                     #
############################################################
Help()
{
   # Display Help
   echo "Add description of the script functions here."
   echo
   echo "Syntax: scriptTemplate [-g|h|v|V]"
   echo "options:"
   echo "g     Print the GPL license notification."
   echo "h     Print this Help."
   echo "v     Verbose mode."
   echo "V     Print software version and exit."
   echo
}

############################################################
############################################################
# Main program                                             #
############################################################
############################################################

# Set variables
Hostname="localhost"
Port="5432"
Username="postgres"
Password="default"

############################################################
# Process the input options. Add options as needed.        #
############################################################
# Get the options
while getopts "T:H:h:p:u:P:" option; do
   case $option in
      H)    # display Help.
         Help
         exit;;
      h)       # Re-assign the default hostname.
         Hostname=$OPTARG;;
      p)       # Re-assign the default port.
         Port=$OPTARG;;
      u)       # Re-assign the default username. 
         Username=$OPTARG;;
      P)
         Password=$OPTARG;;
     \?) # Invalid option
         echo "Error: Invalid option"
         exit;;
   esac
done

echo $Hostname
echo $Port
echo $Username
 
psql -h $Hostname -p $Port -U $Username postgres -f ../Database/data_radar.sql -f ../Database/big_losses.sql -f ../Database/customer_losses.sql