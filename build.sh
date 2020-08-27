#!/bin/csh -f
#=================================================================
# BUILDING SCRIPT for COATJAVA PROJECT (first maven build)
# then the documentatoin is build from the sources and commited
# to the documents page
#=================================================================
# Maven Build

if(`filetest -e lib` == '0') then
    mkdir lib
endif


echo "Building hdIceMon..."
    mvn clean
    mvn install
    cp target/hdIceMon-*-jar-with-dependencies.jar lib/


# Finishing touches
echo ""
echo "--> Done building....."
echo ""
echo "    Usage : build.sh"
echo ""
