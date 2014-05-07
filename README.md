Jenkins-Ansible-CI
==================

Jenkins/Ansible continuous integration setup tutorial

The purpose of this repo is to compile some notes for setting up a basic Jenkins continuous integration server on an Amazon EC2 Instance.

Tools:

Jenkins - multi-purpose continuous integration server

Ansible - Automated deployment tool

SBT - scala build tool

AWS - a variety of web services provided by Amazon



Setup Amazone EC2 Server Instance

Log in or create your AWS account - https://aws.amazon.com/


Navigate to the EC2 dashboard from the management console


Setup a new security group under network and security. You will have to give it a name and a short description and select which outbound and inbound ports are open (you can leave vpc as default). Open up the following inbound ports:
 - 80 (TCP) for http
 - 8080 (TCP) for https
 - 22 (TCP) for ssh
 - 8152 (TCP/UDP)
 - 17123 (TCP/UDP)

You can specify which IPs have access to these ports for tighter security.


Next you want to launch your EC2 instance. Under Instances select "launch instance". Configure your instance settings. For simplicity purposes I selected the default Ubuntu AMI and left everything at default but selected the security group we created in the step before. You will also have to select or create a key pair for ssh access.



Configure Jenkins

SSH into your server

make sure your public key has the right permissions

> chmod 400 yourkey.pem

ssh 

> ssh -i yourkey.pem ubuntu@your_servers_public_dns

install jenkins for first time

> wget -q -O - http://pkg.jenkins-ci.org/debian/jenkins-ci.org.key | sudo apt-key add -
> sudo sh -c 'echo deb http://pkg.jenkins-ci.org/debian binary/ > /etc/apt/sources.list.d/jenkins.list'
> sudo apt-get update
> sudo apt-get install jenkins

The jenkins daemon should now be running in the background and its GUI is accesible at http://your-public-dns:8080. A jenkins user has also been created on your system with a home directory of /var/lib/jenkins and is accesible with 

>  sudo su jenkins

You should set up the jenkins user to not ask for a password on sudo commands to make configuration easier

> visudo -f /etc/sudoers.d/your_sudo_file

The first thing you will want to do is set up security for the GUI so that not everyone can access it. Go into Manage Jenkins -> Configure Global Security  and select 'Enable Security'.

Under Access Control -> Security Realm select 'Jenkins' own user database and leave 'allow users to sign up' enabled. And leave Authorization at Anyone can do anything. Click Save or Apply and Create user login credentials.

Go back to Configure Global Security and under Access Control -> Authorization select 'Matrix-based security'. You can then give yourself or an administrator rights to everything and control other users permissions. You can also disable user sign ups now.


The next step is to manage your jenkins plugins. For our case we will need to install the following:

- Git Client Plugin
- Git Plugin


On the server, git:

> sudo apt-get install git


You will need to setup git ssh access on the jenkins user. You can follow this tutorial to do so. https://help.github.com/articles/generating-ssh-keys


You can now create a Job off of a github repo. Select new Job/Item, give it a name, and select 'build a free-style project'. 

Configure 'Source Code Management' to use Git. Use the format git@github.com:user/Repo.git for the 'Repository URL'. You can leave the credentials at none since we have already set up ssh authentication on the jenkins user on our server. You may also select which branch the project will track.

You can test your Job by running 'Build Now' and browsing your 'Workspace' to make sure your project is there.



Set up Ansible for automating deployments

I will start off by stating that Ansible is not necessary for setting up your continous integration server. You can compile and deploy code using only Jenkins or with other tools. However, our team has found that Ansible is extremely useful for automating and simplifying deployments. 

Instructions for installing Ansible are here - http://docs.ansible.com/intro_installation.html#installation
> sudo apt-add-repository ppa:rquillo/ansible 
> sudo apt-get update
> sudo apt-get install ansible

Thats it! Ansible should now be installed on your server.


Now you will want to configure ansible to work with your aws inventory in order to launch new instances. The instructions to do so are provided here http://docs.ansible.com/intro_dynamic_inventory.html#example-aws-ec2-external-inventory-script

You want to make sure you have pip installed and then make sure you use pip to install the boto module in order for the ec2 script to work.

Also make sure to setup your AWS_ACCESS_KEY_ID and your AWS_SECRET_ACCESS_KEY in order to authenticate your account.



Launch a new EC2 Instance with ansible


You can now run the launch-server.yml playbook which will launch a new EC2 with the configurations specified in the play.

This ec2 server can now be referenced given its tag name, which allows us to communicate with our instances remotely from our CI server.


Set Up Your Build Configurations

Within the jenkins Job configuration you can now set up tasks to run on a build. For our Jenkins build configuration we want to keep the build logic as simple as possible so we will separate our build logic into Ansible playbooks & plays. This way we are able to reuse plays if necessary in different projects without having to reconfigure a lot of stuff in Jenkins.

For our specific project we want to test our service to make sure it passes our requirements, compile/package our project, distribute our executables to the desired EC2 instance, and finally kill our old build and boot our new build.

We can separate this logic into ansible roles. We create test, package, distribute, and deploy roles inside of our playbooks directory. Test executes sbt test-only, package executes sbt build, distribute copies over our executables to our ec2 instance, and deploy checks for a running service and kills it if its up and then initiates the new executables.

We can create a playbook 'test-package.yml' with the test and package roles and add the flag -i files/localhosts to select this playbook to run locally. We can the create another playbook 'distribute-deploy' with the distribute, undeploy, and deploy roles to be run remotely on the EC2 instances.

Finally under your jenkins build configurations we execute these commands in order using 
> ansible-playbook -i files/localhosts test-package.yml

and 
> ansible-playbook distribute-deploy.yml

If any of the plays fail, Jenkins marks the build as failed. You can also view the console output through jenkins' GUI.



Setting up Github Hook Polling on Jenkins

Within Jenkins under a Job's build configuration you can setup scm polling with a cron schedule to poll for events. This checks for any messages sent to the project endpoint. 

You can setup your github project to register changes such as commits or pull requests. This allows you to do things like run tests before pull requests are allowed to be merged. In our example we want to send a message to Jenkins to notify the project to run a new build.



We successfully configured a continuous integration server to monitor repo commits and merges and deploy a new build to our remote server. 





