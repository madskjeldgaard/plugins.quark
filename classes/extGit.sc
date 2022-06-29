+ Git{
    clone { |url, additionalFlags(["--recurse-submodules"])|
        var command = [
			"clone",
			url,
			thisProcess.platform.formatPathForCmdLine(localPath)
		] ++ additionalFlags;

		this.git(command.postln, false);
		this.url = url;
	}

    pull {|additionalFlags|
		this.git(["pull", "origin", "master"] ++ additionalFlags)
	}
}
