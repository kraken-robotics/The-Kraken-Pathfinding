#!/bin/sh
git clone https://github.com/PFGimenez/dependency-injector.git --depth 1
cd dependency-injector
mvn install
cd ..
rm -rf dependency-injector
git clone https://github.com/PFGimenez/config.git --depth 1
cd config/core
mvn install
cd ../..
rm -rf config
git clone https://github.com/PFGimenez/graphic-toolbox.git --depth 1
cd graphic-toolbox/core
mvn install
cd ../..
rm -rf graphic-toolbox
