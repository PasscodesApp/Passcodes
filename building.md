# Building

**Last-Updated-AT**: `2026-07-07T09:46:52Z (UTC)`.

There are 4 ways to build... all ways are pretty standard in expo application development workflow.

all other ways are found at [building.md in Passcodes-Docs](https://passcodesapp.github.io/Passcodes-Docs/user-docs/installing/) repository.

## 4. Production Build

- for production application.
- not the best but is way for end user to install passcodes app.
- for user who can't wait to next release and want latest features.

> [!IMPORTANT]
>
> YOU WILL NEED A EXPO ACCOUNT FOR THIS >>> THIS ALSO MEAN YOU ARE OPTING OUT FROM PASSCODES RELEASE PROCESS.
>
> if do this, please not the commit hash, you are build the app from and make sure your `git status` says no changes to commit in other word your changes are complete commited & and you have clean working tree.

### Steps

1. Clone Repository.

   ```bash
   git clone https://github.com/PasscodesApp/Passcodes.git
   cd Passcodes
   ```

2. Install Dependency.

   ```bash
   npm install
   npm install -g eas-cli
   ```

3. Build preview application.

   ```bash
   eas build --profile production
   ```

4. Install it on your phone.
