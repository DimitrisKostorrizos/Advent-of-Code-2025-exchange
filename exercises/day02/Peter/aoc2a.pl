
my $totalscore = 0;

my %cubeMax = (
    red => 12,
    green => 13,
    blue => 14
);


while (<>) {
	chop();
	my ($gameNo, $gameStr) = ($_ =~ m/Game +(\d+): +([ ,;\w]+)/);

    my $okay = 1;
    foreach $game ( split(/; +/, $gameStr) ) {
        foreach $bag ( split(/, +/, $game) ) {
            ($cubeNum, $tag) = split(/ /, $bag);
            ($cubeNum > $cubeMax{$tag} ) and $okay = 0;
        }
    }
    print "Game $gameNo: $okay\n";
    $okay and $totalscore += $gameNo;
}

print "\n== $totalscore\n";
