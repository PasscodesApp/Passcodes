# Detail Guide On Installion

This file give you a deep guide on installion & updation process. we hope that you have already readed the step written in readme but, aren;t satisfied with that and need more info and guide on installion and updation.

👏👏 We appreciate your efforts. As developer we try to abstract away complicated process, But sometimes this abstraction make thing simple that it is.. Which lead to false image of the process. To avoid that, we have created this guide that help you navigate installion and updation process..

# Table of Contents

- [Installion Process](#installion-process)
- [Updation Process](#updation-process)
- [Guide To Find A Best Release](#guide-to-find-a-best-release)
- [Explictly Yanked Release (Pure Trash)](#explictly-yanked-release-pure-trash)

## Installion Process

For inital installion of app, you follow step given below it work in most scenario.

1. Checking whether the app can run on your device or not.
   - go to your setting app and check whether you are on android 8+ or not?
   - if no, then sorry, app is not comatible with your device.
   - and if yes, then mostly you are good to go but, officially we only support device between android 8 to 14. For more high android version app can be run, But we don;t officially support it.

2. After completing first step, Pick a version of app to install.
   - Go to our [github repository release page](https://github.com/PasscodesApp/Passcodes/releases). There you will find a list of release avaiable.
   - pick up any one of the release you like. (we recommend you to pick a good release. [how can you find a good release](#guide-to-find-a-best-release)?)
   - after pick up a release, you can read the changelog to know what has changed. (optional step)

3. Downloading apk files
   - after pick up a release, you can scroll to the assets and download the apk that better fits your phone cpu architecture.

   - if you don;t know your phone cpu architecture. just download the apk file that has universal in its name.

4. Installing apk files.
   - install the apk file. the apk file will be mostly like downloaded in your browser (probably like chrome, firefox or edge).

   - open the app.

if any problem occur while performing the above 4 step. report your problem using [github issues](https://github.com/PasscodesApp/Passcodes/issues/new) after checking other support docs avaiable in github repositoy "docs/" folder.

## Updation Process

As you are reading this part, we hope that you have passcodes app installed on your phone and now, you want to update it.

you can follow step given below. they work for most scenario.

> [!IMPORTANT]
> updating from one version to another might result into data loss. though, it occur very rarely. but, it a good practice to take a backup of data.
> this try of data loss are likely to occur, when you are update from one major version to another major version meaning from `1.x.x` to `2.x.x`.

1. Checking Whether The Update Exist Or Not
   - compare your current app version with lastest app update. (you can find your current installed passcodes app version under app info in your phone settings)
   - check whether a update exist or not?
   - if no, then you can;t update but, can surely enquire about the next update using github issues.

     > if you know a bit of android then just clone the repo and build from main. it will work fine, because we use main branch as production. take reference from existing building docs in github.
     > Ignore this if it doesn;t make sense to you..

   - if yes, then check how big the update is and how it affect your expirence as user?
   - to find the update impact you can check what the difference between your version and new version you are updating to.
     > eg, if you update from 1.x.x to 2.x.x the the change will be big. if you update from 1.x.0 to 1.x.2 then change might be not visible even.

2. Downloaded the updated apk files.
   - it is same as you did earlier while installing the app.

   - goto release page and download the asset from select release. refer installion guide above if needed.

3. Update the app.
   - if, you updating to a new major release, meaning from 1.x.x to 2.x.x then, don;t forget to backup your data. you will thank your self later, if something goes wrong...
   - now install the new apk file it will ask you to upgrade instead of install.
   - open the app and enjoy your updated expirence.

4. Restore the backup if need.
   - if, needed restore the files/data back. or even revert to prevoius release if, problem occurs.

   - we admire the contribution even in form of your exprience with app.

     > meaning, reporting your data erase on update problem. will be appricate.
     > it help us make better exprience & fix bug that might be missed by us.

---

## Guide To Find A Best Release

To find a best release you should have knowledge of passcodes versioning system and type of releases we release/publish.

### Release Types

On our github release page you will find majorly `5 (3+2)` type of release. Where each type of release label tell you something about release.
Each release on github may have a label but, some even lack them completely. These label tends to tell more about release, so they are IMPORTANT to consider.

There are majorly two ways, In which we classify release. Mean a release will have two labels.
So to speak:

- **Build Label**: a label which won't change once after release. (alpha/beta/stable)
- **Github Release Label**: a label that can change. (pre-release/latest)

#### Three type that are there in app itself, you find them in release title on github:

(this tags/labels can;t change after initial release of that version is done).

- Alpha: special design for development purpose, not intented to use by end users, they comes with likely lots of bugs and security venurablitity.
- Beta: release that can be used by mass audience, and likely can contain minior bugs.
- Stable: are likey to be more stable then others release type, contain nearly zero bugs as they are very well tested. this release also will have extra developer support.

#### Two type are specific to github release. they are label on github release itself:

(this tags can label can change with time as they are dynamic and are not tied to app itself. meaning they are no hard coded anywhere)

- Pre Release: this release are not so good to install or to stick with. they are generally like `marked as deperacted` or more like `yanked release`
- Latest: this release label make a release as the best up to date release to install. but latest doesn't mean stable. it means latest feature.

> [!NOTE]
> this label or flag changes with time. meaning, a app version after it release, can be marked as pre-release as there are lots of bug found in that release later or a release can later be marked as latest release as it all good after test in production enviroment.

So, here is a list in order where starting type are best and ending types are worse.

```
STABLE (the best) > LATEST > BETA > ALPHA > PRE-RELEASE > RELEASE-WITH-NO-FLAGS (the worst) > Explictly Yanked Release (Pure Trash)
```

It more like that a release can have two flag at a same time. for example, a stable and pre-release. In such case, consider the github base label in this case `pre-release`. as they are more dynamic and provide the latest infomation from us (developer part).

> this clear means a pre-release even if they are stable / alpha / beta / gamma (unlikely.... 😂)
> it just doesn't matter it will be considered `the worst`...

---

## Explictly Yanked Release (Pure Trash)

This release will have Yanked Release written in the release title and description on github release page.

Such release are just there for documentation purpose..

JUST DON'T USE THEM!!!!
