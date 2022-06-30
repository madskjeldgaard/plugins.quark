Plugins{
    classvar <path;
    classvar <packageFiles;
    classvar <packageDescriptions;
    classvar <pluginSupportDir;
    classvar <>scheaders;

    *initClass{
        StartUp.add({
            packageDescriptions = IdentityDictionary.new;
            path = Quarks.at("plugins.quark").localPath;
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

        // pluginSupportDir = path.fullPath.replace(" ", "\\ ");
        pluginSupportDir = path.fullPath;

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
        var descTitle = StaticText.new(win).string_("Title").font_(Font.default.bold_(true));
        var desc = StaticText.new(win).string_("description");
        var url = StaticText.new(win).string_("url");
        var copyright = StaticText.new(win).string_("copyright");
        var list = ListView.new(parent:win).items_(packageDescriptions.keys.asArray)
            .selectionAction_({arg obj;
            var index = obj.value;
            var thisKey = packageDescriptions.keys.asArray[index];
            var thisDesc = packageDescriptions[thisKey];
            descTitle.string_(thisDesc[\name]);
            desc.string_("Summary: " ++ thisDesc[\summary]);
            url.string_("url: " ++ thisDesc[\url]);
            copyright.string_("copyright: " ++ thisDesc[\copyright]);
        });

        var installbutton = Button.new(win)
        .states_([["Install selected"]]).action_({
            var val = list.selection[0];
            var key = packageDescriptions.keys.asArray[val];
            this.installPlugin(key)
        });

        layout = VLayout.new(*[installbutton, list, descTitle, desc, url, copyright]);
        win.layout = layout;
        win.front;
    }

    *installPlugin{arg key;
        var cmake;
        var selected = packageDescriptions.at(key);
        var result = this.cloneGitDir(selected[\url], this.pluginSupportDir.replace(" ", "\\ "));

        // TODO: Cmake command
        if(result, {
            cmake = CMake.new(
                path: this.pluginSupportDir.replace(" ", "\\ ") +/+ key,
                pathToSuperColliderHeaders: scheaders.replace(" ", "\\ "),
                installLocation: Platform.userExtensionDir.replace(" ", "\\ ")
            );

            cmake.prepareAndBuild();
        })
    }

    *cloneGitDir{arg url, targetDir;
        var result = "";
        var cdcmd = ["cd", targetDir, ";"];
        var cmd = ["git", "clone", "--recurse-submodules", url];

        if(PathName(targetDir).isFolder, {
            // FIXME: Pull new changes instead
            "Target directory already exists. Not cloning it.".warn;
            result = true
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

        ^result
    }
}
