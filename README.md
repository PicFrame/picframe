# PicFrame #
[![alt text][playstoreimage]][playstorelink]

[playstorelink]: https://play.google.com/store/apps/details?id=picframe.at.picframe
[playstoreimage]: https://developer.android.com/images/brand/en_app_rgb_wo_60.png (PicFrame on Google Play)

### Pre-requisites

Android SDK v21  
Android build tools v21.1.2  
Android Support Repository  

### Getting started

#### Want to develop?
"Import Project" into Android Studio and you should be good to go.

#### Want to build a signed APK without developing?

Use the `build-picframe.bat` script in the folder where it's located.

### Adding example photographs

To include example photographs to be displayed whenever the selected picture source folder is empty, include six pictures named `ex0`, `ex1`, `ex2`, `ex3`, `ex4`, `ex5` in PicFrame's `res/drawable` directory.

### Contributing
#### Branching strategy

This repository holds two branches with an infinite lifetime:

* master
* develop

Branch **master** is considered the main branch where the source code of `HEAD` always reflects the version of the released app on Google Play. Branch **develop** is considered the main branch where the source code of `HEAD` always reflects a state with the latest delivered development changes for the next release.

When the source code in the `develop` branch reaches a stable point and is released as a new version on Google Play, all changes should be merged back into `master` and then tagged with a release number. (If possible, you should also use the version number as the commit message.)

Other branches are used to:

* aid parallel development between team members
* ease tracking of features
* prepare for production releases
* assist in quickly fixing live production problems

Unlike the main branches, any additional branches always have a limited lifetime, since they will be removed eventually.

Another type of branch is:

* branch **perNewFeature**

Such a branch derives from `develop` and must merge back into `develop`. Once it is merged, a **perNewFeature** branch may be deleted.

See also: [A successful Git branching model](http://nvie.com/posts/a-successful-git-branching-model)


### License

Copyright (C) 2015 Martin Bayerl, Myra Fuchs, Clemens Hlawacek, Christoph Krasa, Linda Spindler, Ebenezer Bonney Ussher.

PicFrame is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PicFrame is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PicFrame.  If not, see <http://www.gnu.org/licenses/>.

See also: [LICENSE.MD](LICENSE.MD)

### Third party libraries

PicFrame uses [ownCloud Android Library](https://github.com/owncloud/android-library), which is available under MIT license.

ownCloud Android Library uses Apache JackRabbit, licensed under Apache License, Version 2.0.  
Apache JackRabbit depends on Commons HTTPClient licensed under Apache License, Version 2.0.

For more information on third party libraries' licenses, see [THIRD_PARTY_LICENCES.MD](THIRD_PARTY_LICENCES.MD).

For PicFrame, ownCloud Android Library source code was altered in two places; details are available in [commit 3d57985](https://github.com/PicFrame/android-library/commit/3d57985c2f041b07ae59d46146de9b2e567de951).

