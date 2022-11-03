#!/usr/bin/env bash

# The following script in a supplementary material for our paper and describes the node setup for our self-hosted
# Kubernetes cluster
# THIS SCRIPT IS NOT EXECUTABLE OUT OF THE BOX AND IS MEANT FOR REVIEWERS TO SEE WHICH DEPENDENCIES HAVE BEEN INSTALLED

# ------------ 1. Kubernetes dependencies (executed on all nodes) ----------------
# Disable swap
sudo swapoff -a
# Keep it disabled across restarts
sudo sed -ri '/\sswap\s/s/^#?/#/' /etc/fstab
# containerd setup
cat <<EOF | sudo tee /etc/modules-load.d/containerd.conf
overlay
br_netfilter
EOF
sudo modprobe overlay
sudo modprobe br_netfilter
# Setup required sysctl params, these persist across reboots.
cat <<EOF | sudo tee /etc/sysctl.d/99-kubernetes-cri.conf
net.bridge.bridge-nf-call-iptables  = 1
net.ipv4.ip_forward                 = 1
net.bridge.bridge-nf-call-ip6tables = 1
EOF
# Apply sysctl params without reboot
sudo sysctl --system
# Update package lists and install containerd dependencies
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install libseccomp2
# Install containerd
sudo apt install -y containerd
sudo mkdir -p /etc/containerd/
sudo containerd config default | sudo tee /etc/containerd/config.toml > /dev/null
sudo systemctl daemon-reload
sudo systemctl start containerd

# install k8s
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl
sudo curl -fsSLo /usr/share/keyrings/kubernetes-archive-keyring.gpg https://packages.cloud.google.com/apt/doc/apt-key.gpg
echo "deb [signed-by=/usr/share/keyrings/kubernetes-archive-keyring.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee /etc/apt/sources.list.d/kubernetes.list
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

# ------------ 2. Kubernetes cluster init (executed on master only) ----------------
JOIN_COMMAND=$(sudo kubeadm init --pod-network-cidr=10.244.0.0/16 | tr -d '\n\t\r\\'| tr -s [:space:] ' ' | grep -o 'kubeadm join.*')
echo $JOIN_COMMAND
mkdir -p ${HOME}/.kube
sudo cp /etc/kubernetes/admin.conf ${HOME}/.kube/config
sudo chown $(id -u):$(id -g) ${HOME}/.kube/config
kubectl apply -f https://github.com/coreos/flannel/raw/master/Documentation/kube-flannel.yml

# ------------ 3. Worker join (executed on workers only) ----------------
sudo $JOIN_COMMAND

# ------------ 4. Time sync (executed on all nodes) ----------------
sudo apt install systemd-timesyncd
sudo systemctl start systemd-timesyncd