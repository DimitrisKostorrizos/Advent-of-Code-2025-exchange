use strict;
use warnings;
use feature 'say';
use List::Util qw(max min);
use Data::Printer;


#== LOAD DATA ===================================

my @dataMultiArr01; # multidimentional array of 0s and 1s

my $DataWidth = 0;  # number of columns in input
my $DataHeight = 0; # number of rows in input

my $fh;
if (@ARGV) {
    open $fh, '<', $ARGV[0] or die "Can't open file '$ARGV[0]': $!";
} else {
    $fh = *DATA;
}

while (my $row = <$fh>) {
    chomp $row;                     # drop newline
    next unless $row;               # skip empty lines

    $DataWidth ||= length($row);
    $DataHeight += 1;

    my @rowArr = split(//, $row);
    push @dataMultiArr01, [ map { $_ eq '.' ? 0 : 1 } @rowArr ];
}
close $fh;

# ==============================================

my $boxSize = 3;
my $boxHalf = int($boxSize / 2);

sub countForCoords {
    my ($XX, $YY) = @_;
    my $rollsCount = 0;
    for my $y ( max(0,$YY-$boxHalf) .. min($DataHeight-1,$YY+$boxHalf) ) {
        for my $x ( max(0,$XX-$boxHalf) .. min($DataWidth-1,$XX+$boxHalf) ) {
            next if $x == $XX && $y == $YY; # skip the coords we are checking around
            $rollsCount++ if $dataMultiArr01[$y][$x] == 1;
        }
    }
    return $rollsCount;
}

sub countAndClear {
    my $accessibleRolls = 0;
    my @reachableRollsCoords;
    for my $y (0..$DataHeight-1) {
        for my $x (0..$DataWidth-1) {
            next unless $dataMultiArr01[$y][$x]; # skip 0/.
            if (countForCoords($x,$y) < 4) {
                $accessibleRolls++;
                push @reachableRollsCoords, [$x,$y];
            }
        }
    }
    # clear
    $dataMultiArr01[$_->[1]][$_->[0]] = 0 for @reachableRollsCoords;
    return $accessibleRolls;
}

my $clearedFields = 0;
while (my $clearedRows = countAndClear()) {
    $clearedFields += $clearedRows;
    say "Current: $clearedRows";
}

say "Number of accessible rolls: ".$clearedFields;

# ========================================

__DATA__

..@@.@@@@.
@@@.@.@.@@
@@@@@.@.@@
@.@@@@..@.
@@.@@@@.@@
.@@@@@@@.@
.@.@.@.@@@
@.@@@.@@@@
.@@@@@@@@.
@.@.@@@.@.