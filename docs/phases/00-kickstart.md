# kickstart

## database

- Create database called `finances`
- Create user `finances` with password `finances`
- Grant all access to `finances` user to `finances` database.

## spring

- Create `spring-boot` project called `finances` using `maven`
- Add `liquibase` as database documentator and migration
- Create table `user_account` with basic user information using `liquibase`. The information of the first user is in `.env.local`
- Add a simple endpoint were the user data can be retrieved `/user/:username`

## app

- The application should run, create a script so this run by typing `./finances`
- Check `devenv.nix` every single window in `.tmux-init.conf` should work without problems
