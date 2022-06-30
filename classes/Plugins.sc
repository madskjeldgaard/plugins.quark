Plugins{
    classvar <>includeDraftPackages=false;
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
        });

    }

    *createSupportDirIfNecessary{
        var path = PathName(Platform.userAppSupportDir) +/+ "pluginpackages";

        if(path.isFolder.not, {
            "%: Creating folder for plugin packages".format(this.name).postln;
            File.mkdir(path.fullPath)
        });

        // pluginSupportDir = path.fullPath.replace(" ", "\\ ");
        pluginSupportDir = path.fullPath;
        scheaders = scheaders ? (this.pluginSupportDir +/+ "supercollider");

    }

    *loadPackageDescriptions{
        if(includeDraftPackages, {
            packageFiles = packageFiles ++ PathName(path +/+ "draft-packages").files;

        });

        // FIXME: this shouldn't be done at every class init
        this.createSupportDirIfNecessary();

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
        var installbutton, list;

        this.loadPackageDescriptions();

        list = ListView.new(parent:win).items_(packageDescriptions.keys.asArray)
            .selectionAction_({arg obj;
            var index = obj.value;
            var thisKey = packageDescriptions.keys.asArray[index];
            var thisDesc = packageDescriptions[thisKey];
            descTitle.string_(thisDesc[\name]);
            desc.string_("Summary: " ++ thisDesc[\summary]);
            url.string_("url: " ++ thisDesc[\url]);
            copyright.string_("copyright: " ++ thisDesc[\copyright]);
        });

        installbutton = Button.new(win)
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
        var result = this.cloneGitDir(selected[\url], this.pluginSupportDir);

        // Clone SuperCollider if necessary
        if(PathName(this.pluginSupportDir +/+ "supercollider").isFolder.not, {
            this.cloneGitDir("https://github.com/supercollider/supercollider", this.pluginSupportDir)
        });

        this.loadPackageDescriptions();

        // TODO: Cmake command
        // if(result, {
            cmake = CMake.new(
                path: (this.pluginSupportDir +/+ key),
                pathToSuperColliderHeaders: scheaders,
                installLocation: Platform.userExtensionDir
            );

            cmake.prepareAndBuild(
                prepareFlags:selected[\prepareFlags] ? [],
                buildFlags:selected[\buildFlags] ? []
            );
        // })
    }

    *cloneGitDir{arg url, targetDir;
        var result = "";
        var cdcmd = ["cd", targetDir.quote(), ";"];
        var cmd = ["git", "clone", "--recurse-submodules", url];

        cmd = (cdcmd ++ cmd).join(" ");

        Pipe.callSync(cmd.postln, { |res|
            res.postln;
            result = true;
        }, {|res|
            res.postln;
            result = false;
        });

        ^result
    }
}
