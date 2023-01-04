# Run an experiment with COFFEE

This document provides a step-by-step guide to run an experiment with COFFEE.

1. Finish the configuration according to our [configuration guide](https://github.com/DescartesResearch/COFFEE/blob/main/docs/configuration.md)
2. Change the working directory to the database folder of this repository
3. Start the COFFEE controller's database using `bash startDatabase.sh`
4. Change the working directory to the utils folder of this repository
5. Run the `buildAndPush.sh` script to build COFFEE and push container images of the proxy and test app to your configured repository
6. Run the `run.sh` script to run the experiment

COFFEE will execute the experiment and clear up the environment in the cluster automatically. A successful run finishes
with the log output: `COFFEE executed the test campaign successfully! Thanks for using COFFEE!`. If activated,
measurement values can be found in the report directory. The controller database holds all measured values in every case,
you might use PHPMyAdmin to view some data. The database does not shut down automatically when COFFEE finishes the
experiment. If you want to shut down the database, use the `stopDatabase` script in the database folder of this directory.
Make sure to save the volume anywhere because COFFEE is configured to clear the database during startup.

