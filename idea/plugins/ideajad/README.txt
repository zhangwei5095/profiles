Readme $Id: README.txt,v 1.6 2003/11/15 19:33:42 hendriks73 Exp $

IdeaJad-2169.2
=============


Installation
------------

Before you install this release, please delete all
old releases from your plugins directory!

This release was built with IntelliJIdea 2169.2.
It is suitable for windows systems and may run on Linux
system after you made the file /bin/jad executable.
To install just extract the zip- or tgz-archive (not
the included jar, as you might be tempted to do :-)
into the plugins directory of idea, so that you have a
structure like:

[idea_home]/plugins/ideajad-xxx-x/...


Platforms other than Linux, Mac OSX or Windows
----------------------------------------------

If you need to run a jad version for an operating system
other than Linux, Mac OSX or Windows, place the jad
executable in the plugin's bin directory and make sure it
is named 'jad' and is executable. This only works for OSes
other than Linux, Mac OSX and Windows. If you need a
different version of jad for one of the OSes mentioned above
you will need to replace the jad executable in the
corresponding subdirectory of the plugin's bin directory.

You can get jad for pretty much every platform from
http://kpdus.tripod.com/jad/


Usage
-----

It should work with builds 2169.2+ (unless of course something
crucial in the openapi changed).

To decompile a class, simply navigate to the class, e.g.
by clicking on the menu Goto->Class.
You may be prompted for a target directory. This
will not happen if you already defined a decompile
directory in the project properties.

You can also switch the project sidebar to the classpath
view, choose classes and hit the decompile menuitem in
the context menu. The decompiled files will be opened
automatically.

Note that if you defined only one source directory in
your project, ideajad will automatically write all
decompiled files to this directory. This might mess up
your neat project tree! So it's really a good idea to
define a decompile folder.

It is also a good idea to exclude this folder from
compilation. In order to do this, go to your project's
properties, select 'Compiler' (not 'Paths'!) and exclude
the decompile folder from compilation.

You can configure IdeaJad in Idea's project properties
configuration dialog.

There you can also set the option 'Mark decompiled files
as readonly'. Please note that when you set this option,
a second decompilation might fail, because jad cannot
overwrite a readonly file. The corresponding errormessage
is something like:
"JavaClassFileOutputException: Can't create file `D:\ixxx.java'"
If this happens you have to manually remove the readonly
property of the existing java files.


Bugs & RFEs
-----------

I am always happy about feedback. Please post bug reports
or requests for enhancements directly to me instead of
posting to the plugin forum on http://www.intellij.net/

When sending a bug report, please include the corresponding
section of the idea.log file and the exact error message
(if there is one).


Final Remarks
-------------

This version will not work with 3.0.x, but only with Aurora.
Please use build 813-1, when you are using IntelliJIdea 3.0.x.


Enjoy!

Hendrik Schreiber
<hs@tagtraum.com>
