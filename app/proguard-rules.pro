# R8 / ProGuard rules for Promises.
#
# The app is a small AppCompat/Material app with no reflection or serialization,
# so the default Android rules plus AGP's consumer rules from AppCompat and
# Material are sufficient. Activities/Receivers referenced from the manifest are
# kept automatically.

# Keep source file + line numbers, then hide the original file name, so release
# crash reports stay useful while not leaking class names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
