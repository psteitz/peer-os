#!groovy

// Cofiguring builder:
// Manage Jenkins -> Global Tool Configuration -> Maven installations -> Add Maven:
// Name - M3
// MAVEN_HOME - path to Maven3 home dir
//
// Manage Jenkins -> Configure System -> Environment variables
// SS_TEST_NODE - ip of SS node for smoke tests 
//
// Approve methods:
// in build job log you will see 
// Scripts not permitted to use new <method>
// Goto http://jenkins.domain/scriptApproval/
// and approve methods:
// method groovy.json.JsonSlurperClassic parseText java.lang.String
// new groovy.json.JsonSlurperClassic
//
// TODO:
// - refactor getVersion function on native groovy
// - Stash and unstash for builded artifacts (?)

import groovy.json.JsonSlurperClassic

node() {
	def mvnHome = tool 'M3'
	def workspace = pwd() 
	def artifactVersion = getVersion("${workspace}@script/management/pom.xml")
	String artifactDir = "/tmp/jenkins/${env.JOB_NAME}"
	String debFileName = "management-${env.BRANCH_NAME}.deb"
	String templateFileName = "management-subutai-template_${artifactVersion}-${env.BRANCH_NAME}_amd64.tar.gz"

	
	stage("Build management deb/template")
	// Use maven to to build deb and template files of management

	checkout scm

	// create dir for artifacts
	sh """
		if test ! -d ${artifactDir}; then mkdir -p ${artifactDir}; fi
	"""

	// build deb
	sh """
		cd management
		${mvnHome}/bin/mvn clean install -Dmaven.test.skip=true -P deb -Dgit.branch=${env.BRANCH_NAME}
		find ${workspace}/management/server/server-karaf/target/ -name *.deb | xargs -I {} mv {} ${artifactDir}/${debFileName}
	"""

	// create management template
	sh """ssh root@gw.intra.lan <<- EOF
		set -e
		
		/apps/bin/subutai destroy management
		/apps/bin/subutai clone openjre8 management
		/bin/sleep 5
		/bin/cp /mnt/lib/lxc/jenkins/rootfs/${artifactDir}/${debFileName} /mnt/lib/lxc/management/rootfs/tmp/
		/apps/bin/lxc-attach -n management -- apt-get update
		/apps/bin/lxc-attach -n management -- sync
		/apps/bin/lxc-attach -n management -- apt-get -y --force-yes install --only-upgrade procps
		/apps/bin/lxc-attach -n management -- apt-get -y --force-yes install --only-upgrade udev
		/apps/bin/lxc-attach -n management -- apt-get -y --force-yes install subutai-dnsmasq subutai-influxdb curl gorjun
		/apps/bin/lxc-attach -n management -- dpkg -i /tmp/${debFileName}
		/apps/bin/lxc-attach -n management -- sync
		/bin/rm /mnt/lib/lxc/management/rootfs/tmp/${debFileName}
		/apps/bin/subutai export management -v ${artifactVersion}-${env.BRANCH_NAME}

		mv /mnt/lib/lxc/tmpdir/management-subutai-template_${artifactVersion}-${env.BRANCH_NAME}_amd64.tar.gz /mnt/lib/lxc/jenkins/rootfs/${artifactDir}
	EOF"""

	stage("Update management on test node")
	// Deploy builded template to remore test-server

	// destroy existing management template on test node
	sh """ssh root@${env.SS_TEST_NODE} <<- EOF
		set -e
		subutai destroy management
		rm /mnt/lib/lxc/tmpdir/management-subutai-template_*
	EOF"""

	// copy generated management template on test node
	sh """
		scp ${artifactDir}/management-subutai-template_${artifactVersion}-${env.BRANCH_NAME}_amd64.tar.gz root@${env.SS_TEST_NODE}:/mnt/lib/lxc/tmpdir
	"""

	// install genetared management template
	sh """ssh root@${env.SS_TEST_NODE} <<- EOF
		set -e
		echo -e '[template]\nbranch = ${env.BRANCH_NAME}' > /var/lib/apps/subutai/current/agent.gcfg
		echo -e '[cdn]\nbranch = cdn.local' >> /var/lib/apps/subutai/current/agent.gcfg
		echo y | subutai import management
		sed -i -e 's/cdn.local/cdn.subut.ai/g' /mnt/lib/lxc/management/rootfs/etc/apt/sources.list.d/subutai-repo.list
	EOF"""

	// wait until SS starts
	sh '''
		set +x
		echo "Waiting SS"
		while [ $(curl -k -s -o /dev/null -w %{http_code} "https://${env.SS_TEST_NODE}:8443/rest/v1/peer/ready") != "200" ]; do
			sleep 5
		done
	'''


	stage("Integration tests")
	// Run Serenity Tests

	git url: "https://github.com/subutai-io/playbooks.git"
	sh """
		./run_tests_qa.sh -m ${env.SS_TEST_NODE}
		./run_tests_qa.sh -s all
		./run_tests_qa.sh -r
	"""

	stage("Deploy artifacts on kurjun")
	// Deploy builded and tested artifacts to cdn

	// cdn auth creadentials 
	String url = "https://eu0.cdn.subut.ai:8338/kurjun/rest"
	String user = "jenkins"
	def authID = sh (script: "curl -s -k ${url}/auth/token?user=${user} | gpg --clearsign --no-tty", returnStdout: true)
	def token = sh (script: "curl -s -k -Fmessage=\"${authID}\" -Fuser=${user} ${url}/auth/token", returnStdout: true)

	// upload artifacts on cdn
	// upload deb
	String responseDeb = sh (script: "curl -s -k https://eu0.cdn.subut.ai:8338/kurjun/rest/apt/info?name=${debFileName}", returnStdout: true)
	sh "curl -s -k -Ffile=@${artifactDir}/${debFileName} -Ftoken=${token} ${url}/apt/upload"
	// def signatureDeb = sh (script: "curl -s -k -Ffile=@${artifactDir}/${debFileName} -Ftoken=${token} ${url}/apt/upload | gpg --clearsign --no-tty", returnStdout: true)
	// sh "curl -s -k -Ftoken=${token} -Fsignature=\"${signatureDeb}\" ${url}/auth/sign"

	// delete old deb
	if (responseDeb != "Not found") {
		def jsonDeb = jsonParse(responseDeb)	
		sh "curl -s -k -X DELETE ${url}/apt/delete?id=${jsonDeb["id"]}'&'token=${token}"
	}

	// upload template
	String responseTemplate = sh (script: "curl -s -k https://eu0.cdn.subut.ai:8338/kurjun/rest/template/info?name=${templateFileName}", returnStdout: true)
	def signatureTemplate = sh (script: "curl -s -k -Ffile=@${artifactDir}/${templateFileName} -Ftoken=${token} ${url}/template/upload | gpg --clearsign --no-tty", returnStdout: true)
	sh "curl -s -k -Ftoken=${token} -Fsignature=\"${signatureTemplate}\" ${url}/auth/sign"

	// delete old template
	if (responseTemplate != "Not found") {
		def jsonTemplate = jsonParse(responseTemplate)
		sh "curl -s -k -X DELETE ${url}/template/delete?id=${jsonTemplate["id"]}'&'token=${token}"
	}

}

def getVersionFromPom(pom) {
	def matcher = readFile(pom) =~ '<version>(.+)</version>'
	matcher ? matcher[1][1] : null
}

def String getVersion(pom) {
	def pomver = getVersionFromPom(pom)
	def ver = sh (script: "/bin/echo ${pomver} | cut -d '-' -f 1", returnStdout: true)
	return "${ver}".trim()
}

@NonCPS
def jsonParse(def json) {
    new groovy.json.JsonSlurperClassic().parseText(json)
}
