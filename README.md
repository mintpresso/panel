# MINTPRESSO
## Panel Service

Run Play Framework 2 in working directory.
```bash
$ play
[info] Loading project definition from /Users/eces/panel/project
[info] Set current project to panel (in build file:/Users/eces/panel/)
       _            _
 _ __ | | __ _ _  _| |
| '_ \| |/ _' | || |_|
|  __/|_|\____|\__ (_)
|_|            |__/

play! 2.1.1 (using Java 1.6.0_45 and Scala 2.10.0), http://www.playframework.org

> Type "help play" or "license" for more information.
> Type "exit" or use Ctrl+D to leave this console.

[panel] $ 
```

And then, run on 9000 port for development.
```bash
[panel] $ run 9000
```

You can enable continuous compilation with slash.
```bash
[panel] $ ~run 9000
```

The only dependency, Affogato(Scala API for MINTPRESSO) is managed on git submodule and placed in `/affogato`. To update this:
```bash
$ cd affogato
$ git pull origin master
```
and then, commit with this message 'Update submodule of affogato.'