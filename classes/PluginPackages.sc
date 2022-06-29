PluginPackages{
    classvar <path;
    classvar <packageFiles;

    // *thisPackage{
    //     ^Quarks.findClassPackage(this)
    // }

    *initClass{
        Class.initClassTree(PathName);
        Class.initClassTree(Quarks);
        Class.initClassTree(Main);
        // path = Main.packages.asDict.at('cmake');

        // packageFiles = PathName(localPath. +/+ "packages").files;
    }
}
