#!/bin/sh
git clone https://github.com/PFGimenez/dependency-injector.git
cd dependency-injector
ant
cd ..


git clone https://github.com/PFGimenez/config.git
cd config/core
ant
cd ../..

git clone https://github.com/PFGimenez/graphic-toolbox.git
cp dependency-injector/injector.jar graphic-toolbox/core/lib
cp config/core/config.jar graphic-toolbox/core/lib
cd graphic-toolbox/core
ant
cd ../..

git clone https://github.com/PFGimenez/The-Kraken-Pathfinding.git
cp dependency-injector/injector.jar lib
cp graphic-toolbox/core/graphic.jar lib
cp config/core/config.jar lib
