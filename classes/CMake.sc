CMake{
    classvar <>sc_path;
    classvar <>install_location;

    // cmake -DSC_PATH=$SC_SRC -DCMAKE_BUILD_TYPE=RELEASE -DCMAKE_INSTALL_PREFIX=$DEST ..

    *prepare{|flags|
        var buildflags = flags ? [
            "-DCMAKE_BUILD_TYPE=RELEASE",
            "-DCMAKE_INSTALL_PREFIX=%".format(install_location ? Platform.userExtensionDir)
        ];
        var cmd = ["cmake", buildflags, ".."];

        if(sc_path.notNil, {
            cmd = (cmd ++ ["-DSC_PATH=%".format(sc_path)]).join(" ");
            this.prCall(cmd);
        }, {
            "%: sc_path is not set. Please set it to the location of the SuperCollider header files.".format(this.name).error
        });

        cmd.postln;

    }

    *build{|config="Release", install=true, flags|
        // cmake --build . --config Release --target install
        var cmd = ["cmake", "--build", "."];

        if(install, {
            cmd = cmd ++ ["--target", "install"]
        });

        cmd = cmd ++ ["--config", config];

        cmd = (cmd ++ ["-DSC_PATH=%".format(sc_path)]).join(" ");
        this.prCall(cmd);

    }

    // TODO
    *checkForCMake{
        ^true
    }

    *prCall{|cmd|
        var result = "";

        Pipe.callSync(cmd, { |res|
            result = res;
        }, {
            this.checkForCMake();
        });

        ^result
    }

}

PluginInstall{

}
