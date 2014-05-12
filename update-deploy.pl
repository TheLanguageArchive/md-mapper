#!/usr/bin/env perl
#
# This script checks for updates to either the mapper codebase or to
# the mapping files. It then rebuilds the mapper if necessary and
# unpacks the deployment package into the specified base
# directory. The base directory will contain directories named after
# git hash codes and a symlink 'current' pointing to the most recent
# one.
#
# When using the mapper, always use the 'current' directory. That way
# the version can be updated and rolled back by simply changing the
# symlink.
#
# To run this script:
#
#   update-deploy.pl [-f] [targetdir] [mapdir]
#
# Default values are targetdir="../mapper", mapdir="../md-mapping".
#
# The flag -f forces rebuilding even if there have been no updates.
#
# This script requires git, Maven, JDK >= 1.7, and tar.
#
# This script returns a value, which may be examined by other scripts:
#  0 - nothing has changed
#  1 - something changed (existing records should be re-mapped)
#  2 - an error occurred
#
# Lari Lampen (MPI-PL), 2013-2014.

use strict;
use warnings;
use Cwd qw(getcwd abs_path);


# Get hash of the current commit (HEAD) in either the current
# directory or, if specified, another directory.
sub git_hash(;$) {
    my $dir;
    if (scalar @_ > 0) {
	$dir = getcwd();
	chdir($_[0]);
    }
    my $rev = `git rev-parse HEAD`;
    if (scalar @_ > 0) {
	chdir($dir);
    }
    chomp $rev;
    return $rev;
}

# Update directory via git. Return 0 if no changes were made, 1 if an
# update was received. If a directory is passed as parameter, do the
# update there (and return to the intial working directory
# afterwards).
sub git_pull(;$) {
    my $dir;
    if (scalar @_ > 0) {
	$dir = getcwd();
	chdir($_[0]);
    }
    my $res = `git pull`;
    if (scalar @_ > 0) {
	chdir($dir);
    }
    if ($res =~ /Already up-to-date/) {
	return 0;
    }
    return 1;
}

my $force;
if (scalar @ARGV>0 && $ARGV[0] eq "-f") {
    $force=1;
    shift @ARGV;
}
my $targetdir = (scalar @ARGV > 0) ? $ARGV[0] : "../mapper";
my $mapdir = (scalar @ARGV > 1) ? $ARGV[1] : "../md-mapping";

die "Error: directory $mapdir does not exist" if !-d $mapdir;

my $upd_mapper = git_pull();
my $upd_mappings = git_pull($mapdir);

# Check situations where rebuilding is not needed.
unless ($force || $upd_mapper) {
    if ($upd_mappings) {
	print "Only mappings have changed.\n";
	exit 1;
    }
    print "No changes.\n";
    exit 0;
}

# If we got this far, we'll need to rebuild the mapper.

mkdir $targetdir unless -d $targetdir;
my $hash_mapper = git_hash();

my $curr = $targetdir . '/current';
unlink $curr if -d $curr;

my $target = $targetdir . '/' . $hash_mapper;

print "Rebuilding needed; running Maven.\n";
if (system("mvn -Dmaven.test.skip=true clean package assembly:assembly") != 0) {
    print "Error: Maven build failed. Deployed mapper has not been modified.\n";
    exit 2;
}

my $glob = getcwd() . '/target/md-mapper-*.tar.gz';
my @files = glob $glob;
if (scalar @files != 1) {
    print "Confused trying to find deployment package. Stop.\n";
    exit 2;
}
my $pack = $files[0];

if (-d $target) {
    print "Removing existing $target\n";
    system("rm -rf $target");
}

mkdir $target;

system("tar xfz $pack -C $targetdir/$hash_mapper") == 0 || die "Error: tar failed";

# Find the only directory under $hash_mapper.
$glob = "$targetdir/$hash_mapper/*";
@files = glob $glob;
if (scalar @files != 1) {
    print "Error: Unexpected directory structure. Symlink not created. Report this as a bug!\n";
    exit 2;
}

symlink $files[0], "$targetdir/current";

print "New version $hash_mapper deployed successfully; see $targetdir/current.\n";

unless (-f "$target/mapfiles") {
    symlink abs_path("$mapdir/mapfiles"), "$targetdir/current/mapfiles";
}

exit 1;
