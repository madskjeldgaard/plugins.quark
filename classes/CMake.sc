/*
//
// Example:
CMake
.new(
    path: "/home/mads/tmp/XPlayBuf",
    pathToSuperColliderHeaders: "/usr/share/supercollider-headers/",
    installLoation: "/home/mads/tmp"
)
.prepareAndBuild()

*
*/
CMake{
    var <>sc_path;
    var <>install_location;
    var <>localPath;

    *new{|path, pathToSuperColliderHeaders, installLocation|
        ^super
        .new
        .localPath_(path.quote())
        .sc_path_(pathToSuperColliderHeaders.quote())
        .install_location_(installLocation ? Platform.userExtensionDir)
    }

    // This will prepare, build and optionally install the plugins
    prepareAndBuild{|config="Release", prepareFlags, buildFlags, install=true|
        this.prepare(config: config, flags: prepareFlags, enterBuildDir: true);
        this.build(config: config, install: install, flags: buildFlags, enterBuildDir: true);
    }

    prepare{|config="Release", flags, enterBuildDir=true|
        if(this.checkForCMake(),{
            var buildflags = flags ? [
                "-DCMAKE_INSTALL_PREFIX=\"%\"".format(install_location ? Platform.userExtensionDir)
            ];
            var cmd;
            buildflags = ["-DCMAKE_BUILD_TYPE=\"%\"".format(config)] ++ buildflags;

            cmd = ["cmake"] ++ buildflags;

            if(enterBuildDir, {
                cmd = this.prEnterBuild(makeFolder: true) ++ cmd;
            });

            // Move to project dir
            cmd = ["cd", localPath, ";"] ++ cmd;

            if(sc_path.notNil, {
                cmd = (cmd ++ ["-DSC_PATH=%".format(sc_path)] ++ [".."] ).join(" ");

                this.prCall(cmd.postln);
            }, {
                "%: sc_path is not set. Please set it to the location of the SuperCollider header files.".format(this.name).error
            });
        })
    }

    build{|config="Release", install=true, flags([]), enterBuildDir=true|
        if(this.checkForCMake, {
            var cmd = ["cmake", "--build", "."];

            if(install, {
                cmd = cmd ++ ["--target", "install"]
            });

            cmd = cmd ++ ["--config", config] ++ flags;

            if(enterBuildDir, {
                cmd = this.prEnterBuild(makeFolder: true) ++ cmd;
            });

            // Move to project dir
            cmd = [ "cd", localPath, ";"] ++ cmd;

            cmd = cmd.join(" ");

            this.prCall(cmd.postln);
        })

    }

    prEnterBuild{|makeFolder=true|
        var cmd = ["cd", "build", ";"];
        var debug = [ "echo", "Current dir: $(pwd)", ";" ];

        if(makeFolder, {
            // -p results in the command not failing if it already exists
            cmd = ["mkdir", "-p", "build", ";"] ++ cmd;
        });

        ^debug ++ cmd
    }

    checkForCMake{
        var cmakeFind = "which cmake";
        var result="";
        Pipe.callSync(cmakeFind, { |res|
            result = true;
        }, {
            result = false;
        });

        if(result.not, {
            "CMake is not installed".error;
        });

        ^result
    }

    prCall{|cmd|
        var result = "";

        Pipe.callSync(cmd, { |res|
            result = res.postln;
        }, {
            this.checkForCMake();
        });

        ^result
    }

}

// PluginInstall{ }
