# Building

Updated AT: `2026-07-07T09:46:52Z` (UTC)

There are 4 ways to build... given in order from bad to good... all ways are pretty standard in expo application devlopment workflow.

## 1. Just `Expo GO`.

- use for Testing & Prototyping.
- best for getting started.
- best if just wanna see my work (cuz you probably waana hire me, I am fine just dm me, but expo go might fail, use preview build, 3 option).

### Steps

1. You need to install expo go appliction on your phone, with sdk version, we using in the project. (https://expo.dev/go)

2. Install Dependency.

   ```bash
   npm install
   ```

3. Now connect the phone with desktop using wifi.

4. Start developemt server.

   ```bash
   npm start
   ```

5. Scan the QR Code with your expo go application.

## 2. Dev Build

- customized to our project.
- best for contributors.
- support all native functionality + hot reloading like expo go.

### Steps

1. Install Dependency.

   ```bash
   npm install
   npm install -g eas-cli
   ```

2. Build dev application & Install it on your phone.

   ```bash
   npm run build:develop
   ```

3. Start developemt server.

   ```bash
   npm start
   ```

4. Scan the QR Code with your new dev application.

## 3. Preview Build

- for testing the application.
- best for testers.
- support all native functionality & give near production application.

### Steps

1. Install Dependency.

   ```bash
   npm install
   npm install -g eas-cli
   ```

2. Build preview application.

   ```bash
   npm run build:preview
   ```

3. Install it on your phone.

## 4. Production Build

- for production application.
- not the best but is way for end user to install passcodes app.
- for user who can't wait to next release and want latest features.

> ![IMPORTANT]
> YOU WILL NEED A EXPO ACCOUNT FOR THIS >>> THIS ALSO MEAN YOU ARE OPTING OUT FROM PASSCODES RELEASE PROCESS.
>
> if do this, please not the commit hash, you are build the app from and make sure your `git status` says no changes to commit in other word your changes are complete commited & and you have clean working tree.

### Steps

1. Install Dependency.

   ```bash
   npm install
   npm install -g eas-cli
   ```

2. Build preview application.

   ```bash
   eas build --profile production
   ```

3. Install it on your phone.
