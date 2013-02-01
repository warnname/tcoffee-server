#
# Headless building script 
#
# Used environment variables 
# 
# $WORKSPACE	: root path containing all build artifacts 
# $VERSION 		: (optional) the version used to tag this build
#

#
# Release flag  
# 
if [ -z $RELEASE ]; then 
export RELEASE=0
fi


if [[ (-z $VERSION) || ($VERSION == auto) ]]; then 
export SVN_REVISION=`svn info http://tcoffee.googlecode.com/svn/tserver/trunk | grep "Last Changed Rev:" | awk '{ print $4 }'`
export VERSION=r$SVN_REVISION
fi


# Flag DO_TEST, if true test are executed (default: true)
if [ -z $DO_TEST ]; then 
DO_TEST=0
fi 

#
# script directives
#

set -e
set -u
set -o nounset
set -o errexit
set -x

#
# other common variables 
#
SANDBOX=$WORKSPACE/sandbox

PLAY_VER=play-1.2.5
PLAY_ZIP=$PLAY_VER.zip
PLAY_URL=http://download.playframework.org/releases/$PLAY_ZIP
PLAY_HOME=$WORKSPACE/$PLAY_VER

BUILD_TIME=`date +%Y.%m.%d-%H:%M:%S`


#
# Display the current environment
#
function env() 
{
  echo "[ env ]"

  echo "- WORKSPACE   : $WORKSPACE"
  if [ -z CONFID ]; then 
  echo "- CONFID      : $CONFID"
  fi
  echo "- VERSION     : $VERSION"
  echo "- RELEASE     : $RELEASE"
  echo ". BUILD_TIME  : $BUILD_TIME"
  echo ". SANDBOX     : $SANDBOX"
  echo ". PLAY_VER    : $PLAY_VER" 
  echo ". PLAY_ZIP    : $PLAY_ZIP" 
  echo ". PLAY_URL    : $PLAY_URL"
  echo ". PLAY_HOME   : $PLAY_HOME"


}

#
# clean current sandbox content 
#
function clean() 
{
	echo "[ clean ]"
	rm -rf $SANDBOX

}


#
# if play does not exist download it 
#
function get_play()
{
	echo "[ get_play ]"

	if [ ! -d $PLAY_HOME ]
	then
		cd $WORKSPACE
		wget $PLAY_URL
		unzip $PLAY_ZIP
		rm $PLAY_ZIP
	fi
}


function init() 
{
	echo "[ init=$1 ]"

	#
	# Initialize some variables 
	#
	export CONFID=$1
	DIST_ROOT=$SANDBOX/distributions
	DIST_BASE=$SANDBOX/distributions/
	DIST_DIR=$DIST_BASE/$VERSION/$CONFID
	
	SERVER_NAME=server_$CONFID\_$VERSION
	SERVER_DIR=$SANDBOX/server/$SERVER_NAME
	SERVER_WAR=$SANDBOX/war

} 

function make_distribution() 
{
	echo "[ make_distribution ]"

	#
	# Clean everything
	#
	rm -rf $SERVER_DIR
	mkdir -p $SERVER_DIR/tserver

	#
	# Copy the application folders 
	#
	cp -R $WORKSPACE/tserver/app/    $SERVER_DIR/tserver/app
	cp -R $WORKSPACE/tserver/test/   $SERVER_DIR/tserver/test
	cp -R $WORKSPACE/tserver/conf/   $SERVER_DIR/tserver/conf
	cp -R $WORKSPACE/tserver/lib/    $SERVER_DIR/tserver/lib
	cp -R $WORKSPACE/tserver/public/ $SERVER_DIR/tserver/public
	cp -R $WORKSPACE/tserver/globe/  $SERVER_DIR/tserver/globe

	#
	# Copy the configuration specific /conf files
	#
	if [ -e $WORKSPACE/tserver/build/conf/$CONFID/ ]; then 
	for it in $(find $WORKSPACE/tserver/build/conf/$CONFID/ -name "*" -type f); do 
	  chmod +w $it
	  cp $it $SERVER_DIR/tserver/conf/ 
	done
	fi	
	
	# 
	# Add specific configuration to 'application.conf' file
	#
	APPCONF=$SERVER_DIR/tserver/conf/application.conf
	echo >> $APPCONF
	echo '# Build information' >> $APPCONF
	echo application.server.version=$VERSION >> $APPCONF
	echo application.server.build.time=$BUILD_TIME >> $APPCONF

	
} 


