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





