# COFFEE: Benchmarking of Container Orchestration Frameworks

COFFEE (short for Container Orchestration Frameworks' Full Experimental Evaluation) is a benchmarking framework for
container orchestration frameworks. It is able to automate repeatable experiments and covers
many aspects for modern container orchestration frameworks.

## Prerequisites

For development purposes:

- Java 11 or newer
- Maven

For actual usage:

- Docker
- Test cluster (currently supported: [Kubernetes](https://kubernetes.io/) and [Nomad](https://www.nomadproject.io/))
- Container image repository accessible in the test cluster

## Get started

We provide an overview of COFFEE's components and functionality in our [docs](https://github.com/DescartesResearch/COFFEE/blob/main/docs/overview.md). Before build and usage make sure you are familiar with COFFEE's [configuration options](https://github.com/DescartesResearch/COFFEE/blob/main/docs/configuration.md). After setting all required configuration parameters, consider our guide on [how to run an experiment](https://github.com/DescartesResearch/COFFEE/blob/main/docs/runExperiment.md) with COFFEE. All necessary build and run scripts can be found in the [utils](https://github.com/DescartesResearch/COFFEE/tree/main/utils) folder of this repository. When something doesn't work, check our [troubleshooting doc](https://github.com/DescartesResearch/COFFEE/blob/main/docs/troubleshooting.md) or don't hesitate to create an issue.

## Cite us

Read the full paper on [ACM Digital Library](https://dl.acm.org/doi/10.1145/3578244.3583726)

BibTeX entry:

```
@inproceedings{10.1145/3578244.3583726,
author = {Straesser, Martin and Mathiasch, Jonas and Bauer, Andr\'{e} and Kounev, Samuel},
title = {A Systematic Approach for Benchmarking of Container Orchestration Frameworks},
year = {2023},
isbn = {9798400700682},
publisher = {Association for Computing Machinery},
address = {New York, NY, USA},
url = {https://doi.org/10.1145/3578244.3583726},
doi = {10.1145/3578244.3583726},
booktitle = {Proceedings of the 2023 ACM/SPEC International Conference on Performance Engineering},
pages = {187–198},
numpages = {12},
keywords = {container orchestration, nomad, performance, benchmarking, kubernetes},
location = {Coimbra, Portugal},
series = {ICPE '23}
} 
```

## Any questions?

For questions contact [Martin Straesser](https://se.informatik.uni-wuerzburg.de/software-engineering-group/staff/martin-straesser/).