function make_zip() 
{
	echo "[ make_zip ]"

	# move to the folder to pack 
	echo Zipping content of "$SERVER_DIR"
	cd $SERVER_DIR/..

	# zip them all
	mkdir -p $DIST_DIR
	zip -r $SERVER_NAME.zip $SERVER_NAME/* -x */.svn/* -q

	# Moving to target path 
	mv $SERVER_NAME.zip $DIST_DIR
} 



#
# Make the WAR file for apollo
#
function make_war()
{
	echo "[ make_war ]"

	echo "Creating T-Coffe server WAR file: $SERVER_WAR"
	mkdir -p $DIST_DIR
	mkdir -p $SERVER_WAR
	
	#
	# creating war
	#
	echo Creating PROD war
	cd $SERVER_DIR
	$PLAY_HOME/play war ./tserver -o $SERVER_WAR/tcoffee --%$CONFID
	cd $SERVER_WAR/tcoffee
	
	#
	# packing 
	#
	zip -r ../$SERVER_NAME.war * > /dev/null
	mv ../$SERVER_NAME.war $DIST_DIR
	
	rm -rf $SERVER_WAR
	
}


#
# Run all tests and publish result to workspace
#
# Depends on: pack_server
#  
function test_server_local() 
{
	echo "[ test_server_local ]"

 	# clean required directories
	rm -rf $SANDBOX/web
	rm -rf $SANDBOX/$SERVER_NAME 
	mkdir -p $SANDBOX/$SERVER_NAME

	# unzip the distribution file
	unzip $DIST_DIR/$SERVER_NAME.zip -d $SANDBOX > /dev/null
	mv $SANDBOX/$SERVER_NAME $SANDBOX/web

	# start a play instance and invoke tests
	cd $SANDBOX/web
	./$PLAY_VER/play auto-test tserver 
	count=`ls ./tserver/test-result/*.html | wc -l`
	if [ $count == 0 ]; then
	  cat ./tserver/test-result/application.log
	  exit 1;
	fi 

	# publish the tests
	rm -rf $WORKSPACE/test-result
	cp -r $SANDBOX/web/tserver/test-result $WORKSPACE/test-result

	# check if the failure file exists
	if [ -e $WORKSPACE/test-result/result.failed ]; then exit 1; fi 
}


function pack_server_local() {
	echo "[ pack_server_local ]"

	# initialization 
	init "local"

	#
	# create the basic structure  
	make_distribution
	
	
	#	
	# Copy the Play framework
	#
	cp -R $PLAY_HOME $SERVER_DIR
	rm -rf $SERVER_DIR/$PLAY_VER/documentation/
	rm -rf $SERVER_DIR/$PLAY_VER/samples-and-tests/
	
	#
	# precompile the server application
	#
	#$PLAY_HOME/play precompile $SERVER_DIR/tserver
	
	# 
	# copy required bundles
	# 
	mkdir -p $SERVER_DIR/tserver/bundles
	rm -rf $SERVER_DIR/tserver/bundles/tcoffee
	cp -r $WORKSPACE/tserver/build/bundles/crg/tcoffee $SERVER_DIR/tserver/bundles
	
	# 
	# create start/stop script 
	#
	echo "./$PLAY_VER/play start ./tserver --%prod " > $SERVER_DIR/start.sh
	echo "for i in {5..1}; do echo -n -e \"\r\"; echo -n '~ T-Coffee is warming-up. Your browser will open in '; echo -n \"\$i secs  \"; sleep 1; done;" >> $SERVER_DIR/start.sh
	echo "" >> $SERVER_DIR/start.sh
	echo "python -c \"import webbrowser; webbrowser.open('http://localhost:9000/')\" 2> /dev/null" >> $SERVER_DIR/start.sh
	
	# create STOP batch file
	echo "./$PLAY_VER/play stop ./tserver" > $SERVER_DIR/stop.sh
	
	# create TEST bach file
	echo "./$PLAY_VER/play test ./tserver" > $SERVER_DIR/test.sh 
	
	# giving execute attribute
	chmod a+x $SERVER_DIR/*.sh
	
	#
	# Create the final zip file 
	#
	make_zip
} 


function pack_server_vital() 
{
	echo "[ pack_server_vital ]"

	# initialization 
	init "vital"

	# create the basic structure  
	make_distribution 

	# 
	# copy required bundles
	# 
	mkdir -p $SERVER_DIR/bundles
	cp -r $WORKSPACE/tserver/build/bundles/vital-it/* $SERVER_DIR/bundles

	#
	# Create the final zip file 
	#
	make_zip

}

function pack_server_crg() 
{ 
	echo "[ pack_server_crg ]"

	init "crg"
	make_distribution 
	make_war
}

function pack_server_stage() 
{ 
	echo "[ pack_server_stage ]"

	init "stage"
	make_distribution 
	make_war
}

function pack_server_palestine() 
{
	echo "[ pack_server_palestine ]"

	init "palestine"
	make_distribution 
	make_war
}

function pack_bundles() 
{
 	# Initialization 
	init "bundles-x64"
	
	# Copy required bundles 
	mkdir -p $SERVER_DIR/bundles
	cp -r $WORKSPACE/tserver/build/bundles/crg/* $SERVER_DIR/bundles

	# Create the zip package
	make_zip	
}

function all() {
	echo "[ all ]"

	env
	clean
	get_play
	
	pack_server_local
	test_server_local 

	pack_server_vital
	pack_server_palestine
	pack_server_crg
	pack_server_stage
	pack_bundles

}

#
# when at least a parameter is specified they are invoked as function call
#
if [ $# -gt 0 ] 
then
	while [ "$*" != "" ]
	do
		echo "Target: $1"
		$1
		shift
	done
else
    echo "Usage: build <target>"
    exit 1
fi

