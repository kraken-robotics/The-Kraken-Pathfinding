#!/bin/sh
git clone https://github.com/PFGimenez/dependency-injector.git
cd dependency-injector
git pull
ant
cd ..


git clone https://github.com/PFGimenez/config.git
cd config/core
git pull
ant
cd ../..

git clone https://github.com/PFGimenez/graphic-toolbox.git
cd graphic-toolbox
git pull
cd ..
cp dependency-injector/injector.jar graphic-toolbox/core/lib
cp config/core/config.jar graphic-toolbox/core/lib
cd graphic-toolbox/core
ant
cd ../..

cp dependency-injector/injector.jar lib
cp graphic-toolbox/core/graphic.jar lib
cp config/core/config.jar lib
