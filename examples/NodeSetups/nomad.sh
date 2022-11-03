#!/usr/bin/env bash

# The following script in a supplementary material for our paper and describes the node setup for our self-hosted
# Nomad cluster and Nomad cluster running on Google Cloud VMs
# THIS SCRIPT IS NOT EXECUTABLE OUT OF THE BOX AND IS MEANT FOR REVIEWERS TO SEE WHICH DEPENDENCIES HAVE BEEN INSTALLED

# ------------ 1. Time sync -----------------
sudo apt install systemd-timesyncd
sudo systemctl start systemd-timesyncd

# ------------ 2. Install nomad (execute on all nodes) -------------
sudo apt update
sudo apt install -y gnupg gnupg1 gnupg2 software-properties-common
curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo apt-key add -
sudo apt-add-repository "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main"
sudo apt-get update && sudo apt-get install nomad
nomad
mkdir nomad
# Install consul (currently needed, see: https://github.com/hashicorp/waypoint/issues/3376)
wget -O- https://apt.releases.hashicorp.com/gpg | gpg --dearmor | sudo tee /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install consul
mkdir consul

# ------------ 3. Install nomad master (master node only) -------------
sudo nomad agent -server -bootstrap-expect=1 -data-dir=./nomad &
disown -h %1
sudo consul agent -server -bootstrap-expect=1 -data-dir=./consul &
disown -h %2

# ------------ 4. Install nomad worker (worker nodes only) -------------
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo nomad agent -client -data-dir=./nomad -servers=${MASTER_NODE_IP} &
disown -h %1
sudo consul agent -bind ${THIS_NODES_IP} -join=${MASTER_NODE_IP} -data-dir=./consul &
disown -h %2
