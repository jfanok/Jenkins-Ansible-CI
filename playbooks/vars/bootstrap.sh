#!/bin/bash

# SSH Key
eval `ssh-agent` &&  ssh-add ~/.ssh/server_key.pem 

# AWS Keys
export AWS_ACCESS_KEY_ID=
export AWS_SECRET_ACCESS_KEY=

# Source environment vars
source ~/ansible/hacking/env-setup