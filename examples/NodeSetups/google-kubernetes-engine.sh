#!/usr/bin/env bash

# The following script in a supplementary material for our paper and describes the node setup for our the Google Kubernetes
# Engine cluster
# THIS SCRIPT IS NOT EXECUTABLE OUT OF THE BOX AND IS MEANT FOR REVIEWERS TO SEE WHICH DEPENDENCIES HAVE BEEN INSTALLED

# Go to GKE and select create cluster, settings:
# Self-managed (no autopilot)
# Type: zonal
# Zone: europe-west3-c
# Control Plane: Static Version
# Version: 1.24.4-gke.800
# Nodes: 3
# Image: ubuntu with containerd
# VM-Type: e2-standard-8
# Size of boot disk: 30 GB
# Cluster -> Security -> Issue cluster certificate: yes

# Additionally to the Kubernetes cluster we have to install gcloud auth dependencies to enable access of the controller
# to the cluster control plane (execute on the node where the COFFEE controller is running)
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key --keyring /usr/share/keyrings/cloud.google.gpg add -
sudo apt-get update
sudo apt-get install google-cloud-cli google-cloud-sdk-gke-gcloud-auth-plugin
gcloud init