PluginPackages{
    classvar <path;
    classvar <packageFiles;
    classvar <packageDescriptions;
    classvar <pluginSupportDir;

    // *thisPackage{
    //     ^Quarks.findClassPackage(this)
    // }

    *initClass{
        StartUp.add({
            packageDescriptions = IdentityDictionary.new;
            path = Quarks.at("cmake.quark").localPath;
            packageFiles = PathName(path +/+ "packages").files;

            // FIXME: this shouldn't be done at every class init
            this.createSupportDirIfNecessary();
            this.loadPackageDescriptions();
        });

    }

    *createSupportDirIfNecessary{
        var path = PathName(Platform.userConfigDir) +/+ "pluginpackages";

        if(path.isFolder.not, {
            File.mkdir(path.fullPath)
        });

        pluginSupportDir = path.fullPath.replace(" ", "\\ ");

    }

    *loadPackageDescriptions{
        packageFiles.do{ arg file;
            packageDescriptions.put(file.fileNameWithoutExtension, file.fullPath.load())
        }

    }

    *gui{
        var name = "Install plugin package";
        var win = Window.new(name);
        var layout;
        var list = ListView.new(parent:win).items_(packageDescriptions.keys.asArray);

        var installbutton = Button.new(win)
        .states_([["Install selected"]]).action_({
            var val = list.selection[0];
            var key = packageDescriptions.keys.asArray[val];
            this.installPlugin(key)
        });

        layout = VLayout.new(*[installbutton, list]);
        win.layout = layout;
        win.front;
    }

    *installPlugin{arg key;
        var selected = packageDescriptions.at(key);
        this.cloneGitDir(selected[\url], this.pluginSupportDir);
        // TODO: Cmake command
    }

    *cloneGitDir{arg url, targetDir;
        var result = "";
        var cdcmd = ["cd", targetDir, ";"];
        var cmd = ["git", "clone", "--recurse-submodules", url];

        if(PathName(targetDir).isFolder, {
            // FIXME: Pull new changes instead
            "Target directory already exists. Not cloning it.".warn
        }, {
            cmd = (cdcmd ++ cmd).join(" ");

            Pipe.callSync(cmd.postln, { |res|
                res.postln;
                result = true;
            }, {|res|
                res.postln;
                result = false;
            });

            result.not.if{
                "%: Could not clone url".format(this.name).error;
            };

        });
    }
}
